package com.efthemiosprime.pasabayan.core.network.shipper

import retrofit2.Response
import retrofit2.http.GET

/**
 * Shipper-specific endpoints. Spec
 * [12-legal-support-misc.md](android-spec/12-legal-support-misc.md) § "Shipper". iOS reference:
 * `ShipperAPIService`.
 */
interface ShipperApi {

    @GET("shipper/nearby-carriers")
    suspend fun getNearbyCarriers(): Response<NearbyCarriersResponseJson>
}
