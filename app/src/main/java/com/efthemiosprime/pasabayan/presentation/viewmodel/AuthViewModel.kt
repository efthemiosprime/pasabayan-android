package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.AuthData
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError
import com.efthemiosprime.pasabayan.presentation.common.FunctionalViewModel
import com.efthemiosprime.pasabayan.presentation.common.UiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Functional state for authentication
 */
data class AuthState(
    val user: User? = null,
    val isAuthenticated: Boolean = false,
    val authState: UiState<AuthResponse> = UiState.idle(),
    val signInMethod: SignInMethod? = null
) {
    val isLoading: Boolean get() = authState.isLoading
    val error: AppError? get() = authState.error
    val isSignedIn: Boolean get() = isAuthenticated && user != null
}

/**
 * Authentication actions following functional patterns
 */
sealed class AuthAction {
    object StartGoogleSignIn : AuthAction()
    object StartFacebookSignIn : AuthAction()
    object StartMockLogin : AuthAction()
    data class GoogleSignInResult(val task: Task<GoogleSignInAccount>) : AuthAction()
    data class AuthSuccess(val response: AuthResponse) : AuthAction()
    data class AuthFailure(val error: AppError) : AuthAction()
    object SignOut : AuthAction()
    object ClearError : AuthAction()
    object LoadUserData : AuthAction()
}

/**
 * Authentication side effects
 */
sealed class AuthEffect {
    data class ShowToast(val message: String) : AuthEffect()
    data class LaunchGoogleSignIn(
        val activity: Activity,
        val oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        val regularLauncher: ActivityResultLauncher<Intent>
    ) : AuthEffect()
    data class LaunchFacebookSignIn(val activity: Activity) : AuthEffect()
    object NavigateToMain : AuthEffect()
    object NavigateToAuth : AuthEffect()
}

/**
 * Sign-in method tracking
 */
enum class SignInMethod {
    GOOGLE, FACEBOOK, MOCK
}

/**
 * AuthViewModel manages authentication state and operations using functional patterns
 * Mirrors iOS AuthViewModel structure with functional state management
 * Handles real Google Sign-In authentication with One Tap and regular fallback
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : FunctionalViewModel<AuthState, AuthAction, AuthEffect>(
    initialState = AuthState()
) {
    
    // Expose repository state for backward compatibility
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isLoading: StateFlow<Boolean> = authRepository.isLoading
    val error: StateFlow<String?> = authRepository.error
    
    // MARK: - Pure Reducer Function
    
    override fun reduce(currentState: AuthState, action: AuthAction): AuthState = when (action) {
        is AuthAction.StartGoogleSignIn -> currentState.copy(
            authState = UiState.loading(currentState.authState.data),
            signInMethod = SignInMethod.GOOGLE
        )
        
        is AuthAction.StartFacebookSignIn -> currentState.copy(
            authState = UiState.loading(currentState.authState.data),
            signInMethod = SignInMethod.FACEBOOK
        )
        
        is AuthAction.StartMockLogin -> currentState.copy(
            authState = UiState.loading(currentState.authState.data),
            signInMethod = SignInMethod.MOCK
        )
        
        is AuthAction.AuthSuccess -> currentState.copy(
            user = action.response.data.user,
            isAuthenticated = true,
            authState = UiState.success(action.response)
        )
        
        is AuthAction.AuthFailure -> currentState.copy(
            authState = UiState.error(action.error, currentState.authState.data)
        )
        
        is AuthAction.SignOut -> AuthState() // Reset to initial state
        
        is AuthAction.ClearError -> currentState.copy(
            authState = currentState.authState.copy(error = null)
        )
        
        is AuthAction.LoadUserData -> currentState.copy(
            authState = UiState.loading(currentState.authState.data)
        )
        
        is AuthAction.GoogleSignInResult -> currentState // Handled in side effects
    }
    
    // MARK: - Side Effects Handler
    
    override suspend fun handleSideEffect(action: AuthAction, currentState: AuthState): AuthEffect? = when (action) {
        is AuthAction.StartGoogleSignIn -> null // Handled by public function
        is AuthAction.StartFacebookSignIn -> null // Handled by public function
        is AuthAction.StartMockLogin -> {
            performMockLogin()
            null
        }
        is AuthAction.GoogleSignInResult -> {
            handleGoogleSignInResultInternal(action.task)
            null
        }
        is AuthAction.AuthSuccess -> {
            AuthEffect.ShowToast("Welcome ${currentState.user?.name ?: "User"}!")
        }
        is AuthAction.AuthFailure -> {
            AuthEffect.ShowToast("Authentication failed: ${action.error.message}")
        }
        is AuthAction.SignOut -> {
            performSignOut()
            AuthEffect.NavigateToAuth
        }
        else -> null
    }
    
    // MARK: - Public API Methods (Functional Dispatch)
    
    /**
     * Sign in with Google using activity result launchers
     * Supports both One Tap and regular Google Sign-In fallback
     */
    fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ) {
        dispatch(AuthAction.StartGoogleSignIn)
        
        viewModelScope.launch {
            authRepository.signInWithGoogle(activity, oneTapLauncher, regularLauncher)
                .collect { result ->
                    when (result) {
                        is Result.Success -> dispatch(AuthAction.AuthSuccess(result.data))
                        is Result.Failure -> dispatch(AuthAction.AuthFailure(result.error))
                    }
                }
        }
    }
    
        /**
     * Sign in with Facebook using LoginManager
     * Mirrors iOS AuthViewModel signInWithFacebook method
     */
    fun signInWithFacebook(activity: Activity) {
        dispatch(AuthAction.StartFacebookSignIn)
        
        viewModelScope.launch {
            authRepository.signInWithFacebook(activity)
                .collect { result ->
                    when (result) {
                        is Result.Success -> dispatch(AuthAction.AuthSuccess(result.data))
                        is Result.Failure -> dispatch(AuthAction.AuthFailure(result.error))
                    }
                }
        }
    }

    /**
     * Handle Google Sign-In result from activity
     * This is called from MainActivity when the Google Sign-In activity returns
     */
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        dispatch(AuthAction.GoogleSignInResult(task))
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        dispatch(AuthAction.SignOut)
    }

    /**
     * Mock login for development/testing
     */
    fun mockLogin() {
        dispatch(AuthAction.StartMockLogin)
    }
    
    /**
     * Clear authentication error
     */
    fun clearError() {
        dispatch(AuthAction.ClearError)
    }

    // MARK: - Private Helper Methods (Side Effects Implementation)
    
    private suspend fun performMockLogin() {
        authRepository.mockLogin().collect { result ->
            when (result) {
                is Result.Success -> dispatch(AuthAction.AuthSuccess(result.data))
                is Result.Failure -> dispatch(AuthAction.AuthFailure(result.error))
            }
        }
    }
    
    private suspend fun handleGoogleSignInResultInternal(task: Task<GoogleSignInAccount>) {
        authRepository.handleGoogleSignInResult(task).collect { result ->
            when (result) {
                is Result.Success -> dispatch(AuthAction.AuthSuccess(result.data))
                is Result.Failure -> dispatch(AuthAction.AuthFailure(result.error))
            }
        }
    }
    
    private suspend fun performSignOut() {
        authRepository.signOut().collect { result ->
            when (result) {
                is Result.Success -> {
                    // State already reset by reducer
                }
                is Result.Failure -> {
                    dispatch(AuthAction.AuthFailure(result.error))
                }
            }
        }
    }
    
    // MARK: - Legacy API (for backward compatibility)
    
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
    
    /**
     * Initialize authentication state from repository
     */
    init {
        viewModelScope.launch {
            // Sync initial state from repository
            authRepository.currentUser.collect { user ->
                if (user != null && !state.value.isAuthenticated) {
                    updateState { currentState ->
                        currentState.copy(
                            user = user,
                            isAuthenticated = true,
                            authState = UiState.success(
                                AuthResponse(
                                    success = true,
                                    message = "Already authenticated",
                                    data = AuthData(
                                        user = user,
                                        token = ""
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }
} 