package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardActionFooter
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoSection
import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.bookings.model.BookingAction
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch

/**
 * Unified match card — replaces iOS's 4 separate card components.
 * Expands inline in the list to keep item order stable while viewing details.
 */
@Composable
fun MatchCard(
    match: DeliveryMatch,
    isCarrier: Boolean,
    currentUserId: Int,
    onAction: (BookingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = matchStatusLabel(match.matchStatus)
    var expanded by rememberSaveable { mutableStateOf(false) }
    val actions = match.availableActions(isCarrier, currentUserId)

    PCard(modifier = modifier) {
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            // Header: other party info + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val otherParty = if (isCarrier) match.shipper else match.carrier
                otherParty?.let { user ->
                    PUserInfoSection(
                        name = user.name,
                        rating = user.formattedRating.takeIf { it != "No rating" },
                        verificationLevel = user.verificationLevel,
                        modifier = Modifier.weight(1f),
                    )
                }
                PStatusBadge(config = MatchStatusBadgeConfig(match.matchStatus, statusLabel))
            }

            // Price
            PDetailRow(
                label = stringResource(R.string.bookings_detail_agreed_price),
                value = String.format("$%.2f", match.agreedPrice),
            )

            // Counter-offer indicator
            if (match.isCounterOffer) {
                Text(
                    text = stringResource(R.string.bookings_detail_counter_offer),
                    style = PasabayanTextStyles.Caption.large,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                    // Price details
                    match.originalPriceValue?.let { original ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_detail_original_price),
                            value = String.format("$%.2f", original),
                        )
                    }

                    // Counter-offer details
                    if (match.isCounterOffer) {
                        match.counterOfferRound?.let { round ->
                            PDetailRow(
                                label = stringResource(R.string.bookings_detail_counter_offer),
                                value = stringResource(R.string.bookings_detail_round, round),
                            )
                        }
                        PDetailRow(
                            label = "",
                            value = stringResource(
                                R.string.bookings_detail_remaining_offers,
                                match.remainingCounterOffers,
                            ),
                        )
                    }

                    // Messages
                    match.carrierMessage?.let { msg ->
                        Text(
                            text = "${stringResource(R.string.bookings_detail_carrier)}: $msg",
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    match.shipperMessage?.let { msg ->
                        Text(
                            text = "${stringResource(R.string.bookings_detail_shipper)}: $msg",
                            style = PasabayanTextStyles.Body.small,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Action buttons
                    if (actions.isNotEmpty()) {
                        MatchActionButtons(actions = actions, onAction = onAction)
                    }
                }
            }

            // Footer
            PCardActionFooter(onViewDetails = { expanded = !expanded })
        }
    }
}

@Composable
private fun MatchActionButtons(
    actions: List<BookingAction>,
    onAction: (BookingAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        actions.forEach { action ->
            val (text, style) = when (action) {
                BookingAction.AcceptBooking -> Pair(R.string.bookings_action_accept, PButtonStyle.Primary)
                BookingAction.DeclineBooking -> Pair(R.string.bookings_action_decline, PButtonStyle.Destructive)
                BookingAction.CounterOffer -> Pair(R.string.bookings_action_counter_offer, PButtonStyle.Secondary)
                BookingAction.CancelBooking -> Pair(R.string.bookings_action_cancel, PButtonStyle.Destructive)
                BookingAction.MarkPickedUp -> Pair(R.string.bookings_action_mark_picked_up, PButtonStyle.Primary)
                BookingAction.MarkInTransit -> Pair(R.string.bookings_action_mark_in_transit, PButtonStyle.Primary)
                BookingAction.MarkDelivered -> Pair(R.string.bookings_action_mark_delivered, PButtonStyle.Primary)
                BookingAction.TrackLive -> Pair(R.string.bookings_action_track_live, PButtonStyle.Secondary)
                BookingAction.EnterPickupCode -> Pair(R.string.bookings_action_generate_code, PButtonStyle.Secondary)
                BookingAction.EnterDeliveryCode -> Pair(R.string.bookings_action_enter_code, PButtonStyle.Secondary)
            }
            PButton(
                text = stringResource(text),
                onClick = { onAction(action) },
                style = style,
                size = PButtonSize.Small,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun matchStatusLabel(status: MatchStatus): String = when (status) {
    MatchStatus.PENDING -> stringResource(R.string.bookings_status_pending)
    MatchStatus.CONFIRMED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.PICKED_UP -> stringResource(R.string.bookings_status_picked_up)
    MatchStatus.IN_TRANSIT -> stringResource(R.string.bookings_status_in_transit)
    MatchStatus.DELIVERED -> stringResource(R.string.bookings_status_delivered)
    MatchStatus.CANCELLED -> stringResource(R.string.bookings_status_cancelled)
    MatchStatus.CARRIER_REQUESTED -> stringResource(R.string.bookings_status_carrier_requested)
    MatchStatus.SHIPPER_REQUESTED -> stringResource(R.string.bookings_status_shipper_requested)
    MatchStatus.SHIPPER_ACCEPTED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.CARRIER_ACCEPTED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.SHIPPER_DECLINED -> stringResource(R.string.bookings_status_cancelled)
    MatchStatus.CARRIER_DECLINED -> stringResource(R.string.bookings_status_cancelled)
}

@Preview(showBackground = true, name = "MatchCard — light")
@Preview(showBackground = true, name = "MatchCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MatchCardPreview() {
    PasabayanTheme {
        MatchCard(
            match = DeliveryMatch(
                id = 100, tripId = 1, packageRequestId = 10,
                matchStatus = MatchStatus.CARRIER_REQUESTED, agreedPrice = 150.0,
                initiatedBy = InitiatedBy.CARRIER,
                isCounterOffer = false, originalPrice = null,
                canCounterOffer = true, remainingCounterOffers = 3, counterOfferRound = 0,
                carrierMessage = "I can carry this safely", shipperMessage = null,
                carrier = com.efthemiosprime.pasabayan.core.domain.model.UserSummary(id = 42, name = "John Carrier", rating = "4.8", verificationLevel = "verified"),
                shipper = com.efthemiosprime.pasabayan.core.domain.model.UserSummary(id = 5, name = "Alice Shipper", rating = "4.5"),
                chatConversationId = 77,
                confirmedAt = null, pickedUpAt = null, deliveredAt = null,
                createdAt = "2026-03-28T08:00:00Z", updatedAt = null,
                platformFeePercent = 10, transactionStatus = null,
                receiptPhoto = null, autoCancelAfterDays = 10,
                pickupConfirmationCode = null, codeExpiresAt = null,
                deliveryVerificationCode = null, deliveryCodeExpiresAt = null,
            ),
            isCarrier = false,
            currentUserId = 5,
            onAction = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
