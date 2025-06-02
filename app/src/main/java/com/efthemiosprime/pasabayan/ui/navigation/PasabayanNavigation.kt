package com.efthemiosprime.pasabayan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.ui.screens.auth.AuthScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.DashboardScreen
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.data.repository.AuthRepositoryImpl

/**
 * Main navigation component for the Pasabayan app
 * Mirrors iOS ContentView structure with authentication state handling
 */
@Composable
fun PasabayanNavigation() {
    // Proper ViewModel construction using factory
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepositoryImpl())
    }
    val authState by authViewModel.authState.collectAsState()
    
    if (authState.isAuthenticated) {
        DashboardScreen(authViewModel = authViewModel)
    } else {
        AuthScreen(authViewModel = authViewModel)
    }
} 