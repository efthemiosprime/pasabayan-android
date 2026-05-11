package com.efthemiosprime.pasabayan.features.system.services

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces the `ipAddress` and `userAgent` strings the backend stores alongside each activity
 * log entry. iOS parity: `getDeviceIPAddress` + `getUserAgentString` in `ActivityLogger.swift`.
 *
 * The `ipAddress` field is a misnomer on the wire — it's actually a per-install diagnostic
 * identifier capped at 45 chars. iOS uses `iOS_{first8(vendorUUID)}`; Android uses
 * `Android_{first8(ANDROID_ID)}`.
 */
interface ActivityLogDeviceContext {
    val ipAddress: String
    val userAgent: String
}

@Singleton
class AndroidActivityLogDeviceContext @Inject constructor(
    @ApplicationContext private val context: Context,
) : ActivityLogDeviceContext {

    override val ipAddress: String by lazy {
        val androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull().orEmpty()
        val short = androidId.take(8).ifBlank { "unknown" }
        "Android_$short"
    }

    override val userAgent: String by lazy {
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
        "Pasabayan Android $versionName"
    }
}
