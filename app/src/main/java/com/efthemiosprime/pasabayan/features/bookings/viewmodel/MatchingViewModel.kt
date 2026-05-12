package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.NegotiationMetadata
import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchingUiState(
    val matches: List<DeliveryMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusFilter: MatchStatus? = null,
    val lastCancelRefund: RefundResult? = null,
    val lastCancelConversationId: Int? = null,
    val lastNegotiation: NegotiationMetadata? = null,
    val isSubmittingCounterOffer: Boolean = false,
    /** One-shot: action blocked because the user's phone is not verified. */
    val requiresPhoneVerification: VerifyPhoneReason? = null,
    /**
     * Match ids the user has dismissed from the `IncomingRequestSnackbar` review section
     * (session-only — survives recomposition, resets on process restart). Read by
     * `IncomingRequestContext.incomingRequestSnackbarItems(...)` and
     * `IncomingRequestContext.unseenIncomingRequestBadgeCount(...)`.
     */
    val reviewedIncomingRequestIds: Set<Int> = emptySet(),
) {
    val filteredMatches: List<DeliveryMatch>
        get() = when (statusFilter) {
            null -> matches
            else -> matches.filter { it.matchStatus == statusFilter }
        }
}

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
    private val requirePhoneVerification: RequirePhoneVerificationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadMatches(role: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingsRepository.loadMatches(role = role).fold(
                onSuccess = { matches ->
                    _uiState.update { it.copy(matches = matches, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load matches")
                    }
                },
            )
        }
    }

    fun refreshMatches(role: String? = null) = loadMatches(role)

    fun filterByStatus(status: MatchStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    /**
     * Adds [matchId] to [MatchingUiState.reviewedIncomingRequestIds] so the
     * `IncomingRequestSnackbar` for this match disappears from the review section.
     * Idempotent (a `Set` swallows duplicates).
     */
    fun markIncomingRequestReviewed(matchId: Int) {
        _uiState.update { state ->
            state.copy(reviewedIncomingRequestIds = state.reviewedIncomingRequestIds + matchId)
        }
    }

    fun acceptMatch(matchId: Int, isCarrier: Boolean) {
        viewModelScope.launch {
            val result = if (isCarrier) {
                bookingsRepository.carrierAcceptShipperRequest(matchId)
            } else {
                bookingsRepository.shipperAccept(matchId)
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to accept") }
                },
            )
        }
    }

    fun declineMatch(matchId: Int, isCarrier: Boolean) {
        viewModelScope.launch {
            val result = if (isCarrier) {
                bookingsRepository.carrierDeclineShipperRequest(matchId)
            } else {
                bookingsRepository.shipperDecline(matchId)
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to decline") }
                },
            )
        }
    }

    fun confirmMatch(matchId: Int) {
        viewModelScope.launch {
            bookingsRepository.confirmMatch(matchId).fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to confirm") }
                },
            )
        }
    }

    fun cancelMatch(matchId: Int) {
        viewModelScope.launch {
            bookingsRepository.cancelMatch(matchId).fold(
                onSuccess = { result ->
                    _uiState.update { state ->
                        state.copy(
                            matches = state.matches.map { if (it.id == matchId) result.match else it },
                            lastCancelRefund = result.refund,
                            lastCancelConversationId = result.chatConversationId,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to cancel") }
                },
            )
        }
    }

    fun clearCancelArtifacts() {
        _uiState.update { it.copy(lastCancelRefund = null, lastCancelConversationId = null) }
    }

    /**
     * Submit a counter-offer against an existing match. Posts to the same
     * endpoint as the initial request with `is_counter_offer = true` and
     * `original_match_id` / `original_price` populated from the match in
     * state. Mirrors iOS `submitShipperCounterOffer` / `submitCarrierCounterOffer`
     * (commit 8c9646d).
     */
    fun submitCounterOffer(matchId: Int, proposedPrice: Double, message: String?, isShipper: Boolean) {
        if (requirePhoneVerification().isFailure) {
            val reason = if (isShipper) VerifyPhoneReason.BookTrip else VerifyPhoneReason.RequestToCarry
            _uiState.update { it.copy(requiresPhoneVerification = reason) }
            return
        }
        viewModelScope.launch {
            val original = _uiState.value.matches.firstOrNull { it.id == matchId }
            if (original == null) {
                _uiState.update { it.copy(errorMessage = "Match not found") }
                return@launch
            }
            val packageId = original.packageRequestId
            val tripId = original.tripId
            if (packageId == null || tripId == null) {
                _uiState.update { it.copy(errorMessage = "Match missing trip or package reference") }
                return@launch
            }
            _uiState.update { it.copy(isSubmittingCounterOffer = true, errorMessage = null) }
            val result = if (isShipper) {
                bookingsRepository.shipperRequestTrip(
                    packageId = packageId,
                    tripId = tripId,
                    offeredPrice = proposedPrice,
                    message = message,
                    isCounterOffer = true,
                    originalMatchId = matchId,
                    originalPrice = original.agreedPrice,
                )
            } else {
                bookingsRepository.carrierRequestPackage(
                    tripId = tripId,
                    packageId = packageId,
                    proposedPrice = proposedPrice,
                    message = message,
                    isCounterOffer = true,
                    originalMatchId = matchId,
                    originalPrice = original.agreedPrice,
                )
            }
            result.fold(
                onSuccess = { requestResult ->
                    _uiState.update { state ->
                        state.copy(
                            matches = state.matches.map {
                                if (it.id == matchId) requestResult.match else it
                            },
                            lastNegotiation = requestResult.negotiation,
                            isSubmittingCounterOffer = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingCounterOffer = false,
                            errorMessage = e.message ?: "Failed to submit counter-offer",
                        )
                    }
                },
            )
        }
    }

    fun clearNegotiationArtifacts() {
        _uiState.update { it.copy(lastNegotiation = null) }
    }

    /**
     * First-pass carrier offer on a package (not a counter-offer). Gated on phone
     * verification — matches iOS `RequestToCarrySheet` submission flow.
     */
    fun requestPackageAsCarrier(
        tripId: Int,
        packageId: Int,
        proposedPrice: Double,
        message: String?,
    ) {
        if (requirePhoneVerification().isFailure) {
            _uiState.update { it.copy(requiresPhoneVerification = VerifyPhoneReason.RequestToCarry) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCounterOffer = true, errorMessage = null) }
            bookingsRepository.carrierRequestPackage(
                tripId = tripId,
                packageId = packageId,
                proposedPrice = proposedPrice,
                message = message,
                isCounterOffer = false,
                originalMatchId = null,
                originalPrice = null,
            ).fold(
                onSuccess = { requestResult ->
                    _uiState.update {
                        it.copy(
                            isSubmittingCounterOffer = false,
                            lastNegotiation = requestResult.negotiation,
                            matches = it.matches + requestResult.match,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingCounterOffer = false,
                            errorMessage = e.message ?: "Failed to send carrier offer",
                        )
                    }
                },
            )
        }
    }

    fun consumeRequiresPhoneVerification() {
        _uiState.update { it.copy(requiresPhoneVerification = null) }
    }

    fun updateMatchStatus(matchId: Int, newStatus: MatchStatus) {
        viewModelScope.launch {
            val result = when (newStatus) {
                MatchStatus.PICKED_UP -> bookingsRepository.markPickedUp(matchId)
                MatchStatus.IN_TRANSIT -> bookingsRepository.markInTransit(matchId)
                MatchStatus.DELIVERED -> bookingsRepository.markDelivered(matchId)
                else -> return@launch
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update status") }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
