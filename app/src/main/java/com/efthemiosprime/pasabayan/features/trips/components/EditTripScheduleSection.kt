package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing

/**
 * Editable schedule section for `EditTripSheet`. Mirrors iOS `EditTripSheet.swift:621-648`
 * (departure, arrival, shared pickup, shared delivery as paired date+time pickers).
 *
 * The four times are independent — iOS syncs the carrier-leg departure/arrival from the
 * shared pickup/delivery via `syncEditableCarrierLegFromSharedSchedule()`; we leave that
 * logic to a higher-level form coordinator if needed and accept independent state here.
 *
 * When [locked] is `true`, each field renders as read-only formatted text instead of a
 * tappable picker — iOS parity with the active/completed view of this sheet.
 */
@Composable
fun EditTripScheduleSection(
    departureMillis: Long?,
    onDepartureChange: (Long) -> Unit,
    arrivalMillis: Long?,
    onArrivalChange: (Long) -> Unit,
    sharedPickupMillis: Long?,
    onSharedPickupChange: (Long) -> Unit,
    sharedDeliveryMillis: Long?,
    onSharedDeliveryChange: (Long) -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        PDetailSectionTitle(text = stringResource(R.string.trips_detail_schedule))

        ScheduleRow(
            label = stringResource(R.string.trips_detail_departure),
            millis = departureMillis,
            onChange = onDepartureChange,
            locked = locked,
        )
        ScheduleRow(
            label = stringResource(R.string.trips_detail_arrival),
            millis = arrivalMillis,
            onChange = onArrivalChange,
            locked = locked,
        )
        ScheduleRow(
            label = stringResource(R.string.trips_edit_shared_pickup),
            millis = sharedPickupMillis,
            onChange = onSharedPickupChange,
            locked = locked,
        )
        ScheduleRow(
            label = stringResource(R.string.trips_edit_shared_delivery),
            millis = sharedDeliveryMillis,
            onChange = onSharedDeliveryChange,
            locked = locked,
        )
    }
}

@Composable
private fun ScheduleRow(
    label: String,
    millis: Long?,
    onChange: (Long) -> Unit,
    locked: Boolean,
) {
    if (locked) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = label,
                style = PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = millis?.let { DateTimeParsing.formatShortDate(it) }
                        ?: stringResource(R.string.trips_detail_schedule_not_set),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (millis != null) {
                    Text(
                        text = DateTimeParsing.formatShortTime(millis),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        TripDateTimePicker(
            label = label,
            epochMillis = millis,
            onChange = onChange,
        )
    }
}

@Preview(showBackground = true, name = "EditTripScheduleSection — planning, light", heightDp = 500)
@Preview(showBackground = true, name = "EditTripScheduleSection — planning, dark", heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripSchedulePlanningPreview() {
    var dep by remember { mutableStateOf(DateTimeParsing.parseApiDateTime("2026-07-01T08:00:00Z")) }
    var arr by remember { mutableStateOf(DateTimeParsing.parseApiDateTime("2026-07-01T16:00:00Z")) }
    var sp by remember { mutableStateOf<Long?>(null) }
    var sd by remember { mutableStateOf<Long?>(null) }
    PasabayanTheme {
        EditTripScheduleSection(
            departureMillis = dep, onDepartureChange = { dep = it },
            arrivalMillis = arr, onArrivalChange = { arr = it },
            sharedPickupMillis = sp, onSharedPickupChange = { sp = it },
            sharedDeliveryMillis = sd, onSharedDeliveryChange = { sd = it },
            locked = false,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "EditTripScheduleSection — locked, light", heightDp = 500)
@Preview(showBackground = true, name = "EditTripScheduleSection — locked, dark", heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditTripScheduleLockedPreview() {
    PasabayanTheme {
        EditTripScheduleSection(
            departureMillis = DateTimeParsing.parseApiDateTime("2026-07-01T08:00:00Z"),
            onDepartureChange = {},
            arrivalMillis = DateTimeParsing.parseApiDateTime("2026-07-01T16:00:00Z"),
            onArrivalChange = {},
            sharedPickupMillis = DateTimeParsing.parseApiDateTime("2026-07-01T06:00:00Z"),
            onSharedPickupChange = {},
            sharedDeliveryMillis = null,
            onSharedDeliveryChange = {},
            locked = true,
            modifier = Modifier.padding(PasabayanSpacing.md),
        )
    }
}
