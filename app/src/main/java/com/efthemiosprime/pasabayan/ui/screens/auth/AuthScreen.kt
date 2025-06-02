package com.efthemiosprime.pasabayan.ui.screens.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Authentication Screen for Pasabayan App
 * Mirrors iOS AuthView exactly with gradient background, feature preview, and authentication options
 * Features gradient background, feature preview, and multiple authentication options
 * Supports both One Tap and regular Google Sign-In
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    activity: Activity? = null,
    googleOneTapLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
    googleRegularSignInLauncher: ActivityResultLauncher<Intent>? = null
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    
    AuthScreenContent(
        isLoading = isLoading,
        error = error,
        onSignInWithApple = { 
            // TODO: Implement Apple Sign-In for Android
            // For now, use mock login
            authViewModel.mockLogin()
        },
        onSignInWithGoogle = { 
            // Use real Google Sign-In if available, otherwise mock
            if (activity != null && googleOneTapLauncher != null && googleRegularSignInLauncher != null) {
                authViewModel.signInWithGoogle(activity, googleOneTapLauncher, googleRegularSignInLauncher)
            } else {
                // Fallback to mock for preview/testing
                authViewModel.mockLogin()
            }
        },
        onSignInWithFacebook = {
            // TODO: Implement Facebook Sign-In
            authViewModel.mockLogin()
        },
        onMockLogin = {
            authViewModel.mockLogin()
        }
    )
}

@Composable
private fun AuthScreenContent(
    isLoading: Boolean,
    error: String?,
    onSignInWithApple: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithFacebook: () -> Unit,
    onMockLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background gradient - matching iOS colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.7f),
                            Color.Magenta.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        
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
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Pasabayan Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
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
                        color = Color.White
                    )
                    
                    Text(
                        text = "Your Trusted Delivery Partner",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sign in with Apple
                Button(
                    onClick = onSignInWithApple,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍎", // Apple emoji as placeholder
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Continue with Apple",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Sign in with Google
                Button(
                    onClick = onSignInWithGoogle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "G", // Google icon placeholder
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Sign in with Facebook
                Button(
                    onClick = onSignInWithFacebook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "f",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Continue with Facebook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Demo Login Button
                Button(
                    onClick = onMockLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Demo Login",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Error Message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
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

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInWithApple = { },
            onSignInWithGoogle = { },
            onSignInWithFacebook = { },
            onMockLogin = { }
        )
    }
}

@Preview(showBackground = true, name = "Auth Screen - Loading")
@Composable
fun AuthScreenLoadingPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = true,
            error = null,
            onSignInWithApple = { },
            onSignInWithGoogle = { },
            onSignInWithFacebook = { },
            onMockLogin = { }
        )
    }
}

@Preview(showBackground = true, name = "Auth Screen - Error State")
@Composable
fun AuthScreenErrorPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = "Authentication failed. Please try again.",
            onSignInWithApple = { },
            onSignInWithGoogle = { },
            onSignInWithFacebook = { },
            onMockLogin = { }
        )
    }
}

@Preview(showBackground = true, name = "Auth Screen - Dark Theme")
@Composable
fun AuthScreenDarkPreview() {
    PasabayanTheme(darkTheme = true) {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInWithApple = { },
            onSignInWithGoogle = { },
            onSignInWithFacebook = { },
            onMockLogin = { }
        )
    }
} 