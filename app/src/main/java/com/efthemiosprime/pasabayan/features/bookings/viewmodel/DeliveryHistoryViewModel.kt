package com.efthemiosprime.pasabayan.features.bookings.viewmodel

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
 * UI state for the carrier-side delivered-trips history.
 *
 * iOS parity: `DeliveryHistoryViewModel.swift` (lines 184–242 of
 * `Features/Bookings/Views/History/DeliveryHistoryView.swift`). Same shape
 * as the shipper-side [com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageHistoryUiState]
 * — only the `totalEarned` accumulator label differs.
 */
data class DeliveryHistoryUiState(
    val matches: List<DeliveryMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
) {
    /** Sum of `agreed_price` across delivered matches. iOS parity: `totalEarnings`. */
    val totalEarned: Double
        get() = matches.sumOf { it.agreedPrice }
}

@HiltViewModel
class DeliveryHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryHistoryUiState())
    val uiState: StateFlow<DeliveryHistoryUiState> = _uiState.asStateFlow()

    /**
     * Load carrier-side delivered matches. iOS parity: calls
     * `/bookings?role=carrier&status=delivered`. Client-side filter on
     * `MatchStatus.DELIVERED` guards against server response drift.
     */
    fun loadHistory(force: Boolean = false) {
        if (!force && _uiState.value.hasLoaded) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingsRepository.loadMatches(role = "carrier", status = "delivered").fold(
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
                                ?: context.getString(R.string.bookings_history_error_load),
                        )
                    }
                },
            )
        }
    }

    fun refresh() = loadHistory(force = true)
}
