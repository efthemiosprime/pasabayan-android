package com.efthemiosprime.pasabayan

import android.app.Application
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationChannelSetup
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PasabayanApplication : Application() {

    @Inject lateinit var notificationChannelSetup: NotificationChannelSetup

    override fun onCreate() {
        super.onCreate()
        notificationChannelSetup.ensureChannels()
    }
}
