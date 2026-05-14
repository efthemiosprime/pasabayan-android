package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

/**
 * Country dropdown + city/address inputs with inline city suggestions.
 * Used by both [PickupInformationSection] and [DeliveryInformationSection].
 *
 * iOS parity: matches the address-block layout in
 * `Views/Components/PackageRequest/PickupInformationSection.swift` and
 * `DeliveryInformationSection.swift`.
 */
@Composable
internal fun CountryAndAddressFields(
    countryLabel: String,
    countryValue: String,
    countryOptions: List<String>,
    onCountrySelected: (String) -> Unit,
    cityLabel: String,
    cityValue: String,
    citySuggestions: List<String>,
    onCityChange: (String) -> Unit,
    onCitySuggestionSelected: (String) -> Unit,
    addressLabel: String,
    addressValue: String,
    onAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PackageRequestDropdownField(
            label = countryLabel,
            value = countryValue,
            options = countryOptions,
            onSelected = onCountrySelected,
        )
        POutlinedTextField(
            value = cityValue,
            onValueChange = onCityChange,
            label = { Text(cityLabel) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (citySuggestions.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = PasabayanSpacing.xs,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    citySuggestions.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySuggestionSelected(suggestion) }
                                .padding(
                                    horizontal = PasabayanSpacing.md,
                                    vertical = PasabayanSpacing.sm,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
                            Text(
                                text = suggestion,
                                style = PasabayanTextStyles.Body.regular,
                            )
                        }
                        if (index < citySuggestions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        POutlinedTextField(
            value = addressValue,
            onValueChange = onAddressChange,
            label = { Text(addressLabel) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
