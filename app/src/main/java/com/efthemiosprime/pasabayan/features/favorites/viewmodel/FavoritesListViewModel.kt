package com.efthemiosprime.pasabayan.features.favorites.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.favorites.model.FavoritesSort
import com.efthemiosprime.pasabayan.features.favorites.model.FavoritesUiState
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesError
import com.efthemiosprime.pasabayan.features.favorites.services.FavoritesRepository
import com.efthemiosprime.pasabayan.shared.error.localizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesListViewModel @Inject constructor(
    private val repository: FavoritesRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    fun load(forceRefresh: Boolean = false) {
        val snapshot = _state.value
        if (snapshot.isLoading) return
        if (snapshot.isInitialized && !forceRefresh) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.fetchFavorites(
                sort = snapshot.sort.raw,
                hasUpcomingTrips = if (snapshot.onlyUpcomingTrips) true else null,
            ).fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(
                            favorites = list,
                            isLoading = false,
                            isInitialized = true,
                        )
                    }
                },
                onFailure = { applyError(it) },
            )
        }
    }

    fun setSort(sort: FavoritesSort) {
        if (_state.value.sort == sort) return
        _state.update { it.copy(sort = sort, isInitialized = false) }
        load(forceRefresh = true)
    }

    fun setOnlyUpcomingTrips(value: Boolean) {
        if (_state.value.onlyUpcomingTrips == value) return
        _state.update { it.copy(onlyUpcomingTrips = value, isInitialized = false) }
        load(forceRefresh = true)
    }

    fun removeFavorite(carrierId: Int) {
        if (carrierId in _state.value.pendingRemovalIds) return
        _state.update { it.copy(pendingRemovalIds = it.pendingRemovalIds + carrierId) }
        viewModelScope.launch {
            repository.removeFavorite(carrierId).fold(
                onSuccess = {
                    _state.update { s ->
                        s.copy(
                            favorites = s.favorites.filterNot { it.carrier.id == carrierId },
                            pendingRemovalIds = s.pendingRemovalIds - carrierId,
                            infoMessage = appContext.getString(
                                com.efthemiosprime.pasabayan.R.string.favorites_removed,
                            ),
                        )
                    }
                },
                onFailure = {
                    _state.update { s -> s.copy(pendingRemovalIds = s.pendingRemovalIds - carrierId) }
                    applyError(it)
                },
            )
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun applyError(throwable: Throwable) {
        val message = when (throwable) {
            is FavoritesError.AlreadyFavorited -> appContext.getString(
                com.efthemiosprime.pasabayan.R.string.favorites_error_already_favorited,
            )
            is FavoritesError.NotFound -> appContext.getString(
                com.efthemiosprime.pasabayan.R.string.favorites_error_not_found,
            )
            is FavoritesError.CannotFavorite -> throwable.reason
                ?: appContext.getString(com.efthemiosprime.pasabayan.R.string.favorites_error_cannot_favorite)
            else -> ((throwable as? DomainErrorMapperException)?.domainError
                ?: DomainError.NetworkError(throwable)).localizedMessage(appContext)
        }
        _state.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
