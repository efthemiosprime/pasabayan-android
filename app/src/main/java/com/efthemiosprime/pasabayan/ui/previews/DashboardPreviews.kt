package com.efthemiosprime.pasabayan.ui.previews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.ui.components.CreatePackageView
import com.efthemiosprime.pasabayan.ui.components.packages.PackageListView
import com.efthemiosprime.pasabayan.ui.screens.analytics.AnalyticsScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.tabs.HomeTabScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.BrowseTabContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.ProfileTabContent
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Preview for Shipper Dashboard Home Tab
 */
@Preview(showBackground = true, name = "Shipper Home Tab")
@Composable
fun ShipperHomeTabPreview() {
    PasabayanTheme {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

/**
 * Preview for Carrier Dashboard Home Tab
 */
@Preview(showBackground = true, name = "Carrier Home Tab")
@Composable
fun CarrierHomeTabPreview() {
    PasabayanTheme {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.CARRIER,
            onSignOut = { }
        )
    }
}

/**
 * Preview for Analytics Tab
 */
@Preview(showBackground = true, name = "Analytics Tab")
@Composable
fun AnalyticsTabPreview() {
    PasabayanTheme {
        AnalyticsScreen()
    }
}

/**
 * Preview for Package List View with packages
 */
@Preview(showBackground = true, name = "Package List - With Data")
@Composable
fun PackageListWithDataPreview() {
    PasabayanTheme {
        PackageListView(
            title = "Available Packages",
            packages = samplePackageRequests
        )
    }
}

/**
 * Preview for Package List View empty state
 */
@Preview(showBackground = true, name = "Package List - Empty")
@Composable
fun PackageListEmptyPreview() {
    PasabayanTheme {
        PackageListView(
            title = "My Packages",
            packages = emptyList()
        )
    }
}

/**
 * Preview for Create Package View
 */
@Preview(showBackground = true, name = "Create Package View")
@Composable
fun CreatePackageViewPreview() {
    PasabayanTheme {
        CreatePackageView()
    }
}

/**
 * Preview for Browse Tab - Shipper
 */
@Preview(showBackground = true, name = "Browse Tab - Shipper")
@Composable
fun BrowseTabShipperPreview() {
    PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.SHIPPER)
    }
}

/**
 * Preview for Browse Tab - Carrier
 */
@Preview(showBackground = true, name = "Browse Tab - Carrier")
@Composable
fun BrowseTabCarrierPreview() {
    PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Preview for Profile Tab
 */
@Preview(showBackground = true, name = "Profile Tab")
@Composable
fun ProfileTabPreview() {
    PasabayanTheme {
        ProfileTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

/**
 * Preview for Dark Theme Dashboard
 */
@Preview(showBackground = true, name = "Dashboard - Dark Theme")
@Composable
fun DashboardDarkThemePreview() {
    PasabayanTheme(darkTheme = true) {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

// Sample data for previews
private val sampleUser = User(
    id = 1,
    name = "John Doe",
    email = "john.doe@example.com",
    avatar = null,
    phone = "+63912345678",
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
    rating = 4.8,
    totalRatings = 25,
    verificationLevel = "verified"
)

private val samplePackageRequests = listOf(
    PackageRequest(
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
    ),
    PackageRequest(
        id = 2,
        shipperId = 1,
        title = "E-commerce Package",
        description = "Online shopping items delivery",
        pickupLocation = "Quezon City",
        deliveryLocation = "Manila",
        preferredPickupDate = "2024-01-03",
        packageSize = PackageSize.MEDIUM,
        packageWeight = 2.0,
        packageValue = 120.0, // Converted from ₱5,000 at 0.024 CAD/PHP
        isFragile = false,
        specialInstructions = "Call recipient before delivery",
        status = PackageRequestStatus.MATCHED,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    ),
    PackageRequest(
        id = 3,
        shipperId = 1,
        title = "Food Delivery",
        description = "Fresh food items from local market",
        pickupLocation = "Pasig Market",
        deliveryLocation = "Ortigas Center",
        preferredPickupDate = "2024-01-01",
        packageSize = PackageSize.SMALL,
        packageWeight = 1.5,
        packageValue = 19.2, // Converted from ₱800 at 0.024 CAD/PHP
        isFragile = true,
        specialInstructions = "Keep refrigerated",
        status = PackageRequestStatus.IN_TRANSIT,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )
) 