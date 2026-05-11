package com.efthemiosprime.pasabayan.features.ratings.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.ratings.CommentUpdateRequestJson
import com.efthemiosprime.pasabayan.core.network.ratings.PendingReviewJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRatedJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingsApi
import com.efthemiosprime.pasabayan.core.network.ratings.UserReceivedRatingsDataJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.Response

@Singleton
class RatingsRepositoryImpl @Inject constructor(
    private val api: RatingsApi,
    private val json: Json,
) : RatingsRepository {

    override suspend fun fetchReceivedRatings(
        userId: Int,
        page: Int,
        sort: String,
    ): Result<UserReceivedRatingsDataJson> = wrap {
        val res = api.userRatings(userId = userId, page = page, sort = sort)
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data ?: UserReceivedRatingsDataJson())
    }

    override suspend fun fetchGivenRatings(page: Int): Result<List<RatingWithRatedJson>> = wrap {
        val res = api.givenRatings(page = page)
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data)
    }

    override suspend fun fetchPendingReviews(page: Int): Result<List<PendingReviewJson>> = wrap {
        val res = api.pendingRatings(page = page)
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data)
    }

    override suspend fun updateRatingComment(
        ratingId: Int,
        reviewText: String,
    ): Result<Unit> = wrap {
        val res = api.updateComment(
            ratingId = ratingId,
            body = CommentUpdateRequestJson(reviewText = reviewText),
        )
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        Result.success(Unit)
    }

    private inline fun <T> wrap(block: () -> Result<T>): Result<T> =
        try {
            block()
        } catch (e: Exception) {
            Result.failure(DomainErrorMapperException(DomainError.NetworkError(e)))
        }

    private fun mapError(res: Response<*>): DomainErrorMapperException =
        DomainErrorMapperException(ApiErrorMapper.map(res.code(), res.errorBody()?.bytes(), json))

    private fun invalid(): DomainErrorMapperException =
        DomainErrorMapperException(DomainError.InvalidResponse)
}
