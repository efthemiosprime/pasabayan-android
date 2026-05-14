package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
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
 * Form + submission state for the "Rate Delivery" sheet. Mirrors iOS
 * `RatingViewModel` driven by `RateDeliverySheet.swift`.
 *
 * Star rating is stored as Int (0..5) with 0 = unset. Review text is capped
 * at [REVIEW_CHARACTER_LIMIT] characters, matching iOS' default limit.
 */
data class RateDeliveryUiState(
    val rating: Int = 0,
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val characterCount: Int get() = reviewText.length
    val isOverCharacterLimit: Boolean get() = characterCount > REVIEW_CHARACTER_LIMIT
    val canSubmit: Boolean
        get() = rating in 1..5 && !isSubmitting && !isOverCharacterLimit

    companion object {
        const val REVIEW_CHARACTER_LIMIT: Int = 500
    }
}

@HiltViewModel
class RateDeliveryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RateDeliveryUiState())
    val uiState: StateFlow<RateDeliveryUiState> = _uiState.asStateFlow()

    fun setRating(value: Int) {
        _uiState.update { it.copy(rating = value.coerceIn(0, 5)) }
    }

    fun setReviewText(value: String) {
        _uiState.update { it.copy(reviewText = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Submit the rating for [matchId]. Calls [onSubmitted] with `true` on success
     * so the host can dismiss the sheet, or `false` on failure so it keeps the
     * sheet open for the user to retry.
     */
    fun submitRating(matchId: Int, onSubmitted: (Boolean) -> Unit = {}) {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            bookingsRepository.submitRating(
                matchId = matchId,
                rating = state.rating,
                reviewText = state.reviewText.ifBlank { null },
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = context.getString(R.string.bookings_rate_delivery_success_message),
                        )
                    }
                    onSubmitted(true)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message
                                ?: context.getString(R.string.bookings_rate_delivery_error_generic),
                        )
                    }
                    onSubmitted(false)
                },
            )
        }
    }
}
