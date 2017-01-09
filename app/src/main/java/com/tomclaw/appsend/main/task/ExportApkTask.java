package com.tomclaw.appsend.main.task;

import android.annotation.SuppressLint;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.widget.Toast;

import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.PleaseWaitTask;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.util.FileHelper;

import java.io.*;

/**
 * Created by Solkin on 11.12.2014.
 */
public class ExportApkTask extends PleaseWaitTask {

    public static final int ACTION_EXTRACT = 0x00;
    public static final int ACTION_SHARE = 0x01;
    public static final int ACTION_BLUETOOTH = 0x02;

    private final AppItem appItem;
    private final int actionType;

    private File destination;

    public ExportApkTask(Context context, AppItem appItem, int actionType) {
        super(context);
        this.appItem = appItem;
        this.actionType = actionType;
    }

    @Override
    public void executeBackground() throws Throwable {
        Context context = getWeakObject();
        if (context != null) {
            File file = new File(appItem.getPath());
            File directory = getExternalDirectory();
            destination = new File(directory, getApkName(appItem));
            if (destination.exists()) {
                destination.delete();
            }
            byte[] buffer = new byte[200 * 1024];
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(destination);
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
    }

    @SuppressLint("NewApi")
    public static File getExternalDirectory() {
        File externalDirectory = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ?
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) : Environment.getExternalStorageDirectory();
        File directory = new File(externalDirectory, "Apps");
        directory.mkdirs();
        return directory;
    }

    public static String getApkPrefix(CommonItem item) {
        return FileHelper.escapeFileSymbols(item.getLabel() + "-" + item.getVersion());
    }

    public static String getApkSuffix() {
        return ".apk";
    }

    public static String getApkName(CommonItem item) {
        return getApkPrefix(item) + getApkSuffix();
    }

    @Override
    public void onSuccessMain() {
        final Context context = getWeakObject();
        if (context != null) {
            switch (actionType) {
                case ACTION_EXTRACT: {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.success)
                            .setMessage(Html.fromHtml(context.getString(R.string.app_extract_success, destination.getPath())))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    shareApk(context, destination);
                                }
                            }).setNegativeButton(R.string.no, null)
                            .create();
                    alertDialog.show();
                    break;
                }
                case ACTION_SHARE: {
                    shareApk(context, destination);
                    break;
                }
                case ACTION_BLUETOOTH: {
                    bluetoothApk(context, appItem);
                    break;
                }
            }
        }
    }

    public static void shareApk(Context context, File destination) {
        Uri uri = Uri.fromFile(destination);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, destination.getName());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/zip");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    public static void bluetoothApk(Context context, CommonItem item) {
        Uri uri = Uri.fromFile(new File(item.getPath()));
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, item.getLabel());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/zip");
        sendIntent.setPackage("com.android.bluetooth");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    @Override
    public void onFailMain(Throwable ex) {
        Context context = getWeakObject();
        if (context != null) {
            Toast.makeText(context, R.string.app_extract_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
