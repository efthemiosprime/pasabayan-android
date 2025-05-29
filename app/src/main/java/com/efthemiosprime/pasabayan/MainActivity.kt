package com.efthemiosprime.pasabayan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.efthemiosprime.pasabayan.ui.screen.AuthScreen
import com.efthemiosprime.pasabayan.ui.screen.DashboardScreen
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.viewmodel.DeliveryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasabayanTheme {
                PasabayanApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasabayanApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel = remember { AuthViewModel(context) }
    val deliveryViewModel = remember { DeliveryViewModel() }
    val authUiState by authViewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (authUiState.isLoggedIn) {
                TopAppBar(
                    title = { Text("Pasabayan") },
                    actions = {
                        IconButton(
                            onClick = { authViewModel.signOut() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (authUiState.isLoggedIn) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = true,
                        onClick = { /* Navigate to dashboard */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Deliveries") },
                        selected = false,
                        onClick = { /* Navigate to deliveries */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Create") },
                        selected = false,
                        onClick = { /* Navigate to create delivery */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile") },
                        selected = false,
                        onClick = { /* Navigate to profile */ }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authUiState.isLoggedIn) "dashboard" else "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    authViewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("dashboard") {
                authUiState.currentUser?.let { user ->
                    DashboardScreen(
                        currentUser = user,
                        deliveryViewModel = deliveryViewModel,
                        onCreateDelivery = { /* Navigate to create delivery */ },
                        onViewDeliveries = { /* Navigate to deliveries */ },
                        onViewProfile = { /* Navigate to profile */ }
                    )
                }
            }
        }
    }

    // Handle navigation based on auth state
    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            navController.navigate("dashboard") {
                popUpTo("auth") { inclusive = true }
            }
        } else {
            navController.navigate("auth") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }
}