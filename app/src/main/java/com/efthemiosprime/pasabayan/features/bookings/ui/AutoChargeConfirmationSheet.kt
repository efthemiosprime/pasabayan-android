package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoChargeConfirmationSheet(
    amount: Double,
    currency: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.bookings_auto_charge_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.bookings_auto_charge_description),
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PDetailRow(
                label = stringResource(R.string.bookings_auto_charge_amount),
                value = String.format("$%.2f %s", amount, currency),
            )
            PButton(
                text = stringResource(R.string.bookings_auto_charge_confirm),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.bookings_action_cancel),
                onClick = onDismiss,
                style = PButtonStyle.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "AutoCharge — light")
@Preview(showBackground = true, name = "AutoCharge — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AutoChargePreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            Text("AutoChargeConfirmationSheet would appear here")
        }
    }
}
