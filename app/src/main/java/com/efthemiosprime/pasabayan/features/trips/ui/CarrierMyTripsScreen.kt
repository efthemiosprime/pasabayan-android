package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripCard
import com.efthemiosprime.pasabayan.features.trips.components.carrierTripMenuActions
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CarrierTripsViewModel
import kotlinx.coroutines.launch

@Composable
fun CarrierMyTripsScreen(
    onViewTripDetails: (Trip) -> Unit,
    onEditTrip: (Trip) -> Unit,
    onCreateTrip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CarrierTripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var tripPendingStatusUpdate by remember { mutableStateOf<Trip?>(null) }
    var tripPendingActivate by remember { mutableStateOf<Trip?>(null) }
    var tripPendingCancel by remember { mutableStateOf<Trip?>(null) }
    var activateSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadTrips() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TripStatusFilterRow(
                statusCounts = state.statusCounts,
                selectedFilter = state.statusFilter,
                onFilterSelected = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(
                    horizontal = PasabayanSpacing.screenPadding,
                    vertical = PasabayanSpacing.sm,
                ),
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PCircularProgress()
                    }
                }
                state.filteredTrips.isEmpty() -> {
                    PEmptyState(
                        icon = Icons.Outlined.LocalShipping,
                        title = stringResource(R.string.trips_empty_no_trips),
                        description = stringResource(R.string.trips_empty_no_trips_description),
                        modifier = Modifier.padding(PasabayanSpacing.lg),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = PasabayanSpacing.screenPadding,
                            vertical = PasabayanSpacing.sm,
                        ),
                    ) {
                        items(state.filteredTrips, key = { it.id }) { trip ->
                            TripCard(
                                trip = trip,
                                onViewDetails = { onViewTripDetails(trip) },
                                showCarrierHeader = false,
                                showPackageProgress = true,
                                menuActions = carrierTripMenuActions(
                                    trip = trip,
                                    onEditTrip = { onEditTrip(trip) },
                                    onActivateTrip = { tripPendingActivate = trip },
                                    onUpdateStatus = { tripPendingStatusUpdate = trip },
                                    onCancelTrip = { tripPendingCancel = trip },
                                ),
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateTrip,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(PasabayanSpacing.lg),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.trips_create_trip),
            )
        }
    }

    // Activate Trip confirmation — iOS TripCard.swift:225-240.
    tripPendingActivate?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripPendingActivate = null },
            title = { Text(stringResource(R.string.trips_action_activate_trip_confirm_title)) },
            text = { Text(stringResource(R.string.trips_action_activate_trip_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    tripPendingActivate = null
                    scope.launch {
                        viewModel.suspendUpdateTripStatus(trip.id, TripStatus.ACTIVE)
                            .onSuccess { activateSuccess = true }
                    }
                }) { Text(stringResource(R.string.trips_action_activate_trip)) }
            },
            dismissButton = {
                TextButton(onClick = { tripPendingActivate = null }) {
                    Text(stringResource(R.string.common_buttons_cancel))
                }
            },
        )
    }

    // Activate success — iOS TripCard.swift:241-243.
    if (activateSuccess) {
        AlertDialog(
            onDismissRequest = { activateSuccess = false },
            title = { Text(stringResource(R.string.trips_action_activate_trip_success)) },
            confirmButton = {
                TextButton(onClick = { activateSuccess = false }) {
                    Text(stringResource(R.string.common_buttons_ok))
                }
            },
        )
    }

    // Cancel Trip confirmation — iOS TripCard.swift:189-217.
    tripPendingCancel?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripPendingCancel = null },
            title = { Text(stringResource(R.string.trips_cancel_trip)) },
            text = { Text(stringResource(R.string.trips_action_cancel_trip_message)) },
            confirmButton = {
                TextButton(onClick = {
                    tripPendingCancel = null
                    viewModel.cancelTrip(trip.id)
                }) { Text(stringResource(R.string.trips_cancel_trip)) }
            },
            dismissButton = {
                TextButton(onClick = { tripPendingCancel = null }) {
                    Text(stringResource(R.string.trips_action_keep_trip))
                }
            },
        )
    }

    // Update Status sheet — iOS TripCard.swift:169-175.
    tripPendingStatusUpdate?.let { trip ->
        TripStatusUpdateSheet(
            trip = trip,
            onUpdateStatus = { target -> viewModel.suspendUpdateTripStatus(trip.id, target) },
            onDismiss = { tripPendingStatusUpdate = null },
        )
    }
}

@Composable
private fun TripStatusFilterRow(
    statusCounts: Map<TripStatus, Int>,
    selectedFilter: TripStatus?,
    onFilterSelected: (TripStatus?) -> Unit,
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
            count = statusCounts.values.sum(),
        )
        TripStatus.entries.forEach { status ->
            val count = statusCounts[status] ?: 0
            if (count > 0 || status in listOf(TripStatus.ACTIVE, TripStatus.PLANNING)) {
                PFilterChip(
                    label = tripStatusFilterLabel(status),
                    selected = selectedFilter == status,
                    onClick = { onFilterSelected(status) },
                    count = count,
                )
            }
        }
    }
}

@Composable
private fun tripStatusFilterLabel(status: TripStatus): String = when (status) {
    TripStatus.PLANNING -> stringResource(R.string.trips_status_planning)
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_completed)
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_cancelled)
}

@Preview(showBackground = true, name = "CarrierMyTrips — light", heightDp = 700)
@Preview(showBackground = true, name = "CarrierMyTrips — dark", heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierMyTripsPreview() {
    PasabayanTheme {
        PEmptyState(
            icon = Icons.Outlined.LocalShipping,
            title = "No trips yet",
            description = "Create your first trip to start carrying packages.",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
