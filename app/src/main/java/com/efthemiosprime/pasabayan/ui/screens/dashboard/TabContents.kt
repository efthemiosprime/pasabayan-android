package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole

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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (currentRole) {
                UserRole.SHIPPER -> "Browse Available Trips"
                UserRole.CARRIER -> "Browse Package Requests"
            },
            style = MaterialTheme.typography.headlineSmall
        )
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (currentRole) {
                UserRole.SHIPPER -> "Packages"
                UserRole.CARRIER -> "Trips"
            },
            style = MaterialTheme.typography.headlineSmall
        )
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (currentRole) {
                UserRole.SHIPPER -> "Create Package Request"
                UserRole.CARRIER -> "Create Trip"
            },
            style = MaterialTheme.typography.headlineSmall
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