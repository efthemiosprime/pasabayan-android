package com.efthemiosprime.pasabayan.features.onboarding.services

import android.content.Context
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences
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

    override suspend fun hasCompletedCitySetup(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_HAS_COMPLETED_CITY_SETUP, false)
    }

    override suspend fun setHasCompletedCitySetup(completed: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_CITY_SETUP, completed).apply()
    }

    override suspend fun hasCompletedConsentSetup(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_HAS_COMPLETED_CONSENT_SETUP, false)
    }

    override suspend fun setHasCompletedConsentSetup(completed: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_CONSENT_SETUP, completed).apply()
    }

    override suspend fun getPushNotificationsApiConsentOptIn(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_PUSH_NOTIFICATIONS_API_CONSENT_OPT_IN, false)
    }

    override suspend fun setPushNotificationsApiConsentOptIn(optedIn: Boolean): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS_API_CONSENT_OPT_IN, optedIn).apply()
    }

    companion object {
        const val KEY_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        const val KEY_HAS_VIEWED_CARRIER_JOURNEY = "hasViewedCarrierJourney"
        const val KEY_HAS_VIEWED_SENDER_JOURNEY = "hasViewedSenderJourney"
        const val KEY_USER_PREFERRED_ROLE = "user_preferred_role"
        const val KEY_HAS_COMPLETED_CITY_SETUP = "hasCompletedCitySetup"
        const val KEY_HAS_COMPLETED_CONSENT_SETUP = "hasCompletedConsentSetup"
        const val KEY_PUSH_NOTIFICATIONS_API_CONSENT_OPT_IN = "push_notifications_api_consent_opt_in"
    }
}
