package com.efthemiosprime.pasabayan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.efthemiosprime.pasabayan.ui.navigation.PasabayanNavigation
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * Main Activity that handles authentication and navigation
 * Includes Google Sign-In activity result handling integrated with AuthService
 * Supports both One Tap Sign-In and regular Google Sign-In fallback
 */
class MainActivity : ComponentActivity() {
    
    // Google Sign-In launchers for activity results
    private lateinit var googleOneTapLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var googleRegularSignInLauncher: ActivityResultLauncher<Intent>
    
    // AuthService instance
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AuthService
        authService = AuthService.getInstance(this)
        
        // Setup Google Sign-In result launchers
        setupGoogleSignInLaunchers()
        
        enableEdgeToEdge()
        setContent {
            PasabayanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PasabayanNavigation(
                        activity = this@MainActivity,
                        googleOneTapLauncher = googleOneTapLauncher,
                        googleRegularSignInLauncher = googleRegularSignInLauncher,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    /**
     * Setup Google Sign-In activity result launchers
     * Integrates with AuthService for proper authentication handling
     * Supports both One Tap and regular Google Sign-In
     */
    private fun setupGoogleSignInLaunchers() {
        // One Tap Sign-In launcher
        googleOneTapLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            lifecycleScope.launch {
                when (result.resultCode) {
                    RESULT_OK -> {
                        try {
                            // Handle One Tap Sign-In result
                            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            val authResult = authService.handleGoogleSignInResult(task)
                            
                            authResult.onSuccess { response ->
                                println("✅ One Tap Sign-In successful - User: ${response.data.user.name}")
                            }.onFailure { exception ->
                                println("❌ One Tap Sign-In failed: ${exception.message}")
                            }
                            
                        } catch (e: ApiException) {
                            println("❌ One Tap Sign-In API error: ${e.statusCode} - ${e.message}")
                        } catch (e: Exception) {
                            println("❌ One Tap Sign-In error: ${e.message}")
                        }
                    }
                    RESULT_CANCELED -> {
                        println("🚫 One Tap Sign-In cancelled by user")
                    }
                    else -> {
                        println("❌ One Tap Sign-In failed with result code: ${result.resultCode}")
                    }
                }
            }
        }
        
        // Regular Google Sign-In launcher
        googleRegularSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            lifecycleScope.launch {
                when (result.resultCode) {
                    RESULT_OK -> {
                        try {
                            // Handle regular Google Sign-In result
                            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            val authResult = authService.handleGoogleSignInResult(task)
                            
                            authResult.onSuccess { response ->
                                println("✅ Regular Google Sign-In successful - User: ${response.data.user.name}")
                            }.onFailure { exception ->
                                println("❌ Regular Google Sign-In failed: ${exception.message}")
                            }
                            
                        } catch (e: ApiException) {
                            println("❌ Regular Google Sign-In API error: ${e.statusCode} - ${e.message}")
                        } catch (e: Exception) {
                            println("❌ Regular Google Sign-In error: ${e.message}")
                        }
                    }
                    RESULT_CANCELED -> {
                        println("🚫 Regular Google Sign-In cancelled by user")
                    }
                    else -> {
                        println("❌ Regular Google Sign-In failed with result code: ${result.resultCode}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    PasabayanTheme {
        // Preview without real activity and launchers
        PasabayanNavigation(
            activity = null,
            googleOneTapLauncher = null,
            googleRegularSignInLauncher = null
        )
    }
}