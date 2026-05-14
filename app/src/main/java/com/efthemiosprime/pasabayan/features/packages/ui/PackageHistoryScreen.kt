package com.efthemiosprime.pasabayan.features.packages.ui

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
import androidx.compose.material.icons.filled.Inventory2
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
import com.efthemiosprime.pasabayan.features.bookings.components.MatchCard
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.packages.components.PackageHistorySummaryCard
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageHistoryUiState
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageHistoryViewModel

/**
 * Shipper-side delivered-packages history. iOS parity:
 * `Features/Packages/Views/PackageHistoryView.swift`.
 *
 * Drives the bookings endpoint with `role=shipper&status=delivered` via
 * [PackageHistoryViewModel] and renders a summary card + list of
 * [MatchCard]s (reused from the bookings feature, ensuring visual parity
 * with delivery history elsewhere in the app).
 *
 * [onViewMatchDetails] is wired to whatever sheet/screen the host wants —
 * typically the shipper match details sheet. [onCreatePackage] is fired
 * from the empty state's CTA so the host can dismiss this screen and
 * launch the create flow.
 */
@Composable
fun PackageHistoryScreen(
    onClose: () -> Unit,
    onViewMatchDetails: (DeliveryMatch) -> Unit,
    onCreatePackage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PackageHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHistory() }
    PackageHistoryScreenContent(
        state = state,
        onClose = onClose,
        onRetry = viewModel::refresh,
        onViewMatchDetails = onViewMatchDetails,
        onCreatePackage = onCreatePackage,
        modifier = modifier,
    )
}

/**
 * Stateless content layer — kept separate so previews and tests can drive
 * the UI without instantiating a Hilt ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PackageHistoryScreenContent(
    state: PackageHistoryUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onViewMatchDetails: (DeliveryMatch) -> Unit,
    onCreatePackage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.packages_history_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.packages_history_close),
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
                EmptyState(onCreatePackage = onCreatePackage, padding = padding)
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
    state: PackageHistoryUiState,
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
        PackageHistorySummaryCard(
            deliveredCount = state.matches.size,
            totalSpent = state.totalSpent,
        )
        state.matches.forEach { match ->
            MatchCard(
                match = match,
                isCarrier = false,
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
            text = stringResource(R.string.packages_history_error_title),
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
        PButton(
            text = stringResource(R.string.packages_history_try_again),
            onClick = onRetry,
        )
    }
}

@Composable
private fun EmptyState(
    onCreatePackage: () -> Unit,
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
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(80.dp),
        )
        Text(
            text = stringResource(R.string.packages_history_empty_title),
            style = PasabayanTextStyles.Heading.h4,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.packages_history_empty_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PButton(
            text = stringResource(R.string.packages_history_create_package),
            onClick = onCreatePackage,
        )
    }
}

@Preview(showBackground = true, name = "History — light", heightDp = 1000)
@Preview(showBackground = true, name = "History — dark", heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageHistoryScreenLoadedPreview() {
    PasabayanTheme {
        PackageHistoryScreenContent(
            state = PackageHistoryUiState(
                matches = emptyList(),
                isLoading = false,
                hasLoaded = true,
            ),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onCreatePackage = {},
        )
    }
}

@Preview(showBackground = true, name = "History (loading) — light")
@Composable
private fun PackageHistoryScreenLoadingPreview() {
    PasabayanTheme {
        PackageHistoryScreenContent(
            state = PackageHistoryUiState(isLoading = true),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onCreatePackage = {},
        )
    }
}

@Preview(showBackground = true, name = "History (error) — light")
@Composable
private fun PackageHistoryScreenErrorPreview() {
    PasabayanTheme {
        PackageHistoryScreenContent(
            state = PackageHistoryUiState(
                errorMessage = "Unable to connect. Check your internet and try again.",
                hasLoaded = true,
            ),
            onClose = {},
            onRetry = {},
            onViewMatchDetails = {},
            onCreatePackage = {},
        )
    }
}
