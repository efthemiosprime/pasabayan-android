package com.efthemiosprime.pasabayan.features.notifications.services

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Captures the device metadata sent with `POST /device-tokens`. Indirected behind an interface
 * so unit tests can stub it.
 */
interface DeviceInfoProvider {
    val appVersion: String
    val deviceModel: String
    val osVersion: String
    val deviceName: String?
}

@Singleton
class AndroidDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceInfoProvider {

    override val appVersion: String by lazy {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
    }

    override val deviceModel: String = Build.MODEL ?: "unknown"
    override val osVersion: String = Build.VERSION.RELEASE ?: "unknown"
    override val deviceName: String? = Build.DEVICE
}
