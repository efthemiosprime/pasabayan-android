package com.efthemiosprime.pasabayan.features.favorites.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.favorites.model.SendRequestUiState
import com.efthemiosprime.pasabayan.features.favorites.viewmodel.SendRequestViewModel

object SendRequestTestTags {
    const val Root = "send_request"
    const val Submit = "send_request_submit"
    const val PickupCity = "send_request_pickup_city"
    const val DeliveryCity = "send_request_delivery_city"
    const val Weight = "send_request_weight"
}

@Composable
fun SendRequestSheet(
    carrierId: Int,
    carrierName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendRequestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            viewModel.consumeMessages()
            onClose()
        }
    }
    SendRequestContent(
        state = state,
        carrierName = carrierName,
        onPickupCityChange = viewModel::onPickupCityChange,
        onPickupAddressChange = viewModel::onPickupAddressChange,
        onPickupDateChange = viewModel::onPickupDateChange,
        onDeliveryCityChange = viewModel::onDeliveryCityChange,
        onDeliveryAddressChange = viewModel::onDeliveryAddressChange,
        onDeliveryDateChange = viewModel::onDeliveryDateChange,
        onPackageTypeChange = viewModel::onPackageTypeChange,
        onPackageDescriptionChange = viewModel::onPackageDescriptionChange,
        onPackageWeightChange = viewModel::onPackageWeightChange,
        onOfferedPriceChange = viewModel::onOfferedPriceChange,
        onMessageChange = viewModel::onMessageChange,
        onCancel = onClose,
        onSubmit = { viewModel.submit(carrierId) },
        modifier = modifier,
    )
}

@Composable
fun SendRequestContent(
    state: SendRequestUiState,
    carrierName: String,
    onPickupCityChange: (String) -> Unit,
    onPickupAddressChange: (String) -> Unit,
    onPickupDateChange: (String) -> Unit,
    onDeliveryCityChange: (String) -> Unit,
    onDeliveryAddressChange: (String) -> Unit,
    onDeliveryDateChange: (String) -> Unit,
    onPackageTypeChange: (String) -> Unit,
    onPackageDescriptionChange: (String) -> Unit,
    onPackageWeightChange: (String) -> Unit,
    onOfferedPriceChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
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
            .testTag(SendRequestTestTags.Root),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.favorites_send_request_title),
                style = PasabayanTextStyles.Heading.h3,
            )
            Text(
                text = stringResource(R.string.favorites_send_request_subtitle, carrierName),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PickupSection(
            state = state,
            onCityChange = onPickupCityChange,
            onAddressChange = onPickupAddressChange,
            onDateChange = onPickupDateChange,
        )
        DeliverySection(
            state = state,
            onCityChange = onDeliveryCityChange,
            onAddressChange = onDeliveryAddressChange,
            onDateChange = onDeliveryDateChange,
        )
        PackageSection(
            state = state,
            onTypeChange = onPackageTypeChange,
            onDescriptionChange = onPackageDescriptionChange,
            onWeightChange = onPackageWeightChange,
            onPriceChange = onOfferedPriceChange,
        )
        MessageSection(state, onMessageChange)
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
                text = stringResource(R.string.favorites_send_request_cancel),
                onClick = onCancel,
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f),
            )
            if (state.isSubmitting) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PCircularProgress()
                }
            } else {
                PButton(
                    text = stringResource(R.string.favorites_send_request_submit),
                    onClick = onSubmit,
                    enabled = state.isReadyToSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(SendRequestTestTags.Submit),
                )
            }
        }
    }
}

@Composable
private fun PickupSection(
    state: SendRequestUiState,
    onCityChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.favorites_send_request_pickup_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.pickupCity,
                onValueChange = onCityChange,
                label = { Text(stringResource(R.string.favorites_send_request_pickup_city)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SendRequestTestTags.PickupCity),
            )
            POutlinedTextField(
                value = state.pickupAddress,
                onValueChange = onAddressChange,
                label = { Text(stringResource(R.string.favorites_send_request_pickup_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = state.pickupDate,
                onValueChange = onDateChange,
                label = { Text(stringResource(R.string.favorites_send_request_pickup_date)) },
                placeholder = {
                    Text(stringResource(R.string.favorites_send_request_date_placeholder))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DeliverySection(
    state: SendRequestUiState,
    onCityChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.favorites_send_request_delivery_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.deliveryCity,
                onValueChange = onCityChange,
                label = { Text(stringResource(R.string.favorites_send_request_delivery_city)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SendRequestTestTags.DeliveryCity),
            )
            POutlinedTextField(
                value = state.deliveryAddress,
                onValueChange = onAddressChange,
                label = { Text(stringResource(R.string.favorites_send_request_delivery_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = state.deliveryDate,
                onValueChange = onDateChange,
                label = { Text(stringResource(R.string.favorites_send_request_delivery_date)) },
                placeholder = {
                    Text(stringResource(R.string.favorites_send_request_date_placeholder))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PackageSection(
    state: SendRequestUiState,
    onTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.favorites_send_request_package_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.packageType,
                onValueChange = onTypeChange,
                label = { Text(stringResource(R.string.favorites_send_request_package_type)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = state.packageDescription,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.favorites_send_request_package_description)) },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = state.packageWeightKg,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.favorites_send_request_package_weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SendRequestTestTags.Weight),
            )
            POutlinedTextField(
                value = state.offeredPrice,
                onValueChange = onPriceChange,
                label = { Text(stringResource(R.string.favorites_send_request_offered_price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MessageSection(
    state: SendRequestUiState,
    onMessageChange: (String) -> Unit,
) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            Text(
                text = stringResource(R.string.favorites_send_request_message_section),
                style = PasabayanTextStyles.Heading.h5,
            )
            POutlinedTextField(
                value = state.shipperMessage,
                onValueChange = onMessageChange,
                label = { Text(stringResource(R.string.favorites_send_request_message)) },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "SendRequest — light")
@Preview(showBackground = true, name = "SendRequest — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SendRequestPreview() {
    PasabayanTheme {
        SendRequestContent(
            state = SendRequestUiState(
                pickupCity = "Montreal",
                deliveryCity = "Toronto",
                pickupDate = "2026-06-01",
                deliveryDate = "2026-06-03",
                packageDescription = "Books",
                packageWeightKg = "2",
            ),
            carrierName = "Alex Stewart",
            onPickupCityChange = {},
            onPickupAddressChange = {},
            onPickupDateChange = {},
            onDeliveryCityChange = {},
            onDeliveryAddressChange = {},
            onDeliveryDateChange = {},
            onPackageTypeChange = {},
            onPackageDescriptionChange = {},
            onPackageWeightChange = {},
            onOfferedPriceChange = {},
            onMessageChange = {},
            onCancel = {},
            onSubmit = {},
        )
    }
}
