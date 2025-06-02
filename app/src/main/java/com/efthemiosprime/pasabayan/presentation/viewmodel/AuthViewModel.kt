package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel manages authentication state and operations
 * Mirrors iOS AuthViewModel structure
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        checkAuthenticationStatus()
    }
    
    /**
     * Check if user is already authenticated
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = authRepository.getCurrentUser()
                _authState.value = AuthState(
                    isAuthenticated = currentUser != null,
                    user = currentUser
                )
            } catch (e: Exception) {
                _authState.value = AuthState(
                    isAuthenticated = false,
                    user = null,
                    error = e.message
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sign in with Google
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authRepository.signInWithGoogle()
                _authState.value = AuthState(
                    isAuthenticated = true,
                    user = user
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    error = e.message ?: "Authentication failed"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _authState.value = AuthState(
                    isAuthenticated = false,
                    user = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    error = e.message ?: "Sign out failed"
                )
            }
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

/**
 * Authentication state data class
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
) 