package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.trips.components.TripCard
import com.efthemiosprime.pasabayan.features.trips.components.TripFilterContent
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTripsScreen(
    onViewTripDetails: (tripId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrowseTripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadAvailableTrips() }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        POutlinedTextField(
            value = state.filter.searchText,
            onValueChange = { viewModel.updateSearchText(it) },
            label = { Text(stringResource(R.string.trips_browse_search)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PasabayanSpacing.screenPadding, vertical = PasabayanSpacing.sm),
            trailingIcon = {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.trips_browse_title),
                    )
                }
            },
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
            state.hasLoadedTrips && state.availableTrips.isEmpty() -> {
                PEmptyState(
                    icon = Icons.Outlined.Explore,
                    title = stringResource(R.string.trips_empty_no_available),
                    description = stringResource(R.string.trips_empty_no_available_description),
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
                    items(state.availableTrips, key = { it.id }) { trip ->
                        TripCard(
                            trip = trip,
                            onViewDetails = { onViewTripDetails(trip.id) },
                        )
                    }
                }
            }
        }
    }

    // Filter sheet
    if (showFilterSheet) {
        PModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            TripFilterContent(
                filter = state.filter,
                onSearchChange = { viewModel.updateSearchText(it) },
                onOriginChange = { viewModel.updateOrigin(it) },
                onDestinationChange = { viewModel.updateDestination(it) },
                onApply = {
                    viewModel.applyFilterAndFetch()
                    showFilterSheet = false
                },
                onClear = {
                    viewModel.clearFilters()
                    showFilterSheet = false
                },
            )
        }
    }
}

@Preview(showBackground = true, name = "BrowseTrips — light", heightDp = 700)
@Preview(showBackground = true, name = "BrowseTrips — dark", heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BrowseTripsPreview() {
    PasabayanTheme {
        PEmptyState(
            icon = Icons.Outlined.Explore,
            title = "No available trips",
            description = "Try adjusting your search filters.",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
