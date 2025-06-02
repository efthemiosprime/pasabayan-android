package com.efthemiosprime.pasabayan.data.repository

import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Mock implementation of AuthRepository
 * Provides sample data for development and testing
 * Mirrors iOS mock authentication structure
 */
class AuthRepositoryImpl : AuthRepository {
    
    private var currentUser: User? = null
    
    // Mock user data similar to iOS
    private val mockUser = User(
        id = 1,
        name = "John Doe",
        email = "john.doe@example.com",
        avatar = "https://i.pravatar.cc/150?img=1",
        phone = "+1234567890",
        phoneVerified = true,
        profileCompleted = true,
        provider = "google",
        providerId = "google_123456",
        emailVerifiedAt = "2024-01-01T00:00:00Z",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        userTypes = listOf("shipper", "carrier"),
        isActiveCarrier = true,
        isActiveShipper = true,
        rating = 4.8,
        totalRatings = 142,
        verificationLevel = "verified"
    )
    
    override suspend fun getCurrentUser(): User? {
        // Simulate network delay
        delay(500)
        return currentUser
    }
    
    override suspend fun signInWithGoogle(): User {
        // Simulate authentication process
        delay(1000)
        currentUser = mockUser
        return mockUser
    }
    
    override suspend fun signOut() {
        // Simulate sign out process
        delay(300)
        currentUser = null
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return currentUser != null
    }
} 