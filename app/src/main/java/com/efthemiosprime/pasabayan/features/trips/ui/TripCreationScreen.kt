package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
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
import com.efthemiosprime.pasabayan.features.trips.components.SavedRoutesSheet
import com.efthemiosprime.pasabayan.features.trips.components.TripCreationBaseScaffold
import com.efthemiosprime.pasabayan.features.trips.components.TripRequirementChipUi
import com.efthemiosprime.pasabayan.features.trips.model.TripCityAutocomplete
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationFlowState
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationMode
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationRouteFields
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.shared.components.CityAutocompleteField

@Composable
fun TripCreationScreen(
    savedRoutes: List<SavedRouteTemplate>,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastWizardStep = 4

    var flowState by remember { mutableStateOf(TripCreationFlowState()) }
    var showSavedRoutesSheet by remember { mutableStateOf(false) }

    var originCity by remember { mutableStateOf("") }
    var destinationCity by remember { mutableStateOf("") }
    var originCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    var destinationCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
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
                    text = when {
                        flowState.mode == TripCreationMode.WIZARD && flowState.currentStep == lastWizardStep ->
                            stringResource(R.string.trips_create_wizard_review)
                        flowState.mode == TripCreationMode.WIZARD ->
                            stringResource(R.string.trips_create_wizard_next)
                        else -> stringResource(R.string.trips_create_trip)
                    },
                    onClick = {
                        when {
                            flowState.mode == TripCreationMode.WIZARD && flowState.currentStep == lastWizardStep ->
                                flowState = flowState.switchToReview()
                            flowState.mode == TripCreationMode.WIZARD ->
                                flowState = flowState.nextStep(lastWizardStep)
                            else -> onSave()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (flowState.mode == TripCreationMode.WIZARD) {
                    PButton(
                        text = stringResource(R.string.trips_create_wizard_back),
                        onClick = { flowState = flowState.previousStep() },
                        style = PButtonStyle.Secondary,
                        enabled = flowState.currentStep > 0,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    PButton(
                        text = stringResource(R.string.trips_create_review_edit_wizard),
                        onClick = { flowState = flowState.switchToWizard() },
                        style = PButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
            if (flowState.mode == TripCreationMode.WIZARD) {
                Text(
                    text = stringResource(
                        R.string.trips_create_wizard_step_indicator,
                        flowState.currentStep + 1,
                        lastWizardStep + 1,
                        stringResource(
                            when (flowState.currentStep) {
                                0 -> R.string.trips_detail_route
                                1 -> R.string.trips_detail_schedule
                                2 -> R.string.trips_detail_capacity
                                3 -> R.string.trips_detail_price
                                else -> R.string.trips_detail_notes
                            },
                        ),
                    ),
                )
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 0) {
                PExpandableSection(title = stringResource(R.string.trips_detail_route), initiallyExpanded = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                        if (savedRoutes.isNotEmpty()) {
                            PButton(
                                text = stringResource(R.string.trips_create_saved_routes_use_saved),
                                onClick = { showSavedRoutesSheet = true },
                                style = PButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        CityAutocompleteField(
                            value = originCity,
                            onValueChange = {
                                originCity = it
                                originCitySuggestions = TripCityAutocomplete.suggestions(it)
                            },
                            label = { Text(stringResource(R.string.trips_create_origin)) },
                            suggestions = originCitySuggestions,
                            onSuggestionSelected = { selectedCity ->
                                originCity = selectedCity
                                originCitySuggestions = emptyList()
                            },
                        )
                        CityAutocompleteField(
                            value = destinationCity,
                            onValueChange = {
                                destinationCity = it
                                destinationCitySuggestions = TripCityAutocomplete.suggestions(it)
                            },
                            label = { Text(stringResource(R.string.trips_create_destination)) },
                            suggestions = destinationCitySuggestions,
                            onSuggestionSelected = { selectedCity ->
                                destinationCity = selectedCity
                                destinationCitySuggestions = emptyList()
                            },
                        )
                    }
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 1) {
                PExpandableSection(title = stringResource(R.string.trips_detail_schedule), initiallyExpanded = true) {
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
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 2) {
                PExpandableSection(title = stringResource(R.string.trips_detail_capacity), initiallyExpanded = true) {
                    POutlinedTextField(
                        value = weightCapacity,
                        onValueChange = { weightCapacity = it },
                        label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 3) {
                PExpandableSection(title = stringResource(R.string.trips_detail_price), initiallyExpanded = true) {
                    POutlinedTextField(
                        value = pricePerKg,
                        onValueChange = { pricePerKg = it },
                        label = { Text(stringResource(R.string.trips_create_price_per_kg)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 4) {
                PExpandableSection(title = stringResource(R.string.trips_detail_notes), initiallyExpanded = true) {
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

    if (showSavedRoutesSheet) {
        SavedRoutesSheet(
            routes = savedRoutes,
            onSelectRoute = { selectedRoute ->
                val routeFields = TripCreationRouteFields(
                    originCity = originCity,
                    destinationCity = destinationCity,
                ).applySavedRoute(selectedRoute)
                originCity = routeFields.originCity
                destinationCity = routeFields.destinationCity
                originCitySuggestions = emptyList()
                destinationCitySuggestions = emptyList()
                showSavedRoutesSheet = false
            },
            onDismiss = { showSavedRoutesSheet = false },
        )
    }
}

@Preview(showBackground = true, name = "TripCreation - light", heightDp = 900)
@Preview(showBackground = true, name = "TripCreation - dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCreationPreview() {
    PasabayanTheme {
        TripCreationScreen(
            savedRoutes = emptyList(),
            onSave = {},
            onCancel = {},
        )
    }
}
