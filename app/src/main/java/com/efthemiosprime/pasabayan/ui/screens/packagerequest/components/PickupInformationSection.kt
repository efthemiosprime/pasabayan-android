package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Pickup Information Section - Focused component for pickup details
 * Following functional programming patterns with pure event handlers
 * Updated to use smart country detection (no manual country input)
 */
@Composable
fun PickupInformationSection(
    pickupAddress: String,
    onPickupAddressChange: (String) -> Unit,
    pickupCity: String,
    onPickupCityChange: (String) -> Unit,
    preferredPickupDate: String,
    onPreferredPickupDateChange: (String) -> Unit,
    preferredPickupTime: String,
    onPreferredPickupTimeChange: (String) -> Unit,
    pickupDateFlexible: Boolean,
    onPickupDateFlexibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pickup Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            PickupAddressField(
                value = pickupAddress,
                onValueChange = onPickupAddressChange
            )
            
            PickupCityField(
                value = pickupCity,
                onValueChange = onPickupCityChange
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickupDatePicker(
                    value = preferredPickupDate,
                    onValueChange = onPreferredPickupDateChange,
                    modifier = Modifier.weight(1f)
                )
                
                PickupTimePicker(
                    value = preferredPickupTime,
                    onValueChange = onPreferredPickupTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            PickupFlexibilityToggle(
                isFlexible = pickupDateFlexible,
                onFlexibilityChange = onPickupDateFlexibleChange
            )
        }
    }
}

@Composable
private fun PickupAddressField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Pickup address *") },
        placeholder = { Text("Enter pickup address") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
    )
}

@Composable
private fun PickupCityField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Pickup city *") },
        placeholder = { Text("Enter city") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PickupDatePicker(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Format the display text
    val displayText = if (value.isNotEmpty()) {
        try {
            val date = LocalDate.parse(value)
            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            value
        }
    } else {
        "Select date"
    }
    
    OutlinedTextField(
        value = displayText,
        onValueChange = { }, // Read-only
        label = { Text("Pickup date") },
        placeholder = { Text("Select pickup date") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date"
                )
            }
        },
        modifier = modifier.clickable { showDatePicker = true }
    )
    
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { selectedDate ->
                onValueChange(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun PickupTimePicker(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Format the display text
    val displayText = if (value.isNotEmpty()) {
        try {
            val time = LocalTime.parse(value)
            time.format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e: Exception) {
            value
        }
    } else {
        "Select time"
    }
    
    OutlinedTextField(
        value = displayText,
        onValueChange = { }, // Read-only
        label = { Text("Pickup time") },
        placeholder = { Text("Select pickup time") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Select time"
                )
            }
        },
        modifier = modifier.clickable { showTimePicker = true }
    )
    
    if (showTimePicker) {
        TimePickerModal(
            onTimeSelected = { selectedTime ->
                onValueChange(selectedTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date.toString())
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(selectedTime.toString())
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PickupFlexibilityToggle(
    isFlexible: Boolean,
    onFlexibilityChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Flexible pickup date",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isFlexible) "Date can be adjusted" else "Fixed date required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = isFlexible,
            onCheckedChange = onFlexibilityChange
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupInformationSectionPreview() {
    PasabayanTheme {
        PickupInformationSection(
            pickupAddress = "123 Main Street, Barangay San Antonio",
            onPickupAddressChange = { },
            pickupCity = "Montreal",
            onPickupCityChange = { },
            preferredPickupDate = "2024-01-15",
            onPreferredPickupDateChange = { },
            preferredPickupTime = "10:00",
            onPreferredPickupTimeChange = { },
            pickupDateFlexible = true,
            onPickupDateFlexibleChange = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 