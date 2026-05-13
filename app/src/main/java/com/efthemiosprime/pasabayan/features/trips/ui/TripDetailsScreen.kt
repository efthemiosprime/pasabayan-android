package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.PUserInfoCard
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PricingType
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.trips.components.TripPackageProgressWidget
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripPackagesFilter
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
fun TripDetailsScreen(
    trip: Trip,
    isCarrier: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onRequestBook: (() -> Unit)? = null,
    /**
     * Invoked when the user taps the chat affordance on an accepted-package match row.
     * The id is the `chatConversationId` from the match. iOS parity:
     * `TripMatchPackageCard.onTap` opening the conversation.
     */
    onOpenChat: ((conversationId: Int) -> Unit)? = null,
    /**
     * Invoked when the user picks a new trip status in [TripStatusUpdateSheet]. Wire to
     * `CarrierTripsViewModel.suspendUpdateTripStatus(tripId, target)`. iOS parity: `TripDetailsView`
     * "Update Status" toolbar entry + `TripUpdateTimeoutCoordinator` 15s fallback.
     */
    onUpdateStatus: (suspend (TripStatus) -> Result<Trip>)? = null,
    tripMatches: List<TripMatchPackage> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val statusLabel = tripDetailStatusLabel(trip.tripStatus)
    var packagesFilter by remember { mutableStateOf(TripPackagesFilter.ALL) }
    val filteredMatches = filterTripMatches(tripMatches, packagesFilter)
    val progressMetrics = toProgressMetrics(tripMatches, trip.formattedArrivalDate)
    PDetailSheetScaffold(
        title = stringResource(R.string.trips_detail_title),
        closeContentDescription = stringResource(R.string.trips_detail_close),
        onClose = onBack,
        modifier = modifier,
    ) {

        // Header card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = trip.route,
                    style = PasabayanTextStyles.Heading.h3,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    StatusChip(label = statusLabel, status = trip.tripStatus)
                    TransportChip(label = trip.transportationMethodLabel())
                    // iOS parity: inline "No packages assigned" capsule when the trip is still
                    // in planning and the carrier hasn't yet accepted any matches.
                    if (isCarrier &&
                        trip.tripStatus == TripStatus.PLANNING &&
                        tripMatches.isEmpty()
                    ) {
                        NoPackagesAssignedChip()
                    }
                }
            }
        }

        // Carrier info card — iOS parity with the carrier header in BrowseTripsView,
        // surfaced inside the detail view so shippers see who they're booking with.
        if (!isCarrier) {
            trip.carrier?.let { CarrierInfoCard(carrier = it) }
        }

        // Route information card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_route_information))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_start_location),
                    value = "${trip.originCity}, ${trip.originCountry}",
                    caption = listOfNotNull(trip.pickupAddress, trip.pickupInstructions)
                        .filter { it.isNotBlank() }
                        .joinToString(separator = " • ")
                        .ifBlank { null },
                )
                PDivider()
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_end_location),
                    value = "${trip.destinationCity}, ${trip.destinationCountry}",
                    caption = listOfNotNull(trip.dropoffAddress, trip.dropoffInstructions)
                        .filter { it.isNotBlank() }
                        .joinToString(separator = " • ")
                        .ifBlank { null },
                )
            }
        }

        // Schedule card — iOS parity (`TripDetailsView.scheduleInformationCard`): show the
        // shared pickup window (falling back to `departureDate`) and the shared delivery window
        // (falling back to `arrivalDate`), with a "Not set" placeholder when neither is set.
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_schedule))
                ScheduleOptionalRow(
                    label = stringResource(R.string.trips_detail_pickup),
                    formatted = trip.formattedPickupOrDepartureDate,
                )
                ScheduleOptionalRow(
                    label = stringResource(R.string.trips_detail_delivery),
                    formatted = trip.formattedDeliveryOrArrivalDate,
                )
            }
        }

        // Capacity card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_available_capacity))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Scale,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_weight),
                    value = trip.formattedCapacity,
                )
                // iOS parity (`TripDetailsView.capacityInformationCard`): also surface the
                // available space in liters when the carrier provided it.
                trip.availableSpaceLiters?.takeIf { it > 0.0 }?.let { liters ->
                    LabeledIconRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = PasabayanColors.Info,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = stringResource(R.string.trips_detail_available_space),
                        value = stringResource(R.string.trips_detail_available_space_value, liters),
                    )
                }
            }
        }

        // Pricing card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_pricing_title))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            tint = PasabayanColors.Success,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                    label = stringResource(
                        if (trip.effectivePricingType == PricingType.PER_KG) {
                            R.string.trips_detail_price_per_kg_label
                        } else {
                            R.string.trips_detail_flat_rate_label
                        },
                    ),
                    value = trip.formattedPrice,
                    valueColor = PasabayanColors.Success,
                )
            }
        }

        // Special notes
        trip.specialNotes?.let { notes ->
            PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.trips_detail_notes),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = notes,
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (isCarrier) {
            CarrierEarningsSection(
                trip = trip,
                matches = tripMatches,
            )

            if (tripMatches.isNotEmpty() && progressMetrics != null) {
                PCard(modifier = Modifier.fillMaxWidth()) {
                    TripPackageProgressWidget(metrics = progressMetrics)
                }
            }

            if (tripMatches.isNotEmpty()) {
                CarrierAcceptedPackagesSection(
                    filteredMatches = filteredMatches,
                    filter = packagesFilter,
                    allCount = tripMatches.size,
                    remainingCount = filterTripMatches(tripMatches, TripPackagesFilter.REMAINING).size,
                    deliveredCount = filterTripMatches(tripMatches, TripPackagesFilter.DELIVERED).size,
                    onFilterChange = { packagesFilter = it },
                    onOpenChat = onOpenChat,
                )
            }
        }

        // Shipper booking section parity with iOS TripDetailsView.
        if (!isCarrier) {
            if (trip.isBookable) {
                PButton(
                    text = stringResource(R.string.trips_detail_request_to_book),
                    onClick = { onRequestBook?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onRequestBook != null,
                )
            } else {
                bookingUnavailableMessage(trip)?.let { message ->
                    Text(
                        text = message,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Actions (carrier only). Mirrors iOS `shouldShowActionsSection` — hidden once every
        // package on the trip is delivered, so the cancel/edit affordance disappears for a
        // trip that has effectively run its course.
        val canModifyTrip = isCarrier &&
            trip.tripStatus != TripStatus.CANCELLED &&
            trip.tripStatus != TripStatus.COMPLETED
        val deliveredAll = allPackagesDelivered(tripMatches)
        if (canModifyTrip && !deliveredAll) {
            // iOS parity: dedicated "Update Status" entry surfaces transitions
            // (planning → active, active → in_transit, in_transit → completed).
            val canAdvanceStatus = onUpdateStatus != null &&
                nextStatusOptions(trip.tripStatus).isNotEmpty()
            var showStatusSheet by remember { mutableStateOf(false) }
            if (canAdvanceStatus) {
                PButton(
                    text = stringResource(R.string.trips_status_update_button),
                    onClick = { showStatusSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (showStatusSheet && onUpdateStatus != null) {
                TripStatusUpdateSheet(
                    trip = trip,
                    onUpdateStatus = onUpdateStatus,
                    onDismiss = { showStatusSheet = false },
                )
            }
            PButton(
                text = stringResource(R.string.trips_edit_trip),
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )

            val blockingMatches = hasBlockingMatches(tripMatches)
            var showCancelConfirm by remember { mutableStateOf(false) }
            PButton(
                text = stringResource(R.string.trips_cancel_trip),
                onClick = { showCancelConfirm = true },
                style = PButtonStyle.Destructive,
                enabled = !blockingMatches,
                modifier = Modifier.fillMaxWidth(),
            )
            if (blockingMatches) {
                Text(
                    text = stringResource(R.string.trips_cancel_blocked_message),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (showCancelConfirm) {
                com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog(
                    title = stringResource(R.string.trips_cancel_confirm_title),
                    message = stringResource(R.string.trips_cancel_confirm_message),
                    confirmText = stringResource(R.string.trips_cancel_confirm_action),
                    onConfirm = {
                        showCancelConfirm = false
                        onCancel()
                    },
                    dismissText = stringResource(R.string.trips_cancel_confirm_keep),
                    onDismiss = { showCancelConfirm = false },
                    isDestructive = true,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CarrierAcceptedPackagesSection(
    filteredMatches: List<TripMatchPackage>,
    filter: TripPackagesFilter,
    allCount: Int,
    remainingCount: Int,
    deliveredCount: Int,
    onFilterChange: (TripPackagesFilter) -> Unit,
    onOpenChat: ((Int) -> Unit)? = null,
) {
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailSectionTitle(text = stringResource(R.string.trips_detail_accepted_packages_title))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                PFilterChip(
                    label = stringResource(R.string.trips_filter_all),
                    selected = filter == TripPackagesFilter.ALL,
                    count = allCount,
                    onClick = { onFilterChange(TripPackagesFilter.ALL) },
                )
                PFilterChip(
                    label = stringResource(R.string.trips_filter_remaining),
                    selected = filter == TripPackagesFilter.REMAINING,
                    count = remainingCount,
                    onClick = { onFilterChange(TripPackagesFilter.REMAINING) },
                )
                PFilterChip(
                    label = stringResource(R.string.trips_filter_delivered),
                    selected = filter == TripPackagesFilter.DELIVERED,
                    count = deliveredCount,
                    onClick = { onFilterChange(TripPackagesFilter.DELIVERED) },
                )
            }
            if (filteredMatches.isEmpty()) {
                AcceptedPackagesEmptyState(
                    filter = filter,
                    onShowAll = { onFilterChange(TripPackagesFilter.ALL) },
                )
            } else {
                filteredMatches.forEachIndexed { index, match ->
                    TripMatchPackageCard(match = match, onChatTap = onOpenChat)
                    if (index != filteredMatches.lastIndex) {
                        PDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CarrierInfoCard(carrier: UserSummary) {
    PUserInfoCard(
        name = carrier.name,
        title = stringResource(R.string.trips_detail_carrier_information),
        rating = carrier.rating?.takeIf { it.isNotBlank() },
        totalRatings = carrier.totalRatings,
        verificationLevel = carrier.verificationLevel,
        noRatingsLabel = stringResource(R.string.trips_detail_no_ratings_yet),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun NoPackagesAssignedChip() {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .background(
                color = PasabayanColors.BadgeGray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
            )
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(R.string.trips_detail_no_packages_assigned),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AcceptedPackagesEmptyState(
    filter: TripPackagesFilter,
    onShowAll: () -> Unit,
) {
    // iOS parity (`TripDetailsView.swift` lines 1064–1084): per-filter empty copy + recovery
    // button when a filter other than ALL is active.
    val (titleRes, descRes) = when (filter) {
        TripPackagesFilter.ALL ->
            R.string.trips_detail_accepted_packages_empty_all_title to
                R.string.trips_detail_accepted_packages_empty_all_description
        TripPackagesFilter.REMAINING ->
            R.string.trips_detail_accepted_packages_empty_remaining_title to
                R.string.trips_detail_accepted_packages_empty_remaining_description
        TripPackagesFilter.DELIVERED ->
            R.string.trips_detail_accepted_packages_empty_delivered_title to
                R.string.trips_detail_accepted_packages_empty_delivered_description
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Text(
            text = stringResource(titleRes),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(descRes),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (filter != TripPackagesFilter.ALL) {
            Spacer(modifier = Modifier.height(PasabayanSpacing.xs))
            PButton(
                text = stringResource(R.string.trips_detail_accepted_packages_show_all),
                onClick = onShowAll,
                style = PButtonStyle.Secondary,
            )
        }
    }
}

@Composable
private fun LabeledIconRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        icon()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.ifBlank { "-" },
                style = PasabayanTextStyles.Body.medium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold,
            )
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScheduleOptionalRow(
    label: String,
    formatted: String?,
) {
    val isPlaceholder = formatted.isNullOrBlank()
    val displayValue = formatted?.takeUnless { it.isBlank() }
        ?: stringResource(R.string.trips_detail_schedule_not_set)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = PasabayanColors.Info,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = displayValue,
                style = PasabayanTextStyles.Body.medium,
                color = if (isPlaceholder) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TransportChip(label: String) {
    HeaderChip(
        text = label,
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun StatusChip(label: String, status: TripStatus) {
    val (textColor, backgroundColor) = when (status) {
        TripStatus.ACTIVE -> PasabayanColors.StatusActive to PasabayanColors.BadgeBlueLight
        TripStatus.COMPLETED -> PasabayanColors.StatusCompleted to PasabayanColors.BadgeGreenLight
        TripStatus.IN_TRANSIT -> PasabayanColors.StatusInTransit to PasabayanColors.BadgePurpleLight
        TripStatus.CANCELLED -> PasabayanColors.StatusCancelled to PasabayanColors.BadgeRedLight
        TripStatus.PLANNING -> PasabayanColors.StatusPending to PasabayanColors.BadgeOrangeLight
    }
    HeaderChip(
        text = label,
        textColor = textColor,
        backgroundColor = backgroundColor,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp),
            )
        },
    )
}

@Composable
private fun HeaderChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = PasabayanSpacing.md, vertical = 6.dp),
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            style = PasabayanTextStyles.Caption.large,
            color = textColor,
        )
    }
}

@Composable
private fun Trip.transportationMethodLabel(): String = when (transportationMethod) {
    TransportationMethod.NONE -> stringResource(R.string.trips_transport_other)
    TransportationMethod.FLIGHT -> stringResource(R.string.trips_transport_flight)
    TransportationMethod.BUS -> stringResource(R.string.trips_transport_bus)
    TransportationMethod.CAR -> stringResource(R.string.trips_transport_car)
    TransportationMethod.TRUCK -> stringResource(R.string.trips_transport_truck)
    TransportationMethod.VAN -> stringResource(R.string.trips_transport_van)
    TransportationMethod.MOTORCYCLE -> stringResource(R.string.trips_transport_motorcycle)
    TransportationMethod.SHIP -> stringResource(R.string.trips_transport_ship)
    TransportationMethod.TRAIN -> stringResource(R.string.trips_transport_train)
    TransportationMethod.OTHER -> stringResource(R.string.trips_transport_other)
}

@Composable
private fun bookingUnavailableMessage(trip: Trip): String? {
    if (!trip.hasCapacity) {
        return stringResource(R.string.trips_detail_unavailable_fully_booked)
    }
    return when (trip.tripStatus) {
        TripStatus.IN_TRANSIT -> stringResource(R.string.trips_detail_unavailable_in_transit)
        TripStatus.COMPLETED -> stringResource(R.string.trips_detail_unavailable_completed)
        TripStatus.CANCELLED -> stringResource(R.string.trips_detail_unavailable_cancelled)
        TripStatus.PLANNING, TripStatus.ACTIVE -> null
    }
}

@Composable
private fun tripDetailStatusLabel(status: TripStatus): String = when (status) {
    TripStatus.PLANNING -> stringResource(R.string.trips_status_planning)
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_completed)
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_cancelled)
}

@Preview(showBackground = true, name = "TripDetails — light", heightDp = 900)
@Preview(showBackground = true, name = "TripDetails — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripDetailsPreview() {
    PasabayanTheme {
        TripDetailsScreen(
            trip = Trip(
                id = 1, carrierId = 42,
                originCity = "Toronto", originCountry = "Canada",
                originLat = 43.65, originLng = -79.38,
                destinationCity = "Vancouver", destinationCountry = "Canada",
                destinationLat = 49.28, destinationLng = -123.12,
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T14:00:00Z",
                availableWeightKg = 25.0, availableSpaceLiters = 50.0,
                pricePerKg = 15.0, tripStatus = TripStatus.ACTIVE,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Handle with care — fragile electronics",
                carrier = null, createdAt = null, updatedAt = null,
                pricingType = null, pricingMethod = null,
                flatTripPrice = null, basePrice = null, calculatedPrice = null,
                pickupAddress = "123 Main St", pickupLandmark = "Near Central Station",
                dropoffAddress = "456 Oak Ave", dropoffLandmark = null,
                tripEarningsTotal = null, tripEarningsCurrency = null,
                tripEarningsBreakdown = null,
                hasPendingRequests = null, pendingRequestCount = null,
                pendingRequests = null, distanceKm = 3365.0,
            ),
            isCarrier = true,
            onEdit = {},
            onCancel = {},
            onBack = {},
            onOpenChat = {},
            tripMatches = listOf(
                TripMatchPackage(
                    id = 100,
                    matchStatus = MatchStatus.CONFIRMED,
                    agreedPrice = 25.0,
                    packageDescription = "Laptop and accessories",
                    packageWeightKg = 2.5,
                    packageId = 101,
                    packagePickupCity = "Manila",
                    packageDeliveryCity = "Cebu",
                    packageFragile = true,
                    packageType = "fragile",
                    shipper = com.efthemiosprime.pasabayan.core.domain.model.UserSummary(
                        id = 201,
                        name = "Maria Santos",
                        rating = "4.7",
                    ),
                    chatConversationId = 555,
                    confirmedAt = "2026-03-29T10:00:00Z",
                    pickedUpAt = null,
                    deliveredAt = null,
                    createdAt = "2026-03-28T08:00:00Z",
                ),
                TripMatchPackage(
                    id = 101,
                    matchStatus = MatchStatus.IN_TRANSIT,
                    agreedPrice = 40.0,
                    packageDescription = "Books bundle",
                    packageWeightKg = 4.0,
                    packageId = 102,
                    packagePickupCity = "Manila",
                    packageDeliveryCity = "Davao",
                    packageFragile = false,
                    packageType = "general",
                    shipper = com.efthemiosprime.pasabayan.core.domain.model.UserSummary(
                        id = 202,
                        name = "Pedro Cruz",
                    ),
                    chatConversationId = null,
                    confirmedAt = "2026-03-28T08:00:00Z",
                    pickedUpAt = "2026-03-30T11:00:00Z",
                    deliveredAt = null,
                    createdAt = "2026-03-27T08:00:00Z",
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "TripDetails (shipper) — light", heightDp = 900)
@Preview(
    showBackground = true,
    name = "TripDetails (shipper) — dark",
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TripDetailsShipperPreview() {
    PasabayanTheme {
        TripDetailsScreen(
            trip = Trip(
                id = 1, carrierId = 42,
                originCity = "Toronto", originCountry = "Canada",
                originLat = 43.65, originLng = -79.38,
                destinationCity = "Vancouver", destinationCountry = "Canada",
                destinationLat = 49.28, destinationLng = -123.12,
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T14:00:00Z",
                availableWeightKg = 25.0, availableSpaceLiters = 50.0,
                pricePerKg = 15.0, tripStatus = TripStatus.ACTIVE,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Handle with care — fragile electronics",
                carrier = UserSummary(
                    id = 42,
                    name = "Alex Carrier",
                    avatar = null,
                    verificationLevel = "verified",
                    rating = "4.9",
                    totalRatings = 87,
                ),
                createdAt = null, updatedAt = null,
                pricingType = null, pricingMethod = null,
                flatTripPrice = null, basePrice = null, calculatedPrice = null,
                pickupAddress = "123 Main St", pickupLandmark = "Near Central Station",
                dropoffAddress = "456 Oak Ave", dropoffLandmark = null,
                tripEarningsTotal = null, tripEarningsCurrency = null,
                tripEarningsBreakdown = null,
                hasPendingRequests = null, pendingRequestCount = null,
                pendingRequests = null, distanceKm = 3365.0,
            ),
            isCarrier = false,
            onEdit = {},
            onCancel = {},
            onBack = {},
            onRequestBook = {},
        )
    }
}
