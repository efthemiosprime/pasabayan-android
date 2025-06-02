package com.efthemiosprime.pasabayan.domain.repository

import com.efthemiosprime.pasabayan.data.model.User

/**
 * Authentication repository interface
 * Mirrors iOS AuthService structure
 */
interface AuthRepository {
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(): User
    
    /**
     * Sign out current user
     */
    suspend fun signOut()
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
} 