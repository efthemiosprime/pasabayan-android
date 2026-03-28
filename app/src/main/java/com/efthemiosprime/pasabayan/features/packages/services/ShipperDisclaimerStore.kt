package com.efthemiosprime.pasabayan.features.packages.services

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-user shipper disclaimer acknowledgment with offline sync flag.
 * Keys: `shipper_disclaimer_ack_v1_{userId}`, `shipper_disclaimer_pending_sync_v1_{userId}`.
 */
@Singleton
class ShipperDisclaimerStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun hasAcknowledged(userId: Int): Boolean =
        prefs.getBoolean(ackKey(userId), false)

    fun setAcknowledged(userId: Int) {
        prefs.edit().putBoolean(ackKey(userId), true).apply()
    }

    fun isPendingSync(userId: Int): Boolean =
        prefs.getBoolean(syncKey(userId), false)

    fun setPendingSync(userId: Int, pending: Boolean) {
        prefs.edit().putBoolean(syncKey(userId), pending).apply()
    }

    private fun ackKey(userId: Int) = "shipper_disclaimer_ack_v1_$userId"
    private fun syncKey(userId: Int) = "shipper_disclaimer_pending_sync_v1_$userId"
}
