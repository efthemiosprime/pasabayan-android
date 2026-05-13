package com.efthemiosprime.pasabayan.features.trips.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripPackageProgressState
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripPackageProgressViewModel

/**
 * Owns the per-trip [TripPackageProgressViewModel] and renders the matching
 * Loaded/Loading/Empty/Error variant. Encapsulates Hilt so [TripCard] can stay
 * stateless for the shipper-browse path that doesn't need progress.
 *
 * iOS parity: `TripCard.swift:257-274` (`packageProgressSection`).
 */
@Composable
fun TripCardProgressSection(
    tripId: Int,
    arrivalDateText: String,
    onTap: () -> Unit,
    onDeliveredHistoryTap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripPackageProgressViewModel = hiltViewModel(key = "trip-progress-$tripId"),
) {
    LaunchedEffect(tripId, arrivalDateText) {
        viewModel.loadMatches(tripId, arrivalDateText)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        TripPackageProgressState.Idle,
        TripPackageProgressState.Loading -> TripPackageProgressSkeleton(
            modifier = modifier.fillMaxWidth(),
        )
        TripPackageProgressState.Empty -> TripPackageProgressEmpty(modifier = modifier)
        is TripPackageProgressState.Loaded -> TripPackageProgressWidget(
            metrics = s.metrics,
            onTap = onTap,
            onDeliveredHistoryTap = onDeliveredHistoryTap,
            modifier = modifier,
        )
        is TripPackageProgressState.Error -> TripPackageProgressError(
            message = s.message,
            onRetry = { viewModel.refresh() },
            modifier = modifier,
        )
    }
}
