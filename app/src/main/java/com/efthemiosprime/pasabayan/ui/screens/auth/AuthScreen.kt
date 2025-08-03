package com.efthemiosprime.pasabayan.ui.screens.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.screens.auth.components.AuthScreenContent
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Authentication Screen for Pasabayan App
 * Main screen component that coordinates authentication flow
 * Mirrors iOS AuthView exactly with clean separation of concerns
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
        onSignInWithGoogle = {
            // Only use real Google Sign-In
            if (activity != null && googleOneTapLauncher != null && googleRegularSignInLauncher != null) {
                authViewModel.signInWithGoogle(activity, googleOneTapLauncher, googleRegularSignInLauncher)
            }
        },
        onSignInWithFacebook = {
            // Only use real Facebook Sign-In
            if (activity != null) {
                authViewModel.signInWithFacebook(activity)
            }
        }
    )
}

// MARK: - Previews

@Preview("Default Auth Screen")
@Composable
fun AuthScreenPreview() {
    PasabayanTheme {
        AuthScreenContent(
            isLoading = false,
            error = null,
            onSignInWithGoogle = { },
            onSignInWithFacebook = { }
        )
    }
} 