package com.efthemiosprime.pasabayan.ui.screens.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Main content composable for the authentication screen
 * Features gradient background, feature preview, and multiple authentication options
 * Mirrors iOS AuthView content structure exactly
 */
@Composable
fun AuthScreenContent(
    isLoading: Boolean,
    error: String?,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo and Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // App Icon
                Icon(
                    painter = painterResource(id = R.drawable.intl_delivery),
                    contentDescription = "Pasabayan Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color.Black
                )
                
                // Title and Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pasabayan",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = "Your Trusted Delivery Partner",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Features Preview Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.LocalShipping,
                    title = "Real-time Tracking",
                    description = "Track your deliveries in real-time"
                )
                FeatureRow(
                    icon = Icons.Default.Security,
                    title = "Secure & Safe",
                    description = "Verified drivers and secure payments"
                )
                FeatureRow(
                    icon = Icons.Default.Speed,
                    title = "Fast Delivery",
                    description = "Quick and reliable delivery service"
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Authentication Buttons Section
            SocialLoginButtons(
                isLoading = isLoading,
                onSignInWithGoogle = onSignInWithGoogle,
                onSignInWithFacebook = onSignInWithFacebook
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Error Message
            error?.let { errorMessage ->
                AuthErrorDisplay(errorMessage = errorMessage)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        // Loading Overlay - exactly like iOS
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                    
                    Text(
                        text = "Signing you in...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// MARK: - Previews
@Preview("Default")
@Composable
fun AuthScreenContentPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInWithGoogle = { },
            onSignInWithFacebook = { }
        )
    }
}

@Preview("Loading State")
@Composable
fun AuthScreenContentLoadingPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = true,
            error = null,
            onSignInWithGoogle = { },
            onSignInWithFacebook = { }
        )
    }
}

@Preview("Error State")
@Composable
fun AuthScreenContentErrorPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = "Authentication failed. Please try again.",
            onSignInWithGoogle = { },
            onSignInWithFacebook = { }
        )
    }
} 