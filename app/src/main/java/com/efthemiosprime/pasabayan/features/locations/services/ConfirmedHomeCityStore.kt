package com.efthemiosprime.pasabayan.features.locations.services

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the city id that home-city detection most recently confirmed for the user. Used to
 * skip redundant `PUT /profile { homeCityId }` calls when the server already agrees. iOS
 * parity: `confirmedHomeCityId` UserDefault.
 */
interface ConfirmedHomeCityStore {
    fun get(): Int?
    fun set(cityId: Int?)
}

@Singleton
class SharedPreferencesConfirmedHomeCityStore @Inject constructor(
    @ApplicationContext context: Context,
) : ConfirmedHomeCityStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(): Int? =
        if (prefs.contains(KEY_CONFIRMED_CITY)) prefs.getInt(KEY_CONFIRMED_CITY, -1).takeIf { it >= 0 }
        else null

    override fun set(cityId: Int?) {
        prefs.edit().apply {
            if (cityId == null) remove(KEY_CONFIRMED_CITY)
            else putInt(KEY_CONFIRMED_CITY, cityId)
            apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "pasabayan_locations"
        const val KEY_CONFIRMED_CITY = "confirmed_home_city_id"
    }
}
