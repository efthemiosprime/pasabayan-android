package com.efthemiosprime.pasabayan.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.data.model.DeliveryRequest
import com.efthemiosprime.pasabayan.data.model.DeliveryStatus
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.ui.viewmodel.DeliveryViewModel
import com.efthemiosprime.pasabayan.ui.components.StatusChip
import com.efthemiosprime.pasabayan.ui.components.DeliveryRequestCard
import com.efthemiosprime.pasabayan.ui.components.StatCard
import com.efthemiosprime.pasabayan.ui.components.QuickActionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: User,
    deliveryViewModel: DeliveryViewModel,
    onCreateDelivery: () -> Unit,
    onViewDeliveries: () -> Unit,
    onViewProfile: () -> Unit
) {
    val uiState by deliveryViewModel.uiState.collectAsState()

    DashboardContent(
        currentUser = currentUser,
        isLoading = uiState.isLoading,
        deliveryRequests = uiState.deliveryRequests,
        onCreateDelivery = onCreateDelivery,
        onViewDeliveries = onViewDeliveries,
        onViewProfile = onViewProfile,
        onAccept = { deliveryViewModel.acceptDeliveryRequest(it) },
        onUpdateStatus = { id, status -> deliveryViewModel.updateDeliveryStatus(id, status) }
    )
}

@Composable
fun DashboardContent(
    currentUser: User,
    isLoading: Boolean,
    deliveryRequests: List<DeliveryRequest>,
    onCreateDelivery: () -> Unit,
    onViewDeliveries: () -> Unit,
    onViewProfile: () -> Unit,
    onAccept: (Int) -> Unit,
    onUpdateStatus: (Int, DeliveryStatus) -> Unit
) {
    val myRequests = deliveryRequests.filter { it.userId == currentUser.id }
    val pendingRequests = deliveryRequests.filter { it.status == DeliveryStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Welcome back,", fontSize = 16.sp)
                Text(currentUser.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard("Create Delivery", Icons.Default.Add, MaterialTheme.colorScheme.primary, Modifier.weight(1f), onCreateDelivery)
            QuickActionCard("View Deliveries", Icons.Default.List, MaterialTheme.colorScheme.secondary, Modifier.weight(1f), onViewDeliveries)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("My Requests", myRequests.size.toString(), Icons.Default.Person, Modifier.weight(1f))
            StatCard("Available", pendingRequests.size.toString(), Icons.Default.Star, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Recent Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val recent = deliveryRequests.sortedByDescending { it.createdAt }.take(5)
                if (recent.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No recent activity")
                            }
                        }
                    }
                } else {
                    items(recent) { request ->
                        DeliveryRequestCard(
                            request = request,
                            currentUserId = currentUser.id,
                            onAccept = onAccept,
                            onUpdateStatus = onUpdateStatus
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val mockUser = User(id = 1, name = "Alex Cruz", createdAt = "", updatedAt ="", email = "alex@example.com")

    val mockRequests = listOf(
        DeliveryRequest(
            id = 101,
            userId = 1,
            driverId = null,
            itemDescription = "Documents",
            itemWeight = 1.5,
            itemDimensions = "30x20x10cm",
            pickupAddress = "Makati",
            pickupLatitude = 14.5547,
            pickupLongitude = 121.0244,
            deliveryAddress = "Quezon City",
            deliveryLatitude = 14.6760,
            deliveryLongitude = 121.0437,
            pickupContactName = "Juan Dela Cruz",
            pickupContactPhone = "09171234567",
            deliveryContactName = "Maria Clara",
            deliveryContactPhone = "09179876543",
            requestedPickupTime = "2025-05-01T10:00:00Z",
            requestedDeliveryTime = "2025-05-01T11:30:00Z",
            specialInstructions = "Handle with care",
            deliveryFee = 100.0,
            status = DeliveryStatus.PENDING,
            createdAt = "2025-05-01T09:50:00Z",
            updatedAt = "2025-05-01T10:05:00Z",
            acceptedAt = null,
            pickedUpAt = null,
            deliveredAt = null
        ),
        DeliveryRequest(
            id = 102,
            userId = 2,
            driverId = 1,
            itemDescription = "Package",
            itemWeight = 2.0,
            itemDimensions = "40x25x15cm",
            pickupAddress = "BGC",
            pickupLatitude = 14.5548,
            pickupLongitude = 121.0439,
            deliveryAddress = "Pasig",
            deliveryLatitude = 14.5764,
            deliveryLongitude = 121.0851,
            pickupContactName = "Mark Reyes",
            pickupContactPhone = "09170000000",
            deliveryContactName = "Ana Santos",
            deliveryContactPhone = "09178889999",
            requestedPickupTime = "2025-05-02T08:45:00Z",
            requestedDeliveryTime = "2025-05-02T10:15:00Z",
            specialInstructions = null,
            deliveryFee = 150.0,
            status = DeliveryStatus.ACCEPTED,
            createdAt = "2025-05-02T08:00:00Z",
            updatedAt = "2025-05-02T09:00:00Z",
            acceptedAt = "2025-05-02T08:30:00Z",
            pickedUpAt = null,
            deliveredAt = null
        )
    )

    MaterialTheme {
        DashboardContent(
            currentUser = mockUser,
            isLoading = false,
            deliveryRequests = mockRequests,
            onCreateDelivery = {},
            onViewDeliveries = {},
            onViewProfile = {},
            onAccept = {},
            onUpdateStatus = { _, _ -> }
        )
    }
}
