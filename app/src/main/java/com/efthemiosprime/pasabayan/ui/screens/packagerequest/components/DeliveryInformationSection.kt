package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Delivery Information Section - Focused component for delivery details
 * Following functional programming patterns with pure event handlers
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
                DeliveryDateField(
                    value = preferredDeliveryDate,
                    onValueChange = onPreferredDeliveryDateChange,
                    modifier = Modifier.weight(1f)
                )
                
                DeliveryTimeField(
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
private fun DeliveryDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Delivery date") },
        placeholder = { Text("YYYY-MM-DD") },
        modifier = modifier
    )
}

@Composable
private fun DeliveryTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Delivery time") },
        placeholder = { Text("HH:MM") },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun DeliveryInformationSectionPreview() {
    PasabayanTheme {
        DeliveryInformationSection(
            deliveryAddress = "456 Business Street, BGC",
            onDeliveryAddressChange = { },
            deliveryCity = "Taguig",
            onDeliveryCityChange = { },
            preferredDeliveryDate = "2024-01-16",
            onPreferredDeliveryDateChange = { },
            preferredDeliveryTime = "14:00",
            onPreferredDeliveryTimeChange = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 