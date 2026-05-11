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
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.features.payments.components.PaymentMethodsSection
import com.efthemiosprime.pasabayan.features.payments.components.PaymentProcessingSection
import com.efthemiosprime.pasabayan.features.payments.components.PaymentsActivitySection
import com.efthemiosprime.pasabayan.features.payments.components.StripeConnectSection
import com.efthemiosprime.pasabayan.features.payments.components.TippingRefundSection
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
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
}

@Composable
fun PaymentsProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPayoutSetup: () -> Unit = {},
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
                paymentState = paymentState,
                methodsState = methodsState,
                tippingState = tippingState,
                refundState = refundState,
                historyState = historyState,
                receiptState = receiptState,
                connectState = connectState,
                onCreatePayment = { id, amount ->
                    currentDeliveryMatchId = id
                    paymentViewModel.createPayment(id, amount)
                },
                onConfirmCapture = { id -> paymentViewModel.confirmCapture(id) },
                onPresentPaymentSheet = {
                    val secret = paymentState.clientSecret ?: return@PaymentsProfileContent
                    if (paymentViewModel.isMockClientSecret(secret)) {
                        val deliveryMatchId = currentDeliveryMatchId ?: return@PaymentsProfileContent
                        paymentViewModel.confirmCapture(deliveryMatchId)
                        return@PaymentsProfileContent
                    }
                    val configuration = buildPaymentSheetConfiguration(paymentState)
                    paymentSheet.presentWithPaymentIntent(secret, configuration)
                },
                onAddTip = { transactionId, amount ->
                    tippingViewModel.setCustomTipAmount(amount.toString())
                    tippingViewModel.addTip(transactionId)
                },
                onRequestRefund = { transactionId, reason ->
                    refundViewModel.selectReason(RefundReason.OTHER)
                    refundViewModel.setCustomReason(reason)
                    refundViewModel.submitRefundRequest(transactionId)
                },
                onSetDefaultMethod = { methodId -> paymentMethodsViewModel.setDefaultPaymentMethod(methodId) },
                onLoadMoreReceipts = { receiptListViewModel.loadMore() },
                onOpenTransactionList = { route = PaymentsRoute.TRANSACTIONS },
                onOpenTransaction = { transaction ->
                    selectedTransaction = transaction
                    route = PaymentsRoute.TRANSACTION_DETAIL
                },
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
            val transaction = selectedTransaction
            if (transaction == null) {
                route = PaymentsRoute.TRANSACTIONS
            } else {
                TransactionDetailScreen(
                    transactionId = transaction.id,
                    filter = selectedTransactionFilter,
                    onBack = { route = PaymentsRoute.TRANSACTIONS },
                    onRequestRefund = { transactionId ->
                        refundViewModel.selectReason(RefundReason.OTHER)
                        refundViewModel.setCustomReason("Requesting refund for transaction issue")
                        refundViewModel.submitRefundRequest(transactionId)
                    },
                    onAddTip = { transactionId ->
                        tippingViewModel.selectPresetTip(TippingUiState.PRESET_TIPS[1])
                        tippingViewModel.addTip(transactionId)
                    },
                    modifier = modifier,
                )
            }
        }

    }
}

@Composable
private fun PaymentsProfileContent(
    paymentState: PaymentUiState,
    methodsState: PaymentMethodsUiState,
    tippingState: TippingUiState,
    refundState: RefundUiState,
    historyState: TransactionHistoryUiState,
    receiptState: ReceiptListUiState,
    connectState: StripeConnectUiState,
    onCreatePayment: (Int, Double) -> Unit,
    onConfirmCapture: (Int) -> Unit,
    onPresentPaymentSheet: () -> Unit,
    onAddTip: (Int, Double) -> Unit,
    onRequestRefund: (Int, String) -> Unit,
    onSetDefaultMethod: (String) -> Unit,
    onLoadMoreReceipts: () -> Unit,
    onOpenTransactionList: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    onOpenPayoutSetup: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paymentStatusText = paymentStatusText(paymentState)

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

        PaymentProcessingSection(
            paymentState = paymentState,
            statusText = paymentStatusText,
            onCreatePayment = onCreatePayment,
            onPresentPaymentSheet = onPresentPaymentSheet,
            onConfirmCapture = onConfirmCapture,
        )
        PaymentMethodsSection(
            methodsState = methodsState,
            onSetDefaultMethod = onSetDefaultMethod,
        )
        TippingRefundSection(
            tippingState = tippingState,
            refundState = refundState,
            onAddTip = onAddTip,
            onRequestRefund = onRequestRefund,
        )
        PaymentsActivitySection(
            historyState = historyState,
            receiptState = receiptState,
            onOpenTransactionList = onOpenTransactionList,
            onOpenTransaction = onOpenTransaction,
            onLoadMoreReceipts = onLoadMoreReceipts,
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
            paymentState = PaymentUiState(paymentSuccess = true),
            methodsState = PaymentMethodsUiState(
                paymentMethods = listOf(
                    PaymentMethodDisplay("pm_1", "visa", "4242", 12, 2028, true),
                    PaymentMethodDisplay("pm_2", "mastercard", "5555", 6, 2027, false),
                ),
                defaultPaymentMethodId = "pm_1",
            ),
            tippingState = TippingUiState(),
            refundState = RefundUiState(),
            historyState = TransactionHistoryUiState(
                transactions = listOf(
                    Transaction(
                        id = 1,
                        transactionStatus = TransactionStatus.COMPLETED,
                        deliveryMatchId = 1,
                        description = null,
                        shipperName = null,
                        carrierName = null,
                        totalAmount = 100.0,
                        subtotal = null,
                        platformFee = null,
                        carrierReceives = null,
                        currency = "cad",
                        tipAmount = null,
                        clientSecret = null,
                        customerId = null,
                        ephemeralKey = null,
                        payoutStatus = null,
                        createdAt = null,
                        updatedAt = null,
                    ),
                ),
            ),
            receiptState = ReceiptListUiState(
                receipts = listOf(
                    PaymentReceipt(
                        id = 1,
                        receiptNumber = "RCP-1",
                        receiptUrl = null,
                        date = "2026-01-01",
                        dateFormatted = "Jan 1, 2026",
                        role = "shipper",
                        otherPartyName = "Carrier",
                        totalAmount = 100.0,
                        carrierAmount = null,
                        platformFee = null,
                        tipAmount = null,
                        currency = "cad",
                        status = "completed",
                        pickupCity = "Toronto",
                        deliveryCity = "Montreal",
                        packageTitle = null,
                    ),
                ),
                hasMore = true,
                totalCount = 10,
            ),
            connectState = StripeConnectUiState(),
            onCreatePayment = { _, _ -> },
            onConfirmCapture = {},
            onPresentPaymentSheet = {},
            onAddTip = { _, _ -> },
            onRequestRefund = { _, _ -> },
            onSetDefaultMethod = {},
            onLoadMoreReceipts = {},
            onOpenTransactionList = {},
            onOpenTransaction = {},
            onOpenPayoutSetup = {},
            onRefresh = {},
            onLogout = {},
            modifier = Modifier.fillMaxSize().padding(8.dp),
        )
    }
}

@Composable
private fun paymentStatusText(paymentState: PaymentUiState): String {
    return when {
        paymentState.isProcessing -> stringResource(R.string.payments_profile_status_processing)
        paymentState.flowStatus == PaymentFlowStatus.SHEET_READY -> stringResource(R.string.payments_profile_status_sheet_ready)
        paymentState.flowStatus == PaymentFlowStatus.PAYMENT_CANCELED -> stringResource(R.string.payments_profile_status_canceled)
        paymentState.flowStatus == PaymentFlowStatus.PAYMENT_SUCCESS -> stringResource(R.string.payments_profile_status_paid)
        paymentState.flowStatus == PaymentFlowStatus.ALREADY_PAID -> stringResource(R.string.payments_profile_status_already_paid)
        else -> stringResource(R.string.payments_profile_idle)
    }
}

private fun buildPaymentSheetConfiguration(paymentState: PaymentUiState): PaymentSheet.Configuration {
    val customerId = paymentState.customerId
    val ephemeralKey = paymentState.ephemeralKey
    val customerConfig =
        if (!customerId.isNullOrBlank() && !ephemeralKey.isNullOrBlank()) {
            PaymentSheet.CustomerConfiguration(
                id = customerId,
                ephemeralKeySecret = ephemeralKey,
            )
        } else {
            null
        }
    return if (customerConfig != null) {
        val googlePayConfig = PaymentSheet.GooglePayConfiguration(
            environment = if (paymentState.stripeIsSandbox) {
                PaymentSheet.GooglePayConfiguration.Environment.Test
            } else {
                PaymentSheet.GooglePayConfiguration.Environment.Production
            },
            countryCode = "CA",
            currencyCode = paymentState.stripeCurrencyCode,
        )
        PaymentSheet.Configuration(
            "Pasabayan",
            customerConfig,
            googlePayConfig,
        )
    } else {
        PaymentSheet.Configuration("Pasabayan")
    }
}

