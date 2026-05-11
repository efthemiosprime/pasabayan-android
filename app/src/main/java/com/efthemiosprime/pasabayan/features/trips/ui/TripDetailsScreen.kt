package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PricingType
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
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
     * `CarrierTripsViewModel.updateTripStatus(tripId, target)`. iOS parity: `TripDetailsView`
     * "Update Status" toolbar entry.
     */
    onUpdateStatus: ((TripStatus) -> Unit)? = null,
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
                hasMatches = tripMatches.isNotEmpty(),
                matches = tripMatches,
            )

            if (tripMatches.isNotEmpty() && progressMetrics != null) {
                TripPackageProgressWidget(
                    metrics = progressMetrics,
                    modifier = Modifier.fillMaxWidth(),
                )
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
                AlertDialog(
                    onDismissRequest = { showCancelConfirm = false },
                    title = { Text(stringResource(R.string.trips_cancel_confirm_title)) },
                    text = { Text(stringResource(R.string.trips_cancel_confirm_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCancelConfirm = false
                                onCancel()
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.trips_cancel_confirm_action),
                                color = PasabayanColors.Error,
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelConfirm = false }) {
                            Text(stringResource(R.string.trips_cancel_confirm_keep))
                        }
                    },
                )
            }
        }
    }
}

/**
 * iOS parity (`TripDetailsView.tripEarningsOverviewCard` lines 651–706): only render when total
 * earnings > 0, two-column rows (label left, value right), and locale-aware currency formatting.
 */
@Composable
private fun CarrierEarningsSection(
    trip: Trip,
    @Suppress("UNUSED_PARAMETER") hasMatches: Boolean,
    matches: List<TripMatchPackage>,
) {
    val total = computeTripEarningsTotal(trip)
    if (total <= 0.0) return
    val currency = trip.tripEarningsCurrency
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            PDetailSectionTitle(text = stringResource(R.string.trips_detail_earnings_title))
            EarningsRow(
                label = stringResource(R.string.trips_detail_earnings_label_total),
                value = formatTripCurrency(total, currency),
            )
            trip.tripEarningsBreakdown?.let { breakdown ->
                EarningsRow(
                    label = stringResource(R.string.trips_detail_earnings_label_delivered),
                    value = formatTripCurrency(breakdown.deliveredAmount, breakdown.deliveredCurrency),
                )
                EarningsRow(
                    label = stringResource(R.string.trips_detail_earnings_label_pending),
                    value = formatTripCurrency(breakdown.pendingAmount, breakdown.pendingCurrency),
                )
                // iOS parity: show package counts when the breakdown reports them. Fall back to
                // local match counts so the UI stays informative on older API responses.
                val deliveredCount = breakdown.deliveredCount.takeIf { it > 0 }
                    ?: matches.count { it.matchStatus == MatchStatus.DELIVERED }
                val pendingCount = breakdown.pendingCount.takeIf { it > 0 }
                    ?: matches.count { it.matchStatus != MatchStatus.DELIVERED }
                if (deliveredCount > 0 || pendingCount > 0) {
                    EarningsRow(
                        label = stringResource(R.string.trips_detail_earnings_label_delivered_packages),
                        value = deliveredCount.toString(),
                    )
                    EarningsRow(
                        label = stringResource(R.string.trips_detail_earnings_label_pending_packages),
                        value = pendingCount.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EarningsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * iOS parity (`tripEarningsTotal` in TripDetailsView): prefer the server's total, otherwise fall
 * back to delivered + pending from the breakdown. Returns 0 when neither is available.
 */
internal fun computeTripEarningsTotal(trip: Trip): Double {
    trip.tripEarningsTotal?.let { return it }
    val breakdown = trip.tripEarningsBreakdown ?: return 0.0
    return breakdown.deliveredAmount + breakdown.pendingAmount
}

/**
 * iOS parity (`formatCurrency`): locale-aware currency formatting. Falls back to `<code> <amount>`
 * when the currency code is unknown or invalid.
 */
internal fun formatTripCurrency(amount: Double, currency: String?): String {
    val code = currency?.takeIf { it.isNotBlank() } ?: "CAD"
    return try {
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
        formatter.currency = java.util.Currency.getInstance(code)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        formatter.format(amount)
    } catch (_: IllegalArgumentException) {
        String.format(java.util.Locale.getDefault(), "%s %.2f", code, amount)
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

/**
 * iOS parity (`TripMatchPackageCard` in `TripDetailsView.swift` lines 1196–1395): header row with
 * description + colored status badge, pickup→delivery city row, shipper name + rating, code state
 * rows, optional chat pill. The whole card is tap-to-chat when a conversation id is present, and
 * dimmed to ~85% otherwise.
 */
@Composable
private fun TripMatchPackageCard(
    match: TripMatchPackage,
    onChatTap: ((Int) -> Unit)?,
) {
    val conversationId = match.chatConversationId
    val isChatAvailable = conversationId != null && onChatTap != null
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { base ->
            if (isChatAvailable) {
                base.clickable { onChatTap?.invoke(conversationId!!) }
            } else {
                base
            }
        }
        .padding(vertical = PasabayanSpacing.xs)
    Column(
        modifier = cardModifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        MatchHeaderRow(
            description = match.packageDescription
                ?: stringResource(R.string.trips_detail_package_fallback),
            status = match.matchStatus,
            isDimmed = !isChatAvailable,
        )
        MatchRouteRow(
            pickupCity = match.packagePickupCity,
            deliveryCity = match.packageDeliveryCity,
        )
        MatchShipperRow(
            name = match.shipper?.name,
            rating = match.shipper?.ratingValue,
        )
        MatchCodeStateRow(
            title = stringResource(R.string.trips_detail_pickup_code),
            state = pickupCodeState(match),
        )
        MatchCodeStateRow(
            title = stringResource(R.string.trips_detail_delivery_code),
            state = deliveryCodeState(match),
        )
        if (isChatAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                MatchChatPill(onClick = { onChatTap?.invoke(conversationId!!) })
            }
        }
    }
}

@Composable
private fun MatchHeaderRow(
    description: String,
    status: MatchStatus,
    isDimmed: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = description,
            style = PasabayanTextStyles.Body.medium,
            color = if (isDimmed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        MatchStatusBadge(status = status)
    }
}

@Composable
private fun MatchStatusBadge(status: MatchStatus) {
    val color = matchStatusBadgeColor(status)
    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    ) {
        Text(
            text = matchStatusLabel(status),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
        )
    }
}

@Composable
private fun MatchRouteRow(pickupCity: String?, deliveryCity: String?) {
    val unknown = stringResource(R.string.trips_detail_match_route_unknown)
    val routeText = stringResource(
        R.string.trips_detail_match_route_format,
        pickupCity?.takeIf { it.isNotBlank() } ?: unknown,
        deliveryCity?.takeIf { it.isNotBlank() } ?: unknown,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = routeText,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MatchShipperRow(name: String?, rating: Double?) {
    if (name.isNullOrBlank() && rating == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        if (!name.isNullOrBlank()) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = name,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (rating != null) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = PasabayanColors.Warning,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = stringResource(R.string.trips_detail_match_rating_format, rating),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun matchStatusBadgeColor(status: MatchStatus): Color = when (status) {
    MatchStatus.CONFIRMED,
    MatchStatus.SHIPPER_ACCEPTED,
    MatchStatus.CARRIER_ACCEPTED -> PasabayanColors.BadgeBlue
    MatchStatus.PICKED_UP -> PasabayanColors.BadgeOrange
    MatchStatus.IN_TRANSIT -> PasabayanColors.BadgePurple
    MatchStatus.DELIVERED -> PasabayanColors.BadgeGreen
    else -> PasabayanColors.BadgeGray
}

@Composable
private fun MatchCodeStateRow(
    title: String,
    state: CodeState,
) {
    val (color, label) = when (state) {
        CodeState.Requested -> PasabayanColors.Warning to stringResource(R.string.trips_code_state_requested)
        is CodeState.Verified -> {
            val verified = stringResource(R.string.trips_code_state_verified)
            val labelText = state.dateText?.takeIf { it.isNotBlank() }
                ?.let { "$verified · $it" }
                ?: verified
            PasabayanColors.Success to labelText
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Text(
            text = title,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
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
private fun MatchChatPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = PasabayanSpacing.sm, vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Outlined.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.trips_detail_match_chat_pill),
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
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
    MatchStatus.SHIPPER_ACCEPTED,
    MatchStatus.CARRIER_ACCEPTED -> stringResource(R.string.bookings_status_confirmed)
    MatchStatus.SHIPPER_DECLINED,
    MatchStatus.CARRIER_DECLINED -> stringResource(R.string.bookings_status_cancelled)
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
