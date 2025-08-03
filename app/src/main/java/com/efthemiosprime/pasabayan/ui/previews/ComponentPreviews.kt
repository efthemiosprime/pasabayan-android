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
import com.efthemiosprime.pasabayan.ui.components.cards.StatCard
import com.efthemiosprime.pasabayan.ui.components.packages.PackageRequestCard
import com.efthemiosprime.pasabayan.ui.components.status.StatusChip
import com.efthemiosprime.pasabayan.ui.components.ProfileMenuItem
import com.efthemiosprime.pasabayan.ui.components.VerificationBadgeIcon
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Stat Card previews for different states and data
 */
@Preview(showBackground = true, name = "Stat Card - Packages")
@Composable
fun StatCardPackagesPreview() {
    PasabayanTheme {
        StatCard(
            title = "Total Packages",
            value = "24",
            icon = Icons.Default.Inventory2,
            color = Color.Blue,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stat Card - Earnings")
@Composable
fun StatCardEarningsPreview() {
    PasabayanTheme {
        StatCard(
            title = "Total Earnings",
            value = "$378",
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stat Card - Rating")
@Composable
fun StatCardRatingPreview() {
    PasabayanTheme {
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
 * Stat Cards Grid preview
 */
@Preview(showBackground = true, name = "Stat Cards Grid")
@Composable
fun StatCardsGridPreview() {
    PasabayanTheme {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatCard(
                    title = "Packages",
                    value = "24",
                    icon = Icons.Default.Inventory2,
                    color = Color.Blue
                )
            }
            item {
                StatCard(
                    title = "Earnings",
                    value = "$378", // Converted from ₱15,750 at 0.024 CAD/PHP
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                StatCard(
                    title = "Rating",
                    value = "4.8 ⭐",
                    icon = Icons.Default.Star,
                    color = Color(0xFFFFA726)
                )
            }
            item {
                StatCard(
                    title = "Trips",
                    value = "18",
                    icon = Icons.Default.LocalShipping,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

/**
 * Package Request Card previews for different statuses
 */
@Preview(showBackground = true, name = "Package Card - Pending")
@Composable
fun PackageCardPendingPreview() {
    PasabayanTheme {
        PackageRequestCard(
            packageRequest = PackageRequest(
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
                status = PackageRequestStatus.OPEN,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Package Card - In Transit")
@Composable
fun PackageCardInTransitPreview() {
    PasabayanTheme {
        PackageRequestCard(
            packageRequest = PackageRequest(
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
                status = PackageRequestStatus.DELIVERED,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Package Card - Delivered")
@Composable
fun PackageCardDeliveredPreview() {
    PasabayanTheme {
        PackageRequestCard(
            packageRequest = PackageRequest(
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
                status = PackageRequestStatus.DELIVERED,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Status Chip previews for all statuses
 */
@Preview(showBackground = true, name = "Status Chips")
@Composable
fun StatusChipsPreview() {
    PasabayanTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = PackageRequestStatus.OPEN)
                    StatusChip(status = PackageRequestStatus.PENDING_REQUEST)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = PackageRequestStatus.MATCHED)
                    StatusChip(status = PackageRequestStatus.DELIVERED)
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
 * Empty State View previews
 */
@Preview(showBackground = true, name = "Empty State - Packages")
@Composable
fun EmptyStatePackagesPreview() {
    PasabayanTheme {
        EmptyStateView(
            icon = Icons.Default.Inventory2,
            title = "No packages found",
            description = "You haven't created any package requests yet. Tap the + button to get started.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Trips")
@Composable
fun EmptyStateTripsPreview() {
    PasabayanTheme {
        EmptyStateView(
            icon = Icons.Default.LocalShipping,
            title = "No trips available",
            description = "There are no trips matching your preferences at the moment. Check back later.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Earnings")
@Composable
fun EmptyStateEarningsPreview() {
    PasabayanTheme {
        EmptyStateView(
            icon = Icons.Default.AttachMoney,
            title = "No earnings yet",
            description = "Complete your first delivery to start earning money on the platform.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Profile Menu Items previews
 */
@Preview(showBackground = true, name = "Profile Menu Items")
@Composable
fun ProfileMenuItemsPreview() {
    PasabayanTheme {
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
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help with using the app",
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Verification Badge preview
 */
@Preview(showBackground = true, name = "Verification Badge")
@Composable
fun VerificationBadgePreview() {
    PasabayanTheme {
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
 * Dark theme component previews
 */
@Preview(showBackground = true, name = "Components - Dark Theme")
@Composable
fun ComponentsDarkThemePreview() {
    PasabayanTheme(darkTheme = true) {
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
                StatusChip(status = PackageRequestStatus.DELIVERED)
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

/**
 * Responsive layout preview for tablets
 */
@Preview(
    showBackground = true,
    name = "Components - Tablet Layout",
    widthDp = 1024,
    heightDp = 768
)
@Composable
fun ComponentsTabletPreview() {
    PasabayanTheme {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(
                    title = "Packages",
                    value = "24",
                    icon = Icons.Default.Inventory2,
                    color = Color.Blue
                )
            }
            item {
                StatCard(
                    title = "Earnings",
                    value = "₱15,750",
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                StatCard(
                    title = "Rating",
                    value = "4.8 ⭐",
                    icon = Icons.Default.Star,
                    color = Color(0xFFFFA726)
                )
            }
        }
    }
} 