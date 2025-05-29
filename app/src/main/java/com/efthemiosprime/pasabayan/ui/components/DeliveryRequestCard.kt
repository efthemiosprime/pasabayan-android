package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.data.model.DeliveryRequest
import com.efthemiosprime.pasabayan.data.model.DeliveryStatus

@Composable
fun DeliveryRequestCard(
    request: DeliveryRequest,
    currentUserId: Int,
    onAccept: (Int) -> Unit,
    onUpdateStatus: (Int, DeliveryStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.itemDescription, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("From: ${request.pickupAddress}", fontSize = 14.sp)
                    Text("To: ${request.deliveryAddress}", fontSize = 14.sp)
                }
                StatusChip(request.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("₱${request.deliveryFee}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                when {
                    request.status == DeliveryStatus.PENDING && request.userId != currentUserId -> {
                        Button(onClick = { onAccept(request.id) }, modifier = Modifier.height(32.dp)) {
                            Text("Accept", fontSize = 12.sp)
                        }
                    }
                    request.driverId == currentUserId && request.status == DeliveryStatus.ACCEPTED -> {
                        Button(onClick = { onUpdateStatus(request.id, DeliveryStatus.PICKED_UP) }, modifier = Modifier.height(32.dp)) {
                            Text("Pick Up", fontSize = 12.sp)
                        }
                    }
                    request.driverId == currentUserId && request.status == DeliveryStatus.PICKED_UP -> {
                        Button(onClick = { onUpdateStatus(request.id, DeliveryStatus.DELIVERED) }, modifier = Modifier.height(32.dp)) {
                            Text("Deliver", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeliveryRequestCardPreview() {
    val mockRequest = DeliveryRequest(
        id = 101,
        userId = 1,
        driverId = null,
        itemDescription = "Important Documents",
        itemWeight = 1.5,
        itemDimensions = "30x20x10cm",
        pickupAddress = "Makati CBD, Metro Manila",
        pickupLatitude = 14.5547,
        pickupLongitude = 121.0244,
        deliveryAddress = "Quezon City, Metro Manila",
        deliveryLatitude = 14.6760,
        deliveryLongitude = 121.0437,
        pickupContactName = "Juan Dela Cruz",
        pickupContactPhone = "09171234567",
        deliveryContactName = "Maria Clara",
        deliveryContactPhone = "09179876543",
        requestedPickupTime = "2025-05-01T10:00:00Z",
        requestedDeliveryTime = "2025-05-01T11:30:00Z",
        specialInstructions = "Handle with care",
        deliveryFee = 150.0,
        status = DeliveryStatus.PENDING,
        createdAt = "2025-05-01T09:50:00Z",
        updatedAt = "2025-05-01T10:05:00Z",
        acceptedAt = null,
        pickedUpAt = null,
        deliveredAt = null
    )

    MaterialTheme {
        DeliveryRequestCard(
            request = mockRequest,
            currentUserId = 2,
            onAccept = {},
            onUpdateStatus = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
} 