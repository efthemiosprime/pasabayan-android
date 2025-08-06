package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.efthemiosprime.pasabayan.data.config.NetworkConfig
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * DashboardViewModel - Functional composition of role-specific ViewModels
 * Following functional programming patterns with pure composition
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    // Create APIService for dependency injection
    private val apiService: APIService by lazy {
        createApiService()
    }
    
    // Composition of specialized ViewModels
    private val _roleViewModel = RoleViewModel()
    private val _shipperViewModel = ShipperViewModel(application)
    private val _carrierViewModel = CarrierViewModel(apiService)
    private val _matchViewModel = MatchViewModel(application)
    
    // Public access to role state
    val currentRole: StateFlow<UserRole> = _roleViewModel.currentRole
    
    // Public access to specialized ViewModels
    val shipperViewModel: ShipperViewModel = _shipperViewModel
    val carrierViewModel: CarrierViewModel = _carrierViewModel
    val roleViewModel: RoleViewModel = _roleViewModel
    val matchViewModel: MatchViewModel = _matchViewModel
    
    /**
     * Create APIService using centralized URL configuration with existing auth pattern
     */
    private fun createApiService(): APIService {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        
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
                
                // Get auth token from AuthService - existing pattern
                try {
                    val authService = AuthService.getInstance(getApplication())
                    val token = kotlinx.coroutines.runBlocking { authService.getToken() }
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                } catch (e: Exception) {
                    // Log error but continue without auth header
                }
                
                chain.proceed(requestBuilder.build())
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl("${NetworkConfig.baseUrl}/") // Use centralized URL config
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(APIService::class.java)
    }
} 