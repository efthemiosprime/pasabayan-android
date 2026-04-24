package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

@Composable
fun EditPackageSheet(
    pkg: PackageRequest,
    onDismiss: () -> Unit,
    onSave: (PackageUpdateRequestJson) -> Unit,
    modifier: Modifier = Modifier,
) {
    var maxBudget by remember { mutableStateOf(pkg.maxPriceBudget?.toString().orEmpty()) }
    var urgency by remember { mutableStateOf(pkg.urgencyLevel?.name?.lowercase().orEmpty()) }
    var deliveryDate by remember { mutableStateOf(pkg.deliveryDateNeeded.orEmpty()) }
    var specialHandling by remember { mutableStateOf(pkg.specialHandlingRequirements.orEmpty()) }

    PDetailSheetScaffold(
        title = stringResource(R.string.packages_edit_package),
        closeContentDescription = stringResource(R.string.packages_create_close),
        onClose = onDismiss,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            POutlinedTextField(
                value = maxBudget,
                onValueChange = { maxBudget = it },
                label = { Text(stringResource(R.string.packages_create_budget)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = urgency,
                onValueChange = { urgency = it },
                label = { Text(stringResource(R.string.packages_create_urgency)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = deliveryDate,
                onValueChange = { deliveryDate = it },
                label = { Text(stringResource(R.string.packages_create_delivery_date)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = specialHandling,
                onValueChange = { specialHandling = it },
                label = { Text(stringResource(R.string.packages_create_special_handling)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
            )
            PButton(
                text = stringResource(R.string.packages_edit_package),
                onClick = {
                    onSave(
                        PackageUpdateRequestJson(
                            maxPriceBudget = maxBudget.toDoubleOrNull(),
                            urgencyLevel = urgency.trim().ifBlank { null },
                            deliveryDateNeeded = deliveryDate.trim().ifBlank { null },
                            specialHandlingRequirements = specialHandling.trim().ifBlank { null },
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.packages_cancel_package),
                onClick = onDismiss,
                style = PButtonStyle.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "EditPackageSheet - light")
@Preview(showBackground = true, name = "EditPackageSheet - dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditPackageSheetPreview() {
    PasabayanTheme {
        EditPackageSheet(
            pkg = PackageRequest(
                id = 1,
                shipperId = 5,
                pickupAddress = "123 Main",
                pickupCity = "Toronto",
                pickupCountry = "Canada",
                deliveryAddress = "456 Oak",
                deliveryCity = "Montreal",
                deliveryCountry = "Canada",
                packageWeightKg = 12.0,
                packageDimensions = null,
                packageType = null,
                fragile = false,
                packageValue = null,
                packageDescription = "Books",
                urgencyLevel = null,
                maxPriceBudget = 25.0,
                pickupDatePreferred = "2026-04-12",
                pickupTimePreferred = null,
                pickupDateFlexible = false,
                deliveryDateNeeded = "2026-04-15",
                deliveryTimeNeeded = null,
                specialHandlingRequirements = null,
                requestStatus = null,
                createdAt = null,
                updatedAt = null,
                compatibleTripsCount = null,
                shipper = null,
                images = null,
                imagesProcessing = null,
                serviceType = null,
                shoppingList = null,
                storeName = null,
                storeAddress = null,
                receiptRequired = null,
            ),
            onDismiss = {},
            onSave = {},
        )
    }
}
