package com.efthemiosprime.pasabayan.core.network.ratings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Ratings endpoints per `11-favorites-ratings.md`. Rating submission for a booking flows through
 * the bookings spec (05); this surface only lists received/given/pending and updates an existing
 * review's comment text.
 */
interface RatingsApi {

    @GET("users/{userId}/ratings")
    suspend fun userRatings(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("sort") sort: String = "recent",
    ): Response<UserReceivedRatingsResponseJson>

    @GET("ratings/given")
    suspend fun givenRatings(
        @Query("page") page: Int = 1,
    ): Response<UserGivenRatingsResponseJson>

    @GET("ratings/pending")
    suspend fun pendingRatings(
        @Query("page") page: Int = 1,
    ): Response<PendingReviewsResponseJson>

    @PUT("ratings/{ratingId}/comment")
    suspend fun updateComment(
        @Path("ratingId") ratingId: Int,
        @Body body: CommentUpdateRequestJson,
    ): Response<CommentUpdateResponseJson>
}
