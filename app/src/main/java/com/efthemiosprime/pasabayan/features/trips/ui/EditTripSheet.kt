package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTripSheet(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (availableWeightKg: Double?, notes: String?) -> Unit,
) {
    var weightText by remember { mutableStateOf(trip.availableWeightKg?.toString().orEmpty()) }
    var notesText by remember { mutableStateOf(trip.specialNotes.orEmpty()) }
    val routeLocked = trip.tripStatus != TripStatus.PLANNING

    PModalBottomSheet(onDismissRequest = onDismiss) {
        PDetailSheetScaffold(
            title = stringResource(R.string.trips_edit_trip),
            closeContentDescription = stringResource(R.string.trips_detail_close),
            onClose = onDismiss,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                if (routeLocked) {
                    // iOS parity: small lock icon alongside the explanatory note when route
                    // fields aren't editable for the trip's current status.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.trips_edit_route_locked_note),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                POutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.trips_create_weight_capacity)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(R.string.trips_create_special_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )
                PButton(
                    text = stringResource(R.string.trips_edit_trip),
                    onClick = {
                        onSave(
                            weightText.toDoubleOrNull(),
                            notesText.ifBlank { null },
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

@Preview(showBackground = true, name = "EditTripSheet — light")
@Preview(showBackground = true, name = "EditTripSheet — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripSheetPreview() {
    PasabayanTheme {
        EditTripSheet(
            trip = Trip(
                id = 1,
                carrierId = 1,
                originCity = "Toronto",
                originCountry = "Canada",
                originLat = null,
                originLng = null,
                destinationCity = "Montreal",
                destinationCountry = "Canada",
                destinationLat = null,
                destinationLng = null,
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T14:00:00Z",
                availableWeightKg = 10.0,
                availableSpaceLiters = 20.0,
                pricePerKg = 8.0,
                tripStatus = TripStatus.ACTIVE,
                transportationMethod = TransportationMethod.CAR,
                specialNotes = "Fragile only",
                carrier = null,
                createdAt = null,
                updatedAt = null,
                pricingType = null,
                pricingMethod = null,
                flatTripPrice = null,
                basePrice = null,
                calculatedPrice = null,
                pickupAddress = null,
                pickupLandmark = null,
                dropoffAddress = null,
                dropoffLandmark = null,
                tripEarningsTotal = null,
                tripEarningsCurrency = null,
                tripEarningsBreakdown = null,
                hasPendingRequests = null,
                pendingRequestCount = null,
                pendingRequests = null,
                distanceKm = null,
            ),
            onDismiss = {},
            onSave = { _, _ -> },
        )
    }
}
