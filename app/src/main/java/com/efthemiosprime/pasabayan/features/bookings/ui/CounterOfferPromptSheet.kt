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
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterOfferPromptSheet(
    currentPrice: Double,
    remainingOffers: Int,
    onSubmit: (newPrice: Double, message: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var proposedPrice by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    PModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.bookings_counter_offer_title),
                style = PasabayanTextStyles.Heading.h4,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PDetailRow(
                label = stringResource(R.string.bookings_detail_agreed_price),
                value = String.format("$%.2f", currentPrice),
            )
            Text(
                text = stringResource(R.string.bookings_detail_remaining_offers, remainingOffers),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            POutlinedTextField(
                value = proposedPrice,
                onValueChange = { proposedPrice = it },
                label = { Text(stringResource(R.string.bookings_counter_offer_propose_price)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.bookings_counter_offer_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
            )
            PButton(
                text = stringResource(R.string.bookings_counter_offer_submit),
                onClick = {
                    proposedPrice.toDoubleOrNull()?.let { onSubmit(it, message.ifBlank { null }) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = proposedPrice.toDoubleOrNull() != null,
            )
        }
    }
}

@Preview(showBackground = true, name = "CounterOffer — light")
@Preview(showBackground = true, name = "CounterOffer — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CounterOfferPreview() {
    PasabayanTheme {
        Column(Modifier.padding(PasabayanSpacing.lg)) {
            Text("CounterOfferPromptSheet would appear here")
        }
    }
}
