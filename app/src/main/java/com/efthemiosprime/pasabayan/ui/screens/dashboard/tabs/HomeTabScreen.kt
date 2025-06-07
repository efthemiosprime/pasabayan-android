package com.efthemiosprime.pasabayan.ui.screens.dashboard.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Home Tab Screen that mirrors iOS ShipperHomeTab/CarrierHomeTab
 * Mirrors iOS HomeTabView.swift structure with clean separation
 * Displays welcome message, user info, and quick actions
 */
@Composable
fun HomeTabScreen(
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

// MARK: - Previews

@Preview(showBackground = true, name = "Home Tab - Shipper")
@Composable
fun HomeTabShipperPreview() {
    PasabayanTheme {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@Preview(showBackground = true, name = "Home Tab - Carrier")
@Composable
fun HomeTabCarrierPreview() {
    PasabayanTheme {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.CARRIER,
            onSignOut = { }
        )
    }
}

@Preview(showBackground = true, name = "Home Tab - Dark Theme")
@Composable
fun HomeTabDarkPreview() {
    PasabayanTheme(darkTheme = true) {
        HomeTabScreen(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@Preview(
    showBackground = true, 
    name = "Home Tab - Tablet",
    widthDp = 1024,
    heightDp = 768
)
@Composable
fun HomeTabTabletPreview() {
    PasabayanTheme {
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