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
     * Initiates Google Sign-In flow with One Tap and regular fallback
     */
    fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(activity, oneTapLauncher, regularLauncher).collect { result ->
                result.onSuccess { authResponse ->
                    // Success is handled by the StateFlow in AuthService
                    println("🚀 AuthViewModel: Google Sign-In initiated successfully")
                }.onFailure { exception ->
                    // Error handling is managed by the repository's StateFlow
                    println("❌ AuthViewModel: Google Sign-In initiation failed: ${exception.message}")
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
                result.onSuccess { authResponse ->
                    println("✅ AuthViewModel: Authentication successful - User: ${authResponse.data.user.name}")
                }.onFailure { exception ->
                    println("❌ AuthViewModel: Authentication failed: ${exception.message}")
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
                result.onSuccess {
                    println("🔐 AuthViewModel: User signed out successfully")
                }.onFailure { exception ->
                    println("❌ AuthViewModel: Sign out failed: ${exception.message}")
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
                result.onSuccess { authResponse ->
                    println("🧪 AuthViewModel: Mock login successful - User: ${authResponse.data.user.name}")
                }.onFailure { exception ->
                    println("❌ AuthViewModel: Mock login failed: ${exception.message}")
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