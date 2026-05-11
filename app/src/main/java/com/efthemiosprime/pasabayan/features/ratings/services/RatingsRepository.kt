package com.efthemiosprime.pasabayan.features.ratings.services

import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson
import com.efthemiosprime.pasabayan.core.network.ratings.UserReceivedRatingsDataJson

interface RatingsRepository {

    suspend fun fetchReceivedRatings(
        userId: Int,
        page: Int = 1,
        sort: String = "recent",
    ): Result<UserReceivedRatingsDataJson>

    suspend fun fetchGivenRatings(page: Int = 1): Result<List<RatingWithRatedJson>>

    suspend fun fetchPendingReviews(page: Int = 1): Result<List<PendingReviewJson>>

    suspend fun updateRatingComment(ratingId: Int, reviewText: String): Result<Unit>
}
