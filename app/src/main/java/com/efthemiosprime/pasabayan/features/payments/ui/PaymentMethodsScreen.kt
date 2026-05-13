package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.payments.components.PaymentMethodCard
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsViewModel

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    onViewTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadPaymentMethods() }
    PaymentMethodsContent(
        state = state,
        onBack = onBack,
        onAddCard = { viewModel.showAddCardSheet(true) },
        onSetDefault = { viewModel.setDefaultPaymentMethod(it) },
        onRemove = { viewModel.removePaymentMethod(it) },
        onViewTransactions = onViewTransactions,
        onRetry = { viewModel.loadPaymentMethods() },
        modifier = modifier,
    )
    if (state.showAddCardSheet) {
        AddPaymentMethodSheet(
            flowState = state.addCardFlowState,
            onContinue = { viewModel.prepareAddPaymentMethod() },
            onDismiss = { viewModel.onAddCardCanceled() },
        )
    }
}

@Composable
private fun PaymentMethodsContent(
    state: PaymentMethodsUiState,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onSetDefault: (String) -> Unit,
    onRemove: (String) -> Unit,
    onViewTransactions: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.payments_methods_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.paymentMethods.isEmpty() -> LoadingState(padding)
            state.errorMessage != null && state.paymentMethods.isEmpty() -> ErrorState(
                padding = padding,
                message = state.errorMessage,
                onRetry = onRetry,
            )
            state.paymentMethods.isEmpty() -> EmptyMethodsState(
                padding = padding,
                onAddCard = onAddCard,
            )
            else -> MethodList(
                padding = padding,
                state = state,
                onAddCard = onAddCard,
                onSetDefault = onSetDefault,
                onRemove = onRemove,
                onViewTransactions = onViewTransactions,
            )
        }
    }
}

@Composable
private fun MethodList(
    padding: PaddingValues,
    state: PaymentMethodsUiState,
    onAddCard: () -> Unit,
    onSetDefault: (String) -> Unit,
    onRemove: (String) -> Unit,
    onViewTransactions: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        SecureNoticeCard()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            items(state.paymentMethods, key = { it.id }) { method ->
                PaymentMethodCard(
                    method = method,
                    onSetDefault = { onSetDefault(method.id) },
                    onRemove = { onRemove(method.id) },
                )
            }
        }

        PButton(
            text = stringResource(R.string.payments_methods_add_card),
            onClick = onAddCard,
            modifier = Modifier.fillMaxWidth(),
        )

        PButton(
            text = stringResource(R.string.payments_methods_view_transactions),
            onClick = onViewTransactions,
            style = PButtonStyle.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )

        state.successMessage?.let { msg ->
            Text(
                text = msg,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.Success,
            )
        }
        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SecureNoticeCard() {
    PCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = PasabayanColors.Success,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.payments_methods_secure_notice),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
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
                text = stringResource(R.string.payments_methods_loading),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyMethodsState(padding: PaddingValues, onAddCard: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        SecureNoticeCard()
        PEmptyState(
            icon = Icons.Outlined.CreditCard,
            title = stringResource(R.string.payments_methods_empty_title),
            description = stringResource(R.string.payments_methods_empty_description),
        )
        PButton(
            text = stringResource(R.string.payments_methods_add_card),
            onClick = onAddCard,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ErrorState(
    padding: PaddingValues,
    message: String,
    onRetry: () -> Unit,
) {
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
            text = stringResource(R.string.payments_methods_retry),
            onClick = onRetry,
        )
    }
}

@Preview(showBackground = true, name = "PaymentMethodsScreen list light")
@Preview(showBackground = true, name = "PaymentMethodsScreen list dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentMethodsScreenListPreview() {
    PasabayanTheme {
        PaymentMethodsContent(
            state = PaymentMethodsUiState(
                paymentMethods = listOf(
                    PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, isDefault = true),
                    PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, isDefault = false),
                    PaymentMethodDisplay("pm_3", "amex", "0005", 1, 2030, isDefault = false),
                ),
                defaultPaymentMethodId = "pm_1",
            ),
            onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
            onViewTransactions = {}, onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentMethodsScreen empty light")
@Preview(showBackground = true, name = "PaymentMethodsScreen empty dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentMethodsScreenEmptyPreview() {
    PasabayanTheme {
        PaymentMethodsContent(
            state = PaymentMethodsUiState(),
            onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
            onViewTransactions = {}, onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentMethodsScreen loading")
@Composable
private fun PaymentMethodsScreenLoadingPreview() {
    PasabayanTheme {
        PaymentMethodsContent(
            state = PaymentMethodsUiState(isLoading = true),
            onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
            onViewTransactions = {}, onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentMethodsScreen error")
@Composable
private fun PaymentMethodsScreenErrorPreview() {
    PasabayanTheme {
        PaymentMethodsContent(
            state = PaymentMethodsUiState(errorMessage = "Failed to load — check your connection."),
            onBack = {}, onAddCard = {}, onSetDefault = {}, onRemove = {},
            onViewTransactions = {}, onRetry = {},
        )
    }
}
