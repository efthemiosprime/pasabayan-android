package com.efthemiosprime.pasabayan.onboarding

/** iOS `CitySelectionOnboardingView` network calls (CA catalog + `PUT /profile`). */
interface CityOnboardingRepository {

    suspend fun fetchCitiesCanada(): Result<List<CityPickerOption>>

    suspend fun updateHomeCity(cityId: Int): Result<Unit>
}
