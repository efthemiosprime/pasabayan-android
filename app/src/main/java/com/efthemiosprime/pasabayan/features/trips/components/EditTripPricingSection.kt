package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod

/**
 * Editable pricing section for `EditTripSheet`. Mirrors iOS `EditTripSheet.swift:896-991`:
 * the single price field swaps between `flatTripPrice` (land transport) and `pricePerKg`
 * (flight / ship) driven by [transportationMethod].
 *
 * The caller is responsible for routing the parsed value into the right wire field at
 * submit time (see `EditTripSheet.buildUpdateRequest`).
 */
@Composable
fun EditTripPricingSection(
    transportationMethod: TransportationMethod,
    priceText: String,
    onPriceChange: (String) -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    val labelRes = if (transportationMethod.isLandTransport) {
        R.string.trips_create_flat_price
    } else {
        R.string.trips_create_price_per_kg
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PDetailSectionTitle(text = stringResource(R.string.trips_edit_pricing_section))
        POutlinedTextField(
            value = priceText,
            onValueChange = { if (!locked) onPriceChange(it) },
            label = { Text(stringResource(labelRes)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

@Preview(showBackground = true, name = "EditTripPricingSection — land, light")
@Preview(showBackground = true, name = "EditTripPricingSection — land, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripPricingLandPreview() {
    var price by remember { mutableStateOf("80") }
    PasabayanTheme {
        EditTripPricingSection(
            transportationMethod = TransportationMethod.CAR,
            priceText = price, onPriceChange = { price = it },
            locked = false,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripPricingSection — flight, light")
@Preview(showBackground = true, name = "EditTripPricingSection — flight, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripPricingFlightPreview() {
    var price by remember { mutableStateOf("15") }
    PasabayanTheme {
        EditTripPricingSection(
            transportationMethod = TransportationMethod.FLIGHT,
            priceText = price, onPriceChange = { price = it },
            locked = false,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripPricingSection — locked, light")
@Preview(showBackground = true, name = "EditTripPricingSection — locked, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripPricingLockedPreview() {
    PasabayanTheme {
        EditTripPricingSection(
            transportationMethod = TransportationMethod.CAR,
            priceText = "80", onPriceChange = {},
            locked = true,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
