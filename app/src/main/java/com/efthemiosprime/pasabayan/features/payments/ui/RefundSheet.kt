package com.efthemiosprime.pasabayan.features.payments.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.features.payments.model.refundReasonDisplayText
import com.efthemiosprime.pasabayan.features.payments.viewmodel.RefundUiState

/**
 * Modal bottom sheet for collecting a refund request. State + callbacks are passed
 * in; the host wires them to [RefundViewModel]. Dismissing the sheet does NOT pop
 * the underlying detail screen — back-stack is the host's concern.
 */
@Composable
fun RefundSheet(
    state: RefundUiState,
    transactionId: Int,
    transactionTotal: String,
    onSelectReason: (RefundReason) -> Unit,
    onCustomReasonChange: (String) -> Unit,
    onAdditionalDetailsChange: (String) -> Unit,
    onPartialToggle: (Boolean) -> Unit,
    onPartialAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        RefundSheetContent(
            state = state,
            transactionId = transactionId,
            transactionTotal = transactionTotal,
            onSelectReason = onSelectReason,
            onCustomReasonChange = onCustomReasonChange,
            onAdditionalDetailsChange = onAdditionalDetailsChange,
            onPartialToggle = onPartialToggle,
            onPartialAmountChange = onPartialAmountChange,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun RefundSheetContent(
    state: RefundUiState,
    transactionId: Int,
    transactionTotal: String,
    onSelectReason: (RefundReason) -> Unit,
    onCustomReasonChange: (String) -> Unit,
    onAdditionalDetailsChange: (String) -> Unit,
    onPartialToggle: (Boolean) -> Unit,
    onPartialAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.payments_refund_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.payments_refund_subtitle),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.payments_refund_transaction_info, transactionId, transactionTotal),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RefundTypeSection(
            isPartial = state.isPartialRefund,
            onPartialToggle = onPartialToggle,
        )

        if (state.isPartialRefund) {
            POutlinedTextField(
                value = state.partialAmount,
                onValueChange = onPartialAmountChange,
                label = { Text(stringResource(R.string.payments_refund_partial_amount_label)) },
                placeholder = { Text(stringResource(R.string.payments_refund_partial_amount_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.isPartialRefund && state.partialAmount.isNotBlank() && state.refundAmount == null,
                supportingText = {
                    if (state.isPartialRefund && state.partialAmount.isNotBlank() && state.refundAmount == null) {
                        Text(stringResource(R.string.payments_refund_partial_invalid))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ReasonPicker(
            selectedReason = state.selectedReason,
            onSelectReason = onSelectReason,
        )

        if (state.selectedReason == RefundReason.OTHER) {
            CustomReasonField(
                value = state.customReason,
                onValueChange = onCustomReasonChange,
                state = state,
            )
        }

        POutlinedTextField(
            value = state.additionalDetails,
            onValueChange = onAdditionalDetailsChange,
            label = { Text(stringResource(R.string.payments_refund_additional_details_label)) },
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )

        DisclaimerCard()

        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.Error,
            )
        }

        PButton(
            text = stringResource(R.string.payments_refund_submit),
            onClick = onSubmit,
            isLoading = state.isProcessing,
            enabled = state.isValidRequest && !state.isProcessing,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RefundTypeSection(
    isPartial: Boolean,
    onPartialToggle: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.payments_refund_type_label),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PFilterChip(
                label = stringResource(R.string.payments_refund_type_full),
                selected = !isPartial,
                onClick = { onPartialToggle(false) },
            )
            PFilterChip(
                label = stringResource(R.string.payments_refund_type_partial),
                selected = isPartial,
                onClick = { onPartialToggle(true) },
            )
        }
    }
}

@Composable
private fun ReasonPicker(
    selectedReason: RefundReason?,
    onSelectReason: (RefundReason) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.payments_refund_reason_label),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            items(RefundReason.entries) { reason ->
                PFilterChip(
                    label = refundReasonDisplayText(reason),
                    selected = selectedReason == reason,
                    onClick = { onSelectReason(reason) },
                )
            }
        }
    }
}

@Composable
private fun CustomReasonField(
    value: String,
    onValueChange: (String) -> Unit,
    state: RefundUiState,
) {
    val length = value.length
    val tooShort = state.reasonValidationMessage != null
    POutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.payments_refund_custom_reason_label)) },
        singleLine = false,
        maxLines = 4,
        isError = tooShort,
        supportingText = {
            Text(
                text = if (tooShort) {
                    stringResource(R.string.payments_refund_reason_too_short, RefundUiState.MINIMUM_REASON_LENGTH)
                } else {
                    stringResource(R.string.payments_refund_reason_char_count, length, RefundUiState.MAXIMUM_REASON_LENGTH)
                },
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DisclaimerCard() {
    PCard {
        Text(
            text = stringResource(R.string.payments_refund_disclaimer),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "RefundSheet initial light")
@Preview(showBackground = true, name = "RefundSheet initial dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RefundSheetInitialPreview() {
    PasabayanTheme {
        RefundSheetContent(
            state = RefundUiState(),
            transactionId = 500,
            transactionTotal = "$165.00 CAD",
            onSelectReason = {}, onCustomReasonChange = {},
            onAdditionalDetailsChange = {}, onPartialToggle = {},
            onPartialAmountChange = {}, onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "RefundSheet partial selected light")
@Composable
private fun RefundSheetPartialPreview() {
    PasabayanTheme {
        RefundSheetContent(
            state = RefundUiState(
                isPartialRefund = true,
                partialAmount = "25.00",
                selectedReason = RefundReason.DAMAGED,
            ),
            transactionId = 500,
            transactionTotal = "$165.00 CAD",
            onSelectReason = {}, onCustomReasonChange = {},
            onAdditionalDetailsChange = {}, onPartialToggle = {},
            onPartialAmountChange = {}, onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "RefundSheet OTHER expanded")
@Composable
private fun RefundSheetOtherPreview() {
    PasabayanTheme {
        RefundSheetContent(
            state = RefundUiState(
                selectedReason = RefundReason.OTHER,
                customReason = "Driver was rude during pickup",
            ),
            transactionId = 500,
            transactionTotal = "$165.00 CAD",
            onSelectReason = {}, onCustomReasonChange = {},
            onAdditionalDetailsChange = {}, onPartialToggle = {},
            onPartialAmountChange = {}, onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "RefundSheet OTHER too-short error")
@Composable
private fun RefundSheetTooShortPreview() {
    PasabayanTheme {
        RefundSheetContent(
            state = RefundUiState(
                selectedReason = RefundReason.OTHER,
                customReason = "short",
            ),
            transactionId = 500,
            transactionTotal = "$165.00 CAD",
            onSelectReason = {}, onCustomReasonChange = {},
            onAdditionalDetailsChange = {}, onPartialToggle = {},
            onPartialAmountChange = {}, onSubmit = {},
        )
    }
}
