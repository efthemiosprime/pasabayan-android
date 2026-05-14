package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.bookings.components.DeliveryHistorySummaryCard
import com.efthemiosprime.pasabayan.features.bookings.components.MatchCard
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.DeliveryHistoryUiState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.DeliveryHistoryViewModel

/**
 * Carrier-side delivered-trips history. iOS parity:
 * `Features/Bookings/Views/History/DeliveryHistoryView.swift`.
 *
 * Calls `bookingsRepository.loadMatches(role="carrier", status="delivered")`
 * via [DeliveryHistoryViewModel] and renders a summary card (count + total
 * earned) plus a list of [MatchCard]s with `isCarrier = true`. Mirrors the
 * shipper `PackageHistoryScreen` exactly, only the labels / colors / role
 * filter and CTA differ. iOS' own TODO ("Consolidate with PackageHistoryView
 * — both share 99% identical code") applies here: we ship two thin screens
 * sharing reused components rather than a polymorphic generic.
 *
 * [onBrowsePackages] dismisses this screen so the host can drive the carrier
 * into the browse-available-packages flow.
 */
@Composable
fun DeliveryHistoryScreen(
    onClose: () -> Unit,
    onViewMatchDetails: (DeliveryMatch) -> Unit,
    onBrowsePackages: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeliveryHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHistory() }
    DeliveryHistoryScreenContent(
        state = state,
        onClose = onClose,
        onRetry = viewModel::refresh,
        onViewMatchDetails = onViewMatchDetails,
        onBrowsePackages = onBrowsePackages,
        modifier = modifier,
    )
}

/**
 * Stateless content layer — kept separate so previews and tests can drive
 * the UI without instantiating a Hilt ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeliveryHistoryScreenContent(
    state: DeliveryHistoryUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onViewMatchDetails: (DeliveryMatch) -> Unit,
    onBrowsePackages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.bookings_history_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.bookings_history_close),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.matches.isEmpty() -> LoadingState(padding)
            state.errorMessage != null && state.matches.isEmpty() ->
                ErrorState(message = state.errorMessage, onRetry = onRetry, padding = padding)
            state.matches.isEmpty() && state.hasLoaded ->
                EmptyState(onBrowsePackages = onBrowsePackages, padding = padding)
            else -> ContentList(
                state = state,
                onViewMatchDetails = onViewMatchDetails,
                padding = padding,
            )
        }
    }
}

@Composable
private fun ContentList(
    state: DeliveryHistoryUiState,
    onViewMatchDetails: (DeliveryMatch) -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        DeliveryHistorySummaryCard(
            deliveredCount = state.matches.size,
            totalEarned = state.totalEarned,
        )
        state.matches.forEach { match ->
            MatchCard(
                match = match,
                isCarrier = true,
                onViewDetails = { onViewMatchDetails(match) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        PCircularProgress()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(60.dp),
        )
        Text(
            text = stringResource(R.string.bookings_history_error_title),
            style = PasabayanTextStyles.Heading.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PButton(text = stringResource(R.string.bookings_history_try_again), onClick = onRetry)
    }
}

@Composable
private fun EmptyState(
    onBrowsePackages: () -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(80.dp),
        )
        Text(
            text = stringResource(R.string.bookings_history_empty_title),
            style = PasabayanTextStyles.Heading.h4,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.bookings_history_empty_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PButton(
            text = stringResource(R.string.bookings_history_browse_packages),
            onClick = onBrowsePackages,
        )
    }
}

@Preview(showBackground = true, name = "DeliveryHistory empty — light", heightDp = 1000)
@Preview(showBackground = true, name = "DeliveryHistory empty — dark", heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeliveryHistoryScreenEmptyPreview() {
    PasabayanTheme {
        DeliveryHistoryScreenContent(
            state = DeliveryHistoryUiState(hasLoaded = true),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onBrowsePackages = {},
        )
    }
}

@Preview(showBackground = true, name = "DeliveryHistory loading")
@Composable
private fun DeliveryHistoryScreenLoadingPreview() {
    PasabayanTheme {
        DeliveryHistoryScreenContent(
            state = DeliveryHistoryUiState(isLoading = true),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onBrowsePackages = {},
        )
    }
}

@Preview(showBackground = true, name = "DeliveryHistory error")
@Composable
private fun DeliveryHistoryScreenErrorPreview() {
    PasabayanTheme {
        DeliveryHistoryScreenContent(
            state = DeliveryHistoryUiState(
                errorMessage = "Unable to load delivery history. Check your connection.",
                hasLoaded = true,
            ),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onBrowsePackages = {},
        )
    }
}
