package com.efthemiosprime.pasabayan.features.notifications.services

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl,
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindFCMTokenStore(
        impl: SharedPreferencesFCMTokenStore,
    ): FCMTokenStore

    @Binds
    @Singleton
    abstract fun bindPushNotificationsConsentIntent(
        impl: SharedPreferencesPushNotificationsConsentIntent,
    ): PushNotificationsConsentIntent

    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(
        impl: AndroidDeviceInfoProvider,
    ): DeviceInfoProvider
}
