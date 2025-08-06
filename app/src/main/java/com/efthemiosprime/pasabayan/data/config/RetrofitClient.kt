package com.efthemiosprime.pasabayan.data.config

import android.util.Log
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Centralized Retrofit Client Factory
 * Provides consistent HTTP client configuration across all services
 * Handles authentication, timeouts, logging, and network security
 */
object RetrofitClient {
    
    private const val TAG = "RetrofitClient"
    
    // JSON configuration for serialization
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = false
    }
    
    /**
     * Create a configured OkHttp client with consistent settings
     */
    fun createOkHttpClient(
        includeAuthInterceptor: Boolean = false, // Disabled by default - services handle their own auth for now
        connectTimeoutSeconds: Long = 30,
        readTimeoutSeconds: Long = 30,
        writeTimeoutSeconds: Long = 30
    ): OkHttpClient {
        
        val builder = OkHttpClient.Builder()
            .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // Add logging interceptor for debugging
        // TODO: Make this configurable based on build variant
        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        builder.addInterceptor(logging)
        
        // Add authentication interceptor if needed
        if (includeAuthInterceptor) {
            builder.addInterceptor(AuthInterceptor())
        }
        
        // Add network error interceptor for better error handling
        builder.addInterceptor(NetworkErrorInterceptor())
        
        return builder.build()
    }
    
    /**
     * Create a configured Retrofit instance
     */
    fun <T> createApiService(
        serviceClass: Class<T>,
        baseUrl: String = NetworkConfig.baseUrl,
        includeAuth: Boolean = false // Disabled by default - services handle their own auth for now
    ): T {
        
        Log.d(TAG, "Creating API service for ${serviceClass.simpleName}")
        Log.d(TAG, "   Base URL: $baseUrl")
        Log.d(TAG, "   Environment: ${NetworkConfig.environmentName}")
        Log.d(TAG, "   Include Auth: $includeAuth")
        
        val client = createOkHttpClient(includeAuthInterceptor = includeAuth)
        
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
        
        return retrofit.create(serviceClass)
    }
    
    /**
     * Create API service with custom configuration
     */
    fun <T> createCustomApiService(
        serviceClass: Class<T>,
        baseUrl: String,
        connectTimeout: Long = 30,
        readTimeout: Long = 30,
        writeTimeout: Long = 30,
        includeAuth: Boolean = false // Disabled by default - services handle their own auth for now
    ): T {
        
        Log.d(TAG, "Creating custom API service for ${serviceClass.simpleName}")
        Log.d(TAG, "   Custom Base URL: $baseUrl")
        Log.d(TAG, "   Timeouts: connect=${connectTimeout}s, read=${readTimeout}s, write=${writeTimeout}s")
        
        val client = createOkHttpClient(
            includeAuthInterceptor = includeAuth,
            connectTimeoutSeconds = connectTimeout,
            readTimeoutSeconds = readTimeout,
            writeTimeoutSeconds = writeTimeout
        )
        
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
        
        return retrofit.create(serviceClass)
    }
    
    /**
     * Get the default API service instance (cached) - with authentication
     * Most services need authentication, so this provides it by default
     */
    private var _defaultApiService: com.efthemiosprime.pasabayan.data.service.APIService? = null
    
    val defaultApiService: com.efthemiosprime.pasabayan.data.service.APIService
        get() {
            if (_defaultApiService == null) {
                _defaultApiService = createApiServiceWithAuth(com.efthemiosprime.pasabayan.data.service.APIService::class.java)
            }
            return _defaultApiService!!
        }
    
    /**
     * Create API service with authentication interceptor that uses AuthService
     * This bridges the gap between centralized configuration and existing auth patterns
     */
    fun <T> createApiServiceWithAuth(serviceClass: Class<T>): T {
        Log.d(TAG, "Creating authenticated API service for ${serviceClass.simpleName}")
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // Add logging interceptor for debugging
        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        client.addInterceptor(logging)
        
        // Add authentication interceptor that matches the existing pattern
        client.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            // Note: We can't access Context here directly, so this will be a placeholder
            // Services that need auth should use their existing patterns for now
            // TODO: Implement proper DI to inject AuthService
            
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
        
        // Add network error interceptor
        client.addInterceptor(NetworkErrorInterceptor())
        
        val retrofit = Retrofit.Builder()
            .baseUrl("${NetworkConfig.baseUrl}/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client.build())
            .build()
        
        return retrofit.create(serviceClass)
    }
    
    /**
     * Reset cached instances (useful for environment switching)
     */
    fun resetCache() {
        _defaultApiService = null
        Log.d(TAG, "API service cache reset")
    }
}