package com.efthemiosprime.pasabayan.domain.repository

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.User

/**
 * Authentication repository interface
 * Defines the contract for authentication operations
 * Supports Google Sign-In with One Tap and regular fallback
 */
interface AuthRepository {
    
    // Reactive state properties
    val isAuthenticated: StateFlow<Boolean>
    val currentUser: StateFlow<User?>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    /**
     * Sign in with Google using activity result launchers
     * Supports both One Tap and regular Google Sign-In
     */
    suspend fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ): Flow<Result<AuthResponse>>
    
    /**
     * Handle Google Sign-In result from activity
     */
    suspend fun handleGoogleSignInResult(
        task: Task<GoogleSignInAccount>
    ): Flow<Result<AuthResponse>>
    
    /**
     * Sign out current user
     */
    suspend fun signOut(): Flow<Result<Unit>>
    
    /**
     * Get current user
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Get current authentication token
     */
    suspend fun getToken(): String?
    
    /**
     * Mock login for development
     */
    suspend fun mockLogin(): Flow<Result<AuthResponse>>
} 