package com.tomclaw.appsend.screen.chat.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.di.TIME_FORMATTER
import com.tomclaw.appsend.net.UserData
import com.tomclaw.appsend.screen.chat.ChatInteractor
import com.tomclaw.appsend.screen.chat.ChatInteractorImpl
import com.tomclaw.appsend.screen.chat.ChatPresenter
import com.tomclaw.appsend.screen.chat.ChatPresenterImpl
import com.tomclaw.appsend.screen.chat.ChatResourceProvider
import com.tomclaw.appsend.screen.chat.ChatResourceProviderImpl
import com.tomclaw.appsend.screen.chat.MessageConverter
import com.tomclaw.appsend.screen.chat.MessageConverterImpl
import com.tomclaw.appsend.screen.chat.adapter.msg.IncomingMsgItemBlueprint
import com.tomclaw.appsend.screen.chat.adapter.msg.IncomingMsgItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItemBlueprint
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItemPresenter
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Named

@Module
class ChatModule(
    private val context: Context,
    private val topicId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        converter: MessageConverter,
        interactor: ChatInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): ChatPresenter = ChatPresenterImpl(
        topicId,
        converter,
        interactor,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        userData: UserData,
        api: StoreApi,
        schedulers: SchedulersFactory
    ): ChatInteractor = ChatInteractorImpl(userData, api, schedulers)

    @Provides
    @PerActivity
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerActivity
    internal fun provideMessageConverter(
        @Named(TIME_FORMATTER) timeFormatter: DateFormat,
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        resourceProvider: ChatResourceProvider
    ): MessageConverter = MessageConverterImpl(timeFormatter, dateFormatter, resourceProvider)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): ChatResourceProvider =
        ChatResourceProviderImpl(context.resources)

    @Provides
    @PerActivity
    internal fun provideItemBinder(
        blueprintSet: Set<@JvmSuppressWildcards ItemBlueprint<*, *>>
    ): ItemBinder {
        return ItemBinder.Builder().apply {
            blueprintSet.forEach { registerItem(it) }
        }.build()
    }

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideIncomingMsgItemBlueprint(
        presenter: IncomingMsgItemPresenter
    ): ItemBlueprint<*, *> = IncomingMsgItemBlueprint(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideOutgoingMsgItemBlueprint(
        presenter: OutgoingMsgItemPresenter
    ): ItemBlueprint<*, *> = OutgoingMsgItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideIncomingMsgItemPresenter(
        presenter: ChatPresenter
    ) = IncomingMsgItemPresenter(presenter)

    @Provides
    @PerActivity
    internal fun provideOutgoingMsgItemPresenter(
        presenter: ChatPresenter
    ) = OutgoingMsgItemPresenter(presenter)

}
