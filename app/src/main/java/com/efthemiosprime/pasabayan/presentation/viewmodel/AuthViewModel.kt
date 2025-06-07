package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import com.efthemiosprime.pasabayan.data.common.Result
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel manages authentication state and operations
 * Mirrors iOS AuthViewModel structure with StateFlow integration
 * Handles real Google Sign-In authentication with One Tap and regular fallback
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Use repository's StateFlow directly
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isLoading: StateFlow<Boolean> = authRepository.isLoading
    val error: StateFlow<String?> = authRepository.error
    
    /**
     * Sign in with Google using activity result launchers
     * Supports both One Tap and regular Google Sign-In fallback
     */
    fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(activity, oneTapLauncher, regularLauncher)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Authentication successful - state is managed by AuthService
                        }
                        is Result.Failure -> {
                            // Error handling is managed by AuthService
                        }
                    }
                }
        }
    }
    
    /**
     * Sign in with Facebook using LoginManager
     * Mirrors iOS AuthViewModel signInWithFacebook method
     */
    fun signInWithFacebook(activity: Activity) {
        viewModelScope.launch {
            authRepository.signInWithFacebook(activity)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Authentication successful - state is managed by AuthService
                        }
                        is Result.Failure -> {
                            // Error handling is managed by AuthService
                        }
                    }
                }
        }
    }
    
    /**
     * Handle Google Sign-In result from activity
     * This is called from MainActivity when the Google Sign-In activity returns
     */
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            authRepository.handleGoogleSignInResult(task).collect { result ->
                when (result) {
                    is Result.Success -> {
                        println("✅ AuthViewModel: Authentication successful - User: ${result.data.data.user.name}")
                    }
                    is Result.Failure -> {
                        println("❌ AuthViewModel: Authentication failed: ${result.error.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().collect { result ->
                when (result) {
                    is Result.Success -> {
                        println("🔐 AuthViewModel: User signed out successfully")
                    }
                    is Result.Failure -> {
                        println("❌ AuthViewModel: Sign out failed: ${result.error.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Mock login for development/testing
     */
    fun mockLogin() {
        viewModelScope.launch {
            authRepository.mockLogin().collect { result ->
                when (result) {
                    is Result.Success -> {
                        println("🧪 AuthViewModel: Mock login successful - User: ${result.data.data.user.name}")
                    }
                    is Result.Failure -> {
                        println("❌ AuthViewModel: Mock login failed: ${result.error.message}")
                    }
                }
            }
        }
    }

    /**
     * Get current user (suspend function)
     */
    suspend fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }
    
    /**
     * Get current authentication token
     */
    suspend fun getToken(): String? {
        return authRepository.getToken()
    }
} 