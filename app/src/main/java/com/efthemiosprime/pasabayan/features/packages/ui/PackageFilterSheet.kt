package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.components.PackageFilterContent
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter

/**
 * Bottom sheet for carrier-explore filters. Parity with iOS
 * `PackageFiltersSheet` in `CarrierBrowsePackagesView.swift`.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PackageFilterSheet(
    filter: PackageBrowseFilter,
    onDismiss: () -> Unit,
    onUrgencyChange: (UrgencyLevel?) -> Unit,
    onPackageTypeChange: (PackageType?) -> Unit,
    onMaxWeightChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        PDetailSheetScaffold(
            title = stringResource(R.string.dashboard_carrier_filters_title),
            closeContentDescription = stringResource(R.string.dashboard_carrier_filters_close),
            onClose = onDismiss,
        ) {
            PackageFilterContent(
                filter = filter,
                onUrgencyChange = onUrgencyChange,
                onPackageTypeChange = onPackageTypeChange,
                onMaxWeightChange = onMaxWeightChange,
                onMaxPriceChange = onMaxPriceChange,
                onApply = onApply,
                onClear = onClear,
            )
        }
    }
}

@Preview(showBackground = true, name = "PackageFilterSheet — light")
@Preview(showBackground = true, name = "PackageFilterSheet — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageFilterSheetPreview() {
    PasabayanTheme {
        PackageFilterSheet(
            filter = PackageBrowseFilter(urgency = UrgencyLevel.HIGH, maxWeight = "10"),
            onDismiss = {},
            onUrgencyChange = {},
            onPackageTypeChange = {},
            onMaxWeightChange = {},
            onMaxPriceChange = {},
            onApply = {},
            onClear = {},
        )
    }
}
