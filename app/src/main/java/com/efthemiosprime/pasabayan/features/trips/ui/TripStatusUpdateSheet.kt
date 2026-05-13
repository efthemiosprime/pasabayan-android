package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * iOS-parity fallback for the in-sheet API call (`TripUpdateTimeoutCoordinator`): if the server
 * takes longer than this, abandon waiting and surface a timeout error so the carrier isn't
 * stranded with an indefinite spinner.
 */
private const val STATUS_UPDATE_TIMEOUT_MS: Long = 15_000L

/**
 * Status-only update sheet. iOS parity: `TripStatusUpdateSheet.swift` — the full iOS sheet
 * also edits capacity/pricing/notes, but Android already exposes those in [EditTripSheet];
 * this sheet stays focused on the transition. The picker mirrors iOS' `statusPickerOptions`
 * (Swift :228): planning offers active + cancelled, active offers in_transit + cancelled,
 * in_transit progresses only to completed, terminal states show no options.
 *
 * The sheet dispatches to one of three host callbacks depending on the selected target:
 *   - **PLANNING → ACTIVE** uses [onActivate], which the host wires to the sanctioned
 *     `POST /trips/{id}/activate` (Slice A). Confirmation alert before submit.
 *   - **Any → CANCELLED** uses [onCancel], which goes through `DELETE /trips/{id}` and surfaces
 *     the blocking-match-aware error message when the server returns HTTP 409 (Slice B6).
 *     Destructive confirmation alert before submit.
 *   - All other transitions use [onUpdateStatus] with the target status, routed through
 *     `CarrierTripsViewModel.suspendUpdateTripStatus` exactly as before. No confirmation —
 *     iOS only confirms activate / cancel.
 *
 * The sheet drives its own isUpdating spinner + 15s timeout fallback (parity with iOS
 * `TripUpdateTimeoutCoordinator`) and dismisses only on a successful response.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TripStatusUpdateSheet(
    trip: Trip,
    onUpdateStatus: suspend (TripStatus) -> Result<Trip>,
    onActivate: suspend () -> Result<Trip>,
    onCancel: suspend () -> Result<Unit>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        TripStatusUpdateSheetContent(
            trip = trip,
            onUpdateStatus = onUpdateStatus,
            onActivate = onActivate,
            onCancel = onCancel,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TripStatusUpdateSheetContent(
    trip: Trip,
    onUpdateStatus: suspend (TripStatus) -> Result<Trip>,
    onActivate: suspend () -> Result<Trip> = { Result.failure(IllegalStateException("activate not wired")) },
    onCancel: suspend () -> Result<Unit> = { Result.failure(IllegalStateException("cancel not wired")) },
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember(trip.tripStatus) { nextStatusOptions(trip.tripStatus) }
    var selected by remember(trip.id, trip.tripStatus) {
        mutableStateOf(options.firstOrNull())
    }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingConfirmation by remember { mutableStateOf<TripStatus?>(null) }
    val scope = rememberCoroutineScope()
    val timeoutMessage = stringResource(R.string.trips_status_update_error_timeout)
    val genericErrorMessage = stringResource(R.string.trips_status_update_error_generic)
    val blockingMatchHint = stringResource(R.string.trips_details_cancel_error_match_in_progress_hint)

    // Dispatch a target status: routes activation through onActivate, cancellation through
    // onCancel (with blocking-match-aware error formatting), everything else through
    // onUpdateStatus. Shared isUpdating + timeout + dismiss-on-success behavior.
    fun submitTarget(target: TripStatus) {
        if (isUpdating) return
        isUpdating = true
        errorMessage = null
        scope.launch {
            val outcome: Throwable? = withTimeoutOrNull(STATUS_UPDATE_TIMEOUT_MS) {
                when (target) {
                    TripStatus.ACTIVE -> onActivate().exceptionOrNull()
                    TripStatus.CANCELLED -> onCancel().exceptionOrNull()
                    else -> onUpdateStatus(target).exceptionOrNull()
                }
            } ?: run {
                errorMessage = timeoutMessage
                isUpdating = false
                return@launch
            }
            if (outcome == null) {
                isUpdating = false
                onDismiss()
                return@launch
            }
            val domainError = (outcome as? DomainErrorMapperException)?.domainError
            errorMessage = when (domainError) {
                is DomainError.TripHasBlockingMatch ->
                    listOfNotNull(domainError.message, blockingMatchHint)
                        .joinToString(separator = "\n\n")
                else -> outcome.message?.takeIf { it.isNotBlank() } ?: genericErrorMessage
            }
            isUpdating = false
        }
    }
    PDetailSheetScaffold(
        title = stringResource(R.string.trips_status_update_title),
        closeContentDescription = stringResource(R.string.trips_status_update_close),
        onClose = { if (!isUpdating) onDismiss() },
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
                            onSelect = { if (!isUpdating) selected = option },
                            enabled = !isUpdating,
                        )
                    }
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.Error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = PasabayanSpacing.sm),
            )
        }

        if (isUpdating) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = PasabayanSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.trips_status_update_in_progress),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                enabled = !isUpdating,
                modifier = Modifier.weight(1f),
            )
            val pendingTarget = selected
            PButton(
                text = stringResource(R.string.trips_status_update_submit),
                onClick = onClick@{
                    val target = pendingTarget ?: return@onClick
                    if (isUpdating) return@onClick
                    // iOS parity: activate + cancel require an explicit confirmation alert;
                    // forward transitions (in_transit, completed) submit directly.
                    if (target == TripStatus.ACTIVE || target == TripStatus.CANCELLED) {
                        pendingConfirmation = target
                    } else {
                        submitTarget(target)
                    }
                },
                style = PButtonStyle.Primary,
                enabled = pendingTarget != null && !isUpdating,
                modifier = Modifier.weight(1f),
            )
        }
    }

    pendingConfirmation?.let { target ->
        when (target) {
            TripStatus.ACTIVE -> AlertDialog(
                onDismissRequest = { pendingConfirmation = null },
                title = { Text(stringResource(R.string.trips_action_activate_trip_confirm_title)) },
                text = { Text(stringResource(R.string.trips_action_activate_trip_confirm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingConfirmation = null
                        submitTarget(TripStatus.ACTIVE)
                    }) { Text(stringResource(R.string.trips_action_activate_trip)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingConfirmation = null }) {
                        Text(stringResource(R.string.common_buttons_cancel))
                    }
                },
            )
            TripStatus.CANCELLED -> AlertDialog(
                onDismissRequest = { pendingConfirmation = null },
                title = { Text(stringResource(R.string.trips_cancel_trip)) },
                text = { Text(stringResource(R.string.trips_action_cancel_trip_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingConfirmation = null
                        submitTarget(TripStatus.CANCELLED)
                    }) { Text(stringResource(R.string.trips_cancel_trip)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingConfirmation = null }) {
                        Text(stringResource(R.string.trips_action_keep_trip))
                    }
                },
            )
            else -> Unit
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
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(vertical = PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        RadioButton(selected = selected, onClick = onSelect, enabled = enabled)
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
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_update_hint_cancelled)
    else -> ""
}

private val previewSuccess: suspend (TripStatus) -> Result<Trip> = { Result.success(previewTrip(it)) }

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
            onUpdateStatus = previewSuccess,
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
            onUpdateStatus = previewSuccess,
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
            onUpdateStatus = previewSuccess,
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
