package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.session.AuthRepository
import com.efthemiosprime.pasabayan.features.bookings.model.CarrierOnboardingPrompt
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.NegotiationMetadata
import com.efthemiosprime.pasabayan.features.bookings.model.OverageConfirmationData
import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    /**
     * Authenticated user id from [AuthRepository.currentUser]. `null` before
     * sign-in lands or after sign-out. Drives role-aware copy in
     * `CounterOfferBanner` / `CounterOfferSnackbar` ("You saved $X" /
     * "You'll earn $X more") and lets the match-details sheet render "You"
     * when the viewer is the counter-offerer.
     */
    val currentUserId: Long? = null,
    /**
     * Non-null when an accept is awaiting user confirmation because the
     * package weight exceeds the carrier's stated capacity. Populated by
     * the pre-flight check on [MatchingViewModel.acceptMatch] or by the
     * 422 fallback when the server returns
     * [DomainError.CapacityAcknowledgmentRequired]. The UI binds the
     * "Accept Anyway?" sheet to this and calls
     * [MatchingViewModel.confirmOverageAcceptance] / [MatchingViewModel.dismissOverageConfirmation].
     */
    val pendingOverageConfirmation: OverageConfirmationData? = null,
    /**
     * Non-null when the server has clamped the carrier's trip to zero
     * remaining capacity (HTTP 409 "trip overcommitted"). UI surfaces a
     * distinct "trip is full" state rather than a generic conflict error.
     */
    val tripOvercommitted: String? = null,
    /**
     * Non-null when the carrier-accept call returned 422
     * `carrier_onboarding_required`. iOS parity: `CarrierOnboardingPrompt`
     * — the UI presents [CarrierOnboardingRequiredSheet] and replays
     * [CarrierOnboardingPrompt.action] once Stripe onboarding finishes.
     */
    val pendingCarrierOnboarding: CarrierOnboardingPrompt? = null,
    /**
     * Match ids for which an accept or decline call is in flight. UI binds
     * the per-row Accept/Decline buttons to `matchId !in pendingActionMatchIds`
     * so a double-tap can't fire two parallel `PUT /matches/{id}/accept(-shipper-request)`
     * (or decline) calls — the server would 409 the second one, and worse,
     * the optimistic state update could race itself. Entries are added on
     * call start and removed on success/failure.
     */
    val pendingActionMatchIds: Set<Int> = emptySet(),
) {
    val filteredMatches: List<DeliveryMatch>
        get() = when (statusFilter) {
            null -> matches
            else -> matches.filter { it.matchStatus == statusFilter }
        }

    /** Returns true when an accept/decline call is in-flight for [matchId]. */
    fun isActionInFlight(matchId: Int): Boolean = matchId in pendingActionMatchIds
}

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
    private val requirePhoneVerification: RequirePhoneVerificationUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    init {
        // Mirror the authenticated user id into our state so downstream
        // composables (banner / snackbar) don't have to plumb AuthRepository
        // themselves. Updates automatically on login + logout.
        authRepository.currentUser()
            .onEach { user ->
                _uiState.update { it.copy(currentUserId = user?.id) }
            }
            .launchIn(viewModelScope)
    }

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

    /**
     * Accept a match. Runs a local pre-flight check first: if the package
     * weight exceeds the carrier's stated available capacity, surfaces
     * [MatchingUiState.pendingOverageConfirmation] instead of calling the API.
     * UI is expected to show an "Accept Anyway?" sheet bound to that state.
     *
     * The same sheet is re-surfaced from the failure path when the server
     * returns [DomainError.CapacityAcknowledgmentRequired] — defense in
     * depth for stale local capacity values.
     */
    fun acceptMatch(matchId: Int, isCarrier: Boolean) {
        // Reentrancy guard: ignore the second tap while an accept/decline is
        // still in flight for this match. Without this, a double-tap fires
        // two parallel network calls; the second usually 409s and races the
        // optimistic UI update.
        if (_uiState.value.isActionInFlight(matchId)) return
        val match = _uiState.value.matches.firstOrNull { it.id == matchId }
        val pending = match?.let { buildLocalOverageConfirmation(it, isCarrier) }
        if (pending != null) {
            _uiState.update { it.copy(pendingOverageConfirmation = pending) }
            return
        }
        viewModelScope.launch {
            performAccept(matchId, isCarrier, acknowledgeOverage = null)
        }
    }

    /** Confirm "Accept Anyway" — retries the accept call with `acknowledge_overage = true`. */
    fun confirmOverageAcceptance() {
        val pending = _uiState.value.pendingOverageConfirmation ?: return
        if (_uiState.value.isActionInFlight(pending.matchId)) return
        _uiState.update { it.copy(pendingOverageConfirmation = null) }
        viewModelScope.launch {
            performAccept(pending.matchId, pending.isCarrierAccepting, acknowledgeOverage = true)
        }
    }

    /** Dismiss the overage sheet without accepting. */
    fun dismissOverageConfirmation() {
        _uiState.update { it.copy(pendingOverageConfirmation = null) }
    }

    /** Clear the one-shot "trip is full" state after the UI surfaces it. */
    fun clearTripOvercommitted() {
        _uiState.update { it.copy(tripOvercommitted = null) }
    }

    private suspend fun performAccept(matchId: Int, isCarrier: Boolean, acknowledgeOverage: Boolean?) {
        markActionInFlight(matchId)
        try {
            val result = if (isCarrier) {
                bookingsRepository.carrierAcceptShipperRequest(matchId, acknowledgeOverage)
            } else {
                bookingsRepository.shipperAcceptCarrierRequest(matchId, acknowledgeOverage)
            }
            result.fold(
                onSuccess = { updatedMatch ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) updatedMatch else it })
                    }
                },
                onFailure = { e -> handleAcceptFailure(matchId, isCarrier, e) },
            )
        } finally {
            clearActionInFlight(matchId)
        }
    }

    private fun markActionInFlight(matchId: Int) {
        _uiState.update { it.copy(pendingActionMatchIds = it.pendingActionMatchIds + matchId) }
    }

    private fun clearActionInFlight(matchId: Int) {
        _uiState.update { it.copy(pendingActionMatchIds = it.pendingActionMatchIds - matchId) }
    }

    private fun handleAcceptFailure(matchId: Int, isCarrier: Boolean, error: Throwable) {
        when (val domain = (error as? DomainErrorMapperException)?.domainError) {
            is DomainError.CapacityAcknowledgmentRequired -> {
                val pending = buildServerOverageConfirmation(matchId, isCarrier, domain)
                if (pending != null) {
                    _uiState.update { it.copy(pendingOverageConfirmation = pending) }
                } else {
                    _uiState.update { it.copy(errorMessage = error.message ?: "Failed to accept") }
                }
            }
            is DomainError.TripOvercommitted -> {
                _uiState.update { it.copy(tripOvercommitted = domain.message ?: "Trip is full") }
            }
            is DomainError.CarrierOnboardingRequired -> {
                if (isCarrier) {
                    _uiState.update {
                        it.copy(
                            pendingCarrierOnboarding = CarrierOnboardingPrompt(
                                message = domain.message.orEmpty(),
                                action = CarrierOnboardingPrompt.Action.AcceptShipperRequest(matchId = matchId),
                            ),
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = domain.message ?: "Failed to accept") }
                }
            }
            else -> {
                _uiState.update { it.copy(errorMessage = error.message ?: "Failed to accept") }
            }
        }
    }

    /**
     * Replay the action that triggered a `carrier_onboarding_required` response
     * once the carrier has finished Stripe onboarding. Mirrors iOS' retry path
     * driven by [CarrierOnboardingPrompt.action].
     */
    fun retryAfterCarrierOnboarding(prompt: CarrierOnboardingPrompt) {
        _uiState.update { it.copy(pendingCarrierOnboarding = null) }
        viewModelScope.launch {
            when (val action = prompt.action) {
                is CarrierOnboardingPrompt.Action.AcceptShipperRequest ->
                    performAccept(
                        matchId = action.matchId,
                        isCarrier = true,
                        acknowledgeOverage = action.acknowledgeOverage,
                    )
                is CarrierOnboardingPrompt.Action.ConfirmMatch -> {
                    // The shipper-side `confirmMatch` path is owned by
                    // AutoChargeConfirmationViewModel, not this VM. Drop the
                    // prompt; the host should re-open the confirmation sheet.
                }
            }
        }
    }

    /** Dismiss the onboarding prompt without retrying (user tapped "Not now"). */
    fun dismissCarrierOnboarding() {
        _uiState.update { it.copy(pendingCarrierOnboarding = null) }
    }

    private fun buildLocalOverageConfirmation(match: DeliveryMatch, isCarrier: Boolean): OverageConfirmationData? {
        val pkg = match.packageRequest?.weightKg ?: return null
        val avail = match.carrierTrip?.availableWeightKg ?: return null
        if (pkg <= avail) return null
        return OverageConfirmationData(
            matchId = match.id,
            isCarrierAccepting = isCarrier,
            packageWeightKg = pkg,
            availableWeightKg = avail,
            overageKg = pkg - avail,
        )
    }

    private fun buildServerOverageConfirmation(
        matchId: Int,
        isCarrier: Boolean,
        error: DomainError.CapacityAcknowledgmentRequired,
    ): OverageConfirmationData? {
        val match = _uiState.value.matches.firstOrNull { it.id == matchId }
        val pkg = error.packageWeightKg ?: match?.packageRequest?.weightKg ?: return null
        val avail = error.tripAvailableWeightKg ?: match?.carrierTrip?.availableWeightKg ?: return null
        val over = error.overageKg ?: (pkg - avail)
        return OverageConfirmationData(
            matchId = matchId,
            isCarrierAccepting = isCarrier,
            packageWeightKg = pkg,
            availableWeightKg = avail,
            overageKg = over,
        )
    }

    fun declineMatch(matchId: Int, isCarrier: Boolean, reason: String? = null) {
        // Reentrancy guard — see acceptMatch for rationale.
        if (_uiState.value.isActionInFlight(matchId)) return
        viewModelScope.launch {
            markActionInFlight(matchId)
            val result = try {
                if (isCarrier) {
                    bookingsRepository.carrierDeclineShipperRequest(matchId, reason)
                } else {
                    bookingsRepository.shipperDeclineCarrierRequest(matchId, reason)
                }
            } finally {
                clearActionInFlight(matchId)
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
                onSuccess = { result ->
                    _uiState.update { state ->
                        state.copy(matches = state.matches.map { if (it.id == matchId) result.match else it })
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
