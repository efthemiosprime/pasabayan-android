package com.efthemiosprime.pasabayan.features.packages.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDateTimeField
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField

@Composable
fun PackageRequestScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickupCity by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var deliveryCity by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.packages_create_title),
            style = PasabayanTextStyles.Heading.h3,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Pickup section
        PExpandableSection(
            title = stringResource(R.string.packages_detail_pickup),
            initiallyExpanded = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                POutlinedTextField(
                    value = pickupCity,
                    onValueChange = { pickupCity = it },
                    label = { Text(stringResource(R.string.packages_create_pickup_city)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text(stringResource(R.string.packages_create_pickup_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Delivery section
        PExpandableSection(title = stringResource(R.string.packages_detail_delivery)) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                POutlinedTextField(
                    value = deliveryCity,
                    onValueChange = { deliveryCity = it },
                    label = { Text(stringResource(R.string.packages_create_delivery_city)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text(stringResource(R.string.packages_create_delivery_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Package details section
        PExpandableSection(title = stringResource(R.string.packages_detail_type)) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                POutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.packages_create_weight)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.packages_detail_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                )
            }
        }

        // Dates section
        PExpandableSection(title = stringResource(R.string.packages_detail_delivery)) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                PDateTimeField(
                    value = pickupDate,
                    onClick = { /* TODO: DatePickerDialog */ },
                    label = stringResource(R.string.packages_create_pickup_date),
                    modifier = Modifier.fillMaxWidth(),
                )
                PDateTimeField(
                    value = deliveryDate,
                    onClick = { /* TODO: DatePickerDialog */ },
                    label = stringResource(R.string.packages_create_delivery_date),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Actions
        PButton(
            text = stringResource(R.string.packages_create_package),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
        PButton(
            text = stringResource(R.string.packages_cancel_package),
            onClick = onCancel,
            style = PButtonStyle.Tertiary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, name = "PackageRequest — light", heightDp = 900)
@Preview(showBackground = true, name = "PackageRequest — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageRequestPreview() {
    PasabayanTheme {
        PackageRequestScreen(onSave = {}, onCancel = {})
    }
}
