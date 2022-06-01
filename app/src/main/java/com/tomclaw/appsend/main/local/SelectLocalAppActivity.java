package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.List;

import static com.tomclaw.appsend.util.Analytics.trackEvent;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;

@SuppressLint("Registered")
@EActivity(R.layout.local_apps)
public class SelectLocalAppActivity extends PermisoActivity implements CommonItemClickListener {

    public static final String SELECTED_ITEM = "selected_item";

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewPager pager;

    @ViewById
    TabLayout tabs;

    @Extra
    DialogData dialogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        List<Pair<String, Fragment>> fragments = Arrays.asList(
                new Pair<>(
                        getString(R.string.nav_installed),
                        new SelectInstalledFragment_()
                ),
                new Pair<>(
                        getString(R.string.nav_distro),
                        new SelectDistroFragment_()
                )
        );

        LocalAppsPagerAdapter adapter = new LocalAppsPagerAdapter(getSupportFragmentManager(), fragments);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);

        uploadNotice();
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        leaveScreen();
        return true;
    }

    @Override
    public void onBackPressed() {
        leaveScreen();
    }

    @Override
    public void onClick(final CommonItem item) {
        if (dialogData != null) {
            final Context context = this;
            ListAdapter menuAdapter = new MenuAdapter(this, R.array.upload_actions_titles, R.array.upload_actions_icons);
            new AlertDialog.Builder(this)
                    .setAdapter(menuAdapter, (dialog, which) -> {
                        switch (which) {
                            case 0: {
                                leaveScreen(item);
                                trackEvent("click-upload-apk");
                                break;
                            }
                            case 1: {
                                String packageName = item.getPackageName();
                                openGooglePlay(context, packageName);
                                trackEvent("click-search-google-play");
                                break;
                            }
                            case 2: {
                                Intent intent = new Intent(context, DownloadActivity.class)
                                        .putExtra(DownloadActivity.STORE_APP_PACKAGE, item.getPackageName())
                                        .putExtra(DownloadActivity.STORE_APP_LABEL, item.getLabel())
                                        .putExtra(DownloadActivity.STORE_FINISH_ONLY, true);
                                startActivity(intent);
                                trackEvent("click-search-appteka");
                                break;
                            }
                        }
                    }).show();
        } else {
            leaveScreen(item);
        }
    }

    private void uploadNotice() {
        if (PreferenceHelper.isShowUploadNotice(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.upload_notice_title))
                    .setMessage(getString(R.string.upload_notice_text))
                    .setNegativeButton(R.string.yes, (dialog, which) -> PreferenceHelper.setShowUploadNotice(SelectLocalAppActivity.this, false))
                    .setPositiveButton(R.string.no, (dialog, which) -> {
                        showError(R.string.agree_with_upload_notice);
                        leaveScreen();
                    })
                    .show();
        }
    }

    private void showError(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void leaveScreen() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void leaveScreen(CommonItem item) {
        Intent data = new Intent();
        data.putExtra(SELECTED_ITEM, item);
        setResult(RESULT_OK, data);
        finish();
    }

    public static class LocalAppsPagerAdapter extends FragmentStatePagerAdapter {

        private List<Pair<String, Fragment>> fragments;

        LocalAppsPagerAdapter(FragmentManager fm, List<Pair<String, Fragment>> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(position).second;
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).first;
        }
    }

}
