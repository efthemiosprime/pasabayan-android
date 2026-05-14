package com.efthemiosprime.pasabayan.features.packages.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestBaseScaffold
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequirementChipUi
import com.efthemiosprime.pasabayan.features.packages.components.PackageTutorialOverlay
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.HandoffTemplate
import com.efthemiosprime.pasabayan.features.packages.services.PickupTemplate
import com.efthemiosprime.pasabayan.shared.model.CityCatalog
import com.efthemiosprime.pasabayan.shared.model.CountryCatalog
import com.efthemiosprime.pasabayan.shared.components.CreationWizardStepHeader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PackageRequestScreen(
    onSave: (PackageSubmitPayload, List<Uri>) -> Unit,
    onCancel: () -> Unit,
    savedDescriptions: List<String> = emptyList(),
    savedPickupTemplates: List<PickupTemplate> = emptyList(),
    savedHandoffTemplates: List<HandoffTemplate> = emptyList(),
    showTutorialOverlay: Boolean = false,
    onDismissTutorial: () -> Unit = {},
    isSubmitting: Boolean = false,
    /**
     * iOS parity: active packages with matching pickup/delivery cities found
     * by [com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel.checkSimilarPackages].
     * Surfaced as a warning banner on the review step. Empty list hides it.
     */
    similarPackages: List<com.efthemiosprime.pasabayan.features.packages.model.PackageRequest> = emptyList(),
    /**
     * Triggered when the user reaches the review step so duplicate detection
     * can run. Caller wires this to `viewModel.checkSimilarPackages(pickup, delivery)`.
     */
    onCheckSimilarPackages: (pickupCity: String, deliveryCity: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val countryOptions = CountryCatalog.supportedCountries
    val countryLabelsByCode = countryOptions.associate { it.code to stringResource(it.labelRes) }
    val countryOptionLabels = countryOptions.map { option -> countryLabelsByCode.getValue(option.code) }
    val packageTypeOptions = packageTypeOptions()
    val packageTypeLabelsByCode = packageTypeOptions.associate { it.code to stringResource(it.labelRes) }
    val urgencyOptions = urgencyOptions()
    val urgencyLabelsByCode = urgencyOptions.associate { it.code to stringResource(it.labelRes) }

    var currentStep by remember { mutableStateOf(0) }
    var hasReachedFullReview by remember { mutableStateOf(false) }

    var pickupCity by remember { mutableStateOf("") }
    var pickupCountry by remember(countryOptions) { mutableStateOf(countryOptions.first()) }
    var pickupAddress by remember { mutableStateOf("") }
    var pickupCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    var deliveryCity by remember { mutableStateOf("") }
    var deliveryCountry by remember(countryOptions) { mutableStateOf(countryOptions.first()) }
    var deliveryAddress by remember { mutableStateOf("") }
    var deliveryCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    var description by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var packageValue by remember { mutableStateOf("") }
    var maxBudget by remember { mutableStateOf("") }
    var specialHandling by remember { mutableStateOf("") }
    var isFragile by remember { mutableStateOf(false) }
    var packageType by remember(packageTypeOptions) { mutableStateOf(packageTypeOptions.first()) }
    var urgencyLevel by remember(urgencyOptions) { mutableStateOf(urgencyOptions[1]) }
    var pickupDateFlexible by remember { mutableStateOf(false) }
    val selectedPhotoUris = remember { mutableStateListOf<Uri>() }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        selectedPhotoUris.clear()
        selectedPhotoUris.addAll(uris)
    }

    var pickupDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var pickupTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var deliveryDate by remember { mutableStateOf(LocalDate.now().plusDays(2)) }
    var deliveryTime by remember { mutableStateOf(LocalTime.of(14, 0)) }

    val parsedWeightKg = remember(weight) { weight.toDoubleOrNull() }
    val completedRequirements = remember(
        description,
        pickupAddress,
        pickupCity,
        deliveryAddress,
        deliveryCity,
        parsedWeightKg,
        pickupDate,
        pickupTime,
        deliveryDate,
        deliveryTime,
    ) {
        var count = 0
        if (description.isNotBlank()) count += 1
        if (pickupAddress.isNotBlank() && pickupCity.isNotBlank()) count += 1
        if (deliveryAddress.isNotBlank() && deliveryCity.isNotBlank()) count += 1
        if (parsedWeightKg != null && parsedWeightKg > 0.0) count += 1
        if (isDatesValid(pickupDate, pickupTime, deliveryDate, deliveryTime)) count += 1
        count
    }
    val isFormValid = completedRequirements == 5

    PackageRequestBaseScaffold(
        title = stringResource(R.string.packages_create_title),
        completedRequirements = completedRequirements,
        totalRequirements = 5,
        requirementChips = listOf(
            PackageRequirementChipUi(
                icon = Icons.Default.Description,
                label = stringResource(R.string.packages_create_step_what_it_is),
                isComplete = description.isNotBlank(),
            ),
            PackageRequirementChipUi(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.packages_create_step_pickup),
                isComplete = pickupAddress.isNotBlank() && pickupCity.isNotBlank(),
            ),
            PackageRequirementChipUi(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.packages_create_step_handoff),
                isComplete = deliveryAddress.isNotBlank() && deliveryCity.isNotBlank(),
            ),
            PackageRequirementChipUi(
                icon = Icons.Default.Info,
                label = stringResource(R.string.packages_create_step_weight),
                isComplete = weight.isNotBlank(),
            ),
            PackageRequirementChipUi(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.packages_create_step_dates),
                isComplete = isDatesValid(pickupDate, pickupTime, deliveryDate, deliveryTime),
            ),
        ),
        onClose = onCancel,
        showPagination = !hasReachedFullReview,
        currentStep = currentStep,
        totalSteps = 4,
        modifier = modifier,
        footer = {
            if (hasReachedFullReview) {
                PButton(
                    text = stringResource(R.string.packages_create_submit),
                    onClick = {
                        val safeWeight = parsedWeightKg ?: return@PButton
                        onSave(
                            PackageSubmitPayload(
                                pickupAddress = pickupAddress.trim(),
                                pickupCity = pickupCity.trim(),
                                pickupCountryCode = pickupCountry.code,
                                deliveryAddress = deliveryAddress.trim(),
                                deliveryCity = deliveryCity.trim(),
                                deliveryCountryCode = deliveryCountry.code,
                                packageWeightKg = safeWeight,
                                packageTypeCode = packageType.code,
                                fragile = isFragile,
                                urgencyLevelCode = urgencyLevel.code,
                                pickupDatePreferred = pickupDate,
                                pickupTimePreferred = pickupTime,
                                pickupDateFlexible = pickupDateFlexible,
                                deliveryDateNeeded = deliveryDate,
                                deliveryTimeNeeded = deliveryTime,
                                packageValue = packageValue.toDoubleOrNull(),
                                packageDescription = description.trim().takeIf { it.isNotBlank() },
                                maxPriceBudget = maxBudget.toDoubleOrNull(),
                                specialHandlingRequirements = specialHandling.trim().takeIf { it.isNotBlank() },
                            ),
                            selectedPhotoUris.toList(),
                        )
                    },
                    style = PButtonStyle.Submit,
                    enabled = isFormValid && !isSubmitting,
                    modifier = Modifier.padding(PasabayanSpacing.lg),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PasabayanSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
                ) {
                    if (currentStep > 0) {
                        PButton(
                            text = stringResource(R.string.packages_create_back),
                            onClick = { currentStep -= 1 },
                            style = PButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    PButton(
                        text = if (currentStep == 3) {
                            stringResource(R.string.packages_create_review)
                        } else {
                            stringResource(R.string.packages_create_next)
                        },
                        onClick = {
                            if (currentStep == 3) {
                                hasReachedFullReview = true
                            } else {
                                currentStep += 1
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { contentPadding ->
        val pickupDateFormatted = pickupDate.format(dateFormatter)
        val pickupTimeFormatted = pickupTime.format(timeFormatter)
        val deliveryDateFormatted = deliveryDate.format(dateFormatter)
        val deliveryTimeFormatted = deliveryTime.format(timeFormatter)

        // iOS parity: kick off duplicate detection the moment the shipper opens the review
        // step (or whenever the cities change while review is open). Inert when fields are blank.
        LaunchedEffect(hasReachedFullReview, pickupCity, deliveryCity) {
            if (hasReachedFullReview && pickupCity.isNotBlank() && deliveryCity.isNotBlank()) {
                onCheckSimilarPackages(pickupCity.trim(), deliveryCity.trim())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
        ) {
            if (hasReachedFullReview && similarPackages.isNotEmpty()) {
                SimilarPackagesWarning(count = similarPackages.size)
            }
            if (!hasReachedFullReview) {
                when (currentStep) {
                    0 -> {
                        CreationWizardStepHeader(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.packages_create_step_what_it_is),
                            subtitle = stringResource(R.string.packages_create_what_it_is_hint),
                        )
                        if (savedDescriptions.isNotEmpty()) {
                            PButton(
                                text = stringResource(R.string.packages_create_use_recent_description),
                                onClick = { description = savedDescriptions.first() },
                                style = PButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        UploadPhotosBlock(
                            selectedPhotoUris = selectedPhotoUris,
                            onPickPhotos = { photoPickerLauncher.launch("image/*") },
                        )
                    }

                    1 -> {
                        CreationWizardStepHeader(
                            icon = Icons.Default.NearMe,
                            title = stringResource(R.string.packages_create_section_pickup_details),
                            subtitle = stringResource(R.string.packages_create_pickup_hint),
                        )
                        if (savedPickupTemplates.isNotEmpty()) {
                            PButton(
                                text = stringResource(R.string.packages_create_use_saved_pickup),
                                onClick = {
                                    val template = savedPickupTemplates.first()
                                    pickupCountry = CountryCatalog.byCode(template.pickupCountryCode)
                                    pickupCity = template.pickupCity
                                    pickupAddress = template.pickupAddress.orEmpty()
                                    pickupCitySuggestions = emptyList()
                                },
                                style = PButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        CountryAndAddressFields(
                            countryLabel = stringResource(R.string.packages_create_pickup_country),
                            countryValue = countryLabelsByCode.getValue(pickupCountry.code),
                            countryOptions = countryOptionLabels,
                            onCountrySelected = { label ->
                                val selectedCode = countryLabelsByCode.entries
                                    .firstOrNull { it.value == label }
                                    ?.key
                                    ?: countryOptions.first().code
                                val selected = CountryCatalog.byCode(selectedCode)
                                pickupCountry = selected
                                if (!CityCatalog.containsInCountry(pickupCity, selected.code)) {
                                    pickupCity = ""
                                }
                                pickupCitySuggestions = emptyList()
                            },
                            cityLabel = stringResource(R.string.packages_create_pickup_city),
                            cityValue = pickupCity,
                            citySuggestions = pickupCitySuggestions,
                            onCityChange = {
                                pickupCity = it
                                pickupCitySuggestions = if (CityCatalog.containsInCountry(it, pickupCountry.code)) {
                                    emptyList()
                                } else {
                                    CityCatalog.suggestionsForCountry(it, pickupCountry.code)
                                }
                            },
                            onCitySuggestionSelected = {
                                pickupCity = it
                                pickupCitySuggestions = emptyList()
                            },
                            addressLabel = stringResource(R.string.packages_create_pickup_address),
                            addressValue = pickupAddress,
                            onAddressChange = { pickupAddress = it },
                        )
                        DateTimeRowField(
                            label = stringResource(R.string.packages_create_pickup_date),
                            value = pickupDateFormatted,
                            onClick = { showDatePicker(context, pickupDate) { pickupDate = it } },
                        )
                        DateTimeRowField(
                            label = stringResource(R.string.packages_create_pickup_time),
                            value = pickupTimeFormatted,
                            onClick = { showTimePicker(context, pickupTime) { pickupTime = it } },
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
                                onCheckedChange = { pickupDateFlexible = it },
                            )
                        }
                    }

                    2 -> {
                        CreationWizardStepHeader(
                            icon = Icons.Default.NearMe,
                            title = stringResource(R.string.packages_create_section_handoff_details),
                            subtitle = stringResource(R.string.packages_create_handoff_hint),
                        )
                        if (savedHandoffTemplates.isNotEmpty()) {
                            PButton(
                                text = stringResource(R.string.packages_create_use_saved_handoff),
                                onClick = {
                                    val template = savedHandoffTemplates.first()
                                    deliveryCountry = CountryCatalog.byCode(template.deliveryCountryCode)
                                    deliveryCity = template.deliveryCity
                                    deliveryAddress = template.deliveryAddress.orEmpty()
                                    deliveryCitySuggestions = emptyList()
                                },
                                style = PButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        CountryAndAddressFields(
                            countryLabel = stringResource(R.string.packages_create_delivery_country),
                            countryValue = countryLabelsByCode.getValue(deliveryCountry.code),
                            countryOptions = countryOptionLabels,
                            onCountrySelected = { label ->
                                val selectedCode = countryLabelsByCode.entries
                                    .firstOrNull { it.value == label }
                                    ?.key
                                    ?: countryOptions.first().code
                                val selected = CountryCatalog.byCode(selectedCode)
                                deliveryCountry = selected
                                if (!CityCatalog.containsInCountry(deliveryCity, selected.code)) {
                                    deliveryCity = ""
                                }
                                deliveryCitySuggestions = emptyList()
                            },
                            cityLabel = stringResource(R.string.packages_create_delivery_city),
                            cityValue = deliveryCity,
                            citySuggestions = deliveryCitySuggestions,
                            onCityChange = {
                                deliveryCity = it
                                deliveryCitySuggestions = if (CityCatalog.containsInCountry(it, deliveryCountry.code)) {
                                    emptyList()
                                } else {
                                    CityCatalog.suggestionsForCountry(it, deliveryCountry.code)
                                }
                            },
                            onCitySuggestionSelected = {
                                deliveryCity = it
                                deliveryCitySuggestions = emptyList()
                            },
                            addressLabel = stringResource(R.string.packages_create_delivery_address),
                            addressValue = deliveryAddress,
                            onAddressChange = { deliveryAddress = it },
                        )
                        DateTimeRowField(
                            label = stringResource(R.string.packages_create_delivery_date),
                            value = deliveryDateFormatted,
                            onClick = { showDatePicker(context, deliveryDate) { deliveryDate = it } },
                        )
                        DateTimeRowField(
                            label = stringResource(R.string.packages_create_delivery_time),
                            value = deliveryTimeFormatted,
                            onClick = { showTimePicker(context, deliveryTime) { deliveryTime = it } },
                        )
                    }

                    else -> {
                        CreationWizardStepHeader(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.packages_create_review_title),
                            subtitle = stringResource(R.string.packages_create_review_hint),
                        )
                        Spacer(modifier = Modifier.height(PasabayanSpacing.xxxxl))
                    }
                }
            } else {
                PCard {
                    PExpandableSection(
                        title = stringResource(R.string.packages_create_section_about_package),
                        initiallyExpanded = true,
                    ) {
                        PackageRequestDropdownField(
                            label = stringResource(R.string.packages_create_package_type),
                            value = packageTypeLabelsByCode.getValue(packageType.code),
                            options = packageTypeOptions.map { packageTypeLabelsByCode.getValue(it.code) },
                            onSelected = { selectedLabel ->
                                val selectedCode = packageTypeLabelsByCode.entries
                                    .firstOrNull { it.value == selectedLabel }
                                    ?.key
                                    ?: packageTypeOptions.first().code
                                packageType = packageTypeOptions.firstOrNull { it.code == selectedCode }
                                    ?: packageTypeOptions.first()
                            },
                        )
                        PackageRequestDropdownField(
                            label = stringResource(R.string.packages_create_urgency),
                            value = urgencyLabelsByCode.getValue(urgencyLevel.code),
                            options = urgencyOptions.map { urgencyLabelsByCode.getValue(it.code) },
                            onSelected = { selectedLabel ->
                                val selectedCode = urgencyLabelsByCode.entries
                                    .firstOrNull { it.value == selectedLabel }
                                    ?.key
                                    ?: urgencyOptions.first().code
                                urgencyLevel = urgencyOptions.firstOrNull { it.code == selectedCode }
                                    ?: urgencyOptions.first()
                            },
                        )
                        POutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.packages_create_description)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        POutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text(stringResource(R.string.packages_create_weight)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        POutlinedTextField(
                            value = packageValue,
                            onValueChange = { packageValue = it },
                            label = { Text(stringResource(R.string.packages_create_value)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        POutlinedTextField(
                            value = maxBudget,
                            onValueChange = { maxBudget = it },
                            label = { Text(stringResource(R.string.packages_create_budget)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        POutlinedTextField(
                            value = specialHandling,
                            onValueChange = { specialHandling = it },
                            label = { Text(stringResource(R.string.packages_create_special_handling)) },
                            singleLine = false,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    if (showTutorialOverlay) {
        PackageTutorialOverlay(onDismiss = onDismissTutorial)
    }
}

@Composable
private fun UploadPhotosBlock(
    selectedPhotoUris: List<Uri>,
    onPickPhotos: () -> Unit,
) {
    PCard {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(PasabayanSpacing.xxxl),
                )
                Text(
                    text = stringResource(R.string.packages_create_upload_photos_title),
                    style = PasabayanTextStyles.Heading.h5,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.packages_create_upload_photos_hint),
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PButton(
                    text = stringResource(R.string.packages_create_choose_files),
                    onClick = onPickPhotos,
                    size = com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize.Small,
                )
                if (selectedPhotoUris.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.packages_create_selected_photos_count,
                            selectedPhotoUris.size,
                        ),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.packages_create_upload_formats),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    PCard {
        Text(
            text = stringResource(R.string.packages_create_photo_tips_title),
            style = PasabayanTextStyles.Heading.h6,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
        val tips = listOf(
            stringResource(R.string.packages_create_photo_tip_1),
            stringResource(R.string.packages_create_photo_tip_2),
            stringResource(R.string.packages_create_photo_tip_3),
            stringResource(R.string.packages_create_photo_tip_4),
        )
        tips.forEach { tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(PasabayanSpacing.sm),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {}
                Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
                Text(
                    text = tip,
                    style = PasabayanTextStyles.Body.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CountryAndAddressFields(
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
) {
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
                            .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
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
                        androidx.compose.material3.HorizontalDivider()
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

@Composable
private fun DateTimeRowField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Text(
                text = value,
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PasabayanSpacing.lg, vertical = PasabayanSpacing.sm),
            )
        }
    }
}

@Composable
private fun PackageRequestDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        POutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

private fun showDatePicker(context: Context, current: LocalDate, onSelected: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth,
    ).show()
}

private fun showTimePicker(context: Context, current: LocalTime, onSelected: (LocalTime) -> Unit) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onSelected(LocalTime.of(hourOfDay, minute))
        },
        current.hour,
        current.minute,
        false,
    ).show()
}

private fun isDatesValid(
    pickupDate: LocalDate,
    pickupTime: LocalTime,
    deliveryDate: LocalDate,
    deliveryTime: LocalTime,
): Boolean {
    val now = LocalDateTime.now()
    val minimumPickup = now.plusMinutes(5)
    val pickupDateTime = LocalDateTime.of(pickupDate, pickupTime)
    val deliveryDateTime = LocalDateTime.of(deliveryDate, deliveryTime)
    return pickupDateTime >= minimumPickup && deliveryDateTime.isAfter(pickupDateTime)
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private data class RequestOption(
    val code: String,
    val labelRes: Int,
)

@Composable
private fun packageTypeOptions(): List<RequestOption> = listOf(
    RequestOption(code = "general", labelRes = R.string.packages_type_general),
    RequestOption(code = "electronics", labelRes = R.string.packages_type_electronics),
    RequestOption(code = "clothing", labelRes = R.string.packages_type_clothing),
    RequestOption(code = "books", labelRes = R.string.packages_type_books),
    RequestOption(code = "food", labelRes = R.string.packages_type_food),
    RequestOption(code = "fragile", labelRes = R.string.packages_type_fragile),
    RequestOption(code = "documents", labelRes = R.string.packages_type_documents),
)

@Composable
private fun urgencyOptions(): List<RequestOption> = listOf(
    RequestOption(code = "low", labelRes = R.string.packages_urgency_low),
    RequestOption(code = "normal", labelRes = R.string.packages_urgency_normal),
    RequestOption(code = "high", labelRes = R.string.packages_urgency_high),
    RequestOption(code = "urgent", labelRes = R.string.packages_urgency_urgent),
    RequestOption(code = "express", labelRes = R.string.packages_urgency_express),
)

/**
 * iOS-parity warning surfaced on the review step when the shipper already has
 * active packages with the same pickup and delivery cities. Advisory — not a
 * hard block.
 */
@Composable
private fun SimilarPackagesWarning(count: Int) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = stringResource(R.string.packages_similar_warning_title),
                    style = PasabayanTextStyles.Body.large,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.packages_similar_warning_body, count),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "PackageRequest — light", heightDp = 900)
@Preview(showBackground = true, name = "PackageRequest — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestPreview() {
    PasabayanTheme {
        PackageRequestScreen(onSave = { _, _ -> }, onCancel = {})
    }
}

@Preview(showBackground = true, name = "SimilarPackagesWarning — light")
@Preview(showBackground = true, name = "SimilarPackagesWarning — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SimilarPackagesWarningPreview() {
    PasabayanTheme {
        SimilarPackagesWarning(count = 2)
    }
}
