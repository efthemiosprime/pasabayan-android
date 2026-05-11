package com.efthemiosprime.pasabayan.features.favorites.model

import com.efthemiosprime.pasabayan.core.network.favorites.FavoriteCarrierJson

data class FavoritesUiState(
    val favorites: List<FavoriteCarrierJson> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val sort: FavoritesSort = FavoritesSort.RECENT,
    val onlyUpcomingTrips: Boolean = false,
    val pendingRemovalIds: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

enum class FavoritesSort(val raw: String) {
    RECENT("recent"),
    MOST_USED("most_used"),
    RATING("rating"),
}
