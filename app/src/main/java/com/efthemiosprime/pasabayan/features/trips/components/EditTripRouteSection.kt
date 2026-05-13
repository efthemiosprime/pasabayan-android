package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.trips.model.TripCityAutocomplete
import com.efthemiosprime.pasabayan.shared.components.CityAutocompleteField

/**
 * Editable route section for `EditTripSheet`. Mirrors iOS `EditTripSheet.swift:346-374`
 * (origin/destination autocomplete + country picker + pickup/dropoff address).
 *
 * Reusable by construction — takes plain state + callbacks, no `ViewModel`. The `locked`
 * flag disables every input when the trip has left the planning state (iOS
 * `TripEditSheetEditingPolicy.allowsFullEdit(for:)` returns false). Notes stay editable
 * elsewhere in the sheet.
 */
@Composable
fun EditTripRouteSection(
    originCity: String,
    onOriginCityChange: (String) -> Unit,
    originCountry: String,
    onOriginCountryChange: (String) -> Unit,
    destinationCity: String,
    onDestinationCityChange: (String) -> Unit,
    destinationCountry: String,
    onDestinationCountryChange: (String) -> Unit,
    pickupAddress: String,
    onPickupAddressChange: (String) -> Unit,
    dropoffAddress: String,
    onDropoffAddressChange: (String) -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    var originSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PDetailSectionTitle(text = stringResource(R.string.trips_detail_route))

        if (locked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.trips_edit_route_locked_note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (locked) {
            POutlinedTextField(
                value = originCity,
                onValueChange = {},
                label = { Text(stringResource(R.string.trips_create_origin)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        } else {
            CityAutocompleteField(
                value = originCity,
                onValueChange = {
                    onOriginCityChange(it)
                    originSuggestions = TripCityAutocomplete.suggestions(it)
                },
                label = { Text(stringResource(R.string.trips_create_origin)) },
                suggestions = originSuggestions,
                onSuggestionSelected = { selected ->
                    onOriginCityChange(selected)
                    originSuggestions = emptyList()
                },
            )
        }
        POutlinedTextField(
            value = originCountry,
            onValueChange = { if (!locked) onOriginCountryChange(it.uppercase()) },
            label = { Text(stringResource(R.string.trips_create_origin_country_code)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
        )
        if (locked) {
            POutlinedTextField(
                value = destinationCity,
                onValueChange = {},
                label = { Text(stringResource(R.string.trips_create_destination)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        } else {
            CityAutocompleteField(
                value = destinationCity,
                onValueChange = {
                    onDestinationCityChange(it)
                    destinationSuggestions = TripCityAutocomplete.suggestions(it)
                },
                label = { Text(stringResource(R.string.trips_create_destination)) },
                suggestions = destinationSuggestions,
                onSuggestionSelected = { selected ->
                    onDestinationCityChange(selected)
                    destinationSuggestions = emptyList()
                },
            )
        }
        POutlinedTextField(
            value = destinationCountry,
            onValueChange = { if (!locked) onDestinationCountryChange(it.uppercase()) },
            label = { Text(stringResource(R.string.trips_create_destination_country_code)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
        )
        POutlinedTextField(
            value = pickupAddress,
            onValueChange = { if (!locked) onPickupAddressChange(it) },
            label = { Text(stringResource(R.string.trips_create_pickup_address)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
        )
        POutlinedTextField(
            value = dropoffAddress,
            onValueChange = { if (!locked) onDropoffAddressChange(it) },
            label = { Text(stringResource(R.string.trips_create_dropoff_address)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !locked,
        )
    }
}

@Preview(showBackground = true, name = "EditTripRouteSection — planning, light")
@Preview(showBackground = true, name = "EditTripRouteSection — planning, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripRouteSectionPlanningPreview() {
    var originCity by remember { mutableStateOf("Toronto") }
    var originCountry by remember { mutableStateOf("CA") }
    var destinationCity by remember { mutableStateOf("Montreal") }
    var destinationCountry by remember { mutableStateOf("CA") }
    var pickup by remember { mutableStateOf("123 Main St") }
    var dropoff by remember { mutableStateOf("456 Oak Ave") }
    PasabayanTheme {
        EditTripRouteSection(
            originCity = originCity, onOriginCityChange = { originCity = it },
            originCountry = originCountry, onOriginCountryChange = { originCountry = it },
            destinationCity = destinationCity, onDestinationCityChange = { destinationCity = it },
            destinationCountry = destinationCountry, onDestinationCountryChange = { destinationCountry = it },
            pickupAddress = pickup, onPickupAddressChange = { pickup = it },
            dropoffAddress = dropoff, onDropoffAddressChange = { dropoff = it },
            locked = false,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripRouteSection — locked, light")
@Preview(showBackground = true, name = "EditTripRouteSection — locked, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripRouteSectionLockedPreview() {
    PasabayanTheme {
        EditTripRouteSection(
            originCity = "Toronto", onOriginCityChange = {},
            originCountry = "CA", onOriginCountryChange = {},
            destinationCity = "Montreal", onDestinationCityChange = {},
            destinationCountry = "CA", onDestinationCountryChange = {},
            pickupAddress = "123 Main St", onPickupAddressChange = {},
            dropoffAddress = "456 Oak Ave", onDropoffAddressChange = {},
            locked = true,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
