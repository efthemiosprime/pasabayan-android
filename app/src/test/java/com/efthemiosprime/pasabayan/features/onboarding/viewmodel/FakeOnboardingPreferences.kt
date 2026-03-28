package com.efthemiosprime.pasabayan.features.onboarding.viewmodel

import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingPreferences

class FakeOnboardingPreferences : OnboardingPreferences {
    var onboardingCompleted = false
    var carrierJourneyViewed = false
    var senderJourneyViewed = false
    var preferredRole: String? = null
    var citySetupCompleted = false
    var consentSetupCompleted = false
    var pushConsentOptIn = false

    override suspend fun hasCompletedOnboarding() = onboardingCompleted
    override suspend fun setHasCompletedOnboarding(completed: Boolean) { onboardingCompleted = completed }
    override suspend fun hasViewedCarrierJourney() = carrierJourneyViewed
    override suspend fun setHasViewedCarrierJourney(viewed: Boolean) { carrierJourneyViewed = viewed }
    override suspend fun hasViewedSenderJourney() = senderJourneyViewed
    override suspend fun setHasViewedSenderJourney(viewed: Boolean) { senderJourneyViewed = viewed }
    override suspend fun getPreferredRoleWire() = preferredRole
    override suspend fun setPreferredRoleWire(roleWire: String) { preferredRole = roleWire }
    override suspend fun hasCompletedCitySetup() = citySetupCompleted
    override suspend fun setHasCompletedCitySetup(completed: Boolean) { citySetupCompleted = completed }
    override suspend fun hasCompletedConsentSetup() = consentSetupCompleted
    override suspend fun setHasCompletedConsentSetup(completed: Boolean) { consentSetupCompleted = completed }
    override suspend fun getPushNotificationsApiConsentOptIn() = pushConsentOptIn
    override suspend fun setPushNotificationsApiConsentOptIn(optedIn: Boolean) { pushConsentOptIn = optedIn }
}
