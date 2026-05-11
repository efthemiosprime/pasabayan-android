package com.efthemiosprime.pasabayan.features.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.profile.model.CarrierPackageType
import com.efthemiosprime.pasabayan.features.profile.model.CarrierRestrictedItem
import com.efthemiosprime.pasabayan.features.profile.model.EditCarrierProfileField
import com.efthemiosprime.pasabayan.features.profile.model.EditCarrierProfileUiState
import com.efthemiosprime.pasabayan.features.profile.viewmodel.EditCarrierProfileViewModel

object EditCarrierProfileTestTags {
    const val Root = "edit_carrier_profile"
    const val MaxWeight = "edit_carrier_max_weight"
    const val MaxSpace = "edit_carrier_max_space"
    const val Price = "edit_carrier_price"
    const val Insurance = "edit_carrier_insurance"
    const val Bio = "edit_carrier_bio"
    const val Save = "edit_carrier_save"
}

@Composable
fun EditCarrierProfileSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileUpdated: () -> Unit = {},
    viewModel: EditCarrierProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.initialize() }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            onProfileUpdated()
            viewModel.consumeMessages()
            onClose()
        }
    }
    EditCarrierProfileContent(
        state = state,
        onMaxWeightChange = viewModel::onMaxWeightChange,
        onMaxSpaceChange = viewModel::onMaxSpaceChange,
        onPricePerKgChange = viewModel::onPricePerKgChange,
        onInsuranceChange = viewModel::onInsuranceChange,
        onBioChange = viewModel::onBioChange,
        onTogglePackageType = viewModel::togglePackageType,
        onToggleRestrictedItem = viewModel::toggleRestrictedItem,
        onCancel = onClose,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
fun EditCarrierProfileContent(
    state: EditCarrierProfileUiState,
    onMaxWeightChange: (String) -> Unit,
    onMaxSpaceChange: (String) -> Unit,
    onPricePerKgChange: (String) -> Unit,
    onInsuranceChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onTogglePackageType: (String) -> Unit,
    onToggleRestrictedItem: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.md,
            )
            .testTag(EditCarrierProfileTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.profile_carrier_edit_title),
            style = PasabayanTextStyles.Heading.h3,
        )
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                PCircularProgress()
            }
            return@Column
        }
        CapacitySection(state, onMaxWeightChange, onMaxSpaceChange)
        PricingSection(state, onPricePerKgChange, onInsuranceChange)
        BioSection(state, onBioChange)
        SelectionSection(
            titleRes = R.string.profile_carrier_section_package_types,
            helperRes = R.string.profile_carrier_package_types_helper,
            options = CarrierPackageType.entries.map { it.raw to stringResource(it.labelRes) },
            selected = state.selectedPackageTypes,
            onToggle = onTogglePackageType,
        )
        SelectionSection(
            titleRes = R.string.profile_carrier_section_restricted,
            helperRes = R.string.profile_carrier_restricted_helper,
            options = CarrierRestrictedItem.entries.map { it.raw to stringResource(it.labelRes) },
            selected = state.selectedRestrictedItems,
            onToggle = onToggleRestrictedItem,
            destructive = true,
        )
        state.errorMessage?.let { msg ->
            PCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = PasabayanTextStyles.Body.small,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            PButton(
                text = stringResource(R.string.profile_carrier_cancel),
                onClick = onCancel,
                enabled = !state.isFormBusy,
                modifier = Modifier.weight(1f),
            )
            PButton(
                text = stringResource(R.string.profile_carrier_save),
                onClick = onSave,
                enabled = !state.isFormBusy,
                modifier = Modifier
                    .weight(1f)
                    .testTag(EditCarrierProfileTestTags.Save),
            )
        }
        if (state.isSaving) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PCircularProgress()
            }
        }
    }
}

@Composable
private fun CapacitySection(
    state: EditCarrierProfileUiState,
    onMaxWeightChange: (String) -> Unit,
    onMaxSpaceChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_carrier_section_capacity),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.maxWeightKg,
                onValueChange = onMaxWeightChange,
                label = { Text(stringResource(R.string.profile_carrier_max_weight_label)) },
                placeholder = { Text(stringResource(R.string.profile_carrier_max_weight_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.profile_carrier_max_weight_helper))
                },
                isError = state.fieldHasError(EditCarrierProfileField.MAX_WEIGHT),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditCarrierProfileTestTags.MaxWeight),
            )
            POutlinedTextField(
                value = state.maxSpaceLiters,
                onValueChange = onMaxSpaceChange,
                label = { Text(stringResource(R.string.profile_carrier_max_space_label)) },
                placeholder = { Text(stringResource(R.string.profile_carrier_max_space_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.profile_carrier_max_space_helper))
                },
                isError = state.fieldHasError(EditCarrierProfileField.MAX_SPACE),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditCarrierProfileTestTags.MaxSpace),
            )
        }
    }
}

@Composable
private fun PricingSection(
    state: EditCarrierProfileUiState,
    onPricePerKgChange: (String) -> Unit,
    onInsuranceChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_carrier_section_pricing),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.pricePerKgCad,
                onValueChange = onPricePerKgChange,
                label = { Text(stringResource(R.string.profile_carrier_price_label)) },
                placeholder = { Text(stringResource(R.string.profile_carrier_price_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.profile_carrier_price_helper))
                },
                isError = state.fieldHasError(EditCarrierProfileField.PRICE),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditCarrierProfileTestTags.Price),
            )
            POutlinedTextField(
                value = state.insuranceCoverageCad,
                onValueChange = onInsuranceChange,
                label = { Text(stringResource(R.string.profile_carrier_insurance_label)) },
                placeholder = { Text(stringResource(R.string.profile_carrier_insurance_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.profile_carrier_insurance_helper))
                },
                isError = state.fieldHasError(EditCarrierProfileField.INSURANCE),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditCarrierProfileTestTags.Insurance),
            )
        }
    }
}

@Composable
private fun BioSection(
    state: EditCarrierProfileUiState,
    onBioChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.profile_carrier_section_bio),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.bio,
                onValueChange = onBioChange,
                label = { Text(stringResource(R.string.profile_carrier_section_bio)) },
                singleLine = false,
                maxLines = 6,
                supportingText = {
                    Text(stringResource(R.string.profile_carrier_bio_helper))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditCarrierProfileTestTags.Bio),
            )
            Text(
                text = stringResource(R.string.profile_carrier_bio_counter, state.bio.length, 500),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SelectionSection(
    titleRes: Int,
    helperRes: Int,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    destructive: Boolean = false,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(text = stringResource(titleRes), style = PasabayanTextStyles.Heading.h5)
            Text(
                text = stringResource(helperRes),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Two-column grid using nested Rows — keeps this composable JVM-previewable without
            // pulling in foundation.lazy.grid (which would need explicit height constraints inside
            // a vertically scrolling Column).
            options.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                ) {
                    row.forEach { (raw, label) ->
                        SelectionChip(
                            label = label,
                            isSelected = raw in selected,
                            destructive = destructive,
                            onClick = { onToggle(raw) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) {
                        // Keep the trailing column slot stable when row count is odd.
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionChip(
    label: String,
    isSelected: Boolean,
    destructive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent =
        if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val container =
        if (isSelected) {
            if (destructive) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    val content =
        if (isSelected) {
            if (destructive) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val shape = RoundedCornerShape(PasabayanRadius.sm)
    Box(
        modifier = modifier
            .clip(shape)
            .background(container, shape)
            .border(1.dp, accent.copy(alpha = if (isSelected) 1f else 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = PasabayanSpacing.sm,
                vertical = PasabayanSpacing.sm,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = content,
        )
    }
}

@Preview(showBackground = true, name = "EditCarrier — light")
@Preview(showBackground = true, name = "EditCarrier — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditCarrierProfilePreview() {
    PasabayanTheme {
        EditCarrierProfileContent(
            state = EditCarrierProfileUiState(
                maxWeightKg = "50",
                maxSpaceLiters = "100",
                pricePerKgCad = "5.50",
                insuranceCoverageCad = "5000",
                bio = "Experienced courier serving Toronto to Montreal.",
                selectedPackageTypes = setOf("Electronics", "Documents"),
                selectedRestrictedItems = setOf("Hazardous Materials"),
                isInitialized = true,
                hasExistingProfile = true,
            ),
            onMaxWeightChange = {},
            onMaxSpaceChange = {},
            onPricePerKgChange = {},
            onInsuranceChange = {},
            onBioChange = {},
            onTogglePackageType = {},
            onToggleRestrictedItem = {},
            onCancel = {},
            onSave = {},
        )
    }
}
