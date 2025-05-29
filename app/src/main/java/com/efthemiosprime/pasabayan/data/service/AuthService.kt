package com.efthemiosprime.pasabayan.data.service

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.efthemiosprime.pasabayan.data.model.AuthResponse
import com.efthemiosprime.pasabayan.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthService(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_KEY = stringPreferencesKey("user_data")
        
        @Volatile
        private var INSTANCE: AuthService? = null
        
        fun getInstance(context: Context): AuthService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val apiService = ApiService.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("249733420573-2rfuqsub2eipc6m1ci8ht5mfpq5hsg7n.apps.googleusercontent.com") // Web client ID from google-services.json
            .requestServerAuthCode("249733420573-2rfuqsub2eipc6m1ci8ht5mfpq5hsg7n.apps.googleusercontent.com")
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Token management
    suspend fun saveAuthData(authResponse: AuthResponse) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = authResponse.token
            preferences[USER_KEY] = Json.encodeToString(authResponse.user)
        }
        apiService.setAuthToken(authResponse.token)
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.first()
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val userJson = context.dataStore.data.map { preferences ->
                preferences[USER_KEY]
            }.first()
            
            userJson?.let { Json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            Log.e("AuthService", "Error getting current user", e)
            null
        }
    }

    fun isLoggedInFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY] != null
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    // Google Sign-In
    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    suspend fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>): Result<AuthResponse> {
        return try {
            val account = task.getResult(ApiException::class.java)
            val serverAuthCode = account.serverAuthCode
            
            if (serverAuthCode != null) {
                Log.d("AuthService", "🔍 Got Google server auth code: ${serverAuthCode.take(20)}...")
                
                // Use the server auth code to authenticate with backend
                val result = apiService.loginWithGoogleAuthCode(serverAuthCode)
                
                if (result.isSuccess) {
                    val authResponse = result.getOrThrow()
                    saveAuthData(authResponse)
                    Log.d("AuthService", "🔍 Google authentication successful")
                    result
                } else {
                    Log.e("AuthService", "🔍 Backend authentication failed: ${result.exceptionOrNull()}")
                    result
                }
            } else {
                Log.e("AuthService", "🔍 Google Sign-In failed: No server auth code received")
                Result.failure(Exception("No server auth code received from Google"))
            }
        } catch (e: ApiException) {
            Log.e("AuthService", "🔍 Google Sign-In failed with code: ${e.statusCode}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthService", "🔍 Google Sign-In error", e)
            Result.failure(e)
        }
    }

    // Demo Login for testing
    suspend fun demoLogin(): Result<AuthResponse> {
        return try {
            // Create a mock user for demo purposes
            val mockUser = User(
                id = 999,
                name = "Demo User",
                email = "demo@pasabayan.com",
                avatar = null,
                phone = "+63 912 345 6789",
                provider = "demo",
                providerId = "demo_999",
                emailVerifiedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
            
            val mockAuthResponse = AuthResponse(
                token = "demo_token_${System.currentTimeMillis()}",
                user = mockUser,
                profile = null
            )
            
            saveAuthData(mockAuthResponse)
            Result.success(mockAuthResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign Out
    suspend fun signOut(): Result<Unit> {
        return try {
            // Try to logout from backend
            apiService.logout()
            
            // Clear local data
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            
            // Sign out from Google
            googleSignInClient.signOut()
            
            apiService.clearAuthToken()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthService", "Error during sign out", e)
            Result.failure(e)
        }
    }

    // Initialize auth token on app start
    suspend fun initializeAuthToken() {
        getToken()?.let { token ->
            apiService.setAuthToken(token)
        }
    }
} 