package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Pickup Information Section - Focused component for pickup details
 * Following functional programming patterns with pure event handlers
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
                PickupDateField(
                    value = preferredPickupDate,
                    onValueChange = onPreferredPickupDateChange,
                    modifier = Modifier.weight(1f)
                )
                
                PickupTimeField(
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
private fun PickupDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Pickup date") },
        placeholder = { Text("YYYY-MM-DD") },
        modifier = modifier
    )
}

@Composable
private fun PickupTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Pickup time") },
        placeholder = { Text("HH:MM") },
        modifier = modifier
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
            pickupCity = "Makati",
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