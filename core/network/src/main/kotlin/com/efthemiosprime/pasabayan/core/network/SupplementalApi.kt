package com.efthemiosprime.pasabayan.core.network

import com.efthemiosprime.pasabayan.core.network.location.CitiesResponseJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesResponseJson
import com.efthemiosprime.pasabayan.core.network.profile.ConsentPreferencesUpdateJson
import com.efthemiosprime.pasabayan.core.network.profile.ProfileResponseJson
import com.efthemiosprime.pasabayan.core.network.profile.UpdateProfileHomeCityRequestJson
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Locations + profile endpoints used after auth (iOS `CitySelectionOnboardingView`, `ProfileAPIService`). */
interface SupplementalApi {

    @GET("locations/countries/{country}/cities")
    suspend fun getCitiesForCountry(
        @Path("country") countryCode: String,
    ): Response<CitiesResponseJson>

    @PUT("profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileHomeCityRequestJson,
    ): Response<ProfileResponseJson>

    @PUT("profile/consent-preferences")
    suspend fun updateConsentPreferences(
        @Body body: ConsentPreferencesUpdateJson,
    ): Response<ConsentPreferencesResponseJson>
}
