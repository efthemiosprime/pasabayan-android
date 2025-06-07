package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.ui.components.TripCard
import com.efthemiosprime.pasabayan.ui.components.packages.PackageRequestCard

/**
 * Home tab content that mirrors iOS ShipperHomeTab/CarrierHomeTab
 */
@Composable
fun HomeTabContent(
    modifier: Modifier = Modifier,
    user: User?,
    currentRole: UserRole,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        user?.let {
            Text(
                text = it.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Current role: ${currentRole.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Quick stats placeholder
        Text(
            text = "Quick Stats Coming Soon...",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

/**
 * Analytics tab content
 */
@Composable
fun AnalyticsTabContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Analytics Coming Soon",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

/**
 * Browse tab content
 */
@Composable
fun BrowseTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = when (currentRole) {
                    UserRole.SHIPPER -> "Available Trips"
                    UserRole.CARRIER -> "Available Package Requests"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        when (currentRole) {
            UserRole.SHIPPER -> {
                // Show available trips for shippers
                items(Trip.mockTrips.filter { it.tripStatus == TripStatus.ACTIVE || it.tripStatus == TripStatus.SCHEDULED }) { trip ->
                    TripCard(
                        trip = trip,
                        onTap = { /* Handle trip selection */ }
                    )
                }
            }
            UserRole.CARRIER -> {
                // Show available package requests for carriers
                val mockPackages = listOf(
                    PackageRequest(
                        id = 1,
                        shipperId = 1,
                        title = "Electronics Package",
                        description = "Laptop and accessories",
                        pickupLocation = "SM Mall of Asia",
                        deliveryLocation = "Makati CBD",
                        preferredPickupDate = "2024-01-15",
                        packageSize = PackageSize.MEDIUM,
                        packageWeight = 2.5,
                        packageValue = 45000.0,
                        isFragile = true,
                        status = PackageRequestStatus.PENDING,
                        createdAt = "2024-01-14T10:00:00Z",
                        updatedAt = "2024-01-14T10:00:00Z"
                    ),
                    PackageRequest(
                        id = 2,
                        shipperId = 2,
                        title = "Documents",
                        description = "Important business documents",
                        pickupLocation = "BGC Taguig",
                        deliveryLocation = "Ortigas Center",
                        preferredPickupDate = "2024-01-16",
                        packageSize = PackageSize.SMALL,
                        packageWeight = 0.5,
                        packageValue = 1000.0,
                        status = PackageRequestStatus.PENDING,
                        createdAt = "2024-01-14T11:00:00Z",
                        updatedAt = "2024-01-14T11:00:00Z"
                    )
                )
                
                items(mockPackages) { packageRequest ->
                    PackageRequestCard(
                        packageRequest = packageRequest
                    )
                }
            }
        }
    }
}

/**
 * Packages or trips tab content
 */
@Composable
fun PackagesOrTripsTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = when (currentRole) {
                    UserRole.SHIPPER -> "My Package Requests"
                    UserRole.CARRIER -> "My Trips"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        when (currentRole) {
            UserRole.CARRIER -> {
                // Show carrier's trips with filter chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { },
                                label = { Text("All") },
                                selected = true
                            )
                        }
                        items(TripStatus.allCases) { status ->
                            FilterChip(
                                onClick = { },
                                label = { Text(status.displayName) },
                                selected = false
                            )
                        }
                    }
                }
                
                items(Trip.mockTrips) { trip ->
                    TripCard(
                        trip = trip,
                        onTap = { /* Handle trip tap */ }
                    )
                }
            }
            UserRole.SHIPPER -> {
                // Show shipper's package requests
                val mockPackages = listOf(
                    PackageRequest(
                        id = 1,
                        shipperId = 1,
                        title = "Electronics Package",
                        description = "Laptop and accessories",
                        pickupLocation = "SM Mall of Asia",
                        deliveryLocation = "Makati CBD",
                        preferredPickupDate = "2024-01-15",
                        packageSize = PackageSize.MEDIUM,
                        packageWeight = 2.5,
                        packageValue = 45000.0,
                        isFragile = true,
                        status = PackageRequestStatus.MATCHED,
                        createdAt = "2024-01-14T10:00:00Z",
                        updatedAt = "2024-01-14T11:00:00Z"
                    ),
                    PackageRequest(
                        id = 2,
                        shipperId = 1,
                        title = "Gift Package",
                        description = "Birthday gift for family",
                        pickupLocation = "Quezon City",
                        deliveryLocation = "Manila",
                        preferredPickupDate = "2024-01-17",
                        packageSize = PackageSize.LARGE,
                        packageWeight = 5.0,
                        packageValue = 8000.0,
                        status = PackageRequestStatus.DELIVERED,
                        createdAt = "2024-01-13T15:00:00Z",
                        updatedAt = "2024-01-17T18:30:00Z"
                    ),
                    PackageRequest(
                        id = 3,
                        shipperId = 1,
                        title = "Documents",
                        description = "Important business documents",
                        pickupLocation = "BGC Taguig",
                        deliveryLocation = "Ortigas Center",
                        preferredPickupDate = "2024-01-16",
                        packageSize = PackageSize.SMALL,
                        packageWeight = 0.5,
                        packageValue = 1000.0,
                        status = PackageRequestStatus.PENDING,
                        createdAt = "2024-01-14T11:00:00Z",
                        updatedAt = "2024-01-14T11:00:00Z"
                    )
                )
                
                items(mockPackages) { packageRequest ->
                    PackageRequestCard(
                        packageRequest = packageRequest
                    )
                }
            }
        }
    }
}

/**
 * Create tab content
 */
@Composable
fun CreateTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = when (currentRole) {
                UserRole.SHIPPER -> "Create Package Request"
                UserRole.CARRIER -> "Create Trip"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        when (currentRole) {
            UserRole.SHIPPER -> {
                CreatePackageForm()
            }
            UserRole.CARRIER -> {
                CreateTripForm()
            }
        }
    }
}

@Composable
private fun CreatePackageForm() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Package Request",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "• Package Title: Electronics Package",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Pickup: SM Mall of Asia",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Delivery: Makati CBD",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Size: Medium (2.5kg)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Value: ₱45,000",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = { /* Create package request */ },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Create Package Request")
        }
        
        Text(
            text = "Full create form coming soon!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreateTripForm() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Trip Creation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "• Route: Manila → Cebu",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Method: Flight ✈️",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Departure: Tomorrow 10:00 AM",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Capacity: 15kg, 50L",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Price: ₱25.00/kg",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = { /* Create trip */ },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Create Trip")
        }
        
        Text(
            text = "Full create form coming soon!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Profile tab content
 */
@Composable
fun ProfileTabContent(
    modifier: Modifier = Modifier,
    user: User?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        user?.let {
            Text(text = "Name: ${it.name}")
            Text(text = "Email: ${it.email}")
            Text(text = "Rating: ${it.displayRating}")
            Text(text = "Verification: ${it.verificationLevel}")
        }
        
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

// ===== PREVIEW FUNCTIONS =====

/**
 * Home Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Shipper")
@Composable
fun HomeTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Carrier")
@Composable
fun HomeTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.CARRIER,
            onSignOut = { }
        )
    }
}

/**
 * Analytics Tab Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Analytics Tab")
@Composable
fun AnalyticsTabPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        AnalyticsTabContent()
    }
}

/**
 * Browse Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Browse Tab - Shipper")
@Composable
fun BrowseTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.SHIPPER)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Browse Tab - Carrier")
@Composable
fun BrowseTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Packages/Trips Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Packages Tab - Shipper")
@Composable
fun PackagesTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackagesOrTripsTabContent(currentRole = UserRole.SHIPPER)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Trips Tab - Carrier")
@Composable
fun TripsTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackagesOrTripsTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Create Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Create Tab - Shipper")
@Composable
fun CreateTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        CreateTabContent(currentRole = UserRole.SHIPPER)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Create Tab - Carrier")
@Composable
fun CreateTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        CreateTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Profile Tab Content Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Profile Tab")
@Composable
fun ProfileTabPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        ProfileTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

/**
 * Dark Theme Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Dark Theme")
@Composable
fun HomeTabDarkPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme(darkTheme = true) {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Profile Tab - Dark Theme")
@Composable
fun ProfileTabDarkPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme(darkTheme = true) {
        ProfileTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

/**
 * Tablet Layout Previews
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true, 
    name = "Home Tab - Tablet",
    widthDp = 1024,
    heightDp = 768
)
@Composable
fun HomeTabTabletPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

// Sample data for previews
private val sampleUser = User(
    id = 1,
    name = "Maria Santos",
    email = "maria.santos@example.com",
    avatar = null,
    phone = "+63917123456",
    phoneVerified = true,
    profileCompleted = true,
    provider = "google",
    providerId = "123456789",
    emailVerifiedAt = "2024-01-01T00:00:00Z",
    createdAt = "2024-01-01T00:00:00Z",
    updatedAt = "2024-01-01T00:00:00Z",
    userTypes = listOf("shipper", "carrier"),
    isActiveCarrier = true,
    isActiveShipper = true,
    rating = 4.7,
    totalRatings = 42,
    verificationLevel = "verified"
) 