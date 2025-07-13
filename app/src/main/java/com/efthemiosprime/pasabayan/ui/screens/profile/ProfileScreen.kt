package com.efthemiosprime.pasabayan.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.components.role.RoleSwitcherView
import com.efthemiosprime.pasabayan.ui.components.StatItem
import com.efthemiosprime.pasabayan.ui.components.ProfileMenuItem
import com.efthemiosprime.pasabayan.ui.components.PlaceholderSheet
import com.efthemiosprime.pasabayan.ui.components.VerificationBadgeIcon
import com.efthemiosprime.pasabayan.ui.components.VerificationStatusDisplay
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer

/**
 * Profile Screen - Exact iOS ProfileView implementation
 * Mirrors iOS ProfileView.swift structure with clean component separation
 * Handles user profile display and role-specific menu navigation
 */
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentRole by roleViewModel.currentRole.collectAsState()
    var showingLogoutAlert by remember { mutableStateOf(false) }
    
    // Sheet presentation states
    var showingEditProfile by remember { mutableStateOf(false) }
    var showingHelpSupport by remember { mutableStateOf(false) }
    var showingTermsPrivacy by remember { mutableStateOf(false) }
    
    // Carrier-specific sheets
    var showingVehicleInfo by remember { mutableStateOf(false) }
    var showingDeliveryHistory by remember { mutableStateOf(false) }
    var showingCarrierPaymentMethods by remember { mutableStateOf(false) }
    var showingAvailabilitySettings by remember { mutableStateOf(false) }
    var showingRoutePreferences by remember { mutableStateOf(false) }
    var showingDriverDocuments by remember { mutableStateOf(false) }
    
    // Shipper-specific sheets
    var showingShippingAddresses by remember { mutableStateOf(false) }
    var showingOrderHistory by remember { mutableStateOf(false) }
    var showingShipperPaymentMethods by remember { mutableStateOf(false) }
    var showingActiveShipments by remember { mutableStateOf(false) }
    var showingNotifications by remember { mutableStateOf(false) }
    var showingShippingPreferences by remember { mutableStateOf(false) }
    var showingBillingHistory by remember { mutableStateOf(false) }
    var showingBulkShippingTools by remember { mutableStateOf(false) }
    
    ScreenContainer {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
        item {
            // Profile Header
            PCardStandard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp) // Extra 14dp + 2dp = 16dp total
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column - Avatar and Verification Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = currentUser?.avatar,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                            fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                        )
                        
                        VerificationStatusDisplay(
                            verificationLevel = currentUser?.verificationLevel ?: "unverified",
                            showText = false
                        )
                    }
                    
                    // Right Column - User Information
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        currentUser?.phone?.let { phone ->
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Role Badge
                        Surface(
                            modifier = Modifier.wrapContentSize(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Blue.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentRole == UserRole.CARRIER) Icons.Default.LocalShipping else Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Blue
                                )
                                Text(
                                    text = if (currentRole == UserRole.CARRIER) "Carrier" else "Shipper",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Blue
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Role Switcher Section
            PCardStandard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp) // Extra 14dp + 2dp = 16dp total
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Switch Role",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    RoleSwitcherView(roleViewModel = roleViewModel)
                }
            }
        }
        
        item {
            // Role-Specific Stats Section
            PCardStandard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp) // Extra 14dp + 2dp = 16dp total
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 3-column grid using Row and Column
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentRole == UserRole.CARRIER) {
                            // Carrier Stats
                            StatItem(
                                title = "Deliveries",
                                value = "147",
                                icon = Icons.Default.LocalShipping,
                                color = Color.Blue,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                title = "Rating",
                                value = "4.9",
                                icon = Icons.Default.Star,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                title = "Earnings",
                                value = "$298.80",
                                icon = Icons.Default.AttachMoney,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Shipper Stats
                            StatItem(
                                title = "Packages",
                                value = "28",
                                icon = Icons.Default.Inventory,
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                title = "Rating",
                                value = "4.8",
                                icon = Icons.Default.Star,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                title = "Monthly Spent",
                                value = "$76.80",
                                icon = Icons.Default.CreditCard,
                                color = Color.Blue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        // Role-Specific Menu Items
        if (currentRole == UserRole.CARRIER) {
            // Carrier-specific menu items
            item {
                ProfileMenuItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Vehicle Information",
                    subtitle = "Manage your delivery vehicle details",
                    onClick = { showingVehicleInfo = true },
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Delivery History",
                    subtitle = "View your completed deliveries",
                    onClick = { showingDeliveryHistory = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.CreditCard,
                    title = "Payment Methods",
                    subtitle = "Manage accounts for receiving earnings",
                    onClick = { showingCarrierPaymentMethods = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Schedule,
                    title = "Availability Settings",
                    subtitle = "Set your working hours and availability",
                    onClick = { showingAvailabilitySettings = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Map,
                    title = "Route Preferences",
                    subtitle = "Configure preferred delivery routes",
                    onClick = { showingRoutePreferences = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = "Driver Documents",
                    subtitle = "Manage license and vehicle documents",
                    onClick = { showingDriverDocuments = true },
                    modifier = Modifier.padding(bottom = 14.dp) // Extra 14dp + 2dp = 16dp total
                )
            }
            
        } else {
            // Shipper-specific menu items
            item {
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "Shipping Addresses",
                    subtitle = "Manage pickup and delivery addresses",
                    onClick = { showingShippingAddresses = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Order History",
                    subtitle = "View your shipping history",
                    onClick = { showingOrderHistory = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.CreditCard,
                    title = "Payment Methods",
                    subtitle = "Manage cards for paying shipping costs",
                    onClick = { showingShipperPaymentMethods = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Inventory,
                    title = "Active Shipments",
                    subtitle = "Track your current packages",
                    onClick = { showingActiveShipments = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Configure shipping notifications",
                    onClick = { showingNotifications = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Shipping Preferences",
                    subtitle = "Set default shipping options",
                    onClick = { showingShippingPreferences = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Receipt,
                    title = "Billing History",
                    subtitle = "View detailed billing information",
                    onClick = { showingBillingHistory = true }
                )
            }
            
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Layers,
                    title = "Bulk Shipping Tools",
                    subtitle = "Tools for multiple shipments",
                    onClick = { showingBulkShippingTools = true },
                    modifier = Modifier.padding(bottom = 14.dp) // Extra 14dp + 2dp = 16dp total
                )
            }
        }
        
        // Common Items
        item {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update your personal information",
                onClick = { showingEditProfile = true }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Default.HelpOutline,
                title = "Help & Support",
                subtitle = "Get help and contact support",
                onClick = { showingHelpSupport = true }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Default.Description,
                title = "Terms & Privacy",
                subtitle = "Read our terms and privacy policy",
                onClick = { showingTermsPrivacy = true }
            )
        }
        
        item {
            // Logout Button
            Button(
                onClick = { showingLogoutAlert = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 14.dp), // Extra 14dp + 2dp = 16dp total spacing from menu items
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    }
    
    // Sheet Presentations
    if (showingLogoutAlert) {
        AlertDialog(
            onDismissRequest = { showingLogoutAlert = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        showingLogoutAlert = false
                    }
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showingLogoutAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Placeholder sheets for all the menu items
    if (showingEditProfile) {
        PlaceholderSheet("Edit Profile") { showingEditProfile = false }
    }
    if (showingHelpSupport) {
        PlaceholderSheet("Help & Support") { showingHelpSupport = false }
    }
    if (showingTermsPrivacy) {
        PlaceholderSheet("Terms & Privacy") { showingTermsPrivacy = false }
    }
    
    // Carrier-specific sheets
    if (showingVehicleInfo) {
        PlaceholderSheet("Vehicle Information") { showingVehicleInfo = false }
    }
    if (showingDeliveryHistory) {
        PlaceholderSheet("Delivery History") { showingDeliveryHistory = false }
    }
    if (showingCarrierPaymentMethods) {
        PlaceholderSheet("Payment Methods") { showingCarrierPaymentMethods = false }
    }
    if (showingAvailabilitySettings) {
        PlaceholderSheet("Availability Settings") { showingAvailabilitySettings = false }
    }
    if (showingRoutePreferences) {
        PlaceholderSheet("Route Preferences") { showingRoutePreferences = false }
    }
    if (showingDriverDocuments) {
        PlaceholderSheet("Driver Documents") { showingDriverDocuments = false }
    }
    
    // Shipper-specific sheets
    if (showingShippingAddresses) {
        PlaceholderSheet("Shipping Addresses") { showingShippingAddresses = false }
    }
    if (showingOrderHistory) {
        PlaceholderSheet("Order History") { showingOrderHistory = false }
    }
    if (showingShipperPaymentMethods) {
        PlaceholderSheet("Payment Methods") { showingShipperPaymentMethods = false }
    }
    if (showingActiveShipments) {
        PlaceholderSheet("Active Shipments") { showingActiveShipments = false }
    }
    if (showingNotifications) {
        PlaceholderSheet("Notifications") { showingNotifications = false }
    }
    if (showingShippingPreferences) {
        PlaceholderSheet("Shipping Preferences") { showingShippingPreferences = false }
    }
    if (showingBillingHistory) {
        PlaceholderSheet("Billing History") { showingBillingHistory = false }
    }
    if (showingBulkShippingTools) {
        PlaceholderSheet("Bulk Shipping Tools") { showingBulkShippingTools = false }
    }
}