package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoCard
import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.bookings.components.CounterOfferBanner
import com.efthemiosprime.pasabayan.features.bookings.components.MatchStatusBadgeConfig
import com.efthemiosprime.pasabayan.features.bookings.model.BookingAction
import com.efthemiosprime.pasabayan.features.bookings.model.CounterOfferContext
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.PackageRequestInfo
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ShipperMatchDetailsSheetContent(
    match: DeliveryMatch,
    isCarrier: Boolean,
    onClose: () -> Unit,
    onAction: (BookingAction) -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: Int? = null,
) {
    val statusLabel = matchStatusLabel(match.matchStatus)
    val currentStep = timelineCurrentIndex(match.matchStatus)

    PDetailSheetScaffold(
        title = stringResource(R.string.bookings_details_title),
        closeContentDescription = stringResource(R.string.bookings_detail_close),
        onClose = onClose,
        modifier = modifier,
    ) {
        // Counter-offer banner — iOS parity with top-of-screen banner in
        // ShipperMatchDetailsView / CarrierMatchDetailsView. Renders only
        // when the match is a counter-offer with a recoverable original
        // price. Self-dismisses via local state inside the banner.
        CounterOfferContext.fromMatch(match)?.let { ctx ->
            CounterOfferBanner(
                context = ctx,
                currentUserId = currentUserId,
                onDismiss = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.bookings_details_booking_number, match.id),
                        style = PasabayanTextStyles.Body.medium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.bookings_details_created_at,
                            formatIsoDate(match.createdAt),
                        ),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PStatusBadge(config = MatchStatusBadgeConfig(match.matchStatus, statusLabel))
            }
        }

        match.carrierTrip?.let { trip ->
            PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PDetailSectionTitle(text = stringResource(R.string.bookings_section_trip_summary))
                    PDetailRow(
                        label = stringResource(R.string.bookings_label_from_city),
                        value = trip.originCity,
                    )
                    PDetailRow(
                        label = stringResource(R.string.bookings_label_to_city),
                        value = trip.destinationCity,
                    )
                    PDetailRow(
                        label = stringResource(R.string.bookings_label_departing),
                        value = formatIsoDate(trip.departureDate),
                    )
                    trip.availableWeightKg?.let { weight ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_available_space),
                            value = stringResource(R.string.bookings_weight_kg, weight),
                        )
                    }
                }
            }
        }

        match.packageRequest?.let { pkg ->
            PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PDetailSectionTitle(text = stringResource(R.string.bookings_section_package_summary))
                    PDetailRow(
                        label = stringResource(R.string.bookings_label_package_name),
                        value = pkg.title.orEmpty().ifBlank { "-" },
                    )
                    pkg.description?.let { description ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_description),
                            value = description,
                        )
                    }
                    pkg.weightKg?.let { weight ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_weight),
                            value = stringResource(R.string.bookings_weight_kg, weight),
                        )
                    }
                    pkg.packageType?.let { category ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_category),
                            value = category,
                        )
                    }
                    pkg.pickupAddress?.let { address ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_pickup_address),
                            value = address,
                        )
                    }
                    pkg.deliveryAddress?.let { address ->
                        PDetailRow(
                            label = stringResource(R.string.bookings_label_delivery_address),
                            value = address,
                        )
                    }
                }
            }
        }

        // Counterparty info — iOS parity with CarrierDetailsCard (shipper view) and
        // shipperInfoSection (carrier view). The sheet is shared across roles, so we
        // surface whichever counterparty the viewer is dealing with.
        val counterparty = if (isCarrier) match.shipper else match.carrier
        val counterpartyTitle = stringResource(
            if (isCarrier) R.string.bookings_section_shipper_details
            else R.string.bookings_section_carrier_details
        )
        counterparty?.let { user ->
            PUserInfoCard(
                name = user.name,
                title = counterpartyTitle,
                rating = user.rating?.takeIf { it.isNotBlank() },
                totalRatings = user.totalRatings,
                verificationLevel = user.verificationLevel,
                noRatingsLabel = stringResource(R.string.bookings_no_ratings_yet),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PDetailSectionTitle(text = stringResource(R.string.bookings_section_payment_summary))
                PDetailRow(
                    label = stringResource(R.string.bookings_detail_agreed_price),
                    value = String.format("$%.2f", match.agreedPrice),
                    valueColor = PasabayanColors.Success,
                )
                if (match.packageRequest != null) {
                    PDetailRow(
                        label = stringResource(R.string.bookings_pricing_budget_limit),
                        value = "-",
                    )
                }
                match.carrierTrip?.pricePerKg?.let { perKg ->
                    match.packageRequest?.weightKg?.let { weight ->
                        val total = perKg * weight
                        PDetailRow(
                            label = stringResource(R.string.bookings_pricing_rate_breakdown, weight, perKg),
                            value = String.format("$%.2f", total),
                        )
                    }
                }
            }
        }

        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PDetailSectionTitle(text = stringResource(R.string.bookings_section_timeline_progress))
                timelineStep(
                    title = stringResource(R.string.bookings_timeline_created),
                    completed = currentStep > 0,
                    current = currentStep == 0,
                )
                timelineStep(
                    title = stringResource(R.string.bookings_timeline_confirmed),
                    completed = currentStep > 1,
                    current = currentStep == 1,
                )
                timelineStep(
                    title = stringResource(R.string.bookings_timeline_picked_up),
                    completed = currentStep > 2,
                    current = currentStep == 2,
                )
                timelineStep(
                    title = stringResource(R.string.bookings_timeline_on_the_way),
                    completed = currentStep > 3,
                    current = currentStep == 3,
                )
                timelineStep(
                    title = stringResource(R.string.bookings_timeline_delivered),
                    completed = currentStep > 4,
                    current = currentStep == 4,
                )
            }
        }

        val actions = match.availableActions(isCarrier = isCarrier, currentUserId = -1)
        if (actions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                actions.forEach { action ->
                    PButton(
                        text = actionLabel(action),
                        onClick = { onAction(action) },
                        style = actionStyle(action),
                        size = PButtonSize.Small,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
    }
}

@Composable
private fun timelineStep(title: String, completed: Boolean, current: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = when {
                completed -> PasabayanColors.Success
                current -> PasabayanColors.Info
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(PasabayanSpacing.md),
        )
        Text(
            text = title,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (current) {
            Text(
                text = stringResource(R.string.bookings_timeline_current),
                style = PasabayanTextStyles.Caption.regular,
                color = PasabayanColors.Info,
            )
        }
    }
}

private fun timelineCurrentIndex(status: MatchStatus): Int = when (status) {
    MatchStatus.PENDING,
    MatchStatus.CARRIER_REQUESTED,
    MatchStatus.SHIPPER_REQUESTED -> 0
    MatchStatus.CONFIRMED, MatchStatus.CARRIER_ACCEPTED, MatchStatus.SHIPPER_ACCEPTED -> 1
    MatchStatus.PICKED_UP -> 2
    MatchStatus.IN_TRANSIT -> 3
    MatchStatus.DELIVERED -> 4
    MatchStatus.CANCELLED, MatchStatus.CARRIER_DECLINED, MatchStatus.SHIPPER_DECLINED -> 0
}

private fun formatIsoDate(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
    }.getOrElse { value }
}

@Composable
private fun actionLabel(action: BookingAction): String = when (action) {
    BookingAction.AcceptBooking -> stringResource(R.string.bookings_action_accept)
    BookingAction.DeclineBooking -> stringResource(R.string.bookings_action_decline)
    BookingAction.CounterOffer -> stringResource(R.string.bookings_action_counter_offer)
    BookingAction.CancelBooking -> stringResource(R.string.bookings_action_cancel)
    BookingAction.MarkPickedUp -> stringResource(R.string.bookings_action_mark_picked_up)
    BookingAction.MarkInTransit -> stringResource(R.string.bookings_action_mark_in_transit)
    BookingAction.MarkDelivered -> stringResource(R.string.bookings_action_mark_delivered)
    BookingAction.TrackLive -> stringResource(R.string.bookings_action_track_live)
    BookingAction.EnterPickupCode -> stringResource(R.string.bookings_action_generate_code)
    BookingAction.EnterDeliveryCode -> stringResource(R.string.bookings_action_enter_code)
}

private fun actionStyle(action: BookingAction): PButtonStyle = when (action) {
    BookingAction.DeclineBooking, BookingAction.CancelBooking -> PButtonStyle.Destructive
    BookingAction.CounterOffer, BookingAction.TrackLive, BookingAction.EnterPickupCode, BookingAction.EnterDeliveryCode -> PButtonStyle.Secondary
    else -> PButtonStyle.Primary
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
    MatchStatus.SHIPPER_ACCEPTED, MatchStatus.CARRIER_ACCEPTED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.SHIPPER_DECLINED, MatchStatus.CARRIER_DECLINED -> stringResource(R.string.bookings_status_cancelled)
}

@Preview(showBackground = true, name = "ShipperMatchDetailsSheet - light", heightDp = 900)
@Preview(showBackground = true, name = "ShipperMatchDetailsSheet - dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShipperMatchDetailsSheetPreview() {
    PasabayanTheme {
        ShipperMatchDetailsSheetContent(
            match = DeliveryMatch(
                id = 501,
                tripId = 301,
                packageRequestId = 401,
                matchStatus = MatchStatus.IN_TRANSIT,
                agreedPrice = 120.0,
                initiatedBy = InitiatedBy.CARRIER,
                isCounterOffer = true,
                originalPrice = "150.00",
                canCounterOffer = true,
                remainingCounterOffers = 2,
                counterOfferRound = 1,
                counterOffererId = 7,
                counterOffererName = "Sarah Carrier",
                carrierMessage = null,
                shipperMessage = null,
                carrier = UserSummary(id = 7, name = "Sarah Carrier", rating = "4.9", verificationLevel = "verified"),
                shipper = null,
                chatConversationId = null,
                confirmedAt = null,
                pickedUpAt = null,
                deliveredAt = null,
                createdAt = "2026-03-31T12:30:00Z",
                updatedAt = null,
                platformFeePercent = 10,
                transactionStatus = null,
                receiptPhoto = null,
                autoCancelAfterDays = 10,
                pickupConfirmationCode = null,
                codeExpiresAt = null,
                deliveryVerificationCode = null,
                deliveryCodeExpiresAt = null,
                carrierTrip = CarrierTripInfo(
                    id = 301,
                    originCity = "Toronto",
                    destinationCity = "Montreal",
                    departureDate = "2026-04-01T09:00:00Z",
                    availableWeightKg = 8.0,
                    pricePerKg = 15.0,
                ),
                packageRequest = PackageRequestInfo(
                    id = 401,
                    title = "Electronics Package",
                    description = "Laptop and accessories",
                    weightKg = 2.5,
                    pickupAddress = "123 Main St",
                    deliveryAddress = "55 St-Catherine St",
                    packageType = "electronics",
                ),
            ),
            isCarrier = false,
            onClose = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "CarrierMatchDetailsSheet - light", heightDp = 900)
@Preview(
    showBackground = true,
    name = "CarrierMatchDetailsSheet - dark",
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CarrierMatchDetailsSheetPreview() {
    PasabayanTheme {
        ShipperMatchDetailsSheetContent(
            match = DeliveryMatch(
                id = 502,
                tripId = 302,
                packageRequestId = 402,
                matchStatus = MatchStatus.CONFIRMED,
                agreedPrice = 95.0,
                initiatedBy = InitiatedBy.SHIPPER,
                isCounterOffer = false,
                originalPrice = null,
                canCounterOffer = false,
                remainingCounterOffers = 0,
                counterOfferRound = null,
                counterOffererId = null,
                counterOffererName = null,
                carrierMessage = null,
                shipperMessage = null,
                carrier = null,
                shipper = UserSummary(
                    id = 18,
                    name = "Maria Santos",
                    rating = "4.7",
                    totalRatings = 24,
                    verificationLevel = "verified",
                ),
                chatConversationId = null,
                confirmedAt = "2026-03-31T13:00:00Z",
                pickedUpAt = null,
                deliveredAt = null,
                createdAt = "2026-03-31T12:30:00Z",
                updatedAt = null,
                platformFeePercent = 10,
                transactionStatus = null,
                receiptPhoto = null,
                autoCancelAfterDays = 10,
                pickupConfirmationCode = null,
                codeExpiresAt = null,
                deliveryVerificationCode = null,
                deliveryCodeExpiresAt = null,
                carrierTrip = null,
                packageRequest = PackageRequestInfo(
                    id = 402,
                    title = "Documents",
                    description = "Important paperwork",
                    weightKg = 0.5,
                    pickupAddress = "1 Queen St",
                    deliveryAddress = "200 Rene-Levesque",
                    packageType = "documents",
                ),
            ),
            isCarrier = true,
            onClose = {},
            onAction = {},
        )
    }
}
