package com.efthemiosprime.pasabayan.onboarding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SharedPreferencesOnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : OnboardingPreferences {

    /** Same file as default shared preferences (iOS `UserDefaults.standard` parity for keys). */
    private val prefs by lazy {
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }

    override suspend fun setHasCompletedOnboarding(completed: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
    }

    override suspend fun hasViewedCarrierJourney(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_HAS_VIEWED_CARRIER_JOURNEY, false)
    }

    override suspend fun setHasViewedCarrierJourney(viewed: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_HAS_VIEWED_CARRIER_JOURNEY, viewed).apply()
    }

    override suspend fun hasViewedSenderJourney(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_HAS_VIEWED_SENDER_JOURNEY, false)
    }

    override suspend fun setHasViewedSenderJourney(viewed: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_HAS_VIEWED_SENDER_JOURNEY, viewed).apply()
    }

    override suspend fun getPreferredRoleWire(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_USER_PREFERRED_ROLE, null)
    }

    override suspend fun setPreferredRoleWire(roleWire: String): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_USER_PREFERRED_ROLE, roleWire).apply()
    }

    companion object {
        const val KEY_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        const val KEY_HAS_VIEWED_CARRIER_JOURNEY = "hasViewedCarrierJourney"
        const val KEY_HAS_VIEWED_SENDER_JOURNEY = "hasViewedSenderJourney"
        const val KEY_USER_PREFERRED_ROLE = "user_preferred_role"
    }
}
