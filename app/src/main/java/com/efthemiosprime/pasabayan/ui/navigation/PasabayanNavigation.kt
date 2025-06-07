package com.efthemiosprime.pasabayan.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.ui.screens.auth.AuthScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.DashboardScreen
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.data.repository.AuthRepositoryImpl

/**
 * Main navigation component for the Pasabayan app
 * Mirrors iOS ContentView structure with authentication state handling
 * Includes Google Sign-In integration with One Tap and regular fallback
 */
@Composable
fun PasabayanNavigation(
    activity: Activity?,
    googleOneTapLauncher: ActivityResultLauncher<IntentSenderRequest>?,
    googleRegularSignInLauncher: ActivityResultLauncher<Intent>?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Proper ViewModel construction using factory
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepositoryImpl(context))
    }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    if (isAuthenticated) {
        DashboardScreen(
            authViewModel = authViewModel
        )
    } else {
        AuthScreen(
            authViewModel = authViewModel,
            activity = activity,
            googleOneTapLauncher = googleOneTapLauncher,
            googleRegularSignInLauncher = googleRegularSignInLauncher
        )
    }
} 