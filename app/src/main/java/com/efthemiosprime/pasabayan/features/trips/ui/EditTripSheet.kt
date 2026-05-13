package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import androidx.compose.runtime.rememberCoroutineScope
import com.efthemiosprime.pasabayan.core.domain.error.DomainError
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import com.efthemiosprime.pasabayan.core.network.DomainErrorMapperException
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.features.trips.components.EditTripCapacitySection
import com.efthemiosprime.pasabayan.features.trips.components.EditTripPricingSection
import com.efthemiosprime.pasabayan.features.trips.components.EditTripRouteSection
import com.efthemiosprime.pasabayan.features.trips.components.EditTripScheduleSection
import com.efthemiosprime.pasabayan.features.trips.components.EditTripStatusSection
import com.efthemiosprime.pasabayan.features.trips.model.CarrierTripActionPolicy
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTripSheet(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (TripUpdateRequestJson) -> Unit,
    onActivate: () -> Unit = {},
    onCancelTrip: suspend () -> Result<Unit> = { Result.success(Unit) },
) {
    val scope = rememberCoroutineScope()
    // iOS parity: route + capacity edits are gated by trip status — only PLANNING trips can
    // change route/dates/capacity/pricing. Notes stay editable regardless. See
    // `EditTripSheet.swift:15` (`TripEditSheetEditingPolicy.allowsFullEdit(for:)`).
    val routeLocked = trip.tripStatus != TripStatus.PLANNING

    var originCity by remember { mutableStateOf(trip.originCity) }
    var originCountry by remember { mutableStateOf(trip.originCountry) }
    var destinationCity by remember { mutableStateOf(trip.destinationCity) }
    var destinationCountry by remember { mutableStateOf(trip.destinationCountry) }
    var pickupAddress by remember { mutableStateOf(trip.pickupAddress.orEmpty()) }
    var dropoffAddress by remember { mutableStateOf(trip.dropoffAddress.orEmpty()) }
    var departureMillis by remember { mutableStateOf(DateTimeParsing.parseApiDateTime(trip.departureDate)) }
    var arrivalMillis by remember { mutableStateOf(DateTimeParsing.parseApiDateTime(trip.arrivalDate)) }
    var sharedPickupMillis by remember { mutableStateOf(DateTimeParsing.parseApiDateTime(trip.pickupDate)) }
    var sharedDeliveryMillis by remember { mutableStateOf(DateTimeParsing.parseApiDateTime(trip.deliveryDate)) }
    var weightText by remember { mutableStateOf(trip.availableWeightKg?.toString().orEmpty()) }
    var spaceText by remember { mutableStateOf(trip.availableSpaceLiters?.toString().orEmpty()) }
    // iOS parity (`EditTripSheet.swift:896-991`): the single price input is sourced from
    // flatTripPrice for land transport and pricePerKg for flight/ship; submit serializes
    // back into the appropriate wire field.
    var priceText by remember {
        mutableStateOf(
            if (trip.transportationMethod.isLandTransport) {
                trip.flatTripPrice?.toString().orEmpty()
            } else {
                trip.pricePerKg?.toString().orEmpty()
            },
        )
    }
    var notesText by remember { mutableStateOf(trip.specialNotes.orEmpty()) }
    var showActivateConfirm by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var cancelErrorMessage by remember { mutableStateOf<String?>(null) }

    // Localized hint appended to the server's 409 message — iOS EditTripSheet.swift:1104-1108.
    val blockingMatchHint = stringResource(R.string.trips_details_cancel_error_match_in_progress_hint)
    val genericCancelError = stringResource(R.string.trips_edit_cancel_error_generic)

    // Cancel confirmation — iOS EditTripSheet.swift:179. Destructive role; copy says the action
    // can't be undone. On confirm, runs the suspend `onCancelTrip`; success dismisses, failure
    // surfaces the blocking-match-aware error alert.
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text(stringResource(R.string.trips_cancel_trip)) },
            text = { Text(stringResource(R.string.trips_action_cancel_trip_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancelConfirm = false
                    scope.launch {
                        onCancelTrip().fold(
                            onSuccess = { onDismiss() },
                            onFailure = { throwable ->
                                val domainError = (throwable as? DomainErrorMapperException)?.domainError
                                cancelErrorMessage = when (domainError) {
                                    is DomainError.TripHasBlockingMatch ->
                                        listOfNotNull(domainError.message, blockingMatchHint)
                                            .joinToString(separator = "\n\n")
                                    else -> genericCancelError
                                }
                            },
                        )
                    }
                }) { Text(stringResource(R.string.trips_cancel_trip)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text(stringResource(R.string.trips_action_keep_trip))
                }
            },
        )
    }

    // Cancel error alert — surfaces the blocking-match-aware message inline.
    cancelErrorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { cancelErrorMessage = null },
            title = { Text(stringResource(R.string.trips_cancel_trip)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { cancelErrorMessage = null }) {
                    Text(stringResource(R.string.common_buttons_ok))
                }
            },
        )
    }

    // Activate confirmation — iOS EditTripSheet.swift:212. Routes through the sanctioned
    // POST /trips/{id}/activate via the caller's `onActivate` (Slice A).
    if (showActivateConfirm) {
        AlertDialog(
            onDismissRequest = { showActivateConfirm = false },
            title = { Text(stringResource(R.string.trips_action_activate_trip_confirm_title)) },
            text = { Text(stringResource(R.string.trips_action_activate_trip_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showActivateConfirm = false
                    onActivate()
                }) { Text(stringResource(R.string.trips_action_activate_trip)) }
            },
            dismissButton = {
                TextButton(onClick = { showActivateConfirm = false }) {
                    Text(stringResource(R.string.common_buttons_cancel))
                }
            },
        )
    }

    PModalBottomSheet(onDismissRequest = onDismiss) {
        PDetailSheetScaffold(
            title = stringResource(R.string.trips_edit_trip),
            closeContentDescription = stringResource(R.string.trips_detail_close),
            onClose = onDismiss,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
            ) {
                EditTripRouteSection(
                    originCity = originCity,
                    onOriginCityChange = { originCity = it },
                    originCountry = originCountry,
                    onOriginCountryChange = { originCountry = it },
                    destinationCity = destinationCity,
                    onDestinationCityChange = { destinationCity = it },
                    destinationCountry = destinationCountry,
                    onDestinationCountryChange = { destinationCountry = it },
                    pickupAddress = pickupAddress,
                    onPickupAddressChange = { pickupAddress = it },
                    dropoffAddress = dropoffAddress,
                    onDropoffAddressChange = { dropoffAddress = it },
                    locked = routeLocked,
                )

                EditTripScheduleSection(
                    departureMillis = departureMillis,
                    onDepartureChange = { departureMillis = it },
                    arrivalMillis = arrivalMillis,
                    onArrivalChange = { arrivalMillis = it },
                    sharedPickupMillis = sharedPickupMillis,
                    onSharedPickupChange = { sharedPickupMillis = it },
                    sharedDeliveryMillis = sharedDeliveryMillis,
                    onSharedDeliveryChange = { sharedDeliveryMillis = it },
                    locked = routeLocked,
                )

                EditTripCapacitySection(
                    weightText = weightText,
                    onWeightChange = { weightText = it },
                    spaceText = spaceText,
                    onSpaceChange = { spaceText = it },
                    locked = routeLocked,
                )

                EditTripPricingSection(
                    transportationMethod = trip.transportationMethod,
                    priceText = priceText,
                    onPriceChange = { priceText = it },
                    locked = routeLocked,
                )

                EditTripStatusSection(
                    status = trip.tripStatus,
                    onActivateClick = { showActivateConfirm = true },
                )

                POutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(R.string.trips_create_special_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )

                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    PButton(
                        text = stringResource(R.string.trips_edit_trip),
                        onClick = {
                            onSave(
                                buildUpdateRequest(
                                    routeLocked = routeLocked,
                                    originCity = originCity,
                                    originCountry = originCountry,
                                    destinationCity = destinationCity,
                                    destinationCountry = destinationCountry,
                                    pickupAddress = pickupAddress,
                                    dropoffAddress = dropoffAddress,
                                    departureMillis = departureMillis,
                                    arrivalMillis = arrivalMillis,
                                    sharedPickupMillis = sharedPickupMillis,
                                    sharedDeliveryMillis = sharedDeliveryMillis,
                                    weightText = weightText,
                                    spaceText = spaceText,
                                    priceText = priceText,
                                    isLandTransport = trip.transportationMethod.isLandTransport,
                                    notesText = notesText,
                                    originalDepartureDate = trip.departureDate,
                                    originalArrivalDate = trip.arrivalDate,
                                    originalPickupDate = trip.pickupDate,
                                    originalDeliveryDate = trip.deliveryDate,
                                    originalWeightKg = trip.availableWeightKg,
                                    originalSpaceLiters = trip.availableSpaceLiters,
                                    originalPricePerKg = trip.pricePerKg,
                                    originalFlatTripPrice = trip.flatTripPrice,
                                    originalNotes = trip.specialNotes,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PButton(
                        text = stringResource(R.string.common_buttons_cancel),
                        onClick = onDismiss,
                        style = PButtonStyle.Tertiary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // iOS parity (`EditTripSheet.swift:179`): destructive Cancel Trip action
                    // only when policy allows it (planning or active). The actual deletion
                    // routes through DELETE /trips/{id}; HTTP 409 maps to TripHasBlockingMatch.
                    if (CarrierTripActionPolicy.shouldOfferCancelTrip(trip.tripStatus)) {
                        PButton(
                            text = stringResource(R.string.trips_cancel_trip),
                            onClick = { showCancelConfirm = true },
                            style = PButtonStyle.Tertiary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds the partial `PUT /trips/{id}` body. When the trip is locked (not PLANNING) only the
 * notes field — and weight, since the server still accepts capacity tweaks on active trips
 * via this endpoint — are emitted; route fields are dropped so a stale local copy can't
 * overwrite the server's authoritative route. Returns only changed fields so a no-op save
 * sends an empty body and the server records no change.
 */
internal fun buildUpdateRequest(
    routeLocked: Boolean,
    originCity: String,
    originCountry: String,
    destinationCity: String,
    destinationCountry: String,
    pickupAddress: String,
    dropoffAddress: String,
    departureMillis: Long?,
    arrivalMillis: Long?,
    sharedPickupMillis: Long?,
    sharedDeliveryMillis: Long?,
    weightText: String,
    spaceText: String,
    priceText: String,
    isLandTransport: Boolean,
    notesText: String,
    originalDepartureDate: String?,
    originalArrivalDate: String?,
    originalPickupDate: String?,
    originalDeliveryDate: String?,
    originalWeightKg: Double?,
    originalSpaceLiters: Double?,
    originalPricePerKg: Double?,
    originalFlatTripPrice: Double?,
    originalNotes: String?,
): TripUpdateRequestJson {
    val parsedWeight = weightText.toDoubleOrNull()
    val parsedSpace = spaceText.toDoubleOrNull()
    val parsedPrice = priceText.toDoubleOrNull()
    val notesNormalized = notesText.ifBlank { null }

    // Pricing — like capacity, this is gated by the same lock as route since iOS' canEditDetails
    // mirrors canEditRoute (both turn off outside planning). The price routes into one of two
    // wire fields per transport: land → flat_trip_price, flight/ship → price_per_kg.
    val newPricePerKg = if (routeLocked || isLandTransport) null
        else parsedPrice?.takeIf { it != originalPricePerKg }
    val newFlatTripPrice = if (routeLocked || !isLandTransport) null
        else parsedPrice?.takeIf { it != originalFlatTripPrice }

    // Schedule fields use the same lock as route: only PLANNING trips can change them
    // (iOS `EditTripSheet.swift:148-150`, `canEditRoute`). Notes are always editable.
    val newDeparture = if (routeLocked) null else departureMillis?.let(DateTimeParsing::formatApiDateTime)
    val newArrival = if (routeLocked) null else arrivalMillis?.let(DateTimeParsing::formatApiDateTime)
    val newSharedPickup = if (routeLocked) null else sharedPickupMillis?.let(DateTimeParsing::formatApiDateTime)
    val newSharedDelivery = if (routeLocked) null else sharedDeliveryMillis?.let(DateTimeParsing::formatApiDateTime)

    return TripUpdateRequestJson(
        originCity = if (routeLocked) null else originCity.ifBlank { null },
        originCountry = if (routeLocked) null else originCountry.ifBlank { null },
        destinationCity = if (routeLocked) null else destinationCity.ifBlank { null },
        destinationCountry = if (routeLocked) null else destinationCountry.ifBlank { null },
        pickupAddress = if (routeLocked) null else pickupAddress.ifBlank { null },
        dropoffAddress = if (routeLocked) null else dropoffAddress.ifBlank { null },
        departureDate = newDeparture?.takeIf { it != originalDepartureDate },
        arrivalDate = newArrival?.takeIf { it != originalArrivalDate },
        pickupDate = newSharedPickup?.takeIf { it != originalPickupDate },
        deliveryDate = newSharedDelivery?.takeIf { it != originalDeliveryDate },
        availableWeightKg = parsedWeight?.takeIf { it != originalWeightKg },
        availableSpaceLiters = parsedSpace?.takeIf { it != originalSpaceLiters },
        pricePerKg = newPricePerKg,
        flatTripPrice = newFlatTripPrice,
        specialNotes = notesNormalized?.takeIf { it != originalNotes },
    )
}

@Preview(showBackground = true, name = "EditTripSheet — planning, light", heightDp = 900)
@Preview(showBackground = true, name = "EditTripSheet — planning, dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripSheetPlanningPreview() {
    PasabayanTheme {
        EditTripSheet(
            trip = previewTrip(TripStatus.PLANNING),
            onDismiss = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, name = "EditTripSheet — active locked, light", heightDp = 900)
@Preview(showBackground = true, name = "EditTripSheet — active locked, dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripSheetLockedPreview() {
    PasabayanTheme {
        EditTripSheet(
            trip = previewTrip(TripStatus.ACTIVE),
            onDismiss = {},
            onSave = {},
        )
    }
}

private fun previewTrip(status: TripStatus) = Trip(
    id = 1, carrierId = 1,
    originCity = "Toronto", originCountry = "CA",
    originLat = null, originLng = null,
    destinationCity = "Montreal", destinationCountry = "CA",
    destinationLat = null, destinationLng = null,
    departureDate = "2026-04-01T08:00:00Z", arrivalDate = "2026-04-01T14:00:00Z",
    pickupDate = "2026-04-01T06:30:00Z", deliveryDate = "2026-04-01T15:00:00Z",
    availableWeightKg = 10.0, availableSpaceLiters = 20.0,
    pricePerKg = 8.0, tripStatus = status,
    transportationMethod = TransportationMethod.CAR,
    specialNotes = "Fragile only", carrier = null,
    createdAt = null, updatedAt = null,
    pricingType = null, pricingMethod = null,
    flatTripPrice = null, basePrice = null, calculatedPrice = null,
    pickupAddress = "123 Main St", pickupLandmark = null,
    dropoffAddress = "456 Oak Ave", dropoffLandmark = null,
    tripEarningsTotal = null, tripEarningsCurrency = null,
    tripEarningsBreakdown = null,
    hasPendingRequests = null, pendingRequestCount = null,
    pendingRequests = null, distanceKm = null,
)
