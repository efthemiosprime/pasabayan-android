package com.efthemiosprime.pasabayan.features.favorites.services

import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson
import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierRequestJson
import com.efthemiosprime.pasabayan.core.network.favorites.SendDeliveryRequestJson

interface FavoritesRepository {

    suspend fun fetchFavorites(
        sort: String = "recent",
        hasUpcomingTrips: Boolean? = null,
    ): Result<List<FavoriteCarrierJson>>

    suspend fun addFavorite(
        carrierId: Int,
        notes: String? = null,
        notificationEnabled: Boolean? = null,
    ): Result<Unit>

    suspend fun removeFavorite(carrierId: Int): Result<Unit>

    suspend fun isFavorite(carrierId: Int): Result<Boolean>

    suspend fun sendDeliveryRequest(
        carrierId: Int,
        request: SendDeliveryRequestJson,
    ): Result<Unit>

    suspend fun fetchSentRequests(): Result<List<FavoriteCarrierRequestJson>>
}
