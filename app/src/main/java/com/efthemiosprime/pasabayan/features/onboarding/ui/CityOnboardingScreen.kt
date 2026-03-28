package com.efthemiosprime.pasabayan.features.onboarding.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.onboarding.model.CityPickerOption
import com.efthemiosprime.pasabayan.features.onboarding.viewmodel.CityOnboardingUiState
import com.efthemiosprime.pasabayan.features.onboarding.viewmodel.CityOnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityOnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityOnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadCities() }
    CityOnboardingScreen(
        state = state,
        modifier = modifier,
        onRetryLoad = { viewModel.loadCities() },
        onOpenPicker = { viewModel.setShowPickerSheet(true) },
        onDismissPicker = { viewModel.setShowPickerSheet(false) },
        onSearchQueryChange = viewModel::setSearchQuery,
        onSelectCity = viewModel::selectCity,
        onUseMyLocation = { viewModel.onUseMyLocationStub() },
        onContinue = { viewModel.saveAndContinue(onFinished) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityOnboardingScreen(
    state: CityOnboardingUiState,
    onRetryLoad: () -> Unit,
    onOpenPicker: () -> Unit,
    onDismissPicker: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CityPickerOption) -> Unit,
    onUseMyLocation: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val filtered = remember(state.cities, state.searchQuery) {
        val q = state.searchQuery.trim().lowercase()
        if (q.isEmpty()) state.cities
        else state.cities.filter { it.display.lowercase().contains(q) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PasabayanSpacing.screenPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(bottom = PasabayanSpacing.xl),
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.onboarding_city_title),
                style = PasabayanTextStyles.Heading.h3,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
            Text(
                text = stringResource(R.string.onboarding_city_description),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            PCard(variant = PCardVariant.Secondary) {
                Text(
                    text = stringResource(R.string.onboarding_city_infocard),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(PasabayanSpacing.md),
                )
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            PButton(
                text = stringResource(R.string.onboarding_city_usemylocation),
                onClick = onUseMyLocation,
                style = PButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.LocationOn,
            )
            if (state.showLocationStubMessage) {
                Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
                Text(
                    text = stringResource(R.string.onboarding_city_location_stub),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.lg))
            Text(
                text = stringResource(R.string.onboarding_city_selectlabel),
                style = PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPicker),
            ) {
                POutlinedTextField(
                    value = state.selectedCity?.display.orEmpty(),
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.onboarding_city_sheettitle)) },
                    placeholder = { Text(stringResource(R.string.onboarding_city_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
            when {
                state.isLoadingCities -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PCircularProgress()
                        Spacer(modifier = Modifier.width(PasabayanSpacing.md))
                        Text(
                            text = stringResource(R.string.onboarding_city_loading),
                            style = PasabayanTextStyles.Body.medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                state.loadErrorMessage != null -> {
                    Text(
                        text = state.loadErrorMessage!!,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
                    PButton(
                        text = stringResource(R.string.onboarding_city_retry),
                        onClick = onRetryLoad,
                        style = PButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            state.saveErrorMessage?.let { err ->
                Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
                Text(
                    text = err,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.xl))
            PButton(
                text = stringResource(R.string.onboarding_city_continue),
                onClick = onContinue,
                style = PButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoadingCities && state.loadErrorMessage == null && !state.isSaving,
                isLoading = state.isSaving,
            )
        }
    }

    if (state.showPickerSheet) {
        PModalBottomSheet(onDismissRequest = onDismissPicker) {
            Text(
                text = stringResource(R.string.onboarding_city_sheettitle),
                style = PasabayanTextStyles.Heading.h5,
                modifier = Modifier.padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
            )
            POutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(stringResource(R.string.onboarding_city_searchprompt)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PasabayanSpacing.md),
            )
            Spacer(modifier = Modifier.height(PasabayanSpacing.sm))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .padding(horizontal = PasabayanSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                items(filtered, key = { it.id }) { city ->
                    val selected = state.selectedCity?.id == city.id
                    Text(
                        text = city.display,
                        style = if (selected) {
                            PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold)
                        } else {
                            PasabayanTextStyles.Body.medium
                        },
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCity(city) }
                            .padding(vertical = PasabayanSpacing.sm),
                    )
                }
            }
            Spacer(modifier = Modifier.height(PasabayanSpacing.md))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "City onboarding — ready", heightDp = 900, widthDp = 411)
@Composable
private fun CityOnboardingScreenPreview_Ready() {
    PasabayanTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            CityOnboardingScreen(
                state = CityOnboardingUiState(
                    cities = listOf(
                        CityPickerOption(101, "Toronto, ON"),
                        CityPickerOption(102, "Montreal, QC"),
                    ),
                    selectedCity = CityPickerOption(101, "Toronto, ON"),
                    isLoadingCities = false,
                    showLocationStubMessage = true,
                ),
                onRetryLoad = {},
                onOpenPicker = {},
                onDismissPicker = {},
                onSearchQueryChange = {},
                onSelectCity = {},
                onUseMyLocation = {},
                onContinue = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "City onboarding — loading", heightDp = 900, widthDp = 411)
@Composable
private fun CityOnboardingScreenPreview_Loading() {
    PasabayanTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            CityOnboardingScreen(
                state = CityOnboardingUiState(isLoadingCities = true),
                onRetryLoad = {},
                onOpenPicker = {},
                onDismissPicker = {},
                onSearchQueryChange = {},
                onSelectCity = {},
                onUseMyLocation = {},
                onContinue = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "City onboarding — picker sheet", heightDp = 900, widthDp = 411)
@Composable
private fun CityOnboardingScreenPreview_PickerSheet() {
    PasabayanTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            CityOnboardingScreen(
                state = CityOnboardingUiState(
                    cities = listOf(
                        CityPickerOption(101, "Toronto, ON"),
                        CityPickerOption(102, "Montreal, QC"),
                        CityPickerOption(103, "Vancouver, BC"),
                    ),
                    selectedCity = CityPickerOption(101, "Toronto, ON"),
                    isLoadingCities = false,
                    showPickerSheet = true,
                    searchQuery = "mont",
                ),
                onRetryLoad = {},
                onOpenPicker = {},
                onDismissPicker = {},
                onSearchQueryChange = {},
                onSelectCity = {},
                onUseMyLocation = {},
                onContinue = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
