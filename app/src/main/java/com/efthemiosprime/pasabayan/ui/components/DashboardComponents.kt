package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.presentation.viewmodel.*
import com.efthemiosprime.pasabayan.ui.components.cards.StatCard
import com.efthemiosprime.pasabayan.ui.components.role.RoleSwitcherView
import com.efthemiosprime.pasabayan.ui.components.status.StatusChip
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.components.packages.PackageRequestCard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import kotlinx.coroutines.launch

// StatCard extracted to ui/components/cards/StatCard.kt

/**
 * Role Switcher View component matching iOS role switching behavior
 * Only switches to carrier when API confirms is_active_carrier: true
 */
@Composable
fun RoleSwitcherView(
    roleViewModel: RoleViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by roleViewModel.currentRole.collectAsState()
    val isLoading by roleViewModel.isLoading.collectAsState()
    val errorMessage by roleViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    // Initialize RoleViewModel with context if not already done
    LaunchedEffect(Unit) {
        roleViewModel.initialize(context)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { 
                    if (!isLoading) {
                        roleViewModel.switchRole(UserRole.SHIPPER)
                    }
                },
                label = { 
                    if (isLoading && currentRole == UserRole.SHIPPER) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Shipper")
                        }
                    } else {
                        Text("Shipper")
                    }
                },
                selected = currentRole == UserRole.SHIPPER,
                enabled = !isLoading,
                leadingIcon = if (currentRole == UserRole.SHIPPER && !isLoading) {
                    {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
            
            FilterChip(
                onClick = { 
                    if (!isLoading) {
                        roleViewModel.clearError() // Clear any previous errors
                        roleViewModel.switchRole(UserRole.CARRIER)
                    }
                },
                label = { 
                    if (isLoading && currentRole != UserRole.CARRIER) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Carrier")
                        }
                    } else {
                        Text("Carrier")
                    }
                },
                selected = currentRole == UserRole.CARRIER,
                enabled = !isLoading,
                leadingIcon = if (currentRole == UserRole.CARRIER && !isLoading) {
                    {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
        
        // Show error message if any
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = { roleViewModel.clearError() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Verification Badge Icon component that shows different styles based on verification level
 */
@Composable
fun VerificationBadgeIcon(
    verificationLevel: String = "verified",
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconColor, icon) = when (verificationLevel.lowercase()) {
        "verified" -> Triple(Color.Blue, Color.White, Icons.Default.Verified)
        "basic" -> Triple(Color(0xFFFFA726), Color.White, Icons.Default.VerifiedUser) // Orange
        "unverified" -> Triple(Color(0xFFE0E0E0), Color(0xFF757575), Icons.Default.Cancel) // Gray
        else -> Triple(Color(0xFFE0E0E0), Color(0xFF757575), Icons.Default.HelpOutline) // Gray with question
    }
    
    Box(
        modifier = modifier
            .size(16.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Verification: $verificationLevel",
            tint = iconColor,
            modifier = Modifier.size(12.dp)
        )
    }
}

/**
 * Complete verification status display with badge and text
 */
@Composable
fun VerificationStatusDisplay(
    verificationLevel: String = "verified",
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    val statusText = when (verificationLevel.lowercase()) {
        "verified" -> "Verified Account"
        "basic" -> "Basic Verification"
        "unverified" -> "Unverified Account"
        else -> "Unknown Status"
    }
    
    val textColor = when (verificationLevel.lowercase()) {
        "verified" -> Color.Blue
        "basic" -> Color(0xFFFFA726)
        "unverified" -> Color(0xFF757575)
        else -> Color(0xFF757575)
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VerificationBadgeIcon(verificationLevel = verificationLevel)
        
        if (showText) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Carrier Status Card component matching iOS CarrierStatusCard
 */
@Composable
fun CarrierStatusCard(
    carrierViewModel: CarrierViewModel,
    modifier: Modifier = Modifier
) {
    val isCarrierProfileSetup by carrierViewModel.isCarrierProfileSetup.collectAsState()
    val isCarrierActive by carrierViewModel.isCarrierActive.collectAsState()
    val carrierStatusText = carrierViewModel.carrierStatusText
    
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Carrier Status",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Button(
                    onClick = { carrierViewModel.toggleCarrierStatus() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCarrierProfileSetup) {
                            if (isCarrierActive) Color.Green else Color.Gray
                        } else Color.Gray
                    ),
                    shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card)
                ) {
                    Text(
                        text = carrierStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
            
            if (!isCarrierProfileSetup) {
                Text(
                    text = "Complete your carrier profile to start accepting deliveries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Empty State View component matching iOS EmptyStateView
 */
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Set your desired background color here

    ) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.Gray,
                modifier = Modifier.size(60.dp)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Status Chip component for package/trip status
 */
@Composable
fun StatusChip(
    status: PackageRequestStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PackageRequestStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PackageRequestStatus.OPEN -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        PackageRequestStatus.MATCHED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        PackageRequestStatus.BOOKED -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        PackageRequestStatus.IN_TRANSIT -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        PackageRequestStatus.DELIVERED -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        PackageRequestStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// PackageRequestCard moved to ui.components.packages.PackageRequestCard

// PackageListView moved to ui.components.packages.PackageListView

/**
 * Analytics View placeholder matching iOS AnalyticsView
 */
@Composable
fun AnalyticsView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Analytics dashboard coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Create Package View placeholder
 */
@Composable
fun CreatePackageView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Create Package Request",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Package creation form coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Profile View - Exact iOS ProfileView implementation
 */
@Composable
fun ProfileView(
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
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Set your desired background color here

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
                        
                        VerificationBadgeIcon()
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)

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
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(120.dp)
            ) {
                if (currentRole == UserRole.CARRIER) {
                    // Carrier Stats
                    item {
                        StatItem(
                            title = "Deliveries",
                            value = "147",
                            icon = Icons.Default.LocalShipping,
                            color = Color.Blue
                        )
                    }
                    item {
                        StatItem(
                            title = "Rating",
                            value = "4.9",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFD700)
                        )
                    }
                    item {
                        StatItem(
                            title = "Earnings",
                            value = "$298.80",
                            icon = Icons.Default.AttachMoney,
                            color = Color.Green
                        )
                    }
                } else {
                    // Shipper Stats
                    item {
                        StatItem(
                            title = "Packages",
                            value = "28",
                            icon = Icons.Default.Inventory,
                            color = Color(0xFF9C27B0)
                        )
                    }
                    item {
                        StatItem(
                            title = "Rating",
                            value = "4.7",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFD700)
                        )
                    }
                    item {
                        StatItem(
                            title = "Monthly Spent",
                            value = "$76.80",
                            icon = Icons.Default.CreditCard,
                            color = Color.Blue
                        )
                    }
                }
            }
        }
        
        item {
            // Role-Specific Menu Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
                {

                    if (currentRole == UserRole.CARRIER) {
                        // Carrier-specific menu items
                        ProfileMenuItem(
                            icon = Icons.Default.DirectionsCar,
                            title = "Vehicle Information",
                            subtitle = "Manage your delivery vehicle details",
                            onClick = { showingVehicleInfo = true },
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "Delivery History",
                            subtitle = "View your completed deliveries",
                            onClick = { showingDeliveryHistory = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.CreditCard,
                            title = "Payment Methods",
                            subtitle = "Manage accounts for receiving earnings",
                            onClick = { showingCarrierPaymentMethods = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Schedule,
                            title = "Availability Settings",
                            subtitle = "Set your working hours and availability",
                            onClick = { showingAvailabilitySettings = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Map,
                            title = "Route Preferences",
                            subtitle = "Configure preferred delivery routes",
                            onClick = { showingRoutePreferences = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Description,
                            title = "Driver Documents",
                            subtitle = "Manage license and vehicle documents",
                            onClick = { showingDriverDocuments = true }
                        )
                        
                    } else {
                        // Shipper-specific menu items
                        ProfileMenuItem(
                            icon = Icons.Default.LocationOn,
                            title = "Shipping Addresses",
                            subtitle = "Manage pickup and delivery addresses",
                            onClick = { showingShippingAddresses = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "Order History",
                            subtitle = "View your shipping history",
                            onClick = { showingOrderHistory = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.CreditCard,
                            title = "Payment Methods",
                            subtitle = "Manage cards for paying shipping costs",
                            onClick = { showingShipperPaymentMethods = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Inventory,
                            title = "Active Shipments",
                            subtitle = "Track your current packages",
                            onClick = { showingActiveShipments = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Configure shipping notifications",
                            onClick = { showingNotifications = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Shipping Preferences",
                            subtitle = "Set default shipping options",
                            onClick = { showingShippingPreferences = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Receipt,
                            title = "Billing History",
                            subtitle = "View detailed billing information",
                            onClick = { showingBillingHistory = true }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Layers,
                            title = "Bulk Shipping Tools",
                            subtitle = "Tools for multiple shipments",
                            onClick = { showingBulkShippingTools = true }
                        )
                    }
                }
            }
        }
        
        item {
            // Common Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information",
                        onClick = { showingEditProfile = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { showingHelpSupport = true }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(start = 50.dp))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Description,
                        title = "Terms & Privacy",
                        subtitle = "Read our terms and privacy policy",
                        onClick = { showingTermsPrivacy = true }
                    )
                }
            }
        }
        
        item {
            // Logout Button
            Button(
                onClick = { showingLogoutAlert = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )
                }
            }
        }
        
        item {
            // App Version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Pasabayan",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Version 0.9.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Logout Alert Dialog
    if (showingLogoutAlert) {
        AlertDialog(
            onDismissRequest = { showingLogoutAlert = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        showingLogoutAlert = false
                    }
                ) {
                    Text("Sign Out", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showingLogoutAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Placeholder sheets for all menu items
    // Note: In a real implementation, these would be separate screens/composables
    if (showingEditProfile) {
        PlaceholderSheet(title = "Edit Profile") { showingEditProfile = false }
    }
    if (showingHelpSupport) {
        PlaceholderSheet(title = "Help & Support") { showingHelpSupport = false }
    }
    if (showingTermsPrivacy) {
        PlaceholderSheet(title = "Terms & Privacy") { showingTermsPrivacy = false }
    }
    
    // Carrier sheets
    if (showingVehicleInfo) {
        PlaceholderSheet(title = "Vehicle Information") { showingVehicleInfo = false }
    }
    if (showingDeliveryHistory) {
        PlaceholderSheet(title = "Delivery History") { showingDeliveryHistory = false }
    }
    if (showingCarrierPaymentMethods) {
        PlaceholderSheet(title = "Payment Methods") { showingCarrierPaymentMethods = false }
    }
    if (showingAvailabilitySettings) {
        PlaceholderSheet(title = "Availability Settings") { showingAvailabilitySettings = false }
    }
    if (showingRoutePreferences) {
        PlaceholderSheet(title = "Route Preferences") { showingRoutePreferences = false }
    }
    if (showingDriverDocuments) {
        PlaceholderSheet(title = "Driver Documents") { showingDriverDocuments = false }
    }
    
    // Shipper sheets
    if (showingShippingAddresses) {
        PlaceholderSheet(title = "Shipping Addresses") { showingShippingAddresses = false }
    }
    if (showingOrderHistory) {
        PlaceholderSheet(title = "Order History") { showingOrderHistory = false }
    }
    if (showingShipperPaymentMethods) {
        PlaceholderSheet(title = "Payment Methods") { showingShipperPaymentMethods = false }
    }
    if (showingActiveShipments) {
        PlaceholderSheet(title = "Active Shipments") { showingActiveShipments = false }
    }
    if (showingNotifications) {
        PlaceholderSheet(title = "Notifications") { showingNotifications = false }
    }
    if (showingShippingPreferences) {
        PlaceholderSheet(title = "Shipping Preferences") { showingShippingPreferences = false }
    }
    if (showingBillingHistory) {
        PlaceholderSheet(title = "Billing History") { showingBillingHistory = false }
    }
    if (showingBulkShippingTools) {
        PlaceholderSheet(title = "Bulk Shipping Tools") { showingBulkShippingTools = false }
    }
}

/**
 * Stat Item component matching iOS StatItem
 */
@Composable
fun StatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Profile Menu Item component matching iOS ProfileMenuItem
 */
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Placeholder Sheet for menu items
 */
@Composable
fun PlaceholderSheet(
    title: String,
    onDismiss: () -> Unit
) {
    // In a real implementation, this would be a proper Sheet/Dialog
    // For now, we'll use an AlertDialog as a placeholder
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "This feature is under development and will be available soon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// CarrierTripsView moved to ui.screens.carrier.CarrierTripsScreen

@Composable
fun CarrierBookingsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Carrier Matches",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun CarrierEarningsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Carrier Earnings",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// ===== PREVIEW FUNCTIONS =====

/**
 * Stat Card Component Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Stat Card - Packages")
@Composable
fun StatCardPackagesPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        StatCard(
            title = "Total Packages",
            value = "24",
            icon = Icons.Default.Inventory2,
            color = Color.Blue,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Stat Card - Earnings")
@Composable
fun StatCardEarningsPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        StatCard(
            title = "Total Earnings",
            value = "$378",
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Stat Card - Rating")
@Composable
fun StatCardRatingPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        StatCard(
            title = "Average Rating",
            value = "4.8 ⭐",
            icon = Icons.Default.Star,
            color = Color(0xFFFFA726),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Package Request Card Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Package Card - Pending")
@Composable
fun PackageCardPendingPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackageRequestCard(
            packageRequest = samplePackageRequestPending,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Package Card - In Transit")
@Composable
fun PackageCardInTransitPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackageRequestCard(
            packageRequest = samplePackageRequestInTransit,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Status Chip Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Status Chips")
@Composable
fun StatusChipsPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = PackageRequestStatus.PENDING)
                    StatusChip(status = PackageRequestStatus.MATCHED)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = PackageRequestStatus.BOOKED)
                    StatusChip(status = PackageRequestStatus.IN_TRANSIT)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = PackageRequestStatus.DELIVERED)
                    StatusChip(status = PackageRequestStatus.CANCELLED)
                }
            }
        }
    }
}

/**
 * Empty State View Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Empty State - Packages")
@Composable
fun EmptyStatePackagesPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        EmptyStateView(
            icon = Icons.Default.Inventory2,
            title = "No packages found",
            description = "You haven't created any package requests yet. Tap the + button to get started.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Empty State - Trips")
@Composable
fun EmptyStateTripsPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        EmptyStateView(
            icon = Icons.Default.LocalShipping,
            title = "No trips available",
            description = "There are no trips matching your preferences at the moment. Check back later.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Profile Menu Item Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Profile Menu Items")
@Composable
fun ProfileMenuItemsPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    subtitle = "Update your personal information",
                    onClick = { }
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Payment,
                    title = "Payment Methods",
                    subtitle = "Manage your payment options",
                    onClick = { }
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Customize your notification preferences",
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Verification Badge Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Verification Badge")
@Composable
fun VerificationBadgePreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Verified User")
            VerificationBadgeIcon()
        }
    }
}

/**
 * Dark Theme Components Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Components - Dark Theme")
@Composable
fun ComponentsDarkThemePreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme(darkTheme = true) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(
                    title = "Total Packages",
                    value = "24",
                    icon = Icons.Default.Inventory2,
                    color = Color.Blue
                )
            }
            item {
                StatusChip(status = PackageRequestStatus.IN_TRANSIT)
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    subtitle = "Update your personal information",
                    onClick = { }
                )
            }
        }
    }
}

// Sample data for previews
private val samplePackageRequestPending = PackageRequest(
    id = 1,
    shipperId = 1,
    title = "Important Documents",
    description = "Legal documents that need urgent delivery",
    pickupLocation = "Makati CBD",
    deliveryLocation = "BGC, Taguig",
    preferredPickupDate = "2024-01-02",
    packageSize = PackageSize.SMALL,
    packageWeight = 0.5,
    packageValue = 24.0, // Converted from ₱1,000 at 0.024 CAD/PHP
    isFragile = false,
    specialInstructions = "Handle with care",
    status = PackageRequestStatus.PENDING,
    createdAt = "2024-01-01T00:00:00Z",
    updatedAt = "2024-01-01T00:00:00Z"
)

private val samplePackageRequestInTransit = PackageRequest(
    id = 2,
    shipperId = 1,
    title = "E-commerce Package",
    description = "Online shopping items from various stores",
    pickupLocation = "Quezon City",
    deliveryLocation = "Manila",
    preferredPickupDate = "2024-01-03",
    packageSize = PackageSize.MEDIUM,
    packageWeight = 2.5,
    packageValue = 120.0, // Converted from ₱5,000 at 0.024 CAD/PHP
    isFragile = false,
    specialInstructions = "Call recipient before delivery",
    status = PackageRequestStatus.IN_TRANSIT,
    createdAt = "2024-01-01T00:00:00Z",
    updatedAt = "2024-01-01T00:00:00Z"
) 