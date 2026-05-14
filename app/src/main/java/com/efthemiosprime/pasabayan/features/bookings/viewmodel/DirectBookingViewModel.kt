package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.bookings.model.DirectBookingPayload
import com.efthemiosprime.pasabayan.features.bookings.model.nested.DirectBookingData
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
 * Form state for the "Book this trip directly" sheet. Mirrors iOS
 * `DirectBookingSheet`'s `@State` fields (`spaceNeeded`, `weightNeeded`,
 * `pickupLocation`, `deliveryLocation`, `priceAgreed`, `serviceFee`,
 * `specialRequirements`). Inputs are kept as strings until submission to
 * preserve user typing (including in-progress decimals).
 */
data class DirectBookingFormState(
    val spaceNeeded: String = "",
    val weightNeeded: String = "",
    val pickupLocation: String = "",
    val deliveryLocation: String = "",
    val priceAgreed: String = "",
    val serviceFee: String = "",
    val specialRequirements: String = "",
) {
    val priceAgreedValue: Double? get() = priceAgreed.toDoubleOrNull()
    val serviceFeeValue: Double get() = serviceFee.toDoubleOrNull() ?: 0.0

    /** Total displayed under pricing — `priceAgreed + serviceFee`. Falls back to 0 when invalid. */
    val calculatedTotal: Double
        get() = (priceAgreedValue ?: 0.0) + serviceFeeValue

    /** iOS' `isFormValid` — must have a numeric agreed price > 0. */
    val isValid: Boolean get() = (priceAgreedValue ?: 0.0) > 0.0
}

/** Submission state. Distinct from the form so the VM can show in-flight UI without losing inputs. */
sealed interface DirectBookingSubmissionState {
    data object Idle : DirectBookingSubmissionState
    data object Submitting : DirectBookingSubmissionState
    data class Success(val booking: DirectBookingData) : DirectBookingSubmissionState
    data class Error(val message: String) : DirectBookingSubmissionState
}

/**
 * Drives `DirectBookingSheet`. Owns form state, validation, and submission
 * via `BookingsRepository.bookTripDirect`. iOS parity:
 * `BrowseTripsViewModel.bookTripDirectly` + the form's `@State` block.
 *
 * The sheet host observes [submissionState] and dismisses on
 * [DirectBookingSubmissionState.Success].
 */
@HiltViewModel
class DirectBookingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingsRepository: BookingsRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(DirectBookingFormState())
    val form: StateFlow<DirectBookingFormState> = _form.asStateFlow()

    private val _submissionState =
        MutableStateFlow<DirectBookingSubmissionState>(DirectBookingSubmissionState.Idle)
    val submissionState: StateFlow<DirectBookingSubmissionState> = _submissionState.asStateFlow()

    fun updateSpaceNeeded(value: String) = _form.update { it.copy(spaceNeeded = value) }
    fun updateWeightNeeded(value: String) = _form.update { it.copy(weightNeeded = value) }
    fun updatePickupLocation(value: String) = _form.update { it.copy(pickupLocation = value) }
    fun updateDeliveryLocation(value: String) = _form.update { it.copy(deliveryLocation = value) }
    fun updatePriceAgreed(value: String) = _form.update { it.copy(priceAgreed = value) }
    fun updateServiceFee(value: String) = _form.update { it.copy(serviceFee = value) }
    fun updateSpecialRequirements(value: String) = _form.update { it.copy(specialRequirements = value) }

    /**
     * Submit the form for the given [tripId]. No-ops while another submission is in flight or
     * the form is invalid (matches iOS' disabled-button gating).
     */
    fun submit(tripId: Int) {
        val state = _form.value
        if (!state.isValid) return
        if (_submissionState.value is DirectBookingSubmissionState.Submitting) return

        _submissionState.value = DirectBookingSubmissionState.Submitting
        viewModelScope.launch {
            bookingsRepository.bookTripDirect(
                tripId = tripId,
                payload = DirectBookingPayload(
                    spaceNeededLiters = state.spaceNeeded.toDoubleOrNull() ?: 0.0,
                    weightNeededKg = state.weightNeeded.toDoubleOrNull() ?: 0.0,
                    pickupLocation = state.pickupLocation,
                    deliveryLocation = state.deliveryLocation,
                    priceAgreed = state.calculatedTotal,
                    specialRequirements = state.specialRequirements.ifBlank { null },
                ),
            ).fold(
                onSuccess = { booking ->
                    _submissionState.value = DirectBookingSubmissionState.Success(booking)
                },
                onFailure = { e ->
                    _submissionState.value = DirectBookingSubmissionState.Error(
                        e.message ?: context.getString(R.string.bookings_direct_error_generic),
                    )
                },
            )
        }
    }

    /** Reset error state so the user can edit and retry without seeing a stale alert. */
    fun clearError() {
        if (_submissionState.value is DirectBookingSubmissionState.Error) {
            _submissionState.value = DirectBookingSubmissionState.Idle
        }
    }
}
