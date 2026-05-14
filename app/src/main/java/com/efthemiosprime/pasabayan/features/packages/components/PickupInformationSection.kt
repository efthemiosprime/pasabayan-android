package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.shared.components.CreationWizardStepHeader

/**
 * Step-2 of the shipper package-create wizard — pickup details (where the
 * carrier should collect the package, plus the preferred pickup date / time
 * and a flexible-window toggle).
 *
 * iOS parity: `Views/Components/PackageRequest/PickupInformationSection.swift`.
 *
 * State-hoisted: all values + callbacks are passed in. The parent owns the
 * pickup/handoff form state via `PackageFormState`.
 */
@Composable
internal fun PickupInformationSection(
    countryValue: String,
    countryOptions: List<String>,
    onCountrySelected: (String) -> Unit,
    cityValue: String,
    citySuggestions: List<String>,
    onCityChange: (String) -> Unit,
    onCitySuggestionSelected: (String) -> Unit,
    addressValue: String,
    onAddressChange: (String) -> Unit,
    pickupDateFormatted: String,
    pickupTimeFormatted: String,
    onPickupDateClick: () -> Unit,
    onPickupTimeClick: () -> Unit,
    pickupDateFlexible: Boolean,
    onPickupDateFlexibleChange: (Boolean) -> Unit,
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
            title = stringResource(R.string.packages_create_section_pickup_details),
            subtitle = stringResource(R.string.packages_create_pickup_hint),
        )
        if (hasSavedTemplate) {
            PButton(
                text = stringResource(R.string.packages_create_use_saved_pickup),
                onClick = onUseSavedTemplate,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        CountryAndAddressFields(
            countryLabel = stringResource(R.string.packages_create_pickup_country),
            countryValue = countryValue,
            countryOptions = countryOptions,
            onCountrySelected = onCountrySelected,
            cityLabel = stringResource(R.string.packages_create_pickup_city),
            cityValue = cityValue,
            citySuggestions = citySuggestions,
            onCityChange = onCityChange,
            onCitySuggestionSelected = onCitySuggestionSelected,
            addressLabel = stringResource(R.string.packages_create_pickup_address),
            addressValue = addressValue,
            onAddressChange = onAddressChange,
        )
        PackageRequestDateTimeRow(
            label = stringResource(R.string.packages_create_pickup_date),
            value = pickupDateFormatted,
            onClick = onPickupDateClick,
        )
        PackageRequestDateTimeRow(
            label = stringResource(R.string.packages_create_pickup_time),
            value = pickupTimeFormatted,
            onClick = onPickupTimeClick,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.packages_create_pickup_flexible),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = pickupDateFlexible,
                onCheckedChange = onPickupDateFlexibleChange,
            )
        }
    }
}

@Preview(showBackground = true, name = "PickupInformation — light")
@Preview(showBackground = true, name = "PickupInformation — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PickupInformationSectionPreview() {
    PasabayanTheme {
        PickupInformationSection(
            countryValue = "Canada",
            countryOptions = listOf("Canada", "United States"),
            onCountrySelected = {},
            cityValue = "Toronto",
            citySuggestions = emptyList(),
            onCityChange = {},
            onCitySuggestionSelected = {},
            addressValue = "123 Main St",
            onAddressChange = {},
            pickupDateFormatted = "Apr 5, 2026",
            pickupTimeFormatted = "10:00 AM",
            onPickupDateClick = {},
            onPickupTimeClick = {},
            pickupDateFlexible = false,
            onPickupDateFlexibleChange = {},
            hasSavedTemplate = true,
            onUseSavedTemplate = {},
        )
    }
}
