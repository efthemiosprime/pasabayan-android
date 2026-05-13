package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.AutoChargeConfirmationState

/**
 * Stateless sheet that renders [AutoChargeConfirmationState]. Callbacks
 * route back to the host (typically `MatchListScreen` holding the VM and a
 * `rememberPaymentSheet` instance for the add-card branch).
 *
 * Idle is treated as "don't render" — the host should only mount the sheet
 * when state is non-Idle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoChargeConfirmationSheet(
    state: AutoChargeConfirmationState,
    onConfirm: () -> Unit,
    onAddPaymentMethod: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is AutoChargeConfirmationState.Idle) return
    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            when (state) {
                AutoChargeConfirmationState.Idle -> Unit
                AutoChargeConfirmationState.CheckingPaymentMethod -> CheckingBody()
                is AutoChargeConfirmationState.NeedsPaymentMethod -> NeedsPaymentMethodBody(
                    afterConfirm = false,
                    onAddPaymentMethod = onAddPaymentMethod,
                    onDismiss = onDismiss,
                )
                is AutoChargeConfirmationState.AddingPaymentMethod -> CheckingBody()
                is AutoChargeConfirmationState.ReadyToConfirm -> ReadyToConfirmBody(
                    price = state.price,
                    cardLast4 = state.cardLast4,
                    onConfirm = onConfirm,
                    onUseDifferentCard = onAddPaymentMethod,
                    onDismiss = onDismiss,
                )
                AutoChargeConfirmationState.Confirming -> ConfirmingBody()
                AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm -> NeedsPaymentMethodBody(
                    afterConfirm = true,
                    onAddPaymentMethod = onAddPaymentMethod,
                    onDismiss = onDismiss,
                )
                is AutoChargeConfirmationState.Success -> SuccessBody(
                    queued = state.autoChargeQueued,
                    onDismiss = onDismiss,
                )
                is AutoChargeConfirmationState.Error -> ErrorBody(
                    message = state.message,
                    onRetry = onRetry,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun CheckingBody() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PCircularProgress(modifier = Modifier.size(PasabayanSpacing.xxl))
        Spacer(Modifier.width(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.bookings_auto_charge_checking_pm),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(PasabayanSpacing.sm))
}

@Composable
private fun ConfirmingBody() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PCircularProgress(modifier = Modifier.size(PasabayanSpacing.xxl))
        Spacer(Modifier.width(PasabayanSpacing.md))
        Text(
            text = stringResource(R.string.bookings_auto_charge_confirming),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(PasabayanSpacing.sm))
}

@Composable
private fun NeedsPaymentMethodBody(
    afterConfirm: Boolean,
    onAddPaymentMethod: () -> Unit,
    onDismiss: () -> Unit,
) {
    HeaderRow(
        icon = Icons.Filled.CreditCard,
        tint = PasabayanColors.Warning,
        title = stringResource(
            if (afterConfirm) R.string.bookings_auto_charge_after_confirm_title
            else R.string.bookings_auto_charge_no_pm_title,
        ),
    )
    Text(
        text = stringResource(
            if (afterConfirm) R.string.bookings_auto_charge_after_confirm_body
            else R.string.bookings_auto_charge_no_pm_body,
        ),
        style = PasabayanTextStyles.Body.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_add_pm),
        onClick = onAddPaymentMethod,
        modifier = Modifier.fillMaxWidth(),
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_cancel),
        onClick = onDismiss,
        style = PButtonStyle.Tertiary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReadyToConfirmBody(
    price: Double,
    cardLast4: String?,
    onConfirm: () -> Unit,
    onUseDifferentCard: () -> Unit,
    onDismiss: () -> Unit,
) {
    HeaderRow(
        icon = Icons.Filled.VerifiedUser,
        tint = PasabayanColors.Success,
        title = stringResource(R.string.bookings_auto_charge_ready_title),
    )
    Text(
        text = stringResource(R.string.bookings_auto_charge_amount_label),
        style = PasabayanTextStyles.Caption.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = stringResource(R.string.bookings_auto_charge_amount_format, price),
        style = PasabayanTextStyles.Heading.h3,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
    if (cardLast4 != null) {
        Text(
            text = stringResource(R.string.bookings_auto_charge_card_last4, cardLast4),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        text = stringResource(R.string.bookings_auto_charge_ready_body),
        style = PasabayanTextStyles.Caption.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_confirm_pay),
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_use_different_card),
        onClick = onUseDifferentCard,
        style = PButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth(),
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_cancel),
        onClick = onDismiss,
        style = PButtonStyle.Tertiary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SuccessBody(queued: Boolean, onDismiss: () -> Unit) {
    HeaderRow(
        icon = Icons.Filled.CheckCircle,
        tint = PasabayanColors.Success,
        title = stringResource(R.string.bookings_auto_charge_success_title),
    )
    Text(
        text = stringResource(
            if (queued) R.string.bookings_auto_charge_success_queued
            else R.string.bookings_auto_charge_success_pending_charge,
        ),
        style = PasabayanTextStyles.Body.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_close),
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ErrorBody(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    HeaderRow(
        icon = Icons.Filled.ErrorOutline,
        tint = PasabayanColors.Error,
        title = stringResource(R.string.bookings_auto_charge_error_title),
    )
    Text(
        text = message,
        style = PasabayanTextStyles.Body.regular,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_retry),
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(),
    )
    PButton(
        text = stringResource(R.string.bookings_auto_charge_cancel),
        onClick = onDismiss,
        style = PButtonStyle.Tertiary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun HeaderRow(icon: ImageVector, tint: androidx.compose.ui.graphics.Color, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(PasabayanSpacing.xxxl),
        )
        Spacer(Modifier.width(PasabayanSpacing.md))
        Text(
            text = title,
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ----------------- previews -----------------

@Preview(showBackground = true, name = "AutoCharge — NeedsPM — light")
@Preview(showBackground = true, name = "AutoCharge — NeedsPM — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeNeedsPaymentMethodPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            NeedsPaymentMethodBody(afterConfirm = false, onAddPaymentMethod = {}, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "AutoCharge — ReadyToConfirm — light")
@Preview(showBackground = true, name = "AutoCharge — ReadyToConfirm — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeReadyPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            ReadyToConfirmBody(
                price = 150.00,
                cardLast4 = "4242",
                onConfirm = {},
                onUseDifferentCard = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "AutoCharge — Success queued — light")
@Preview(showBackground = true, name = "AutoCharge — Success queued — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeSuccessPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            SuccessBody(queued = true, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "AutoCharge — AfterConfirm — light")
@Preview(showBackground = true, name = "AutoCharge — AfterConfirm — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeAfterConfirmPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            NeedsPaymentMethodBody(afterConfirm = true, onAddPaymentMethod = {}, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "AutoCharge — Error — light")
@Preview(showBackground = true, name = "AutoCharge — Error — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargeErrorPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg), verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            ErrorBody(message = "Could not charge the card.", onRetry = {}, onDismiss = {})
        }
    }
}
