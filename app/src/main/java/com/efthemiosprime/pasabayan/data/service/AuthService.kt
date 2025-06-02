package com.efthemiosprime.pasabayan.data.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.efthemiosprime.pasabayan.data.model.*

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

/**
 * Authentication Service for Android
 * Mirrors iOS AuthService functionality with Google Sign-In integration
 */
class AuthService(private val context: Context) {
    
    companion object {
        private const val TAG = "AuthService"
        private const val TOKEN_KEY = "pasabayan_auth_token"
        private const val USER_KEY = "pasabayan_user"
        
        // DataStore keys
        private val TOKEN_PREFERENCE_KEY = stringPreferencesKey(TOKEN_KEY)
        private val USER_PREFERENCE_KEY = stringPreferencesKey(USER_KEY)
        
        @Volatile
        private var INSTANCE: AuthService? = null
        
        fun getInstance(context: Context): AuthService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // State management
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Google Sign-In clients
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    
    // API Service
    private val apiService: APIService
    
    // JSON serializer
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    init {
        setupGoogleSignIn()
        apiService = createApiService()
        
        // Check authentication status on initialization
        CoroutineScope(Dispatchers.IO).launch {
            checkAuthenticationStatus()
        }
    }
    
    /**
     * Setup Google Sign-In configuration
     * Mirrors iOS setupGoogleSignIn method
     */
    private fun setupGoogleSignIn() {
        try {
            // Configure Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId())
                .requestEmail()
                .requestProfile()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            // Setup One Tap Sign-In
            oneTapClient = Identity.getSignInClient(context)
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getGoogleClientId())
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
            
            Log.d(TAG, "Google Sign-In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Google Sign-In: ${e.message}", e)
        }
    }
    
    /**
     * Get Google Client ID from resources or manifest
     */
    private fun getGoogleClientId(): String {
        // Use the web client ID from google-services.json for OAuth
        // This matches the iOS configuration
        return "249733420573-2rfuqsub2eipc6m1ci8ht5mfpq5hsg7n.apps.googleusercontent.com"
    }
    
    /**
     * Create Retrofit API service
     */
    private fun createApiService(): APIService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                
                // Add auth token if available
                CoroutineScope(Dispatchers.IO).launch {
                    getToken()?.let { token ->
                        request.addHeader("Authorization", "Bearer $token")
                    }
                }
                
                chain.proceed(request.build())
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl(APIService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(APIService::class.java)
    }
    
    /**
     * Sign in with Google using Intent launchers
     * Mirrors iOS signInWithGoogle method
     * Supports One Tap Sign-In with regular Google Sign-In fallback
     */
    suspend fun signInWithGoogle(
        activity: Activity,
        oneTapLauncher: ActivityResultLauncher<IntentSenderRequest>,
        regularLauncher: ActivityResultLauncher<Intent>
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            _isLoading.value = true
            _error.value = null
            
            Log.d(TAG, "🚀 Starting Google Sign-In process")
            Log.d(TAG, "   - Activity: ${activity.javaClass.simpleName}")
            Log.d(TAG, "   - Client ID: ${getGoogleClientId()}")
            
            // Try One Tap Sign-In first
            val beginSignInTask = oneTapClient.beginSignIn(signInRequest)
            
            try {
                val result = beginSignInTask.result
                Log.d(TAG, "✅ One Tap Sign-In available, launching intent")
                
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                oneTapLauncher.launch(intentSenderRequest)
                
                Log.d(TAG, "🔄 One Tap Sign-In intent launched, waiting for result...")
                Result.failure(Exception("One Tap Sign-in in progress"))
                
            } catch (e: Exception) {
                Log.d(TAG, "⚠️ One Tap Sign-In not available: ${e.message}")
                Log.d(TAG, "🔄 Falling back to regular Google Sign-In")
                
                // Fall back to regular Google Sign-In
                val signInIntent = googleSignInClient.signInIntent
                regularLauncher.launch(signInIntent)
                
                Log.d(TAG, "📱 Regular Google Sign-In intent launched, waiting for result...")
                Result.failure(Exception("Regular Google Sign-in in progress"))
            }
            
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message
            Log.e(TAG, "❌ Google Sign-In failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Handle Google Sign-In result
     * Called after sign-in activity returns
     */
    suspend fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>): Result<AuthResponse> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                
                if (idToken == null) {
                    _isLoading.value = false
                    _error.value = "Failed to get Google ID token"
                    return@withContext Result.failure(Exception("No ID token received"))
                }
                
                Log.d(TAG, "🚀 Got Google ID token, authenticating with backend")
                
                // Authenticate with backend
                authenticateWithBackend("google", idToken)
                
            } catch (e: ApiException) {
                _isLoading.value = false
                _error.value = "Google Sign-In failed: ${e.message}"
                Log.e(TAG, "Google Sign-In API error: ${e.statusCode} - ${e.message}", e)
                Result.failure(e)
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
                Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    
    /**
     * Authenticate with backend API
     * Mirrors iOS authenticateWithBackend method
     */
    private suspend fun authenticateWithBackend(
        provider: String, 
        accessToken: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Authenticating with backend...")
            Log.d(TAG, "   - Provider: $provider")
            Log.d(TAG, "   - Token length: ${accessToken.length}")
            Log.d(TAG, "   - API endpoint: ${APIService.BASE_URL}auth/$provider/login")
            
            val deviceInfo = DeviceInfo(
                platform = "android",
                osVersion = Build.VERSION.RELEASE,
                appVersion = getAppVersion()
            )
            
            val loginRequest = LoginRequest(
                provider = provider,
                accessToken = accessToken,
                deviceInfo = deviceInfo
            )
            
            val response = apiService.loginWithToken(provider, loginRequest)
            
            // Save authentication data
            saveToken(response.data.token)
            saveUser(response.data.user)
            
            _isAuthenticated.value = true
            _currentUser.value = response.data.user
            _isLoading.value = false
            _error.value = null
            
            Log.d(TAG, "🔍 Backend authentication successful - User: ${response.data.user.name}")
            
            Result.success(response)
            
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Authentication failed: ${e.message}"
            Log.e(TAG, "Backend authentication failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save authentication token
     * Mirrors iOS saveToken method
     */
    private suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_PREFERENCE_KEY] = token
        }
        Log.d(TAG, "Auth token saved")
    }
    
    /**
     * Get authentication token
     * Mirrors iOS getToken method
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_PREFERENCE_KEY]
        }.first()
    }
    
    /**
     * Save user data
     * Mirrors iOS saveUser method
     */
    private suspend fun saveUser(user: User) {
        try {
            val userJson = json.encodeToString(User.serializer(), user)
            context.dataStore.edit { preferences ->
                preferences[USER_PREFERENCE_KEY] = userJson
            }
            Log.d(TAG, "User data saved: ${user.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user data: ${e.message}", e)
        }
    }
    
    /**
     * Get current user
     * Mirrors iOS getCurrentUser method
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val userJson = context.dataStore.data.map { preferences ->
                preferences[USER_PREFERENCE_KEY]
            }.first()
            
            userJson?.let { json.decodeFromString(User.serializer(), it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user data: ${e.message}", e)
            null
        }
    }
    
    /**
     * Check authentication status
     * Mirrors iOS checkAuthenticationStatus method
     */
    private suspend fun checkAuthenticationStatus() {
        val token = getToken()
        val user = getCurrentUser()
        
        _isAuthenticated.value = token != null && user != null
        _currentUser.value = user
        
        Log.d(TAG, "Authentication status checked - Authenticated: ${_isAuthenticated.value}")
    }
    
    /**
     * Sign out user
     * Mirrors iOS logout method
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔐 Signing out user")
            
            // Sign out from Google
            googleSignInClient.signOut()
            
            // Call backend logout
            try {
                apiService.logout()
            } catch (e: Exception) {
                Log.w(TAG, "Backend logout failed, but continuing with local logout: ${e.message}")
            }
            
            // Clear local data
            removeAuthData()
            
            _isAuthenticated.value = false
            _currentUser.value = null
            _error.value = null
            
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove authentication data
     * Mirrors iOS removeToken method
     */
    private suspend fun removeAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_PREFERENCE_KEY)
            preferences.remove(USER_PREFERENCE_KEY)
        }
        Log.d(TAG, "Authentication data cleared")
    }
    
    /**
     * Get app version
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    
    /**
     * Mock login for development/testing
     * Mirrors iOS mockLogin method
     */
    suspend fun mockLogin(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🧪 Performing mock login")
            
            val mockUser = User(
                id = 1,
                name = "Test User Android",
                email = "android.test@example.com",
                avatar = null,
                phone = "+63 123 456 7890",
                phoneVerified = true,
                profileCompleted = true,
                provider = "mock",
                providerId = "mock_android_123",
                emailVerifiedAt = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                userTypes = listOf("shipper"),
                isActiveCarrier = false,
                isActiveShipper = true,
                rating = 4.5,
                totalRatings = 10,
                verificationLevel = "basic"
            )
            
            val mockProfile = UserProfile(
                id = 1,
                userId = 1,
                profilePicture = null,
                address = "123 Test Street, Metro Manila",
                dateOfBirth = null,
                gender = null,
                emergencyContactName = "Emergency Contact",
                emergencyContactPhone = "+63 987 654 3210",
                preferredLanguage = "en",
                notificationSettings = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
            
            val authResponse = AuthResponse(
                success = true,
                message = "Mock login successful",
                data = AuthData(
                    token = "mock_android_token_123",
                    user = mockUser,
                    tokenType = "Bearer"
                )
            )
            
            // Save mock data
            saveToken(authResponse.data.token)
            saveUser(authResponse.data.user)
            
            _isAuthenticated.value = true
            _currentUser.value = authResponse.data.user
            _isLoading.value = false
            _error.value = null
            
            Log.d(TAG, "Mock login successful")
            Result.success(authResponse)
            
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message
            Log.e(TAG, "Mock login failed: ${e.message}", e)
            Result.failure(e)
        }
    }
} 