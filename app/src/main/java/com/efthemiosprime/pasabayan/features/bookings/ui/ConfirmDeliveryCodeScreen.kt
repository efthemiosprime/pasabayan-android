package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

@Composable
fun ConfirmDeliveryCodeScreen(
    onSubmit: (code: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.bookings_confirm_delivery_code_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.bookings_confirm_delivery_code_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PasabayanSpacing.sm),
        )
        POutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text(stringResource(R.string.bookings_action_enter_code)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.lg),
        )
        PButton(
            text = stringResource(R.string.bookings_confirm_delivery_code_submit),
            onClick = { onSubmit(code) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.md),
            enabled = code.length >= 6,
        )
    }
}

@Preview(showBackground = true, name = "ConfirmDelivery — light")
@Preview(showBackground = true, name = "ConfirmDelivery — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfirmDeliveryPreview() {
    PasabayanTheme {
        ConfirmDeliveryCodeScreen(onSubmit = {}, onCancel = {})
    }
}
