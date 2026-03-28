package com.efthemiosprime.pasabayan.features.onboarding.model

/**
 * Cold-start onboarding + journey flags. Keys align with iOS `AppStorage` /
 * `UserDefaults` (`hasCompletedOnboarding`, `hasViewedCarrierJourney`, `hasViewedSenderJourney`,
 * `user_preferred_role`).
 */
interface OnboardingPreferences {

    suspend fun hasCompletedOnboarding(): Boolean

    suspend fun setHasCompletedOnboarding(completed: Boolean)

    suspend fun hasViewedCarrierJourney(): Boolean

    suspend fun setHasViewedCarrierJourney(viewed: Boolean)

    suspend fun hasViewedSenderJourney(): Boolean

    suspend fun setHasViewedSenderJourney(viewed: Boolean)

    suspend fun getPreferredRoleWire(): String?

    suspend fun setPreferredRoleWire(roleWire: String)

    /** iOS `@AppStorage("hasCompletedCitySetup")` in `ContentView`. */
    suspend fun hasCompletedCitySetup(): Boolean

    suspend fun setHasCompletedCitySetup(completed: Boolean)

    /** iOS `@AppStorage("hasCompletedConsentSetup")` in `ContentView`. */
    suspend fun hasCompletedConsentSetup(): Boolean

    suspend fun setHasCompletedConsentSetup(completed: Boolean)

    /**
     * iOS `PushNotificationsAPIConsentIntent` (`UserDefaults` key `push_notifications_api_consent_opt_in`).
     * Set when consent API save succeeds with the user’s push toggle value.
     */
    suspend fun getPushNotificationsApiConsentOptIn(): Boolean

    suspend fun setPushNotificationsApiConsentOptIn(optedIn: Boolean)
}
