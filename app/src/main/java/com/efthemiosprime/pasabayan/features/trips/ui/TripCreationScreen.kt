package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDateTimeField
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.trips.components.TripCreationBaseScaffold
import com.efthemiosprime.pasabayan.features.trips.components.TripRequirementChipUi

/**
 * Trip creation wizard — expandable sections for route, schedule, capacity, pricing, notes.
 * Parity with iOS `TripCreationView` wizard mode.
 */
@Composable
fun TripCreationScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var originCity by remember { mutableStateOf("") }
    var destinationCity by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf("") }
    var weightCapacity by remember { mutableStateOf("") }
    var pricePerKg by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }

    val completedRequirements = listOf(
        originCity.isNotBlank() && destinationCity.isNotBlank(),
        weightCapacity.isNotBlank(),
        pricePerKg.isNotBlank(),
        departureDate.isNotBlank() && arrivalDate.isNotBlank(),
    ).count { it }

    TripCreationBaseScaffold(
        title = stringResource(R.string.trips_create_title),
        completedRequirements = completedRequirements,
        totalRequirements = 4,
        requirementChips = listOf(
            TripRequirementChipUi(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.trips_detail_route),
                isComplete = originCity.isNotBlank() && destinationCity.isNotBlank(),
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Info,
                label = stringResource(R.string.trips_detail_capacity),
                isComplete = weightCapacity.isNotBlank(),
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Payments,
                label = stringResource(R.string.trips_detail_price),
                isComplete = pricePerKg.isNotBlank(),
            ),
            TripRequirementChipUi(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.trips_detail_schedule),
                isComplete = departureDate.isNotBlank() && arrivalDate.isNotBlank(),
            ),
        ),
        onClose = onCancel,
        modifier = modifier,
        footer = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PasabayanSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                PButton(
                    text = stringResource(R.string.trips_create_trip),
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                )
                PButton(
                    text = stringResource(R.string.trips_cancel_trip),
                    onClick = onCancel,
                    style = PButtonStyle.Tertiary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {

            // Route section
            PExpandableSection(title = stringResource(R.string.trips_detail_route), initiallyExpanded = true) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    POutlinedTextField(
                        value = originCity,
                        onValueChange = { originCity = it },
                        label = { Text(stringResource(R.string.trips_create_origin)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    POutlinedTextField(
                        value = destinationCity,
                        onValueChange = { destinationCity = it },
                        label = { Text(stringResource(R.string.trips_create_destination)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Schedule section
            PExpandableSection(title = stringResource(R.string.trips_detail_schedule)) {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PDateTimeField(
                        value = departureDate,
                        onClick = { /* TODO: show DatePickerDialog */ },
                        label = stringResource(R.string.trips_create_departure_date),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PDateTimeField(
                        value = arrivalDate,
                        onClick = { /* TODO: show DatePickerDialog */ },
                        label = stringResource(R.string.trips_create_arrival_date),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Capacity section
            PExpandableSection(title = stringResource(R.string.trips_detail_capacity)) {
                POutlinedTextField(
                    value = weightCapacity,
                    onValueChange = { weightCapacity = it },
                    label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Pricing section
            PExpandableSection(title = stringResource(R.string.trips_detail_price)) {
                POutlinedTextField(
                    value = pricePerKg,
                    onValueChange = { pricePerKg = it },
                    label = { Text(stringResource(R.string.trips_create_price_per_kg)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Notes section
            PExpandableSection(title = stringResource(R.string.trips_detail_notes)) {
                POutlinedTextField(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    label = { Text(stringResource(R.string.trips_create_special_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "TripCreation — light", heightDp = 900)
@Preview(showBackground = true, name = "TripCreation — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCreationPreview() {
    PasabayanTheme {
        TripCreationScreen(
            onSave = {},
            onCancel = {},
        )
    }
}
