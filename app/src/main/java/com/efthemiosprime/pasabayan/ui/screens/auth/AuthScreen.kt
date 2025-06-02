package com.efthemiosprime.pasabayan.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.data.repository.AuthRepositoryImpl

/**
 * Authentication screen that mirrors iOS AuthView
 * Provides Google Sign-In functionality with modern Material 3 design
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    AuthScreenContent(
        isLoading = isLoading,
        error = authState.error,
        onSignInClick = { authViewModel.signInWithGoogle() }
    )
}

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Brand
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App Subtitle
        Text(
            text = "Peer-to-peer delivery platform",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Sign In Button
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
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
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Terms and Privacy
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInClick = { }
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
            onSignInClick = { }
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
            onSignInClick = { }
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
            onSignInClick = { }
        )
    }
} 