package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.payments.components.ReceiptRow
import com.efthemiosprime.pasabayan.features.payments.model.OtherParty
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptAmount
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptDelivery
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReceiptListScreen(
    onBack: () -> Unit,
    onOpenReceipt: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadReceipts() }
    ReceiptListContent(
        state = state,
        onBack = onBack,
        onOpenReceipt = onOpenReceipt,
        onLoadMore = { viewModel.loadMore() },
        onRetry = { viewModel.loadReceipts() },
        modifier = modifier,
    )
}

@Composable
private fun ReceiptListContent(
    state: ReceiptListUiState,
    onBack: () -> Unit,
    onOpenReceipt: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.payments_receipts_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.receipts.isEmpty() -> Loading(padding)
            state.error != null && state.receipts.isEmpty() -> ErrorState(padding, state.error, onRetry)
            state.receipts.isEmpty() -> Empty(padding)
            else -> ReceiptsList(
                padding = padding,
                state = state,
                onOpenReceipt = onOpenReceipt,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Composable
private fun ReceiptsList(
    padding: PaddingValues,
    state: ReceiptListUiState,
    onOpenReceipt: (Int) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Auto-paginate when the user is near the end (within 3 items of the last loaded one).
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            state.hasMore && !state.isLoadingMore && last >= total - 3
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }.collectLatest { if (it) onLoadMore() }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.payments_receipts_count, state.receipts.size, state.totalCount),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            modifier = Modifier.weight(1f),
        ) {
            items(state.receipts, key = { it.id }) { receipt ->
                ReceiptRow(receipt = receipt, onClick = { onOpenReceipt(receipt.id) })
            }
            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PasabayanSpacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        PCircularProgress()
                    }
                }
            }
        }
        if (state.hasMore && !state.isLoadingMore) {
            PButton(
                text = stringResource(R.string.payments_receipts_load_more),
                onClick = onLoadMore,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Loading(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PCircularProgress()
            Text(
                text = stringResource(R.string.payments_receipts_loading),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Empty(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PEmptyState(
            icon = Icons.Outlined.ReceiptLong,
            title = stringResource(R.string.payments_receipts_empty_title),
            description = stringResource(R.string.payments_receipts_empty_description),
        )
    }
}

@Composable
private fun ErrorState(padding: PaddingValues, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.error,
        )
        PButton(
            text = stringResource(R.string.payments_receipts_retry),
            onClick = onRetry,
        )
    }
}

private fun sampleReceipt(id: Int) = PaymentReceipt(
    id = id,
    receiptNumber = "RCP-${1000 + id}",
    date = "2026-05-${10 + id}T10:00:00Z",
    dateFormatted = "May ${10 + id}, 2026",
    role = if (id % 2 == 0) "shipper" else "carrier",
    otherParty = OtherParty(id = id, name = "Sample Carrier"),
    amount = PaymentReceiptAmount(total = 100.0 + id, currency = "cad", tip = if (id % 3 == 0) 5.0 else 0.0),
    delivery = PaymentReceiptDelivery(pickupCity = "Toronto", deliveryCity = "Montreal"),
    status = "completed",
)

@Preview(showBackground = true, name = "ReceiptListScreen list light")
@Preview(showBackground = true, name = "ReceiptListScreen list dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptListScreenListPreview() {
    PasabayanTheme {
        ReceiptListContent(
            state = ReceiptListUiState(
                receipts = (1..5).map { sampleReceipt(it) },
                hasMore = true,
                totalCount = 12,
            ),
            onBack = {}, onOpenReceipt = {}, onLoadMore = {}, onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "ReceiptListScreen empty")
@Composable
private fun ReceiptListScreenEmptyPreview() {
    PasabayanTheme {
        ReceiptListContent(
            state = ReceiptListUiState(),
            onBack = {}, onOpenReceipt = {}, onLoadMore = {}, onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "ReceiptListScreen error")
@Composable
private fun ReceiptListScreenErrorPreview() {
    PasabayanTheme {
        ReceiptListContent(
            state = ReceiptListUiState(error = "Unable to load receipts."),
            onBack = {}, onOpenReceipt = {}, onLoadMore = {}, onRetry = {},
        )
    }
}
