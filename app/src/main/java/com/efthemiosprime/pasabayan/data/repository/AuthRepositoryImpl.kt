package com.efthemiosprime.pasabayan.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.service.AuthService

/**
 * Authentication repository implementation
 * Acts as a bridge between domain layer and data layer
 * Integrates with the comprehensive AuthService for Google Sign-In with One Tap and regular fallback
 */
class AuthRepositoryImpl(context: Context) : AuthRepository {
    
    private val authService = AuthService.getInstance(context)
    
    override val isAuthenticated: StateFlow<Boolean> = authService.isAuthenticated
    override val currentUser: StateFlow<User?> = authService.currentUser
    override val isLoading: StateFlow<Boolean> = authService.isLoading
    override val error: StateFlow<String?> = authService.error
    
    override suspend fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ): Flow<Result<AuthResponse>> = flow {
        val result = authService.signInWithGoogle(activity, oneTapLauncher, regularLauncher)
        emit(result)
    }
    
    override suspend fun handleGoogleSignInResult(
        task: Task<GoogleSignInAccount>
    ): Flow<Result<AuthResponse>> = flow {
        emit(authService.handleGoogleSignInResult(task))
    }
    
    override suspend fun signOut(): Flow<Result<Unit>> = flow {
        emit(authService.signOut())
    }
    
    override suspend fun getCurrentUser(): User? {
        return authService.getCurrentUser()
    }
    
    override suspend fun getToken(): String? {
        return authService.getToken()
    }
    
    override suspend fun mockLogin(): Flow<Result<AuthResponse>> = flow {
        emit(authService.mockLogin())
    }
    
    override suspend fun signInWithFacebook(
        activity: Activity
    ): Flow<Result<AuthResponse>> = flow {
        val result = authService.signInWithFacebook(activity)
        emit(result)
    }
} 