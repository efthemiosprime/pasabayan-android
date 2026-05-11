package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip

/**
 * Status-only update sheet. iOS parity: `TripStatusUpdateSheet.swift` — the full iOS sheet
 * also edits capacity/pricing/notes, but Android already exposes those in [EditTripSheet];
 * this sheet stays focused on the transition.
 *
 * The host wires [onUpdateStatus] to `CarrierTripsViewModel.updateTripStatus(tripId, target)`.
 * The sheet dismisses on submit; the host observes the VM state for success / error feedback.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TripStatusUpdateSheet(
    trip: Trip,
    onUpdateStatus: (TripStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        TripStatusUpdateSheetContent(
            trip = trip,
            onUpdateStatus = onUpdateStatus,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TripStatusUpdateSheetContent(
    trip: Trip,
    onUpdateStatus: (TripStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember(trip.tripStatus) { nextStatusOptions(trip.tripStatus) }
    var selected by remember(trip.id, trip.tripStatus) {
        mutableStateOf(options.firstOrNull())
    }
    PDetailSheetScaffold(
        title = stringResource(R.string.trips_status_update_title),
        closeContentDescription = stringResource(R.string.trips_status_update_close),
        onClose = onDismiss,
        modifier = modifier,
    ) {
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_status_update_current_label))
                CurrentStatusRow(currentStatus = trip.tripStatus)

                if (options.isEmpty()) {
                    Text(
                        text = stringResource(R.string.trips_status_update_no_transitions),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    PDetailSectionTitle(text = stringResource(R.string.trips_status_update_next_label))
                    options.forEach { option ->
                        StatusOptionRow(
                            status = option,
                            selected = option == selected,
                            onSelect = { selected = option },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PButton(
                text = stringResource(R.string.trips_status_update_cancel),
                onClick = onDismiss,
                style = PButtonStyle.Secondary,
                modifier = Modifier.weight(1f),
            )
            val pendingTarget = selected
            PButton(
                text = stringResource(R.string.trips_status_update_submit),
                onClick = {
                    if (pendingTarget != null) {
                        onUpdateStatus(pendingTarget)
                        onDismiss()
                    }
                },
                style = PButtonStyle.Primary,
                enabled = pendingTarget != null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CurrentStatusRow(currentStatus: TripStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
        )
        Text(
            text = tripStatusDisplayName(currentStatus),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusOptionRow(
    status: TripStatus,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .clickable(onClick = onSelect)
            .padding(vertical = PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column {
            Text(
                text = tripStatusDisplayName(status),
                style = PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = tripStatusOptionHint(status),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun tripStatusDisplayName(status: TripStatus): String = when (status) {
    TripStatus.PLANNING -> stringResource(R.string.trips_status_planning)
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_completed)
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_cancelled)
}

@Composable
private fun tripStatusOptionHint(status: TripStatus): String = when (status) {
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_update_hint_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_update_hint_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_update_hint_completed)
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Status update - planning", showBackground = true)
@Preview(
    name = "Status update - planning dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TripStatusUpdateSheetPlanningPreview() {
    PasabayanTheme {
        TripStatusUpdateSheetContent(
            trip = previewTrip(TripStatus.PLANNING),
            onUpdateStatus = {},
            onDismiss = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Status update - in transit", showBackground = true)
@Composable
private fun TripStatusUpdateSheetInTransitPreview() {
    PasabayanTheme {
        TripStatusUpdateSheetContent(
            trip = previewTrip(TripStatus.IN_TRANSIT),
            onUpdateStatus = {},
            onDismiss = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Status update - completed terminal", showBackground = true)
@Composable
private fun TripStatusUpdateSheetCompletedPreview() {
    PasabayanTheme {
        TripStatusUpdateSheetContent(
            trip = previewTrip(TripStatus.COMPLETED),
            onUpdateStatus = {},
            onDismiss = {},
        )
    }
}

private fun previewTrip(status: TripStatus): Trip = Trip(
    id = 1,
    carrierId = 42,
    originCity = "Toronto",
    originCountry = "Canada",
    originLat = null,
    originLng = null,
    destinationCity = "Montreal",
    destinationCountry = "Canada",
    destinationLat = null,
    destinationLng = null,
    departureDate = "2026-04-01T08:00:00Z",
    arrivalDate = "2026-04-01T14:00:00Z",
    availableWeightKg = 25.0,
    availableSpaceLiters = null,
    pricePerKg = 15.0,
    tripStatus = status,
    transportationMethod = TransportationMethod.FLIGHT,
    specialNotes = null,
    carrier = null,
    createdAt = null,
    updatedAt = null,
    pricingType = null,
    pricingMethod = null,
    flatTripPrice = null,
    basePrice = null,
    calculatedPrice = null,
    pickupAddress = null,
    pickupLandmark = null,
    pickupInstructions = null,
    dropoffAddress = null,
    dropoffLandmark = null,
    dropoffInstructions = null,
    tripEarningsTotal = null,
    tripEarningsCurrency = null,
    tripEarningsBreakdown = null,
    hasPendingRequests = null,
    pendingRequestCount = null,
    pendingRequests = null,
    distanceKm = null,
)
