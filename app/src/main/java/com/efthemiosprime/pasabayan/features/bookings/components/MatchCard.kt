package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
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
    onViewDetails: () -> Unit,
    onAction: (BookingAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val statusLabel = matchStatusLabel(match.matchStatus)
    val menuActions = rememberMatchMenuActions(
        match = match,
        isCarrier = isCarrier,
        onAction = onAction,
    )
    val directTrailingAction = if (menuActions.size == 1) menuActions.first() else null
    val overflowActions = if (directTrailingAction != null) emptyList() else menuActions
    val routeSummary = match.carrierTrip?.route ?: run {
        val fromCity = match.packageRequest?.pickupCity
        val toCity = match.packageRequest?.deliveryCity
        if (!fromCity.isNullOrBlank() && !toCity.isNullOrBlank()) "$fromCity → $toCity" else null
    }

    PCard(modifier = modifier) {
        Column(
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

            routeSummary?.let { route ->
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.bookings_detail_route),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = route,
                        style = PasabayanTextStyles.Body.medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
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

            // Footer
            PCardActionFooter(
                onViewDetails = onViewDetails,
                menuActions = overflowActions,
                directTrailingAction = directTrailingAction,
            )
        }
    }
}

@Composable
private fun rememberMatchMenuActions(
    match: DeliveryMatch,
    isCarrier: Boolean,
    onAction: (BookingAction) -> Unit,
): List<CardMenuAction> {
    return match.availableActions(isCarrier = isCarrier, currentUserId = -1).map { action ->
        CardMenuAction(
            title = bookingActionLabel(action),
            onClick = { onAction(action) },
        )
    }
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
            onViewDetails = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
