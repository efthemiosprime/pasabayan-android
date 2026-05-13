package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.network.trips.TripUpdateRequestJson
import com.efthemiosprime.pasabayan.features.trips.components.EditTripRouteSection
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTripSheet(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (TripUpdateRequestJson) -> Unit,
) {
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
    var weightText by remember { mutableStateOf(trip.availableWeightKg?.toString().orEmpty()) }
    var notesText by remember { mutableStateOf(trip.specialNotes.orEmpty()) }

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

                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    POutlinedTextField(
                        value = weightText,
                        onValueChange = { if (!routeLocked) weightText = it },
                        label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !routeLocked,
                    )
                    POutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(stringResource(R.string.trips_create_special_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4,
                    )
                }

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
                                    weightText = weightText,
                                    notesText = notesText,
                                    originalWeightKg = trip.availableWeightKg,
                                    originalNotes = trip.specialNotes,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PButton(
                        text = stringResource(R.string.trips_cancel_trip),
                        onClick = onDismiss,
                        style = PButtonStyle.Tertiary,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
    weightText: String,
    notesText: String,
    originalWeightKg: Double?,
    originalNotes: String?,
): TripUpdateRequestJson {
    val parsedWeight = weightText.toDoubleOrNull()
    val notesNormalized = notesText.ifBlank { null }
    return TripUpdateRequestJson(
        originCity = if (routeLocked) null else originCity.ifBlank { null },
        originCountry = if (routeLocked) null else originCountry.ifBlank { null },
        destinationCity = if (routeLocked) null else destinationCity.ifBlank { null },
        destinationCountry = if (routeLocked) null else destinationCountry.ifBlank { null },
        pickupAddress = if (routeLocked) null else pickupAddress.ifBlank { null },
        dropoffAddress = if (routeLocked) null else dropoffAddress.ifBlank { null },
        availableWeightKg = parsedWeight?.takeIf { it != originalWeightKg },
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
