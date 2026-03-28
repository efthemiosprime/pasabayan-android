package com.efthemiosprime.pasabayan.onboarding

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
}
