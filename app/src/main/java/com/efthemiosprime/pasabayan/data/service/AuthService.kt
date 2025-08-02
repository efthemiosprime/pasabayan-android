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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.efthemiosprime.pasabayan.data.model.*
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

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
    
    // Facebook Sign-In clients
    private lateinit var facebookCallbackManager: CallbackManager
    private lateinit var facebookLoginManager: LoginManager
    
    // API Service
    private val apiService: APIService
    
    // JSON serializer
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    init {
        setupGoogleSignIn()
        setupFacebookSignIn()
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
     * Setup Facebook Sign-In configuration
     * Mirrors iOS setupFacebookSignIn method
     */
    private fun setupFacebookSignIn() {
        try {
            // Initialize Facebook callback manager and login manager
            facebookCallbackManager = CallbackManager.Factory.create()
            facebookLoginManager = LoginManager.getInstance()
            
            Log.d(TAG, "Facebook Sign-In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Facebook Sign-In: ${e.message}", e)
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
    fun createApiService(): APIService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                
                // Get auth token synchronously using runBlocking
                try {
                    val token = kotlinx.coroutines.runBlocking { getToken() }
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                        Log.d(TAG, "🔐 Added Authorization header to request")
                        Log.d(TAG, "   Token length: ${token.length}")
                        Log.d(TAG, "   Token preview: ${token.take(20)}...")
                    } else {
                        Log.w(TAG, "⚠️ No auth token available for request")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get token for request: ${e.message}")
                }
                
                chain.proceed(requestBuilder.build())
            }
            .build()
        
        return Retrofit.Builder()
                            .baseUrl("${APIService.BASE_URL}/")
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
     * Sign in with Facebook using LoginManager
     * Mirrors iOS signInWithFacebook method
     * Uses Facebook SDK LoginManager to get access token and exchange with backend
     */
    suspend fun signInWithFacebook(activity: Activity): Result<AuthResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            _isLoading.value = true
            _error.value = null
            
            Log.d(TAG, "🚀 Starting Facebook Sign-In process")
            Log.d(TAG, "   - Activity: ${activity.javaClass.simpleName}")
            
            // Register Facebook callback
            facebookLoginManager.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "✅ Facebook Sign-In successful, got access token")
                    
                    val accessToken = result.accessToken.token
                    Log.d(TAG, "🔍 Access Token: ${accessToken.take(20)}...")
                    
                    // Exchange Facebook token with backend
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val authResult = authenticateWithBackend("facebook", accessToken)
                            authResult.onSuccess { response ->
                                Log.d(TAG, "✅ Facebook authentication successful - User: ${response.data.user.name}")
                            }.onFailure { exception ->
                                Log.e(TAG, "❌ Facebook backend authentication failed: ${exception.message}")
                                _isLoading.value = false
                                _error.value = "Facebook authentication failed: ${exception.message}"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Facebook authentication error: ${e.message}", e)
                            _isLoading.value = false
                            _error.value = "Facebook authentication failed: ${e.message}"
                        }
                    }
                }
                
                override fun onCancel() {
                    Log.d(TAG, "🚫 Facebook Sign-In cancelled by user")
                    _isLoading.value = false
                    _error.value = "Facebook Sign-In cancelled"
                }
                
                override fun onError(error: FacebookException) {
                    Log.e(TAG, "❌ Facebook Sign-In error: ${error.message}", error)
                    _isLoading.value = false
                    _error.value = "Facebook Sign-In failed: ${error.message}"
                }
            })
            
            // Start Facebook login with required permissions
            facebookLoginManager.logInWithReadPermissions(activity, listOf("public_profile", "email"))
            
            Log.d(TAG, "📱 Facebook Sign-In intent launched, waiting for result...")
            Result.failure(Exception("Facebook Sign-in in progress"))
            
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message
            Log.e(TAG, "❌ Facebook Sign-In failed: ${e.message}", e)
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
                            Log.d(TAG, "   - API endpoint: ${APIService.BASE_URL}/auth/$provider/login")
            
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
            val userJson = json.encodeToString(user)
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
            
            userJson?.let { json.decodeFromString<User>(it) }
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
     * Refresh current user data from API
     * Mirrors iOS refreshUserData method
     */
    suspend fun refreshCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔄 Refreshing current user data from API")
            val response = apiService.getCurrentUser()
            
            if (response.success) {
                val user = response.data.user
                saveUser(user)
                _currentUser.value = user
                Log.d(TAG, "✅ User data refreshed successfully")
                Result.success(user)
            } else {
                Log.e(TAG, "❌ Failed to refresh user data: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while refreshing user data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle carrier status with API call
     * Mirrors iOS toggleCarrierStatus method
     * Uses authenticated API service with proper headers
     */
    suspend fun toggleCarrierStatus(): CarrierStatusResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "🚛 Making authenticated toggleCarrierStatus API call")
        return@withContext apiService.toggleCarrierStatus()
    }
    
    /**
     * Check carrier profile to see if user is already a carrier
     * Returns Result with success/failure for caching
     */
    suspend fun checkCarrierProfile(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Checking carrier profile status")
            val response = apiService.getCarrierProfile()
            Log.d(TAG, "✅ Carrier profile check successful - user IS a carrier")
            Result.success(true)
        } catch (e: Exception) {
            when {
                e.message?.contains("403") == true || 
                e.message?.contains("forbidden", ignoreCase = true) == true -> {
                    Log.d(TAG, "🚫 Carrier profile check returned 403 - user is NOT a carrier")
                    Result.success(false)
                }
                else -> {
                    Log.e(TAG, "❌ Carrier profile check failed with error: ${e.message}")
                    Result.failure(e)
                }
            }
        }
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
     * REMOVED: Mock login disabled to enforce real authentication
     * Use Google OAuth, Facebook login, or phone verification instead
     */
    @Deprecated("Mock login removed - use real authentication methods only", ReplaceWith(""))
    suspend fun mockLogin(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        Log.w(TAG, "❌ Mock login disabled - use real authentication")
        return@withContext Result.failure(
            UnsupportedOperationException("Mock login disabled - use real authentication methods (Google, Facebook, Phone)")
        )
    }
    
    /**
     * Get Facebook callback manager for activity result handling
     * Mirrors iOS callback handling pattern
     */
    fun getFacebookCallbackManager(): CallbackManager {
        return facebookCallbackManager
    }

    /**
     * Debug token status for testing
     * Returns a map with token information
     */
    suspend fun debugTokenStatus(): Map<String, Any> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Any>()
        
        try {
            val token = getToken()
            results["hasToken"] = token != null
            if (token != null) {
                results["tokenLength"] = token.length
                results["tokenPrefix"] = token.take(20)
                results["tokenSuffix"] = token.takeLast(20)
            }
            
            val user = getCurrentUser()
            results["hasUser"] = user != null
            if (user != null) {
                results["userId"] = user.id
                results["userEmail"] = user.email
                results["userProvider"] = user.provider ?: "unknown"
            }
            
            results["isAuthenticated"] = _isAuthenticated.value
            results["isLoading"] = _isLoading.value
            results["hasError"] = _error.value != null
            
        } catch (e: Exception) {
            results["debugError"] = e.message ?: "Unknown error"
        }
        
        return@withContext results
    }
    
    /**
     * Test API connectivity
     * Returns a Result indicating if the API is accessible
     */
    suspend fun testApiConnectivity(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Test a simple endpoint to check connectivity
            val response = apiService.getCurrentUser()
            Result.success("API connectivity successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Debug authentication state
     * Comprehensive method to check what's happening with authentication
     */
    suspend fun debugAuthenticationState(): String = withContext(Dispatchers.IO) {
        val debugInfo = StringBuilder()
        
        try {
            debugInfo.appendLine("🔍 === AUTHENTICATION DEBUG INFO ===")
            debugInfo.appendLine("📱 Platform: Android")
            debugInfo.appendLine("🌐 Base URL: ${APIService.BASE_URL}")
            debugInfo.appendLine("")
            
            // Check token
            val token = getToken()
            debugInfo.appendLine("🎫 Token Status:")
            if (token != null) {
                debugInfo.appendLine("   ✅ Token exists")
                debugInfo.appendLine("   📏 Length: ${token.length}")
                debugInfo.appendLine("   🔤 First 20 chars: ${token.take(20)}...")
                debugInfo.appendLine("   🔤 Last 20 chars: ...${token.takeLast(20)}")
            } else {
                debugInfo.appendLine("   ❌ No token found")
            }
            debugInfo.appendLine("")
            
            // Check user
            val user = getCurrentUser()
            debugInfo.appendLine("👤 User Status:")
            if (user != null) {
                debugInfo.appendLine("   ✅ User data exists")
                debugInfo.appendLine("   👤 Name: ${user.name}")
                debugInfo.appendLine("   📧 Email: ${user.email}")
                debugInfo.appendLine("   🆔 ID: ${user.id}")
                debugInfo.appendLine("   🔐 Provider: ${user.provider}")
            } else {
                debugInfo.appendLine("   ❌ No user data found")
            }
            debugInfo.appendLine("")
            
            // Check authentication state
            debugInfo.appendLine("🔐 Authentication State:")
            debugInfo.appendLine("   📊 isAuthenticated: ${_isAuthenticated.value}")
            debugInfo.appendLine("   ⏳ isLoading: ${_isLoading.value}")
            debugInfo.appendLine("   ❌ error: ${_error.value}")
            debugInfo.appendLine("")
            
            // Test API connectivity
            debugInfo.appendLine("🌐 API Test:")
            try {
                // Test a simple endpoint to check connectivity
                debugInfo.appendLine("   📡 Testing connectivity to: ${APIService.BASE_URL}")
                debugInfo.appendLine("   ⏱️ Testing basic endpoint access...")
                
                if (token != null) {
                    debugInfo.appendLine("   🔐 Will test with authentication")
                } else {
                    debugInfo.appendLine("   🔓 No token available for authenticated test")
                }
            } catch (e: Exception) {
                debugInfo.appendLine("   ❌ API test failed: ${e.message}")
            }
            
            debugInfo.appendLine("=== END DEBUG INFO ===")
            
        } catch (e: Exception) {
            debugInfo.appendLine("❌ Debug failed: ${e.message}")
        }
        
        val result = debugInfo.toString()
        Log.d(TAG, result)
        return@withContext result
    }
} 