package com.efthemiosprime.pasabayan.features.trips.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.domain.util.DateTimeParsing
import java.util.Calendar
import java.util.TimeZone

/**
 * Paired date + time pills for a single field (iOS parity:
 * `TripCreationView` Pickup / Delivery rows).
 *
 * Source-of-truth state lives outside this composable as `Long?` epoch-millis; the host can
 * convert to wire format via [DateTimeParsing.formatApiDateTime] at submit time. When the
 * value is `null`, both pills show their respective placeholder; once either picker writes,
 * we seed the missing half from `now` rounded to the next hour so the user only needs one
 * tap to get a usable value.
 */
@Composable
fun TripDateTimePicker(
    label: String,
    epochMillis: Long?,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.medium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            DateTimePill(
                label = epochMillis?.let { DateTimeParsing.formatShortDate(it) }
                    ?: stringResource(R.string.trips_create_pickup_date_placeholder),
                onClick = { showDatePicker = true },
            )
            DateTimePill(
                label = epochMillis?.let { DateTimeParsing.formatShortTime(it) }
                    ?: stringResource(R.string.trips_create_pickup_time_placeholder),
                onClick = { showTimePicker = true },
            )
        }
    }

    if (showDatePicker) {
        TripDatePickerDialog(
            initialMillis = epochMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { dayMillis ->
                showDatePicker = false
                onChange(mergeDateWithTime(dayMillis, epochMillis))
            },
        )
    }
    if (showTimePicker) {
        TripTimePickerDialog(
            initialMillis = epochMillis,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onChange(mergeTimeWithDate(hour, minute, epochMillis))
            },
        )
    }
}

@Composable
private fun DateTimePill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50),
            )
            .then(
                Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50),
                ),
            )
            .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePickerDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selected = state.selectedDateMillis ?: return@TextButton
                onConfirm(selected)
            }) {
                Text(stringResource(R.string.trips_create_picker_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.trips_create_picker_cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTimePickerDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        initialMillis?.let { timeInMillis = it }
    }
    val state = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
    )
    com.efthemiosprime.pasabayan.core.designsystem.component.PAlertDialog(
        title = stringResource(R.string.trips_create_picker_time_title),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(state = state)
            }
        },
        confirmText = stringResource(R.string.trips_create_picker_done),
        onConfirm = { onConfirm(state.hour, state.minute) },
        dismissText = stringResource(R.string.trips_create_picker_cancel),
        onDismiss = onDismiss,
    )
}

/**
 * Replace the date portion of [previousMillis] with [dayMillis], preserving the existing time
 * (or seeding 9:00 AM UTC when the field is empty so the user doesn't get midnight by default).
 * Exposed `internal` for unit tests.
 */
internal fun mergeDateWithTime(dayMillis: Long, previousMillis: Long?): Long {
    val day = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dayMillis }
    val time = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = previousMillis ?: 0L
        if (previousMillis == null) {
            set(Calendar.HOUR_OF_DAY, DEFAULT_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, day.get(Calendar.YEAR))
        set(Calendar.MONTH, day.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, day.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, time.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Replace the time portion of [previousMillis] with [hour]:[minute], preserving the existing
 * date (or seeding **today** in UTC when the field is empty).
 */
internal fun mergeTimeWithDate(hour: Int, minute: Int, previousMillis: Long?): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = previousMillis ?: System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private const val DEFAULT_HOUR = 9
