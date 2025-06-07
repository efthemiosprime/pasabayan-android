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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.data.common.resultOf

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
        emit(resultOf {
            val serviceResult = authService.signInWithGoogle(activity, oneTapLauncher, regularLauncher)
            // Convert Kotlin Result to our functional Result type
            serviceResult.getOrThrow()
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.AuthenticationError(exception.message ?: "Authentication failed")))
    }
    
    override suspend fun handleGoogleSignInResult(
        task: Task<GoogleSignInAccount>
    ): Flow<Result<AuthResponse>> = flow {
        emit(resultOf {
            authService.handleGoogleSignInResult(task).getOrThrow()
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.AuthenticationError(exception.message ?: "Google sign-in failed")))
    }
    
    override suspend fun signOut(): Flow<Result<Unit>> = flow {
        emit(resultOf {
            authService.signOut().getOrThrow()
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.UnknownError(exception.message ?: "Sign out failed", exception)))
    }
    
    override suspend fun getCurrentUser(): User? = resultOf {
        authService.getCurrentUser()
    }.getOrNull()
    
    override suspend fun getToken(): String? = resultOf {
        authService.getToken()
    }.getOrNull()
    
    override suspend fun mockLogin(): Flow<Result<AuthResponse>> = flow {
        emit(resultOf {
            authService.mockLogin().getOrThrow()
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.AuthenticationError(exception.message ?: "Mock login failed")))
    }
    
    override suspend fun signInWithFacebook(
        activity: Activity
    ): Flow<Result<AuthResponse>> = flow {
        emit(resultOf {
            authService.signInWithFacebook(activity).getOrThrow()
        })
    }.catch { exception ->
        emit(Result.Failure(AppError.AuthenticationError(exception.message ?: "Facebook sign-in failed")))
    }
} 