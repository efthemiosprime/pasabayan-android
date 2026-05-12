package com.efthemiosprime.pasabayan.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.ratings.RatingWithRaterJson
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesRepository
import com.efthemiosprime.pasabayan.features.profile.services.ProfileRepository
import com.efthemiosprime.pasabayan.features.ratings.services.RatingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfilePopoverUiState(
    // -- Reviews / ratings list --
    val reviews: List<RatingWithRaterJson> = emptyList(),
    val reviewsCurrentPage: Int = 0,
    val reviewsHasMore: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val isLoadingMoreReviews: Boolean = false,
    val reviewsErrorMessage: String? = null,
    // -- Carrier performance stats (only meaningful when viewing self as carrier) --
    val carrierStats: CarrierStatsJson? = null,
    val isLoadingStats: Boolean = false,
    val statsErrorMessage: String? = null,
    // -- Favorite (only meaningful when shipper viewing a carrier) --
    val isFavorite: Boolean = false,
    val isCheckingFavorite: Boolean = false,
    val isTogglingFavorite: Boolean = false,
    val favoriteErrorMessage: String? = null,
) {
    /**
     * Map of `1..5` → count, computed from [reviews]. Empty when no reviews
     * have loaded yet. Used by the rating-distribution histogram.
     */
    val ratingDistribution: Map<Int, Int>
        get() = reviews.groupingBy { it.rating }.eachCount()
}

/**
 * Drives the reusable user-profile popover. Parity with iOS
 * `UserProfilePopover.swift`.
 *
 * The popover surfaces three independent data feeds:
 * - Public reviews list (`RatingsRepository.fetchReceivedRatings(userId)`) — works for any user.
 * - Performance stats — only loads when the viewer is looking at themselves (the
 *   carrier-stats endpoint is `/api/carrier/stats`, which always returns the
 *   authenticated user's own stats). iOS calls this for every viewed user and
 *   accidentally shows the viewer's own stats labeled as someone else's;
 *   Android takes the correct path and only loads when `viewerIsSelf`.
 * - Favorite toggle — only relevant when a shipper is viewing a carrier.
 */
@HiltViewModel
class UserProfilePopoverViewModel @Inject constructor(
    private val ratingsRepository: RatingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfilePopoverUiState())
    val uiState: StateFlow<UserProfilePopoverUiState> = _uiState.asStateFlow()

    /**
     * Entry point. Loads the public reviews unconditionally; loads stats only
     * when [viewerIsSelf]; runs the favorite check only when
     * [shouldCheckFavorite] (`true` when a shipper is viewing a carrier).
     */
    fun load(
        userId: Int,
        viewerIsSelf: Boolean,
        shouldCheckFavorite: Boolean,
    ) {
        loadReviews(userId = userId, reset = true)
        if (viewerIsSelf) loadCarrierStats()
        if (shouldCheckFavorite) checkFavorite(userId)
    }

    fun loadReviews(userId: Int, reset: Boolean = false) {
        val requestPage = if (reset) 1 else _uiState.value.reviewsCurrentPage + 1
        _uiState.update {
            it.copy(
                isLoadingReviews = reset,
                isLoadingMoreReviews = !reset,
                reviewsErrorMessage = null,
            )
        }
        viewModelScope.launch {
            ratingsRepository.fetchReceivedRatings(userId = userId, page = requestPage).fold(
                onSuccess = { data ->
                    val pagination = data.ratings
                    val incoming = pagination.data
                    _uiState.update { state ->
                        val merged = if (reset) {
                            incoming
                        } else {
                            (state.reviews + incoming).distinctBy { it.id }
                        }
                        state.copy(
                            reviews = merged,
                            reviewsCurrentPage = pagination.currentPage,
                            reviewsHasMore = pagination.currentPage < pagination.lastPage,
                            isLoadingReviews = false,
                            isLoadingMoreReviews = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingReviews = false,
                            isLoadingMoreReviews = false,
                            reviewsErrorMessage = e.message,
                        )
                    }
                },
            )
        }
    }

    fun loadMoreReviews(userId: Int) {
        val current = _uiState.value
        if (current.isLoadingReviews || current.isLoadingMoreReviews) return
        if (!current.reviewsHasMore) return
        loadReviews(userId = userId, reset = false)
    }

    private fun loadCarrierStats() {
        _uiState.update { it.copy(isLoadingStats = true, statsErrorMessage = null) }
        viewModelScope.launch {
            profileRepository.fetchCarrierStats().fold(
                onSuccess = { stats ->
                    _uiState.update {
                        it.copy(
                            carrierStats = stats,
                            isLoadingStats = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingStats = false,
                            statsErrorMessage = e.message,
                        )
                    }
                },
            )
        }
    }

    private fun checkFavorite(carrierId: Int) {
        _uiState.update { it.copy(isCheckingFavorite = true) }
        viewModelScope.launch {
            favoritesRepository.isFavorite(carrierId).fold(
                onSuccess = { fav ->
                    _uiState.update {
                        it.copy(isFavorite = fav, isCheckingFavorite = false)
                    }
                },
                onFailure = {
                    // Graceful degradation: treat as "not favorited" so the
                    // button still works for adds. iOS does the same.
                    _uiState.update {
                        it.copy(isFavorite = false, isCheckingFavorite = false)
                    }
                },
            )
        }
    }

    /**
     * Optimistic add/remove. Reverts on failure.
     */
    fun toggleFavorite(carrierId: Int) {
        val wasFavorite = _uiState.value.isFavorite
        _uiState.update {
            it.copy(
                isFavorite = !wasFavorite,
                isTogglingFavorite = true,
                favoriteErrorMessage = null,
            )
        }
        viewModelScope.launch {
            val result = if (wasFavorite) {
                favoritesRepository.removeFavorite(carrierId)
            } else {
                favoritesRepository.addFavorite(carrierId)
            }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isTogglingFavorite = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isFavorite = wasFavorite, // revert
                            isTogglingFavorite = false,
                            favoriteErrorMessage = e.message,
                        )
                    }
                },
            )
        }
    }

    /** Reset on dismiss so the next open starts clean. */
    fun reset() {
        _uiState.value = UserProfilePopoverUiState()
    }
}
