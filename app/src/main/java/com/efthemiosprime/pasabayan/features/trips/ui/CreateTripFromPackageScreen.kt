package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PFilterChip
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import com.efthemiosprime.pasabayan.features.trips.components.TripDateTimePicker
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CreateTripFromPackageViewModel

@Composable
fun CreateTripFromPackageScreen(
    packageId: Int,
    userId: Long? = null,
    onClose: () -> Unit,
    onTripCreated: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateTripFromPackageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // iOS parity (`scheduleInformationCard`): epoch-millis state lets us use the existing
    // `TripDateTimePicker` (paired Date + Time pills) and serialize via `formatApiDateTime`.
    var departureMillis by remember { mutableStateOf<Long?>(null) }
    var arrivalMillis by remember { mutableStateOf<Long?>(null) }
    var availableWeightKg by remember { mutableStateOf("") }
    var availableSpaceLiters by remember { mutableStateOf("") }
    // iOS parity: real transport selector instead of a free-text "car" string.
    var transportationMethod by remember { mutableStateOf(TransportationMethod.CAR) }
    // iOS parity (pricingSection): single price input routed to pricePerKg (air/sea) or
    // flatTripPrice (land) based on the selected transport.
    var price by remember { mutableStateOf("") }
    var proposedPrice by remember { mutableStateOf("") }
    var requestMessage by remember { mutableStateOf("") }
    // iOS parity: these were previously dropped on the floor; the form now captures them.
    var pickupAddress by remember { mutableStateOf("") }
    var dropoffAddress by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }

    LaunchedEffect(packageId) {
        viewModel.loadTemplate(packageId)
    }

    LaunchedEffect(state.template) {
        val template = state.template ?: return@LaunchedEffect
        departureMillis = DateTimeParsing.parseApiDateTime(template.suggestedDepartureDate)
        arrivalMillis = DateTimeParsing.parseApiDateTime(template.suggestedArrivalDate)
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
            PackageInfoCard(template = template)
            TripDateTimePicker(
                label = stringResource(R.string.trips_create_departure_date),
                epochMillis = departureMillis,
                onChange = { departureMillis = it },
            )
            TripDateTimePicker(
                label = stringResource(R.string.trips_create_arrival_date),
                epochMillis = arrivalMillis,
                onChange = { arrivalMillis = it },
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
            TransportMethodSelector(
                selected = transportationMethod,
                onSelect = { transportationMethod = it },
            )
            POutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = {
                    Text(
                        stringResource(
                            if (transportationMethod.isLandTransport) {
                                R.string.trips_from_package_price_flat_label
                            } else {
                                R.string.trips_from_package_price_per_kg_label
                            },
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = pickupAddress,
                onValueChange = { pickupAddress = it },
                label = { Text(stringResource(R.string.trips_create_pickup_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = dropoffAddress,
                onValueChange = { dropoffAddress = it },
                label = { Text(stringResource(R.string.trips_create_dropoff_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
            POutlinedTextField(
                value = specialNotes,
                onValueChange = { specialNotes = it },
                label = { Text(stringResource(R.string.trips_create_special_notes)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
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
                        departureDate = departureMillis?.let(DateTimeParsing::formatApiDateTime).orEmpty(),
                        arrivalDate = arrivalMillis?.let(DateTimeParsing::formatApiDateTime).orEmpty(),
                        availableWeightKg = availableWeightKg,
                        availableSpaceLiters = availableSpaceLiters,
                        transportationMethod = transportationMethod,
                        price = price,
                        pickupAddress = pickupAddress,
                        dropoffAddress = dropoffAddress,
                        specialNotes = specialNotes,
                        proposedPrice = proposedPrice,
                        requestMessage = requestMessage,
                    )
                    viewModel.createTrip(request, userId = userId)
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

/**
 * Translates the form state into the wire request. Kept top-level + internal so a unit test can
 * verify field plumbing without standing up a Compose host.
 *
 * iOS parity (`pricingSection`): the single [price] input maps to [CreateTripFromPackageRequest.flatTripPrice]
 * for land transport and to [CreateTripFromPackageRequest.pricePerKg] for air / sea. Blank or
 * unparseable input leaves both null.
 */
internal fun buildRequestFromState(
    packageId: Int,
    template: TripTemplateData?,
    departureDate: String,
    arrivalDate: String,
    availableWeightKg: String,
    availableSpaceLiters: String,
    transportationMethod: TransportationMethod,
    price: String,
    pickupAddress: String,
    dropoffAddress: String,
    specialNotes: String,
    proposedPrice: String,
    requestMessage: String,
): CreateTripFromPackageRequest {
    val parsedPrice = price.toDoubleOrNull()
    val isLand = transportationMethod.isLandTransport
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
        transportationMethod = transportationMethod.name.lowercase(),
        pricePerKg = parsedPrice?.takeIf { !isLand },
        flatTripPrice = parsedPrice?.takeIf { isLand },
        specialNotes = specialNotes.ifBlank { null },
        pickupAddress = pickupAddress.ifBlank { null },
        dropoffAddress = dropoffAddress.ifBlank { null },
        proposedPrice = proposedPrice.toDoubleOrNull(),
        requestMessage = requestMessage.ifBlank { null },
    )
}

@Composable
private fun PackageInfoCard(template: TripTemplateData?) {
    if (template == null) return
    PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PDetailSectionTitle(text = stringResource(R.string.trips_from_package_card_title))
            Text(
                text = stringResource(
                    R.string.trips_create_from_package_template_route,
                    template.originCity,
                    template.destinationCity,
                ),
                style = PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            template.packageWeightKg?.takeIf { it > 0.0 }?.let { weight ->
                PackageInfoRow(
                    icon = Icons.Outlined.Scale,
                    text = stringResource(R.string.trips_from_package_weight_label, weight),
                )
            }
            // iOS parity (`packageInfoCard`): the package type row sits between weight and the
            // fragile/urgent tags. Server returns lowercase ("documents", "fragile", "food").
            template.packageType?.takeIf { it.isNotBlank() }?.let { type ->
                PackageInfoRow(
                    icon = Icons.Outlined.Inventory2,
                    text = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                )
            }
            val isUrgent = template.packageUrgencyLevel?.equals("urgent", ignoreCase = true) == true
            if (template.packageFragile || isUrgent) {
                Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    if (template.packageFragile) {
                        PackageTag(
                            icon = Icons.Outlined.Warning,
                            label = stringResource(R.string.trips_from_package_fragile),
                            color = PasabayanColors.Warning,
                        )
                    }
                    if (isUrgent) {
                        PackageTag(
                            icon = Icons.Outlined.Schedule,
                            label = stringResource(R.string.trips_from_package_urgent),
                            color = PasabayanColors.Error,
                        )
                    }
                }
            }
            template.packageDescription?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PackageInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PackageTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = PasabayanSpacing.sm, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Caption.regular.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun TransportMethodSelector(
    selected: TransportationMethod,
    onSelect: (TransportationMethod) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
        Text(
            text = stringResource(R.string.trips_create_transport_method),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            TRANSPORT_OPTIONS.forEach { method ->
                PFilterChip(
                    label = stringResource(method.toLabelRes()),
                    selected = method == selected,
                    onClick = { onSelect(method) },
                )
            }
        }
    }
}

private val TRANSPORT_OPTIONS = listOf(
    TransportationMethod.FLIGHT,
    TransportationMethod.CAR,
    TransportationMethod.BUS,
    TransportationMethod.TRAIN,
    TransportationMethod.SHIP,
)

@Preview(showBackground = true, name = "CreateTripFromPackage — light", heightDp = 1100)
@Preview(showBackground = true, name = "CreateTripFromPackage — dark", heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateTripFromPackagePreview() {
    var departureMillis by remember {
        mutableStateOf<Long?>(DateTimeParsing.parseApiDateTime("2026-07-01T08:00:00Z"))
    }
    var arrivalMillis by remember {
        mutableStateOf<Long?>(DateTimeParsing.parseApiDateTime("2026-07-01T16:00:00Z"))
    }
    var weight by remember { mutableStateOf("5") }
    var space by remember { mutableStateOf("25") }
    var method by remember { mutableStateOf(TransportationMethod.CAR) }
    var pickupAddress by remember { mutableStateOf("123 Main St") }
    var dropoffAddress by remember { mutableStateOf("456 Oak Ave") }
    var notes by remember { mutableStateOf("Fragile — handle with care") }
    var tripPrice by remember { mutableStateOf("80") }
    var proposedPrice by remember { mutableStateOf("60") }
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
                PackageInfoCard(
                    template = TripTemplateData(
                        packageId = 12,
                        originCity = "Toronto",
                        originCountry = "Canada",
                        destinationCity = "Montreal",
                        destinationCountry = "Canada",
                        suggestedDepartureDate = "2026-07-01T08:00:00Z",
                        suggestedArrivalDate = "2026-07-01T16:00:00Z",
                        suggestedWeightKg = 5.0,
                        suggestedSpaceLiters = 25.0,
                        packageDescription = "Electronics bundle (handle with care).",
                        packageWeightKg = 3.2,
                        packageUrgencyLevel = "urgent",
                        packageFragile = true,
                        packageType = "fragile",
                    ),
                )
                TripDateTimePicker(
                    label = stringResource(R.string.trips_create_departure_date),
                    epochMillis = departureMillis,
                    onChange = { departureMillis = it },
                )
                TripDateTimePicker(
                    label = stringResource(R.string.trips_create_arrival_date),
                    epochMillis = arrivalMillis,
                    onChange = { arrivalMillis = it },
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
                TransportMethodSelector(selected = method, onSelect = { method = it })
                POutlinedTextField(
                    value = tripPrice,
                    onValueChange = { tripPrice = it },
                    label = {
                        Text(
                            stringResource(
                                if (method.isLandTransport) {
                                    R.string.trips_from_package_price_flat_label
                                } else {
                                    R.string.trips_from_package_price_per_kg_label
                                },
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text(stringResource(R.string.trips_create_pickup_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = dropoffAddress,
                    onValueChange = { dropoffAddress = it },
                    label = { Text(stringResource(R.string.trips_create_dropoff_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.trips_create_special_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )
                POutlinedTextField(
                    value = proposedPrice,
                    onValueChange = { proposedPrice = it },
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
