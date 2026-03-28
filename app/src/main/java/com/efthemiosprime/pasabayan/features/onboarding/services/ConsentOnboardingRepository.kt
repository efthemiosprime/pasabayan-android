package com.efthemiosprime.pasabayan.features.onboarding.services

import com.efthemiosprime.pasabayan.features.onboarding.model.ConsentSelections

/** iOS `ProfileAPIService.updateConsentPreferences` (`PUT /profile/consent-preferences`). */
interface ConsentOnboardingRepository {

    suspend fun updateConsentPreferences(selections: ConsentSelections): Result<Unit>
}
