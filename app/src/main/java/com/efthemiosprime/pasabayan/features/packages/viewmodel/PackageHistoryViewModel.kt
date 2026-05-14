package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the shipper-side delivered-packages history.
 *
 * iOS parity: `PackageHistoryViewModel.swift`. Same data shape (list of
 * delivered [DeliveryMatch]es plus a summary card) — driven by the
 * existing bookings endpoint (`role=shipper&status=delivered`).
 */
data class PackageHistoryUiState(
    val matches: List<DeliveryMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
) {
    /** Total amount paid across delivered matches. iOS parity: `totalSpent`. */
    val totalSpent: Double
        get() = matches.sumOf { it.agreedPrice }
}

@HiltViewModel
class PackageHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageHistoryUiState())
    val uiState: StateFlow<PackageHistoryUiState> = _uiState.asStateFlow()

    /**
     * Load delivered shipper matches. iOS parity: calls
     * `/bookings?role=shipper&status=delivered`. Filters defensively on the
     * client so the summary card never includes non-delivered matches even
     * if the server response drifts.
     */
    fun loadHistory(force: Boolean = false) {
        if (!force && _uiState.value.hasLoaded) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingsRepository.loadMatches(role = "shipper", status = "delivered").fold(
                onSuccess = { all ->
                    val delivered = all.filter { it.matchStatus == MatchStatus.DELIVERED }
                    _uiState.update {
                        it.copy(
                            matches = delivered,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = e.message
                                ?: context.getString(R.string.packages_history_error_load),
                        )
                    }
                },
            )
        }
    }

    fun refresh() = loadHistory(force = true)
}
