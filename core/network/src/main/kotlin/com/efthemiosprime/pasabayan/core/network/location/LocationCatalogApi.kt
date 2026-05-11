package com.efthemiosprime.pasabayan.core.network.location

import retrofit2.Response
import retrofit2.http.GET

/**
 * Full location catalog (countries / states / cities / aliases). Spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Location catalog".
 * iOS reference: `LocationCatalogService`.
 *
 * Per-country city listings live on
 * [com.efthemiosprime.pasabayan.core.network.SupplementalApi.getCitiesForCountry].
 */
interface LocationCatalogApi {

    @GET("locations/catalog")
    suspend fun getCatalog(): Response<LocationCatalogResponseJson>
}
