package com.efthemiosprime.pasabayan.features.packages.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

/**
 * Review-step form on the shipper package-create wizard — collects the
 * remaining package descriptors (type, urgency, description, weight,
 * declared value, max budget, special handling).
 *
 * iOS parity: `Views/Components/PackageRequest/PackageDetailsSection.swift`
 * + the review-screen card on `PackageRequestView.swift`.
 */
@Composable
internal fun PackageReviewFormSection(
    packageTypeValue: String,
    packageTypeOptions: List<String>,
    onPackageTypeSelected: (String) -> Unit,
    urgencyValue: String,
    urgencyOptions: List<String>,
    onUrgencySelected: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    packageValue: String,
    onPackageValueChange: (String) -> Unit,
    maxBudget: String,
    onMaxBudgetChange: (String) -> Unit,
    specialHandling: String,
    onSpecialHandlingChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        PExpandableSection(
            title = stringResource(R.string.packages_create_section_about_package),
            initiallyExpanded = true,
        ) {
            PackageRequestDropdownField(
                label = stringResource(R.string.packages_create_package_type),
                value = packageTypeValue,
                options = packageTypeOptions,
                onSelected = onPackageTypeSelected,
            )
            PackageRequestDropdownField(
                label = stringResource(R.string.packages_create_urgency),
                value = urgencyValue,
                options = urgencyOptions,
                onSelected = onUrgencySelected,
            )
            POutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.packages_create_description)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.packages_create_weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = packageValue,
                onValueChange = onPackageValueChange,
                label = { Text(stringResource(R.string.packages_create_value)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = maxBudget,
                onValueChange = onMaxBudgetChange,
                label = { Text(stringResource(R.string.packages_create_budget)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = specialHandling,
                onValueChange = onSpecialHandlingChange,
                label = { Text(stringResource(R.string.packages_create_special_handling)) },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "ReviewForm — light")
@Preview(showBackground = true, name = "ReviewForm — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageReviewFormSectionPreview() {
    PasabayanTheme {
        PackageReviewFormSection(
            packageTypeValue = "General",
            packageTypeOptions = listOf("General", "Electronics"),
            onPackageTypeSelected = {},
            urgencyValue = "Normal",
            urgencyOptions = listOf("Low", "Normal", "High"),
            onUrgencySelected = {},
            description = "Box of books",
            onDescriptionChange = {},
            weight = "5.0",
            onWeightChange = {},
            packageValue = "100",
            onPackageValueChange = {},
            maxBudget = "30",
            onMaxBudgetChange = {},
            specialHandling = "",
            onSpecialHandlingChange = {},
        )
    }
}
