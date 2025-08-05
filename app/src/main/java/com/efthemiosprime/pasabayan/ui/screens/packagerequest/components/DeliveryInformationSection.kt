package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Delivery Information Section - Focused component for delivery details
 * Following functional programming patterns with pure event handlers
 * Updated to use smart country detection (no manual country input)
 */
@Composable
fun DeliveryInformationSection(
    deliveryAddress: String,
    onDeliveryAddressChange: (String) -> Unit,
    deliveryCity: String,
    onDeliveryCityChange: (String) -> Unit,
    preferredDeliveryDate: String,
    onPreferredDeliveryDateChange: (String) -> Unit,
    preferredDeliveryTime: String,
    onPreferredDeliveryTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Delivery Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            DeliveryAddressField(
                value = deliveryAddress,
                onValueChange = onDeliveryAddressChange
            )
            
            DeliveryCityField(
                value = deliveryCity,
                onValueChange = onDeliveryCityChange
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeliveryDatePicker(
                    value = preferredDeliveryDate,
                    onValueChange = onPreferredDeliveryDateChange,
                    modifier = Modifier.weight(1f)
                )
                
                DeliveryTimePicker(
                    value = preferredDeliveryTime,
                    onValueChange = onPreferredDeliveryTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DeliveryAddressField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Delivery address *") },
        placeholder = { Text("Enter delivery address") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
    )
}

@Composable
private fun DeliveryCityField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Delivery city *") },
        placeholder = { Text("Enter city") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeliveryDatePicker(
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
        label = { Text("Delivery date") },
        placeholder = { Text("Select delivery date") },
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
private fun DeliveryTimePicker(
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
        label = { Text("Delivery time") },
        placeholder = { Text("Select delivery time") },
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
                        // Fix timezone issue: Add 24 hours to ensure correct date selection
                        val adjustedMillis = millis + (24 * 60 * 60 * 1000) // Add 24 hours
                        val date = java.time.Instant.ofEpochMilli(adjustedMillis)
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
        initialHour = 14,
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

@Preview(showBackground = true)
@Composable
private fun DeliveryInformationSectionPreview() {
    PasabayanTheme {
        DeliveryInformationSection(
            deliveryAddress = "456 Business Street, BGC",
            onDeliveryAddressChange = { },
            deliveryCity = "Makati",
            onDeliveryCityChange = { },
            preferredDeliveryDate = "2024-01-16",
            onPreferredDeliveryDateChange = { },
            preferredDeliveryTime = "14:00",
            onPreferredDeliveryTimeChange = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 