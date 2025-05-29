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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.data.model.DeliveryRequest
import com.efthemiosprime.pasabayan.data.model.DeliveryStatus
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.ui.viewmodel.DeliveryViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Welcome back,",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentUser.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Create Delivery",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onCreateDelivery
            )

            QuickActionCard(
                title = "View Deliveries",
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onViewDeliveries
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics
        Text(
            text = "Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val myRequests = deliveryViewModel.getMyRequests(currentUser.id)
        val pendingRequests = deliveryViewModel.getPendingRequests()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "My Requests",
                value = myRequests.size.toString(),
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Available",
                value = pendingRequests.size.toString(),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        Text(
            text = "Recent Activity",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val recentRequests = uiState.deliveryRequests
                    .sortedByDescending { it.createdAt }
                    .take(5)

                if (recentRequests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No recent activity",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(recentRequests) { request ->
                        DeliveryRequestCard(
                            request = request,
                            currentUserId = currentUser.id,
                            onAccept = { deliveryViewModel.acceptDeliveryRequest(it) },
                            onUpdateStatus = { id, status -> 
                                deliveryViewModel.updateDeliveryStatus(id, status) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeliveryRequestCard(
    request: DeliveryRequest,
    currentUserId: Int,
    onAccept: (Int) -> Unit,
    onUpdateStatus: (Int, DeliveryStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.itemDescription,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "From: ${request.pickupAddress}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "To: ${request.deliveryAddress}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = request.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₱${request.deliveryFee}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Show action buttons based on user role and request status
                when {
                    request.status == DeliveryStatus.PENDING && request.userId != currentUserId -> {
                        Button(
                            onClick = { onAccept(request.id) },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Accept", fontSize = 12.sp)
                        }
                    }
                    request.driverId == currentUserId && request.status == DeliveryStatus.ACCEPTED -> {
                        Button(
                            onClick = { onUpdateStatus(request.id, DeliveryStatus.PICKED_UP) },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Pick Up", fontSize = 12.sp)
                        }
                    }
                    request.driverId == currentUserId && request.status == DeliveryStatus.PICKED_UP -> {
                        Button(
                            onClick = { onUpdateStatus(request.id, DeliveryStatus.DELIVERED) },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Deliver", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: DeliveryStatus) {
    val backgroundColor = when (status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        DeliveryStatus.ACCEPTED -> Color(0xFFE3F2FD)
        DeliveryStatus.PICKED_UP -> Color(0xFFFFF3E0)
        DeliveryStatus.IN_TRANSIT -> Color(0xFFFFF3E0)
        DeliveryStatus.DELIVERED -> Color(0xFFE8F5E8)
        DeliveryStatus.CANCELLED -> Color(0xFFFFEBEE)
    }

    val textColor = when (status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        DeliveryStatus.ACCEPTED -> Color(0xFF1976D2)
        DeliveryStatus.PICKED_UP -> Color(0xFFE65100)
        DeliveryStatus.IN_TRANSIT -> Color(0xFFE65100)
        DeliveryStatus.DELIVERED -> Color(0xFF2E7D32)
        DeliveryStatus.CANCELLED -> Color(0xFFC62828)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
} 