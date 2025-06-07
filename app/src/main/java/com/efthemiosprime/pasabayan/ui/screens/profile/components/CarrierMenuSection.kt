package com.efthemiosprime.pasabayan.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.components.ProfileMenuItem

/**
 * Carrier Menu Section Component
 * Configuration-based menu items with pure event handlers
 * Follows immutable data patterns
 */
@Composable
fun CarrierMenuSection(
    onVehicleInfo: () -> Unit,
    onDeliveryHistory: () -> Unit,
    onCarrierPaymentMethods: () -> Unit,
    onAvailabilitySettings: () -> Unit,
    onRoutePreferences: () -> Unit,
    onDriverDocuments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = remember {
        listOf(
            MenuItemConfig(
                icon = Icons.Default.DirectionsCar,
                title = "Vehicle Information",
                subtitle = "Manage your delivery vehicle details",
                onClick = onVehicleInfo
            ),
            MenuItemConfig(
                icon = Icons.Default.History,
                title = "Delivery History",
                subtitle = "View your completed deliveries",
                onClick = onDeliveryHistory
            ),
            MenuItemConfig(
                icon = Icons.Default.Payment,
                title = "Payment Methods",
                subtitle = "Manage accounts for receiving earnings",
                onClick = onCarrierPaymentMethods
            ),
            MenuItemConfig(
                icon = Icons.Default.Schedule,
                title = "Availability Settings",
                subtitle = "Set your working hours and availability",
                onClick = onAvailabilitySettings
            ),
            MenuItemConfig(
                icon = Icons.Default.Map,
                title = "Route Preferences",
                subtitle = "Configure preferred delivery routes",
                onClick = onRoutePreferences
            ),
            MenuItemConfig(
                icon = Icons.Default.Description,
                title = "Driver Documents",
                subtitle = "Manage license and vehicle documents",
                onClick = onDriverDocuments
            )
        )
    }
    
    MenuCard(
        title = "Carrier Services",
        menuItems = menuItems,
        modifier = modifier
    )
}

@Composable
private fun MenuCard(
    title: String,
    menuItems: List<MenuItemConfig>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Column {
                menuItems.forEachIndexed { index, item ->
                    ProfileMenuItem(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = item.onClick
                    )
                    
                    if (index < menuItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 50.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

// Configuration data class for menu items
private data class MenuItemConfig(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
) 