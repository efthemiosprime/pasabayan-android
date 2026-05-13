package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.features.payments.components.PaymentMethodsSection
import com.efthemiosprime.pasabayan.features.payments.components.PaymentsActivitySection
import com.efthemiosprime.pasabayan.features.payments.components.StripeConnectSection
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.services.PaymentSheetConfigFactory
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentFlowStatus
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentMethodsViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.PaymentViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.ReceiptListViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.RefundUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.RefundViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.StripeConnectViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TippingUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TippingViewModel
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryUiState
import com.efthemiosprime.pasabayan.features.payments.viewmodel.TransactionHistoryViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

private enum class PaymentsRoute {
    PROFILE,
    TRANSACTIONS,
    TRANSACTION_DETAIL,
    TIP_SELECTION,
}

/** Context captured at the moment the user chooses to tip — used by the [TipSelectionScreen] route. */
private data class TipFlowContext(val transactionId: Int, val carrierName: String)

@Composable
fun PaymentsProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPayoutSetup: () -> Unit = {},
    onOpenPaymentMethods: () -> Unit = {},
    onOpenReceipts: () -> Unit = {},
    /**
     * When set (typically from notifications routing — `OpenTransactionDetail(id)`), the screen
     * mounts directly on the transaction detail surface for the given id. iOS parity:
     * `NotificationCenter` → `navigateFromNotification`.
     */
    initialTransactionId: Int? = null,
    onInitialTransactionConsumed: () -> Unit = {},
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    paymentMethodsViewModel: PaymentMethodsViewModel = hiltViewModel(),
    tippingViewModel: TippingViewModel = hiltViewModel(),
    refundViewModel: RefundViewModel = hiltViewModel(),
    transactionHistoryViewModel: TransactionHistoryViewModel = hiltViewModel(),
    receiptListViewModel: ReceiptListViewModel = hiltViewModel(),
    stripeConnectViewModel: StripeConnectViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val methodsState by paymentMethodsViewModel.uiState.collectAsStateWithLifecycle()
    val tippingState by tippingViewModel.uiState.collectAsStateWithLifecycle()
    val refundState by refundViewModel.uiState.collectAsStateWithLifecycle()
    val historyState by transactionHistoryViewModel.uiState.collectAsStateWithLifecycle()
    val receiptState by receiptListViewModel.uiState.collectAsStateWithLifecycle()
    val connectState by stripeConnectViewModel.uiState.collectAsStateWithLifecycle()
    var currentDeliveryMatchId by remember { mutableStateOf<Int?>(null) }
    var route by remember { mutableStateOf(PaymentsRoute.PROFILE) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedTransactionFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    // Set only when entering the detail surface via deep-link (no `selectedTransaction`).
    var deepLinkedTransactionId by remember { mutableStateOf<Int?>(null) }
    // Overlay state — when non-null, a RefundSheet renders on top of the current route.
    var refundSheetForTransaction by remember { mutableStateOf<Transaction?>(null) }
    // Captured when entering TIP_SELECTION; carries id + carrier name for header copy.
    var tipFlowContext by remember { mutableStateOf<TipFlowContext?>(null) }

    LaunchedEffect(initialTransactionId) {
        val target = initialTransactionId ?: return@LaunchedEffect
        deepLinkedTransactionId = target
        selectedTransactionFilter = TransactionFilter.ALL
        route = PaymentsRoute.TRANSACTION_DETAIL
        onInitialTransactionConsumed()
    }

    // [rememberPaymentSheet] registers the activity-result launcher during composition setup;
    // constructing [PaymentSheet] with an Activity inside [remember] crashes with
    // "LifecycleOwner is attempting to register while current state is RESUMED" once the
    // composable mounts after onStart.
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                val deliveryMatchId = currentDeliveryMatchId ?: return@rememberPaymentSheet
                paymentViewModel.confirmCapture(deliveryMatchId)
            }
            is PaymentSheetResult.Canceled -> paymentViewModel.onPaymentSheetCanceled()
            is PaymentSheetResult.Failed -> paymentViewModel.onPaymentSheetFailed(result.error.localizedMessage)
        }
    }

    LaunchedEffect(Unit) {
        paymentMethodsViewModel.loadPaymentMethods()
        transactionHistoryViewModel.loadTransactions()
        receiptListViewModel.loadReceipts()
        stripeConnectViewModel.loadStatus()
    }
    LaunchedEffect(paymentState.publicKey) {
        val key = paymentState.publicKey
        if (!key.isNullOrBlank()) {
            PaymentConfiguration.init(context, key)
        }
    }

    when (route) {
        PaymentsRoute.PROFILE -> {
            PaymentsProfileContent(
                methodsState = methodsState,
                historyState = historyState,
                receiptState = receiptState,
                connectState = connectState,
                onSetDefaultMethod = { methodId -> paymentMethodsViewModel.setDefaultPaymentMethod(methodId) },
                onRemoveMethod = { methodId -> paymentMethodsViewModel.removePaymentMethod(methodId) },
                onManagePaymentMethods = onOpenPaymentMethods,
                onOpenTransactionList = { route = PaymentsRoute.TRANSACTIONS },
                onOpenTransaction = { transaction ->
                    selectedTransaction = transaction
                    route = PaymentsRoute.TRANSACTION_DETAIL
                },
                onOpenReceipts = onOpenReceipts,
                onOpenPayoutSetup = onOpenPayoutSetup,
                onRefresh = {
                    paymentMethodsViewModel.loadPaymentMethods()
                    transactionHistoryViewModel.refreshTransactions()
                    receiptListViewModel.refresh()
                    stripeConnectViewModel.loadStatus(forceRefresh = true)
                },
                onLogout = onLogout,
                modifier = modifier,
            )
        }

        PaymentsRoute.TRANSACTIONS -> {
            TransactionHistoryScreen(
                onBack = { route = PaymentsRoute.PROFILE },
                onOpenTransaction = { transaction, filter ->
                    selectedTransaction = transaction
                    selectedTransactionFilter = filter
                    route = PaymentsRoute.TRANSACTION_DETAIL
                },
                modifier = modifier,
                viewModel = transactionHistoryViewModel,
            )
        }

        PaymentsRoute.TRANSACTION_DETAIL -> {
            // Prefer the deep-linked id (set on initialTransactionId arrival); fall back to the
            // transaction the user picked from the in-screen list. If neither is set, bounce to
            // the transactions list rather than rendering an empty detail surface.
            val targetTransactionId = deepLinkedTransactionId ?: selectedTransaction?.id
            if (targetTransactionId == null) {
                route = PaymentsRoute.TRANSACTIONS
            } else {
                TransactionDetailScreen(
                    transactionId = targetTransactionId,
                    filter = selectedTransactionFilter,
                    onBack = {
                        deepLinkedTransactionId = null
                        route = PaymentsRoute.TRANSACTIONS
                    },
                    onRequestRefund = { transactionId ->
                        refundViewModel.reset()
                        refundSheetForTransaction = selectedTransaction?.takeIf { it.id == transactionId }
                            ?: Transaction(id = transactionId)
                    },
                    onAddTip = { transactionId ->
                        val carrierName = selectedTransaction?.takeIf { it.id == transactionId }
                            ?.carrier?.name.orEmpty()
                        tippingViewModel.reset()
                        tipFlowContext = TipFlowContext(transactionId, carrierName)
                        route = PaymentsRoute.TIP_SELECTION
                    },
                    modifier = modifier,
                )
            }
        }

        PaymentsRoute.TIP_SELECTION -> {
            val ctx = tipFlowContext
            if (ctx == null) {
                route = PaymentsRoute.TRANSACTION_DETAIL
            } else {
                TipSelectionScreen(
                    state = tippingState,
                    carrierName = ctx.carrierName,
                    onPresetTip = { tippingViewModel.selectPresetTip(it) },
                    onCustomTip = { tippingViewModel.setCustomTipAmount(it) },
                    onSubmit = { tippingViewModel.addTip(ctx.transactionId) },
                    onSkip = {
                        tipFlowContext = null
                        route = PaymentsRoute.TRANSACTION_DETAIL
                    },
                    onBack = {
                        tipFlowContext = null
                        route = PaymentsRoute.TRANSACTION_DETAIL
                    },
                    modifier = modifier,
                )
            }
        }
    }

    // Refund sheet overlays whatever route is currently rendered (typically TRANSACTION_DETAIL).
    refundSheetForTransaction?.let { tx ->
        RefundSheet(
            state = refundState,
            transactionId = tx.id,
            transactionTotal = tx.formattedTotal,
            onSelectReason = { refundViewModel.selectReason(it) },
            onCustomReasonChange = { refundViewModel.setCustomReason(it) },
            onAdditionalDetailsChange = { refundViewModel.setAdditionalDetails(it) },
            onPartialToggle = { refundViewModel.setPartialRefund(it) },
            onPartialAmountChange = { refundViewModel.setPartialAmount(it) },
            onSubmit = { refundViewModel.submitRefundRequest(tx.id) },
            onDismiss = { refundSheetForTransaction = null },
        )
    }
    // Close the sheet on successful submission.
    LaunchedEffect(refundState.refundSuccess) {
        if (refundState.refundSuccess) refundSheetForTransaction = null
    }
}

@Composable
private fun PaymentsProfileContent(
    methodsState: PaymentMethodsUiState,
    historyState: TransactionHistoryUiState,
    receiptState: ReceiptListUiState,
    connectState: StripeConnectUiState,
    onSetDefaultMethod: (String) -> Unit,
    onRemoveMethod: (String) -> Unit,
    onManagePaymentMethods: () -> Unit,
    onOpenTransactionList: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    onOpenReceipts: () -> Unit,
    onOpenPayoutSetup: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.payments_profile_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PButton(
                text = stringResource(R.string.payments_profile_refresh),
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
            )
            PButton(
                text = stringResource(R.string.auth_sign_out),
                onClick = onLogout,
                modifier = Modifier.weight(1f),
            )
        }

        PaymentMethodsSection(
            methodsState = methodsState,
            onSetDefaultMethod = onSetDefaultMethod,
            onRemoveMethod = onRemoveMethod,
            onManagePaymentMethods = onManagePaymentMethods,
        )
        PaymentsActivitySection(
            historyState = historyState,
            receiptState = receiptState,
            onOpenTransactionList = onOpenTransactionList,
            onOpenTransaction = onOpenTransaction,
            onOpenReceipts = onOpenReceipts,
        )
        StripeConnectSection(
            connectState = connectState,
            onOpenPayoutSetup = onOpenPayoutSetup,
        )
    }
}

@Preview(showBackground = true, name = "PaymentsProfile light")
@Preview(showBackground = true, name = "PaymentsProfile dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentsProfilePreview() {
    PasabayanTheme {
        PaymentsProfileContent(
            methodsState = PaymentMethodsUiState(
                paymentMethods = listOf(
                    PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, true),
                    PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
                ),
                defaultPaymentMethodId = "pm_1",
            ),
            historyState = TransactionHistoryUiState(
                transactions = listOf(
                    Transaction(
                        id = 1,
                        transactionStatus = TransactionStatus.COMPLETED,
                        deliveryMatchId = 1,
                        amounts = com.efthemiosprime.pasabayan.features.payments.model.TransactionAmounts(total = 100.0),
                    ),
                ),
            ),
            receiptState = ReceiptListUiState(
                receipts = listOf(
                    PaymentReceipt(
                        id = 1,
                        receiptNumber = "RCP-1",
                        date = "2026-01-01",
                        dateFormatted = "Jan 1, 2026",
                        role = "shipper",
                        otherParty = com.efthemiosprime.pasabayan.features.payments.model.OtherParty(id = 1, name = "Carrier"),
                        amount = com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptAmount(total = 100.0),
                        delivery = com.efthemiosprime.pasabayan.features.payments.model.PaymentReceiptDelivery(
                            pickupCity = "Toronto",
                            deliveryCity = "Montreal",
                        ),
                        status = "completed",
                    ),
                ),
                hasMore = true,
                totalCount = 10,
            ),
            connectState = StripeConnectUiState(),
            onSetDefaultMethod = {},
            onRemoveMethod = {},
            onManagePaymentMethods = {},
            onOpenTransactionList = {},
            onOpenTransaction = {},
            onOpenReceipts = {},
            onOpenPayoutSetup = {},
            onRefresh = {},
            onLogout = {},
            modifier = Modifier.fillMaxSize().padding(8.dp),
        )
    }
}


