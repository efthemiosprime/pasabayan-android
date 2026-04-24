package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow

@Composable
fun PackageDetailsSection(
    weightKg: Double?,
    dimensions: String?,
    packageType: String?,
    isFragile: Boolean,
    description: String?,
    specialHandling: String?,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.packages_detail_type),
                style = PasabayanTextStyles.Heading.h6,
                color = MaterialTheme.colorScheme.onSurface,
            )
            weightKg?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_weight),
                    value = stringResource(R.string.packages_detail_weight_value, it),
                )
            }
            dimensions?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_dimensions),
                    value = it,
                )
            }
            packageType?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_type),
                    value = it,
                )
            }
            if (isFragile) {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_fragile),
                    value = stringResource(R.string.packages_detail_fragile_yes),
                )
            }
            description?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_description),
                    value = it,
                )
            }
            specialHandling?.let {
                PDetailRow(
                    label = stringResource(R.string.packages_detail_handling),
                    value = it,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "PackageDetails — light")
@Preview(showBackground = true, name = "PackageDetails — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageDetailsSectionPreview() {
    PasabayanTheme {
        PackageDetailsSection(
            weightKg = 12.5,
            dimensions = "30.0×20.0×15.0 cm",
            packageType = "Electronics",
            isFragile = true,
            description = "Laptop and accessories",
            specialHandling = "Keep upright",
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
