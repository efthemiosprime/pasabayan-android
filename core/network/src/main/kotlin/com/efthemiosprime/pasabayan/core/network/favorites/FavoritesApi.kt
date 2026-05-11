package com.efthemiosprime.pasabayan.core.network.favorites

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Favorites endpoints per `11-favorites-ratings.md`. Add returns HTTP 201; 409 means already
 * favorited; 400 carries a `message` to surface to the user.
 */
interface FavoritesApi {

    @GET("favorites/carriers")
    suspend fun listFavorites(
        @Query("sort") sort: String = "recent",
        @Query("has_upcoming_trips") hasUpcomingTrips: Boolean? = null,
    ): Response<FavoritesResponseJson>

    @POST("carriers/{carrierId}/favorite")
    suspend fun addFavorite(
        @Path("carrierId") carrierId: Int,
        @Body body: AddFavoriteRequestJson,
    ): Response<ResponseBody>

    @DELETE("carriers/{carrierId}/unfavorite")
    suspend fun removeFavorite(@Path("carrierId") carrierId: Int): Response<ResponseBody>

    @GET("carriers/{carrierId}/is-favorite")
    suspend fun isFavorite(@Path("carrierId") carrierId: Int): Response<IsFavoriteResponseJson>

    @POST("carriers/{carrierId}/request-delivery")
    suspend fun requestDelivery(
        @Path("carrierId") carrierId: Int,
        @Body body: SendDeliveryRequestJson,
    ): Response<ResponseBody>

    @GET("favorites/requests/sent")
    suspend fun sentRequests(): Response<DirectRequestsResponseJson>
}
