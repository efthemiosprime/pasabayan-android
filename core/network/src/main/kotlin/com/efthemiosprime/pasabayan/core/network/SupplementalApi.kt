package com.efthemiosprime.pasabayan.core.network

import com.efthemiosprime.pasabayan.core.network.location.CitiesResponseJson
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/** City catalog and other supplemental endpoints. Profile + consent: [com.efthemiosprime.pasabayan.core.network.profile.ProfileApi]. */
interface SupplementalApi {

    @GET("locations/countries/{country}/cities")
    suspend fun getCitiesForCountry(
        @Path("country") countryCode: String,
    ): Response<CitiesResponseJson>
}
