package com.efthemiosprime.pasabayan.ui.previews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.screens.auth.AuthScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.DashboardScreen
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole

/**
 * Auth Screen Preview - Light Theme
 */
@Preview(showBackground = true, name = "Auth Screen - Light Theme")
@Composable
fun AuthScreenLightPreview() {
    PasabayanTheme {
        // Use AuthScreenContent directly to avoid ViewModel issues in preview
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInClick = { }
        )
    }
}

/**
 * Auth Screen Preview - Dark Theme
 */
@Preview(showBackground = true, name = "Auth Screen - Dark Theme")
@Composable
fun AuthScreenDarkPreview() {
    PasabayanTheme(darkTheme = true) {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInClick = { }
        )
    }
}

/**
 * Dashboard Preview - Phone Portrait
 */
@Preview(
    showBackground = true,
    name = "Dashboard - Phone Portrait",
    device = Devices.PIXEL_4
)
@Composable
fun DashboardPhonePortraitPreview() {
    PasabayanTheme {
        // Show dashboard content directly
        DashboardContentPreview()
    }
}

/**
 * Dashboard Preview - Phone Landscape
 */
@Preview(
    showBackground = true,
    name = "Dashboard - Phone Landscape",
    device = Devices.PIXEL_4,
    widthDp = 891,
    heightDp = 411
)
@Composable
fun DashboardPhoneLandscapePreview() {
    PasabayanTheme {
        DashboardContentPreview()
    }
}

/**
 * Dashboard Preview - Tablet
 */
@Preview(
    showBackground = true,
    name = "Dashboard - Tablet",
    device = Devices.PIXEL_C
)
@Composable
fun DashboardTabletPreview() {
    PasabayanTheme {
        DashboardContentPreview()
    }
}

/**
 * Dashboard Preview - Foldable
 */
@Preview(
    showBackground = true,
    name = "Dashboard - Foldable",
    device = Devices.FOLDABLE
)
@Composable
fun DashboardFoldablePreview() {
    PasabayanTheme {
        DashboardContentPreview()
    }
}

/**
 * Theme Color Scheme Preview
 */
@Preview(showBackground = true, name = "Theme Colors - Light")
@Composable
fun ThemeColorsLightPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Material 3 Color Scheme - Light",
                style = MaterialTheme.typography.headlineSmall
            )
            
            ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
            ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
            ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
            ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
            ColorSwatch("Background", MaterialTheme.colorScheme.background)
        }
    }
}

/**
 * Theme Color Scheme Preview - Dark
 */
@Preview(showBackground = true, name = "Theme Colors - Dark")
@Composable
fun ThemeColorsDarkPreview() {
    PasabayanTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Material 3 Color Scheme - Dark",
                style = MaterialTheme.typography.headlineSmall
            )
            
            ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
            ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
            ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
            ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
            ColorSwatch("Background", MaterialTheme.colorScheme.background)
        }
    }
}

/**
 * Typography Preview
 */
@Preview(showBackground = true, name = "Typography Styles")
@Composable
fun TypographyPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Display Large",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Headline Large",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Headline Medium",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Title Large",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Title Medium",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Body Large",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Body Medium",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Label Small",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Helper composable for color swatches
 */
@Composable
private fun ColorSwatch(
    name: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(all = 16.dp),
            color = if (color.luminance() > 0.5) {
                Color.Black
            } else {
                Color.White
            }
        )
    }
}

/**
 * Helper composable for dashboard preview without ViewModels
 */
@Composable
private fun DashboardContentPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pasabayan Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to the logistics platform",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Simplified AuthScreenContent composable for preview
 */
@Composable
private fun AuthScreenContent(
    isLoading: Boolean,
    error: String?,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Brand
        Card(
            modifier = Modifier.size(120.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📦",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Title
        Text(
            text = "Pasabayan",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App Subtitle
        Text(
            text = "Peer-to-peer delivery platform",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Sign In Button
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
        
        error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
} 