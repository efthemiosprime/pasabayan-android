package com.efthemiosprime.pasabayan.features.onboarding.services

import com.efthemiosprime.pasabayan.features.onboarding.model.CityPickerOption

/** iOS `CitySelectionOnboardingView` network calls (CA catalog + `PUT /profile`). */
interface CityOnboardingRepository {

    suspend fun fetchCitiesCanada(): Result<List<CityPickerOption>>

    suspend fun updateHomeCity(cityId: Int): Result<Unit>
}
