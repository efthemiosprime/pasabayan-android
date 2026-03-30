package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
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

@Composable
fun PaymentsProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    paymentMethodsViewModel: PaymentMethodsViewModel = hiltViewModel(),
    tippingViewModel: TippingViewModel = hiltViewModel(),
    refundViewModel: RefundViewModel = hiltViewModel(),
    transactionHistoryViewModel: TransactionHistoryViewModel = hiltViewModel(),
    receiptListViewModel: ReceiptListViewModel = hiltViewModel(),
    stripeConnectViewModel: StripeConnectViewModel = hiltViewModel(),
) {
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val methodsState by paymentMethodsViewModel.uiState.collectAsStateWithLifecycle()
    val tippingState by tippingViewModel.uiState.collectAsStateWithLifecycle()
    val refundState by refundViewModel.uiState.collectAsStateWithLifecycle()
    val historyState by transactionHistoryViewModel.uiState.collectAsStateWithLifecycle()
    val receiptState by receiptListViewModel.uiState.collectAsStateWithLifecycle()
    val connectState by stripeConnectViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        paymentMethodsViewModel.loadPaymentMethods()
        transactionHistoryViewModel.loadTransactions()
        receiptListViewModel.loadReceipts()
        stripeConnectViewModel.loadStatus()
    }

    PaymentsProfileContent(
        paymentState = paymentState,
        methodsState = methodsState,
        tippingState = tippingState,
        refundState = refundState,
        historyState = historyState,
        receiptState = receiptState,
        connectState = connectState,
        onCreatePayment = { id, amount -> paymentViewModel.createPayment(id, amount) },
        onConfirmCapture = { id -> paymentViewModel.confirmCapture(id) },
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
        onStartOnboarding = { stripeConnectViewModel.startOnboarding() },
        onOpenDashboard = { stripeConnectViewModel.openDashboard() },
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
    onAddTip: (Int, Double) -> Unit,
    onRequestRefund: (Int, String) -> Unit,
    onSetDefaultMethod: (String) -> Unit,
    onLoadMoreReceipts: () -> Unit,
    onStartOnboarding: () -> Unit,
    onOpenDashboard: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var paymentMatchId by remember { mutableStateOf("0") }
    var paymentAmount by remember { mutableStateOf("0.0") }
    var tipTransactionId by remember { mutableStateOf("0") }
    var tipAmount by remember { mutableStateOf("0.0") }
    var refundTransactionId by remember { mutableStateOf("0") }
    var refundReason by remember { mutableStateOf("") }

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

        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(stringResource(R.string.payments_profile_payment_section), style = PasabayanTextStyles.Heading.h5)
                POutlinedTextField(
                    value = paymentMatchId,
                    onValueChange = { paymentMatchId = it },
                    label = { Text(stringResource(R.string.payments_profile_delivery_match_id)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    label = { Text(stringResource(R.string.payments_profile_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PButton(
                        text = stringResource(R.string.payments_profile_create_payment),
                        onClick = {
                            val id = paymentMatchId.toIntOrNull() ?: return@PButton
                            val amount = paymentAmount.toDoubleOrNull() ?: return@PButton
                            onCreatePayment(id, amount)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    PButton(
                        text = stringResource(R.string.payments_profile_confirm_capture),
                        onClick = {
                            val id = paymentMatchId.toIntOrNull() ?: return@PButton
                            onConfirmCapture(id)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = paymentState.errorMessage ?: if (paymentState.paymentSuccess) {
                        stringResource(R.string.payments_profile_success)
                    } else {
                        stringResource(R.string.payments_profile_idle)
                    },
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }

        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(stringResource(R.string.payments_profile_methods_section), style = PasabayanTextStyles.Heading.h5)
                methodsState.paymentMethods.take(3).forEach { method ->
                    Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                        Text(
                            text = method.displayName,
                            modifier = Modifier.weight(1f),
                            style = PasabayanTextStyles.Body.medium,
                        )
                        if (!method.isDefault) {
                            PButton(
                                text = stringResource(R.string.payments_profile_set_default),
                                onClick = { onSetDefaultMethod(method.id) },
                            )
                        }
                    }
                }
            }
        }

        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(stringResource(R.string.payments_profile_tipping_refund_section), style = PasabayanTextStyles.Heading.h5)
                POutlinedTextField(
                    value = tipTransactionId,
                    onValueChange = { tipTransactionId = it },
                    label = { Text(stringResource(R.string.payments_profile_transaction_id)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = tipAmount,
                    onValueChange = { tipAmount = it },
                    label = { Text(stringResource(R.string.payments_profile_tip_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                PButton(
                    text = stringResource(R.string.payments_profile_add_tip),
                    onClick = {
                        val id = tipTransactionId.toIntOrNull() ?: return@PButton
                        val amount = tipAmount.toDoubleOrNull() ?: return@PButton
                        onAddTip(id, amount)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = refundTransactionId,
                    onValueChange = { refundTransactionId = it },
                    label = { Text(stringResource(R.string.payments_profile_refund_transaction_id)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = refundReason,
                    onValueChange = { refundReason = it },
                    label = { Text(stringResource(R.string.payments_profile_refund_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                PButton(
                    text = stringResource(R.string.payments_profile_request_refund),
                    onClick = {
                        val id = refundTransactionId.toIntOrNull() ?: return@PButton
                        if (refundReason.isBlank()) return@PButton
                        onRequestRefund(id, refundReason)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = tippingState.errorMessage ?: refundState.errorMessage ?: stringResource(R.string.payments_profile_idle),
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }

        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(stringResource(R.string.payments_profile_activity_section), style = PasabayanTextStyles.Heading.h5)
                Text(
                    text = stringResource(R.string.payments_profile_transactions_count, historyState.transactions.size),
                    style = PasabayanTextStyles.Body.small,
                )
                Text(
                    text = stringResource(R.string.payments_profile_receipts_count, receiptState.receipts.size, receiptState.totalCount),
                    style = PasabayanTextStyles.Body.small,
                )
                if (receiptState.hasMore) {
                    PButton(
                        text = stringResource(R.string.payments_profile_load_more_receipts),
                        onClick = onLoadMoreReceipts,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(stringResource(R.string.payments_profile_connect_section), style = PasabayanTextStyles.Heading.h5)
                Text(
                    text = if (connectState.status?.onboardingComplete == true) {
                        stringResource(R.string.payments_profile_connect_ready)
                    } else {
                        stringResource(R.string.payments_profile_connect_not_ready)
                    },
                    style = PasabayanTextStyles.Body.small,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PButton(
                        text = stringResource(R.string.payments_profile_start_onboarding),
                        onClick = onStartOnboarding,
                        modifier = Modifier.weight(1f),
                    )
                    PButton(
                        text = stringResource(R.string.payments_profile_open_dashboard),
                        onClick = onOpenDashboard,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
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
            onAddTip = { _, _ -> },
            onRequestRefund = { _, _ -> },
            onSetDefaultMethod = {},
            onLoadMoreReceipts = {},
            onStartOnboarding = {},
            onOpenDashboard = {},
            onRefresh = {},
            onLogout = {},
        )
    }
}
