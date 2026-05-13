package com.efthemiosprime.pasabayan

import android.app.Application
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationChannelSetup
import com.efthemiosprime.pasabayan.features.system.services.SessionAuthSyncObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class PasabayanApplication : Application() {

    @Inject lateinit var notificationChannelSetup: NotificationChannelSetup

    @Inject lateinit var sessionAuthSyncObserver: SessionAuthSyncObserver

    /**
     * App-lifetime scope for singleton observers (auth-sync, etc.). `SupervisorJob`
     * so a single observer crash doesn't tear down the others; `Dispatchers.Default`
     * keeps lifecycle work off the main thread.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        notificationChannelSetup.ensureChannels()
        sessionAuthSyncObserver.start(appScope)
    }
}
