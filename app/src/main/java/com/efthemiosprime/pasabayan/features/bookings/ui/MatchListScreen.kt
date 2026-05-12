package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.components.IncomingRequestSnackbar
import com.efthemiosprime.pasabayan.features.bookings.components.MatchCard
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.BookingAction
import com.efthemiosprime.pasabayan.features.bookings.model.IncomingRequestContext
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.MatchingViewModel
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason

/**
 * Unified match list screen — single screen for both carrier and shipper.
 * Parity with iOS BookingListView + ShipperMatchesView combined.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MatchListScreen(
    isCarrier: Boolean,
    onAction: (BookingAction, matchId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchingViewModel = hiltViewModel(),
    /**
     * When set (e.g. from a push-tap or in-app notification card routing through the dashboard),
     * the matching match is selected as soon as the list has loaded and the details sheet opens
     * directly. iOS parity: `NotificationCenter.navigateFromNotification` → `OpenMatch(matchId)`.
     */
    initialMatchId: Int? = null,
    onInitialMatchConsumed: () -> Unit = {},
    /**
     * When set, the counter-offer composer for the matching match opens directly (skips the
     * details sheet). iOS parity: `OpenCounterOffer(matchId)` from `NotificationRouter`.
     */
    initialCounterOfferMatchId: Int? = null,
    onInitialCounterOfferConsumed: () -> Unit = {},
    /** Forwarded when a gated action (counter-offer / carrier request) was blocked. */
    onPhoneVerificationRequired: (VerifyPhoneReason) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val role = if (isCarrier) "carrier" else "shipper"

    LaunchedEffect(state.requiresPhoneVerification) {
        state.requiresPhoneVerification?.let { reason ->
            onPhoneVerificationRequired(reason)
            viewModel.consumeRequiresPhoneVerification()
        }
    }
    var selectedMatch by remember { mutableStateOf<DeliveryMatch?>(null) }
    // Distinct surface from [selectedMatch]: the composer is shown on its own so it doesn't
    // require stacking the details sheet underneath.
    var counterOfferTarget by remember { mutableStateOf<DeliveryMatch?>(null) }

    LaunchedEffect(role) { viewModel.loadMatches(role) }

    // Key on the match list as well so a route fired before matches finish loading still resolves.
    LaunchedEffect(initialMatchId, state.matches) {
        val target = initialMatchId ?: return@LaunchedEffect
        val match = state.matches.firstOrNull { it.id == target } ?: return@LaunchedEffect
        selectedMatch = match
        onInitialMatchConsumed()
    }

    LaunchedEffect(initialCounterOfferMatchId, state.matches) {
        val target = initialCounterOfferMatchId ?: return@LaunchedEffect
        val match = state.matches.firstOrNull { it.id == target } ?: return@LaunchedEffect
        counterOfferTarget = match
        onInitialCounterOfferConsumed()
    }

    // Snackbar review section (spec § 1278: max 3 IncomingRequestSnackbar items above the
    // match list; "+N more" pill when there are more pending than rendered). Independent
    // of the status filter — the snackbar surfaces real pending incoming requests
    // regardless of which status bucket the user is browsing.
    val snackbarItems = IncomingRequestContext.incomingRequestSnackbarItems(
        matches = state.matches,
        isCarrier = isCarrier,
        reviewedIds = state.reviewedIncomingRequestIds,
    )
    val totalUnseenIncoming = IncomingRequestContext.unseenIncomingRequestBadgeCount(
        matches = state.matches,
        isCarrier = isCarrier,
        reviewedIds = state.reviewedIncomingRequestIds,
    )
    val extraPending = (totalUnseenIncoming - snackbarItems.size).coerceAtLeast(0)

    Column(modifier = modifier.fillMaxSize()) {
        // Status filter chips
        MatchStatusFilterRow(
            selectedFilter = state.statusFilter,
            onFilterSelected = { viewModel.filterByStatus(it) },
            modifier = Modifier.padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.sm,
            ),
        )

        // Content
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
            state.filteredMatches.isEmpty() && snackbarItems.isEmpty() -> {
                PEmptyState(
                    icon = Icons.Outlined.SwapHoriz,
                    title = stringResource(R.string.bookings_empty_no_matches),
                    description = stringResource(R.string.bookings_empty_no_matches_description),
                    modifier = Modifier.padding(PasabayanSpacing.lg),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    contentPadding = PaddingValues(
                        horizontal = PasabayanSpacing.screenPadding,
                        vertical = PasabayanSpacing.sm,
                    ),
                ) {
                    // Review section — snackbar items + "+N more" pill.
                    items(snackbarItems, key = { "incoming-${it.matchId}" }) { incoming ->
                        IncomingRequestSnackbar(
                            item = incoming,
                            isCarrier = isCarrier,
                            onOpen = {
                                state.matches.firstOrNull { it.id == incoming.matchId }
                                    ?.let { selectedMatch = it }
                            },
                            onDismiss = {
                                viewModel.markIncomingRequestReviewed(incoming.matchId)
                            },
                        )
                    }
                    if (extraPending > 0) {
                        item(key = "incoming-more") {
                            androidx.compose.material3.Text(
                                text = stringResource(
                                    R.string.bookings_incoming_snackbar_more,
                                    extraPending,
                                ),
                                style = com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles.Caption.regular,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = PasabayanSpacing.xs, bottom = PasabayanSpacing.sm),
                            )
                        }
                    }

                    items(state.filteredMatches, key = { it.id }) { match ->
                        MatchCard(
                            match = match,
                            isCarrier = isCarrier,
                            onViewDetails = { selectedMatch = match },
                            onAction = { action ->
                                onAction(action, match.id)
                                when (action) {
                                    BookingAction.CounterOffer -> {
                                        counterOfferTarget = match
                                    }
                                    BookingAction.TrackLive,
                                    BookingAction.EnterPickupCode,
                                    BookingAction.EnterDeliveryCode -> {
                                        selectedMatch = match
                                    }
                                    else -> {
                                        handleMatchAction(
                                            action = action,
                                            isCarrier = isCarrier,
                                            matchId = match.id,
                                            viewModel = viewModel,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    selectedMatch?.let { match ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { selectedMatch = null },
        ) {
            ShipperMatchDetailsSheetContent(
                match = match,
                isCarrier = isCarrier,
                onClose = { selectedMatch = null },
                onAction = { action ->
                    onAction(action, match.id)
                    when (action) {
                        BookingAction.CounterOffer -> {
                            // Hand off to the dedicated composer instead of leaving the user
                            // staring at the details sheet with no follow-up affordance.
                            selectedMatch = null
                            counterOfferTarget = match
                        }
                        else -> {
                            handleMatchAction(
                                action = action,
                                isCarrier = isCarrier,
                                matchId = match.id,
                                viewModel = viewModel,
                            )
                            if (action == BookingAction.DeclineBooking ||
                                action == BookingAction.CancelBooking ||
                                action == BookingAction.MarkDelivered
                            ) {
                                selectedMatch = null
                            }
                        }
                    }
                },
            )
        }
    }

    counterOfferTarget?.let { match ->
        CounterOfferPromptSheet(
            currentPrice = match.agreedPrice,
            remainingOffers = match.remainingCounterOffers,
            onSubmit = { newPrice, message ->
                viewModel.submitCounterOffer(
                    matchId = match.id,
                    proposedPrice = newPrice,
                    message = message,
                    isShipper = !isCarrier,
                )
                counterOfferTarget = null
            },
            onDismiss = { counterOfferTarget = null },
        )
    }
}

private fun handleMatchAction(
    action: BookingAction,
    isCarrier: Boolean,
    matchId: Int,
    viewModel: MatchingViewModel,
) {
    when (action) {
        BookingAction.AcceptBooking -> viewModel.acceptMatch(matchId = matchId, isCarrier = isCarrier)
        BookingAction.DeclineBooking -> viewModel.declineMatch(matchId = matchId, isCarrier = isCarrier)
        BookingAction.MarkPickedUp -> viewModel.updateMatchStatus(matchId = matchId, newStatus = MatchStatus.PICKED_UP)
        BookingAction.MarkInTransit -> viewModel.updateMatchStatus(matchId = matchId, newStatus = MatchStatus.IN_TRANSIT)
        BookingAction.MarkDelivered -> viewModel.updateMatchStatus(matchId = matchId, newStatus = MatchStatus.DELIVERED)
        BookingAction.CancelBooking -> viewModel.cancelMatch(matchId = matchId)
        BookingAction.CounterOffer,
        BookingAction.TrackLive,
        BookingAction.EnterPickupCode,
        BookingAction.EnterDeliveryCode -> Unit
    }
}

@Composable
private fun MatchStatusFilterRow(
    selectedFilter: MatchStatus?,
    onFilterSelected: (MatchStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PFilterChip(
            label = stringResource(R.string.trips_filter_all),
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
        )
        PFilterChip(
            label = stringResource(R.string.bookings_status_pending),
            selected = selectedFilter == MatchStatus.PENDING,
            onClick = { onFilterSelected(MatchStatus.PENDING) },
        )
        PFilterChip(
            label = stringResource(R.string.bookings_status_confirmed),
            selected = selectedFilter == MatchStatus.CONFIRMED,
            onClick = { onFilterSelected(MatchStatus.CONFIRMED) },
        )
        PFilterChip(
            label = stringResource(R.string.bookings_status_in_transit),
            selected = selectedFilter == MatchStatus.IN_TRANSIT,
            onClick = { onFilterSelected(MatchStatus.IN_TRANSIT) },
        )
        PFilterChip(
            label = stringResource(R.string.bookings_status_delivered),
            selected = selectedFilter == MatchStatus.DELIVERED,
            onClick = { onFilterSelected(MatchStatus.DELIVERED) },
        )
    }
}

@Preview(showBackground = true, name = "MatchList — light", heightDp = 700)
@Preview(showBackground = true, name = "MatchList — dark", heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MatchListPreview() {
    PasabayanTheme {
        PEmptyState(
            icon = Icons.Outlined.SwapHoriz,
            title = "No matches yet",
            description = "Your booking matches will appear here.",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
