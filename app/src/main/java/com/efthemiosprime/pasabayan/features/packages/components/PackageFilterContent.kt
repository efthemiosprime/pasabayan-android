package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter

/**
 * Content for the carrier-explore filter bottom sheet. iOS parity with
 * `PackageFiltersSheet` in `CarrierBrowsePackagesView.swift` — urgency,
 * package type, max weight, max price, plus clear / apply actions.
 *
 * State is hoisted: the caller owns the filter and a per-field change
 * callback. iOS uses `@Binding` for the same effect.
 */
@Composable
fun PackageFilterContent(
    filter: PackageBrowseFilter,
    onUrgencyChange: (UrgencyLevel?) -> Unit,
    onPackageTypeChange: (PackageType?) -> Unit,
    onMaxWeightChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        // Urgency
        SectionLabel(stringResource(R.string.dashboard_carrier_filters_urgency_section))
        ChipRow {
            PFilterChip(
                label = stringResource(R.string.dashboard_carrier_filters_urgency_all),
                selected = filter.urgency == null,
                onClick = { onUrgencyChange(null) },
            )
            UrgencyLevel.values().forEach { level ->
                PFilterChip(
                    label = level.displayLabel(),
                    selected = filter.urgency == level,
                    onClick = { onUrgencyChange(level) },
                )
            }
        }

        // Package type
        SectionLabel(stringResource(R.string.dashboard_carrier_filters_package_type_section))
        ChipRow {
            PFilterChip(
                label = stringResource(R.string.dashboard_carrier_filters_package_type_all),
                selected = filter.packageType == null,
                onClick = { onPackageTypeChange(null) },
            )
            PackageType.values().forEach { type ->
                PFilterChip(
                    label = type.displayLabel(),
                    selected = filter.packageType == type,
                    onClick = { onPackageTypeChange(type) },
                )
            }
        }

        // Weight + price
        SectionLabel(stringResource(R.string.dashboard_carrier_filters_weight_price_section))
        POutlinedTextField(
            value = filter.maxWeight,
            onValueChange = onMaxWeightChange,
            label = { Text(stringResource(R.string.dashboard_carrier_filters_max_weight_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        POutlinedTextField(
            value = filter.maxPrice,
            onValueChange = onMaxPriceChange,
            label = { Text(stringResource(R.string.dashboard_carrier_filters_max_price_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        PButton(
            text = stringResource(R.string.dashboard_carrier_filters_apply),
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
        )
        PButton(
            text = stringResource(R.string.dashboard_carrier_filters_clear_all),
            onClick = onClear,
            style = PButtonStyle.Tertiary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = PasabayanTextStyles.Caption.large,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        content()
    }
}

private fun UrgencyLevel.displayLabel(): String {
    val name = name.lowercase().replaceFirstChar { it.uppercase() }
    return "$icon $name"
}

private fun PackageType.displayLabel(): String {
    val name = name.lowercase().replaceFirstChar { it.uppercase() }
    return "$icon $name"
}

@Preview(showBackground = true, name = "PackageFilter — light", heightDp = 800)
@Preview(showBackground = true, name = "PackageFilter — dark", heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageFilterContentPreview() {
    PasabayanTheme {
        PackageFilterContent(
            filter = PackageBrowseFilter(
                urgency = UrgencyLevel.URGENT,
                packageType = PackageType.ELECTRONICS,
                maxWeight = "10",
                maxPrice = "200",
            ),
            onUrgencyChange = {},
            onPackageTypeChange = {},
            onMaxWeightChange = {},
            onMaxPriceChange = {},
            onApply = {},
            onClear = {},
        )
    }
}
