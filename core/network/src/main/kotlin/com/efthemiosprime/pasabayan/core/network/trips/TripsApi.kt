package com.efthemiosprime.pasabayan.core.network.trips

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

/** Trip endpoints per `android-spec/03-trips.md`. */
interface TripsApi {

    @GET("trips")
    suspend fun getTrips(): Response<TripsResponseJson>

    @GET("carrier/trips")
    suspend fun getCarrierTrips(): Response<TripsResponseJson>

    @GET("trips/{id}")
    suspend fun getTrip(@Path("id") id: Int): Response<TripResponseJson>

    @GET("trips/{id}/matches")
    suspend fun getTripMatches(@Path("id") id: Int): Response<TripMatchesResponseJson>

    @POST("trips")
    suspend fun createTrip(@Body body: CreateTripRequestJson): Response<TripResponseJson>

    @PUT("trips/{id}")
    suspend fun updateTrip(
        @Path("id") id: Int,
        @Body body: TripUpdateRequestJson,
    ): Response<TripResponseJson>

    @DELETE("trips/{id}")
    suspend fun deleteTrip(@Path("id") id: Int): Response<TripDeleteResponseJson>

    @GET("trips/available")
    suspend fun getAvailableTrips(@QueryMap params: Map<String, String>): Response<TripsSuccessResponseJson>

    @GET("packages/{id}/trip-template")
    suspend fun getTripTemplate(@Path("id") packageId: Int): Response<TripTemplateResponseJson>

    @GET("routes/popular-packages")
    suspend fun getPopularPackageRoutes(): Response<PopularRoutesResponseJson>

    @GET("route-activity/summary")
    suspend fun getRouteActivitySummary(): Response<RouteActivitySummaryResponseJson>
}
