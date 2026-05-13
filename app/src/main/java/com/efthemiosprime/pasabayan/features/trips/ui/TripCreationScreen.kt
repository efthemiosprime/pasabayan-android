package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson
import com.efthemiosprime.pasabayan.features.trips.components.SavedRoutesSheet
import com.efthemiosprime.pasabayan.features.trips.components.TripCreationBaseScaffold
import com.efthemiosprime.pasabayan.features.trips.components.TripDateTimePicker
import com.efthemiosprime.pasabayan.features.trips.components.TripRequirementChipUi
import com.efthemiosprime.pasabayan.features.trips.components.TripTutorialOverlay
import com.efthemiosprime.pasabayan.features.trips.model.TripCityAutocomplete
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationFlowState
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationMode
import com.efthemiosprime.pasabayan.features.trips.model.TripCreationRouteFields
import com.efthemiosprime.pasabayan.features.trips.model.TripFormState
import com.efthemiosprime.pasabayan.features.trips.model.TripFormValidator
import com.efthemiosprime.pasabayan.features.trips.model.TripValidationError
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.shared.components.CityAutocompleteField
import com.efthemiosprime.pasabayan.shared.model.CountryCatalog
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripCreationViewModel

@Composable
fun TripCreationScreen(
    userId: Long,
    savedRoutes: List<SavedRouteTemplate>,
    onTripCreated: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripCreationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastWizardStep = 5
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var flowState by remember { mutableStateOf(TripCreationFlowState()) }
    var showSavedRoutesSheet by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    var originCity by remember { mutableStateOf("") }
    var destinationCity by remember { mutableStateOf("") }
    var originCountryCode by remember { mutableStateOf(CountryCatalog.CODE_CANADA) }
    var destinationCountryCode by remember { mutableStateOf(CountryCatalog.CODE_CANADA) }
    var pickupAddress by remember { mutableStateOf("") }
    var dropoffAddress by remember { mutableStateOf("") }
    var originCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    var destinationCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    // iOS parity: source of truth is epoch-millis; UI shows separate date + time pills.
    // Wire format (`yyyy-MM-dd HH:mm:ss`) is derived at submit time.
    var departureMillis by remember { mutableStateOf<Long?>(null) }
    var arrivalMillis by remember { mutableStateOf<Long?>(null) }
    var weightCapacity by remember { mutableStateOf("") }
    var spaceCapacity by remember { mutableStateOf("") }
    var transportationMethod by remember { mutableStateOf(TransportationMethod.CAR) }
    var pricePerKg by remember { mutableStateOf("") }
    var flatTripPrice by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }

    val originCityFocusRequester = remember { FocusRequester() }
    val destinationCityFocusRequester = remember { FocusRequester() }
    val originCountryFocusRequester = remember { FocusRequester() }
    val destinationCountryFocusRequester = remember { FocusRequester() }
    val pickupAddressFocusRequester = remember { FocusRequester() }
    val dropoffAddressFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val spaceFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    val completedRequirements = listOf(
        originCity.isNotBlank() &&
            destinationCity.isNotBlank() &&
            originCountryCode.isNotBlank() &&
            destinationCountryCode.isNotBlank(),
        weightCapacity.isNotBlank() && (spaceCapacity.isBlank() || spaceCapacity.toDoubleOrNull() != null),
        transportationMethod != TransportationMethod.NONE,
        if (transportationMethod.isLandTransport) flatTripPrice.isNotBlank() else pricePerKg.isNotBlank(),
        departureMillis != null && arrivalMillis != null,
        specialNotes.length <= 1000,
    ).count { it }

    LaunchedEffect(userId) {
        viewModel.initializeTutorial(userId)
    }

    TripCreationBaseScaffold(
        title = stringResource(R.string.trips_create_title),
        completedRequirements = completedRequirements,
        totalRequirements = 6,
        showPagination = flowState.mode == TripCreationMode.WIZARD,
        currentStep = flowState.currentStep,
        totalSteps = lastWizardStep + 1,
        requirementChips = listOf(
            TripRequirementChipUi(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.trips_detail_route),
                isComplete = originCity.isNotBlank() &&
                    destinationCity.isNotBlank() &&
                    originCountryCode.isNotBlank() &&
                    destinationCountryCode.isNotBlank(),
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Info,
                label = stringResource(R.string.trips_detail_capacity),
                isComplete = weightCapacity.isNotBlank(),
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Info,
                label = stringResource(R.string.trips_create_transport_method),
                isComplete = transportationMethod != TransportationMethod.NONE,
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Payments,
                label = stringResource(R.string.trips_detail_price),
                isComplete = if (transportationMethod.isLandTransport) {
                    flatTripPrice.isNotBlank()
                } else {
                    pricePerKg.isNotBlank()
                },
            ),
            TripRequirementChipUi(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.trips_detail_schedule),
                isComplete = departureMillis != null && arrivalMillis != null,
            ),
            TripRequirementChipUi(
                icon = Icons.Default.Info,
                label = stringResource(R.string.trips_detail_notes),
                isComplete = specialNotes.length <= 1000,
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
                            else -> {
                                val formState = TripFormState(
                                    originCity = originCity,
                                    destinationCity = destinationCity,
                                    pickupAddress = pickupAddress,
                                    dropoffAddress = dropoffAddress,
                                    weightCapacityKg = weightCapacity.toDoubleOrNull(),
                                    spaceCapacityLiters = spaceCapacity.toDoubleOrNull(),
                                    pricePerKg = pricePerKg.toDoubleOrNull(),
                                    flatTripPrice = flatTripPrice.toDoubleOrNull(),
                                    transportationMethod = transportationMethod,
                                    departureDateMillis = departureMillis,
                                    arrivalDateMillis = arrivalMillis,
                                    specialNotes = specialNotes,
                                )
                                val errors = TripFormValidator.validate(formState)
                                if (errors.isNotEmpty()) {
                                    validationMessage = context.getString(errors.first().toStringRes())
                                } else {
                                    validationMessage = null
                                    viewModel.clearError()
                                    viewModel.createTrip(
                                        CreateTripRequestJson(
                                            originCity = originCity.trim(),
                                            originCountry = originCountryCode.trim().uppercase(),
                                            destinationCity = destinationCity.trim(),
                                            destinationCountry = destinationCountryCode.trim().uppercase(),
                                            departureDate = DateTimeParsing.formatApiDateTime(departureMillis!!),
                                            arrivalDate = DateTimeParsing.formatApiDateTime(arrivalMillis!!),
                                            availableWeightKg = weightCapacity.toDoubleOrNull() ?: 0.0,
                                            availableSpaceLiters = spaceCapacity.toDoubleOrNull() ?: 0.0,
                                            pricePerKg = if (transportationMethod.isLandTransport) {
                                                null
                                            } else {
                                                pricePerKg.toDoubleOrNull()
                                            },
                                            transportationMethod = transportationMethod.name.lowercase(),
                                            specialNotes = specialNotes.trim().ifBlank { null },
                                            pricingMethod = null,
                                            flatTripPrice = if (transportationMethod.isLandTransport) {
                                                flatTripPrice.toDoubleOrNull()
                                            } else {
                                                null
                                            },
                                            basePrice = null,
                                            originCityId = null,
                                            destinationCityId = null,
                                            pickupAddress = pickupAddress.trim().ifBlank { null },
                                            dropoffAddress = dropoffAddress.trim().ifBlank { null },
                                            autoRequestPackageId = null,
                                            proposedPrice = null,
                                            requestMessage = null,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    enabled = !uiState.isSubmitting,
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
                validationMessage?.let {
                    Text(text = it)
                }
                uiState.errorMessage?.let {
                    Text(text = it)
                }
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
                                3 -> R.string.trips_create_transport_method
                                4 -> R.string.trips_detail_price
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
                            focusRequester = originCityFocusRequester,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                destinationCityFocusRequester.requestFocus()
                            }),
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
                            focusRequester = destinationCityFocusRequester,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                originCountryFocusRequester.requestFocus()
                            }),
                        )
                        POutlinedTextField(
                            value = originCountryCode,
                            onValueChange = { originCountryCode = it.uppercase() },
                            label = { Text(stringResource(R.string.trips_create_origin_country_code)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(originCountryFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                destinationCountryFocusRequester.requestFocus()
                            }),
                        )
                        POutlinedTextField(
                            value = destinationCountryCode,
                            onValueChange = { destinationCountryCode = it.uppercase() },
                            label = { Text(stringResource(R.string.trips_create_destination_country_code)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(destinationCountryFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                pickupAddressFocusRequester.requestFocus()
                            }),
                        )
                        POutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            label = { Text(stringResource(R.string.trips_create_pickup_address)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(pickupAddressFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                dropoffAddressFocusRequester.requestFocus()
                            }),
                        )
                        POutlinedTextField(
                            value = dropoffAddress,
                            onValueChange = { dropoffAddress = it },
                            label = { Text(stringResource(R.string.trips_create_dropoff_address)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(dropoffAddressFocusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        )
                    }
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 1) {
                PExpandableSection(title = stringResource(R.string.trips_detail_schedule), initiallyExpanded = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                        TripDateTimePicker(
                            label = stringResource(R.string.trips_create_departure_date),
                            epochMillis = departureMillis,
                            onChange = { departureMillis = it },
                        )
                        TripDateTimePicker(
                            label = stringResource(R.string.trips_create_arrival_date),
                            epochMillis = arrivalMillis,
                            onChange = { arrivalMillis = it },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(weightFocusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            spaceFocusRequester.requestFocus()
                        }),
                    )
                    POutlinedTextField(
                        value = spaceCapacity,
                        onValueChange = { spaceCapacity = it },
                        label = { Text(stringResource(R.string.trips_create_space_capacity)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(spaceFocusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            priceFocusRequester.requestFocus()
                        }),
                    )
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 3) {
                PExpandableSection(
                    title = stringResource(R.string.trips_create_transport_method),
                    initiallyExpanded = true,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                    ) {
                        listOf(
                            TransportationMethod.FLIGHT,
                            TransportationMethod.CAR,
                            TransportationMethod.BUS,
                            TransportationMethod.TRAIN,
                            TransportationMethod.SHIP,
                        ).forEach { method ->
                            PFilterChip(
                                label = stringResource(method.toLabelRes()),
                                selected = transportationMethod == method,
                                onClick = {
                                    transportationMethod = method
                                    if (method.isLandTransport) {
                                        pricePerKg = ""
                                    } else {
                                        flatTripPrice = ""
                                    }
                                },
                            )
                        }
                    }
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 4) {
                PExpandableSection(title = stringResource(R.string.trips_detail_price), initiallyExpanded = true) {
                    if (transportationMethod.isLandTransport) {
                        POutlinedTextField(
                            value = flatTripPrice,
                            onValueChange = { flatTripPrice = it },
                            label = { Text(stringResource(R.string.trips_create_flat_price)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(priceFocusRequester),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                notesFocusRequester.requestFocus()
                            }),
                        )
                    } else {
                        POutlinedTextField(
                            value = pricePerKg,
                            onValueChange = { pricePerKg = it },
                            label = { Text(stringResource(R.string.trips_create_price_per_kg)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(priceFocusRequester),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                notesFocusRequester.requestFocus()
                            }),
                        )
                    }
                }
            }

            if (flowState.mode == TripCreationMode.REVIEW || flowState.currentStep == 5) {
                PExpandableSection(title = stringResource(R.string.trips_detail_notes), initiallyExpanded = true) {
                    POutlinedTextField(
                        value = specialNotes,
                        onValueChange = { specialNotes = it },
                        label = { Text(stringResource(R.string.trips_create_special_notes)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(notesFocusRequester),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                    originCountryCode = originCountryCode,
                    pickupAddress = pickupAddress,
                    destinationCity = destinationCity,
                    destinationCountryCode = destinationCountryCode,
                    dropoffAddress = dropoffAddress,
                ).applySavedRoute(selectedRoute)
                originCity = routeFields.originCity
                originCountryCode = routeFields.originCountryCode
                pickupAddress = routeFields.pickupAddress
                destinationCity = routeFields.destinationCity
                destinationCountryCode = routeFields.destinationCountryCode
                dropoffAddress = routeFields.dropoffAddress
                originCitySuggestions = emptyList()
                destinationCitySuggestions = emptyList()
                showSavedRoutesSheet = false
            },
            onDismiss = { showSavedRoutesSheet = false },
        )
    }

    if (uiState.shouldShowSaveRoutePrompt) {
        val routeTemplate = if (
            originCity.isNotBlank() &&
            destinationCity.isNotBlank() &&
            originCountryCode.isNotBlank() &&
            destinationCountryCode.isNotBlank()
        ) {
            SavedRouteTemplate(
                startCountryCode = originCountryCode.trim().uppercase(),
                startLocation = originCity.trim(),
                pickupAddress = pickupAddress.trim().ifBlank { null },
                endCountryCode = destinationCountryCode.trim().uppercase(),
                endLocation = destinationCity.trim(),
                dropoffAddress = dropoffAddress.trim().ifBlank { null },
            )
        } else {
            null
        }
        com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog(
            title = stringResource(R.string.trips_create_success_title),
            message = stringResource(R.string.trips_create_success_save_route_prompt),
            confirmText = stringResource(R.string.trips_create_success_save_route_action),
            onConfirm = {
                viewModel.completeSuccessFlow(
                    userId = userId,
                    transportationMethod = transportationMethod,
                    saveRouteTemplate = routeTemplate,
                    saveRoute = true,
                )
                onTripCreated()
            },
            dismissText = stringResource(R.string.trips_create_success_skip_route_action),
            onDismiss = {
                viewModel.completeSuccessFlow(
                    userId = userId,
                    transportationMethod = transportationMethod,
                    saveRouteTemplate = routeTemplate,
                    saveRoute = false,
                )
                onTripCreated()
            },
        )
    }

    if (uiState.shouldShowTutorial) {
        TripTutorialOverlay(
            onDismiss = { viewModel.dismissTutorial(userId) },
        )
    }
}

@Preview(showBackground = true, name = "TripCreation - light", heightDp = 900)
@Preview(showBackground = true, name = "TripCreation - dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCreationPreview() {
    PasabayanTheme {
        TripCreationScreen(
            userId = 1L,
            savedRoutes = emptyList(),
            onTripCreated = {},
            onCancel = {},
        )
    }
}

private fun TripValidationError.toStringRes(): Int = when (this) {
    TripValidationError.OriginRequired -> R.string.trips_create_validation_origin_required
    TripValidationError.DestinationRequired -> R.string.trips_create_validation_destination_required
    TripValidationError.SameOriginDestination -> R.string.trips_create_validation_same_route
    TripValidationError.PickupAddressRequired -> R.string.trips_create_validation_pickup_address_required
    TripValidationError.DropoffAddressRequired -> R.string.trips_create_validation_dropoff_address_required
    TripValidationError.WeightRequired -> R.string.trips_create_validation_weight_required
    TripValidationError.WeightOutOfRange -> R.string.trips_create_validation_weight_out_of_range
    TripValidationError.SpaceInvalid -> R.string.trips_create_validation_space_invalid
    TripValidationError.SpaceOutOfRange -> R.string.trips_create_validation_space_out_of_range
    TripValidationError.PriceRequired -> R.string.trips_create_validation_price_required
    TripValidationError.PricePerKgOutOfRange -> R.string.trips_create_validation_price_per_kg_out_of_range
    TripValidationError.TransportMethodRequired -> R.string.trips_create_validation_transport_required
    TripValidationError.DepartureRequired -> R.string.trips_create_validation_departure_required
    TripValidationError.DepartureTooSoon -> R.string.trips_create_validation_departure_soon
    TripValidationError.ArrivalRequired -> R.string.trips_create_validation_arrival_required
    TripValidationError.ArrivalBeforeDeparture -> R.string.trips_create_validation_arrival_before_departure
    TripValidationError.DurationTooShort -> R.string.trips_create_validation_duration_short
    TripValidationError.NotesTooLong -> R.string.trips_create_validation_notes_long
}

internal fun TransportationMethod.toLabelRes(): Int = when (this) {
    TransportationMethod.NONE -> R.string.trips_transport_other
    TransportationMethod.FLIGHT -> R.string.trips_transport_flight
    TransportationMethod.BUS -> R.string.trips_transport_bus
    TransportationMethod.CAR -> R.string.trips_transport_car
    TransportationMethod.TRUCK -> R.string.trips_transport_truck
    TransportationMethod.VAN -> R.string.trips_transport_van
    TransportationMethod.MOTORCYCLE -> R.string.trips_transport_motorcycle
    TransportationMethod.SHIP -> R.string.trips_transport_ship
    TransportationMethod.TRAIN -> R.string.trips_transport_train
    TransportationMethod.OTHER -> R.string.trips_transport_other
}
