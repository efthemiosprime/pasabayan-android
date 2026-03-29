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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestToCarrySheet(
    onSubmit: (price: Double, message: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var price by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.bookings_request_to_carry_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            POutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text(stringResource(R.string.bookings_request_to_carry_price)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.bookings_request_to_carry_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
            )
            PButton(
                text = stringResource(R.string.bookings_request_to_carry_submit),
                onClick = {
                    price.toDoubleOrNull()?.let { onSubmit(it, message.ifBlank { null }) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = price.toDoubleOrNull() != null,
            )
        }
    }
}

@Preview(showBackground = true, name = "RequestToCarry — light")
@Preview(showBackground = true, name = "RequestToCarry — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RequestToCarryPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            Text("RequestToCarrySheet would appear here")
        }
    }
}
