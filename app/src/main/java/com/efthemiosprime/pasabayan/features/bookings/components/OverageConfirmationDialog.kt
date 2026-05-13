package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog
import com.efthemiosprime.pasabayan.features.bookings.model.OverageConfirmationData

/**
 * "Accept Anyway?" confirmation rendered when the user taps Accept on an
 * over-capacity match. Wraps [PAlertDialog] — copy varies by role
 * (carrier-accepting vs shipper-accepting) per the advisory-weight policy.
 *
 * Stateless: the host binds it to [com.efthemiosprime.pasabayan.features.bookings.viewmodel.MatchingUiState.pendingOverageConfirmation]
 * and routes [onConfirm] / [onDismiss] back to the ViewModel.
 */
@Composable
fun OverageConfirmationDialog(
    data: OverageConfirmationData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val packageLabel = stringResource(R.string.matching_overage_kg_format, data.packageWeightKg)
    val availableLabel = stringResource(R.string.matching_overage_kg_format, data.availableWeightKg)
    val overageLabel = stringResource(R.string.matching_overage_kg_format, data.overageKg)
    val body = if (data.isCarrierAccepting) {
        stringResource(
            R.string.matching_overage_confirm_body_carrier,
            packageLabel,
            availableLabel,
            overageLabel,
        )
    } else {
        stringResource(
            R.string.matching_overage_confirm_body_shipper,
            packageLabel,
            availableLabel,
            overageLabel,
        )
    }
    PAlertDialog(
        title = stringResource(R.string.matching_overage_confirm_title),
        message = body,
        confirmText = stringResource(R.string.matching_overage_confirm_accept),
        dismissText = stringResource(R.string.matching_overage_confirm_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = Icons.Filled.WarningAmber,
        iconTint = PasabayanColors.Warning,
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "OverageConfirm — carrier — light")
@Preview(showBackground = true, name = "OverageConfirm — carrier — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverageConfirmationCarrierPreview() {
    PasabayanTheme {
        OverageConfirmationDialog(
            data = OverageConfirmationData(
                matchId = 1,
                isCarrierAccepting = true,
                packageWeightKg = 2.0,
                availableWeightKg = 1.0,
                overageKg = 1.0,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "OverageConfirm — shipper — light")
@Preview(showBackground = true, name = "OverageConfirm — shipper — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverageConfirmationShipperPreview() {
    PasabayanTheme {
        OverageConfirmationDialog(
            data = OverageConfirmationData(
                matchId = 1,
                isCarrierAccepting = false,
                packageWeightKg = 5.5,
                availableWeightKg = 2.0,
                overageKg = 3.5,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
