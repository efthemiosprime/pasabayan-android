package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptDetailUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptDetailViewModel

@Composable
fun ReceiptDetailScreen(
    transactionId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(transactionId) { viewModel.loadReceipt(transactionId) }
    ReceiptDetailContent(state = state, onBack = onBack, modifier = modifier)
}

@Composable
private fun ReceiptDetailContent(
    state: ReceiptDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.payments_receipt_share_chooser_title)
    val onShare: (() -> Unit)? = state.receiptUrl?.let { url ->
        {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            context.startActivity(Intent.createChooser(intent, shareChooserTitle))
        }
    }

    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.payments_receipt_detail_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (onShare != null) {
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Outlined.IosShare,
                                contentDescription = stringResource(R.string.payments_receipt_share),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Loading(padding)
            state.errorMessage != null -> ErrorState(padding, state.errorMessage)
            state.receiptUrl != null -> ReceiptWebView(
                url = state.receiptUrl,
                modifier = Modifier.padding(padding),
            )
            else -> Unavailable(padding)
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
        PCircularProgress()
    }
}

@Composable
private fun ErrorState(padding: PaddingValues, message: String) {
    Box(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun Unavailable(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.payments_receipt_unavailable),
            style = PasabayanTextStyles.Body.medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "ReceiptDetailScreen loading")
@Composable
private fun ReceiptDetailScreenLoadingPreview() {
    PasabayanTheme {
        ReceiptDetailContent(
            state = ReceiptDetailUiState(isLoading = true),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "ReceiptDetailScreen unavailable")
@Composable
private fun ReceiptDetailScreenUnavailablePreview() {
    PasabayanTheme {
        ReceiptDetailContent(state = ReceiptDetailUiState(), onBack = {})
    }
}

@Preview(showBackground = true, name = "ReceiptDetailScreen error")
@Composable
private fun ReceiptDetailScreenErrorPreview() {
    PasabayanTheme {
        ReceiptDetailContent(
            state = ReceiptDetailUiState(errorMessage = "Could not load receipt."),
            onBack = {},
        )
    }
}
