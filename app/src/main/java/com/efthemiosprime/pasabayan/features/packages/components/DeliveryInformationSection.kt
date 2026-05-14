package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.shared.components.CreationWizardStepHeader

/**
 * Step-3 of the shipper package-create wizard — handoff details (where the
 * recipient should receive the package and the preferred delivery date / time).
 *
 * iOS parity: `Views/Components/PackageRequest/DeliveryInformationSection.swift`.
 */
@Composable
internal fun DeliveryInformationSection(
    countryValue: String,
    countryOptions: List<String>,
    onCountrySelected: (String) -> Unit,
    cityValue: String,
    citySuggestions: List<String>,
    onCityChange: (String) -> Unit,
    onCitySuggestionSelected: (String) -> Unit,
    addressValue: String,
    onAddressChange: (String) -> Unit,
    deliveryDateFormatted: String,
    deliveryTimeFormatted: String,
    onDeliveryDateClick: () -> Unit,
    onDeliveryTimeClick: () -> Unit,
    hasSavedTemplate: Boolean,
    onUseSavedTemplate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        CreationWizardStepHeader(
            icon = Icons.Default.NearMe,
            title = stringResource(R.string.packages_create_section_handoff_details),
            subtitle = stringResource(R.string.packages_create_handoff_hint),
        )
        if (hasSavedTemplate) {
            PButton(
                text = stringResource(R.string.packages_create_use_saved_handoff),
                onClick = onUseSavedTemplate,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        CountryAndAddressFields(
            countryLabel = stringResource(R.string.packages_create_delivery_country),
            countryValue = countryValue,
            countryOptions = countryOptions,
            onCountrySelected = onCountrySelected,
            cityLabel = stringResource(R.string.packages_create_delivery_city),
            cityValue = cityValue,
            citySuggestions = citySuggestions,
            onCityChange = onCityChange,
            onCitySuggestionSelected = onCitySuggestionSelected,
            addressLabel = stringResource(R.string.packages_create_delivery_address),
            addressValue = addressValue,
            onAddressChange = onAddressChange,
        )
        PackageRequestDateTimeRow(
            label = stringResource(R.string.packages_create_delivery_date),
            value = deliveryDateFormatted,
            onClick = onDeliveryDateClick,
        )
        PackageRequestDateTimeRow(
            label = stringResource(R.string.packages_create_delivery_time),
            value = deliveryTimeFormatted,
            onClick = onDeliveryTimeClick,
        )
    }
}

@Preview(showBackground = true, name = "DeliveryInformation — light")
@Preview(showBackground = true, name = "DeliveryInformation — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeliveryInformationSectionPreview() {
    PasabayanTheme {
        DeliveryInformationSection(
            countryValue = "Canada",
            countryOptions = listOf("Canada", "United States"),
            onCountrySelected = {},
            cityValue = "Montreal",
            citySuggestions = emptyList(),
            onCityChange = {},
            onCitySuggestionSelected = {},
            addressValue = "456 Oak Ave",
            onAddressChange = {},
            deliveryDateFormatted = "Apr 7, 2026",
            deliveryTimeFormatted = "2:00 PM",
            onDeliveryDateClick = {},
            onDeliveryTimeClick = {},
            hasSavedTemplate = false,
            onUseSavedTemplate = {},
        )
    }
}
