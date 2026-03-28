package com.efthemiosprime.pasabayan.features.trips.services

import android.content.SharedPreferences
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the carrier's last-used transportation method.
 * Auto-saved on successful trip creation. Key: `carrier_usual_transport_v1_{userId}`.
 */
@Singleton
class UsualTransportStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun get(userId: Int): TransportationMethod? {
        val raw = prefs.getString(key(userId), null) ?: return null
        return TransportationMethod.entries.find { it.name.equals(raw, ignoreCase = true) }
    }

    fun set(userId: Int, method: TransportationMethod) {
        prefs.edit().putString(key(userId), method.name.lowercase()).apply()
    }

    private fun key(userId: Int) = "carrier_usual_transport_v1_$userId"
}
