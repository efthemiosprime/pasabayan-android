package com.efthemiosprime.pasabayan.features.favorites.services

import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.ApiErrorMapper
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.favorites.AddFavoriteRequestJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierRequestJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoritesApi
import com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val api: FavoritesApi,
    private val json: Json,
) : FavoritesRepository {

    override suspend fun fetchFavorites(
        sort: String,
        hasUpcomingTrips: Boolean?,
    ): Result<List<FavoriteCarrierJson>> = wrap {
        val res = api.listFavorites(sort = sort, hasUpcomingTrips = hasUpcomingTrips)
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data)
    }

    override suspend fun addFavorite(
        carrierId: Int,
        notes: String?,
        notificationEnabled: Boolean?,
    ): Result<Unit> = wrap {
        val res = api.addFavorite(
            carrierId = carrierId,
            body = AddFavoriteRequestJson(
                notes = notes?.takeIf { it.isNotBlank() },
                notificationEnabled = notificationEnabled,
            ),
        )
        // 201 Created is also in the 200..299 range, so isSuccessful is true.
        if (res.isSuccessful) return@wrap Result.success(Unit)
        when (res.code()) {
            400 -> Result.failure(FavoritesError.CannotFavorite(parseMessage(res.errorBody()?.bytes())))
            409 -> Result.failure(FavoritesError.AlreadyFavorited)
            404 -> Result.failure(FavoritesError.NotFound)
            else -> Result.failure(mapError(res))
        }
    }

    override suspend fun removeFavorite(carrierId: Int): Result<Unit> = wrap {
        val res = api.removeFavorite(carrierId)
        if (!res.isSuccessful) {
            return@wrap when (res.code()) {
                404 -> Result.failure(FavoritesError.NotFound)
                else -> Result.failure(mapError(res))
            }
        }
        Result.success(Unit)
    }

    override suspend fun isFavorite(carrierId: Int): Result<Boolean> = wrap {
        val res = api.isFavorite(carrierId)
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.isFavorite)
    }

    override suspend fun sendDeliveryRequest(
        carrierId: Int,
        request: SendDeliveryRequestJson,
    ): Result<Unit> = wrap {
        val res = api.requestDelivery(carrierId, request)
        if (res.isSuccessful) return@wrap Result.success(Unit)
        Result.failure(mapError(res))
    }

    override suspend fun fetchSentRequests(): Result<List<FavoriteCarrierRequestJson>> = wrap {
        val res = api.sentRequests()
        if (!res.isSuccessful) return@wrap Result.failure(mapError(res))
        val body = res.body() ?: return@wrap Result.failure(invalid())
        Result.success(body.data)
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

    private fun parseMessage(body: ByteArray?): String? {
        if (body == null || body.isEmpty()) return null
        return runCatching {
            json.parseToJsonElement(String(body, Charsets.UTF_8))
                .jsonObject["message"]
                ?.jsonPrimitive
                ?.content
        }.getOrNull()
    }
}
