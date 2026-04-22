package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CreateTripFromPackageViewModel

@Composable
fun CreateTripFromPackageScreen(
    packageId: Int,
    onClose: () -> Unit,
    onTripCreated: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateTripFromPackageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var departureDate by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf("") }
    var availableWeightKg by remember { mutableStateOf("") }
    var availableSpaceLiters by remember { mutableStateOf("") }
    var transportMethod by remember { mutableStateOf("car") }
    var proposedPrice by remember { mutableStateOf("") }
    var requestMessage by remember { mutableStateOf("") }

    LaunchedEffect(packageId) {
        viewModel.loadTemplate(packageId)
    }

    LaunchedEffect(state.template) {
        val template = state.template ?: return@LaunchedEffect
        departureDate = template.suggestedDepartureDate.orEmpty()
        arrivalDate = template.suggestedArrivalDate.orEmpty()
        availableWeightKg = template.suggestedWeightKg?.toString().orEmpty()
        availableSpaceLiters = template.suggestedSpaceLiters?.toString().orEmpty()
    }

    LaunchedEffect(state.createdTrip?.id) {
        val tripId = state.createdTrip?.id ?: return@LaunchedEffect
        onTripCreated(tripId)
        viewModel.clearCreatedTrip()
    }

    PDetailSheetScaffold(
        title = stringResource(R.string.trips_create_from_package_title),
        closeContentDescription = stringResource(R.string.trips_detail_close),
        onClose = onClose,
        modifier = modifier,
    ) {
        val template = state.template
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            TemplateSummary(template = template)
            POutlinedTextField(
                value = departureDate,
                onValueChange = { departureDate = it },
                label = { Text(stringResource(R.string.trips_create_departure_date)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = arrivalDate,
                onValueChange = { arrivalDate = it },
                label = { Text(stringResource(R.string.trips_create_arrival_date)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = availableWeightKg,
                onValueChange = { availableWeightKg = it },
                label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = availableSpaceLiters,
                onValueChange = { availableSpaceLiters = it },
                label = { Text(stringResource(R.string.trips_create_space_capacity)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = transportMethod,
                onValueChange = { transportMethod = it },
                label = { Text(stringResource(R.string.trips_create_transport_method)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = proposedPrice,
                onValueChange = { proposedPrice = it },
                label = { Text(stringResource(R.string.trips_create_from_package_proposed_price)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = requestMessage,
                onValueChange = { requestMessage = it },
                label = { Text(stringResource(R.string.trips_create_from_package_request_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
            )

            PButton(
                text = stringResource(R.string.trips_create_trip),
                onClick = {
                    val request = buildRequestFromState(
                        packageId = packageId,
                        template = template,
                        departureDate = departureDate,
                        arrivalDate = arrivalDate,
                        availableWeightKg = availableWeightKg,
                        availableSpaceLiters = availableSpaceLiters,
                        transportMethod = transportMethod,
                        proposedPrice = proposedPrice,
                        requestMessage = requestMessage,
                    )
                    viewModel.createTrip(request)
                },
                enabled = !state.isSavingTrip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = PasabayanSpacing.sm),
            )
            PButton(
                text = stringResource(R.string.trips_cancel_trip),
                onClick = onClose,
                style = PButtonStyle.Tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun buildRequestFromState(
    packageId: Int,
    template: TripTemplateData?,
    departureDate: String,
    arrivalDate: String,
    availableWeightKg: String,
    availableSpaceLiters: String,
    transportMethod: String,
    proposedPrice: String,
    requestMessage: String,
): CreateTripFromPackageRequest {
    return CreateTripFromPackageRequest(
        packageId = packageId,
        originCity = template?.originCity.orEmpty(),
        originCountry = template?.originCountry.orEmpty(),
        destinationCity = template?.destinationCity.orEmpty(),
        destinationCountry = template?.destinationCountry.orEmpty(),
        departureDate = departureDate,
        arrivalDate = arrivalDate,
        availableWeightKg = availableWeightKg.toDoubleOrNull() ?: 0.0,
        availableSpaceLiters = availableSpaceLiters.toDoubleOrNull() ?: 0.0,
        transportationMethod = transportMethod,
        pricePerKg = null,
        flatTripPrice = null,
        specialNotes = null,
        pickupAddress = null,
        dropoffAddress = null,
        proposedPrice = proposedPrice.toDoubleOrNull(),
        requestMessage = requestMessage.ifBlank { null },
    )
}

@Composable
private fun TemplateSummary(template: TripTemplateData?) {
    if (template == null) return
    Text(
        text = stringResource(
            R.string.trips_create_from_package_template_route,
            template.originCity,
            template.destinationCity,
        ),
    )
    template.packageDescription?.let {
        Text(text = it)
    }
}

@Preview(showBackground = true, name = "CreateTripFromPackage — light", heightDp = 900)
@Preview(showBackground = true, name = "CreateTripFromPackage — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateTripFromPackagePreview() {
    var departureDate by remember { mutableStateOf("2026-07-01T08:00:00Z") }
    var arrivalDate by remember { mutableStateOf("2026-07-01T16:00:00Z") }
    var weight by remember { mutableStateOf("5") }
    var space by remember { mutableStateOf("25") }
    var method by remember { mutableStateOf("car") }
    var price by remember { mutableStateOf("80") }
    var message by remember { mutableStateOf("Can carry same day.") }
    PasabayanTheme {
        PDetailSheetScaffold(
            title = stringResource(R.string.trips_create_from_package_title),
            closeContentDescription = stringResource(R.string.trips_detail_close),
            onClose = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                TemplateSummary(
                    template = TripTemplateData(
                        packageId = 12,
                        originCity = "Toronto",
                        originCountry = "Canada",
                        destinationCity = "Montreal",
                        destinationCountry = "Canada",
                        suggestedDepartureDate = departureDate,
                        suggestedArrivalDate = arrivalDate,
                        suggestedWeightKg = 5.0,
                        suggestedSpaceLiters = 25.0,
                        packageDescription = "Electronics bundle",
                        packageWeightKg = 3.2,
                        packageUrgencyLevel = "normal",
                    ),
                )
                POutlinedTextField(
                    value = departureDate,
                    onValueChange = { departureDate = it },
                    label = { Text(stringResource(R.string.trips_create_departure_date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = arrivalDate,
                    onValueChange = { arrivalDate = it },
                    label = { Text(stringResource(R.string.trips_create_arrival_date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = space,
                    onValueChange = { space = it },
                    label = { Text(stringResource(R.string.trips_create_space_capacity)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text(stringResource(R.string.trips_create_transport_method)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.trips_create_from_package_proposed_price)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.trips_create_from_package_request_message)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )
            }
        }
    }
}
