package com.efthemiosprime.pasabayan.features.packages.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.packages.components.DeliveryInformationSection
import com.efthemiosprime.pasabayan.features.packages.components.PackageImagesSection
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestBaseScaffold
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestFormActions
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequirementChipUi
import com.efthemiosprime.pasabayan.features.packages.components.PackageReviewFormSection
import com.efthemiosprime.pasabayan.features.packages.components.PackageTutorialOverlay
import com.efthemiosprime.pasabayan.features.packages.components.PickupInformationSection
import com.efthemiosprime.pasabayan.features.packages.components.SimilarPackagesWarning
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.HandoffTemplate
import com.efthemiosprime.pasabayan.features.packages.services.PickupTemplate
import com.efthemiosprime.pasabayan.shared.components.CreationWizardStepHeader
import com.efthemiosprime.pasabayan.shared.model.CityCatalog
import com.efthemiosprime.pasabayan.shared.model.CountryCatalog
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
            PackageRequestFormActions(
                hasReachedFullReview = hasReachedFullReview,
                currentStep = currentStep,
                isFormValid = isFormValid,
                isSubmitting = isSubmitting,
                onBack = { currentStep -= 1 },
                onNext = {
                    if (currentStep == 3) {
                        hasReachedFullReview = true
                    } else {
                        currentStep += 1
                    }
                },
                onSubmit = {
                    val safeWeight = parsedWeightKg ?: return@PackageRequestFormActions
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
            )
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
                    0 -> WhatItIsStep(
                        savedDescriptions = savedDescriptions,
                        onPickRecentDescription = { description = savedDescriptions.first() },
                        selectedPhotoUris = selectedPhotoUris,
                        onPickPhotos = { photoPickerLauncher.launch("image/*") },
                    )

                    1 -> PickupInformationSection(
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
                        addressValue = pickupAddress,
                        onAddressChange = { pickupAddress = it },
                        pickupDateFormatted = pickupDateFormatted,
                        pickupTimeFormatted = pickupTimeFormatted,
                        onPickupDateClick = { showDatePicker(context, pickupDate) { pickupDate = it } },
                        onPickupTimeClick = { showTimePicker(context, pickupTime) { pickupTime = it } },
                        pickupDateFlexible = pickupDateFlexible,
                        onPickupDateFlexibleChange = { pickupDateFlexible = it },
                        hasSavedTemplate = savedPickupTemplates.isNotEmpty(),
                        onUseSavedTemplate = {
                            val template = savedPickupTemplates.first()
                            pickupCountry = CountryCatalog.byCode(template.pickupCountryCode)
                            pickupCity = template.pickupCity
                            pickupAddress = template.pickupAddress.orEmpty()
                            pickupCitySuggestions = emptyList()
                        },
                    )

                    2 -> DeliveryInformationSection(
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
                        addressValue = deliveryAddress,
                        onAddressChange = { deliveryAddress = it },
                        deliveryDateFormatted = deliveryDateFormatted,
                        deliveryTimeFormatted = deliveryTimeFormatted,
                        onDeliveryDateClick = { showDatePicker(context, deliveryDate) { deliveryDate = it } },
                        onDeliveryTimeClick = { showTimePicker(context, deliveryTime) { deliveryTime = it } },
                        hasSavedTemplate = savedHandoffTemplates.isNotEmpty(),
                        onUseSavedTemplate = {
                            val template = savedHandoffTemplates.first()
                            deliveryCountry = CountryCatalog.byCode(template.deliveryCountryCode)
                            deliveryCity = template.deliveryCity
                            deliveryAddress = template.deliveryAddress.orEmpty()
                            deliveryCitySuggestions = emptyList()
                        },
                    )

                    else -> ReviewPlaceholder()
                }
            } else {
                PackageReviewFormSection(
                    packageTypeValue = packageTypeLabelsByCode.getValue(packageType.code),
                    packageTypeOptions = packageTypeOptions.map { packageTypeLabelsByCode.getValue(it.code) },
                    onPackageTypeSelected = { selectedLabel ->
                        val selectedCode = packageTypeLabelsByCode.entries
                            .firstOrNull { it.value == selectedLabel }
                            ?.key
                            ?: packageTypeOptions.first().code
                        packageType = packageTypeOptions.firstOrNull { it.code == selectedCode }
                            ?: packageTypeOptions.first()
                    },
                    urgencyValue = urgencyLabelsByCode.getValue(urgencyLevel.code),
                    urgencyOptions = urgencyOptions.map { urgencyLabelsByCode.getValue(it.code) },
                    onUrgencySelected = { selectedLabel ->
                        val selectedCode = urgencyLabelsByCode.entries
                            .firstOrNull { it.value == selectedLabel }
                            ?.key
                            ?: urgencyOptions.first().code
                        urgencyLevel = urgencyOptions.firstOrNull { it.code == selectedCode }
                            ?: urgencyOptions.first()
                    },
                    description = description,
                    onDescriptionChange = { description = it },
                    weight = weight,
                    onWeightChange = { weight = it },
                    packageValue = packageValue,
                    onPackageValueChange = { packageValue = it },
                    maxBudget = maxBudget,
                    onMaxBudgetChange = { maxBudget = it },
                    specialHandling = specialHandling,
                    onSpecialHandlingChange = { specialHandling = it },
                )
            }
        }
    }

    if (showTutorialOverlay) {
        PackageTutorialOverlay(onDismiss = onDismissTutorial)
    }
}

/**
 * Step-1 wrapper. Inlined here rather than promoted to its own file because
 * it only ties the recent-description shortcut to [PackageImagesSection];
 * nothing else needs it.
 */
@Composable
private fun WhatItIsStep(
    savedDescriptions: List<String>,
    onPickRecentDescription: () -> Unit,
    selectedPhotoUris: List<Uri>,
    onPickPhotos: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        CreationWizardStepHeader(
            icon = Icons.Default.Description,
            title = stringResource(R.string.packages_create_step_what_it_is),
            subtitle = stringResource(R.string.packages_create_what_it_is_hint),
        )
        if (savedDescriptions.isNotEmpty()) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
                text = stringResource(R.string.packages_create_use_recent_description),
                onClick = onPickRecentDescription,
                style = com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PackageImagesSection(
            selectedPhotoUris = selectedPhotoUris,
            onPickPhotos = onPickPhotos,
        )
    }
}

@Composable
private fun ReviewPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        CreationWizardStepHeader(
            icon = Icons.Default.Description,
            title = stringResource(R.string.packages_create_review_title),
            subtitle = stringResource(R.string.packages_create_review_hint),
        )
        Spacer(modifier = Modifier.height(PasabayanSpacing.xxxxl))
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

@Preview(showBackground = true, name = "PackageRequest — light", heightDp = 900)
@Preview(showBackground = true, name = "PackageRequest — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestPreview() {
    PasabayanTheme {
        PackageRequestScreen(onSave = { _, _ -> }, onCancel = {})
    }
}
