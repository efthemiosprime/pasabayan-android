package com.efthemiosprime.pasabayan.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.efthemiosprime.pasabayan.domain.repository.PackageRepository
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService

/**
 * Package Repository Implementation - matches existing repository pattern
 * Uses APIService directly for network requests with proper error handling
 */
class PackageRepositoryImpl(
    private val apiService: APIService
) : PackageRepository {
    
    override suspend fun getPackageRequests(): Result<List<PackageRequest>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Fetching package requests from API")
            Log.d(TAG, "   📡 URL: ${APIService.BASE_URL}/packages")
            Log.d(TAG, "   🔐 Auth: Will include Bearer token if available")
            
            val response = apiService.getPackageRequests()
            
            Log.d(TAG, "📥 Raw API Response received")
            Log.d(TAG, "   ✅ Success: ${response.success}")
            Log.d(TAG, "   📝 Message: ${response.message}")
            Log.d(TAG, "   📊 Paginated data - Current page: ${response.data.currentPage}")
            Log.d(TAG, "   📊 Total packages: ${response.data.total}")
            Log.d(TAG, "   📊 Packages in current page: ${response.data.data.size}")
            
            if (response.success) {
                // Extract the actual packages from the paginated structure and convert to UI models
                val apiPackages = response.data.data
                val uiPackages = apiPackages.map { it.toPackageRequest() }
                Log.d(TAG, "✅ Package requests fetched successfully - Count: ${uiPackages.size}")
                if (uiPackages.isNotEmpty()) {
                    Log.d(TAG, "   📦 First package: ${uiPackages.first().title}")
                }
                Result.success(uiPackages)
            } else {
                Log.e(TAG, "❌ Package requests fetch failed: ${response.message}")
                Result.failure(Exception("API Error: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching package requests")
            Log.e(TAG, "   🔥 Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   📝 Exception message: ${e.message}")
            Log.e(TAG, "   📍 Cause: ${e.cause?.message ?: "Unknown"}")
            
            // Enhanced debugging for HTTP errors
            if (e.message?.contains("500") == true) {
                Log.e(TAG, "   🚨 HTTP 500 Internal Server Error detected")
                Log.e(TAG, "   💡 This suggests the server endpoint exists but has an internal error")
                Log.e(TAG, "   🔧 Server is returning HTML instead of JSON")
                Log.e(TAG, "   🔧 Check your server logs for the /api/packages endpoint")
                Log.e(TAG, "   🔧 Ensure your server has proper API routes configured")
                Log.e(TAG, "   🔧 Test the endpoint directly: curl -X GET http://127.0.0.1:8000/api/packages")
            }
            
            // Check for authentication issues
            if (e.message?.contains("401") == true) {
                Log.e(TAG, "   🚨 HTTP 401 Unauthorized detected")
                Log.e(TAG, "   💡 Authentication token is invalid, expired, or missing")
                Log.e(TAG, "   🔧 Check if user is properly logged in")
                Log.e(TAG, "   🔧 Check if token is valid: may need to refresh or re-login")
                Log.e(TAG, "   🔧 Test without auth: curl -X GET http://127.0.0.1:8000/api/packages")
                Log.e(TAG, "   🔧 Test with valid token: curl -X GET http://127.0.0.1:8000/api/packages -H \"Authorization: Bearer YOUR_TOKEN\"")
            }
            
            // Check if it's a network connectivity issue
            if (e.message?.contains("Failed to connect") == true || 
                e.message?.contains("ConnectException") == true) {
                Log.e(TAG, "   🚨 Network connectivity issue detected")
                Log.e(TAG, "   💡 Make sure your local server is running on port 8000")
                Log.e(TAG, "   🔧 Test: curl -X GET http://127.0.0.1:8000/api/packages")
            }
            
            Result.failure(e)
        }
    }
    
    override suspend fun createPackageRequest(request: CreatePackageRequest): Result<PackageRequest> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Creating package request: ${request.title}")
            
            // Convert to API format
            val apiRequest = request.toApiRequest()
            Log.d(TAG, "📦 API Request fields:")
            Log.d(TAG, "   📍 Pickup: ${apiRequest.pickupAddress}, ${apiRequest.pickupCity}, ${apiRequest.pickupCountry}")
            Log.d(TAG, "   📍 Delivery: ${apiRequest.deliveryAddress}, ${apiRequest.deliveryCity}, ${apiRequest.deliveryCountry}")
            Log.d(TAG, "   📦 Package: ${apiRequest.packageType}, ${apiRequest.packageWeightKg}kg")
            Log.d(TAG, "   📏 Dimensions: ${apiRequest.packageDimensions.length}x${apiRequest.packageDimensions.width}x${apiRequest.packageDimensions.height}")
            Log.d(TAG, "   🚀 Urgency: ${apiRequest.urgencyLevel}")
            Log.d(TAG, "   📅 Pickup date: ${apiRequest.pickupDatePreferred}")
            Log.d(TAG, "   📅 Delivery date: ${apiRequest.deliveryDateNeeded}")
            
            val response = apiService.createPackageRequest(apiRequest)
            
            if (response.success) {
                Log.d(TAG, "✅ Package request created successfully - ID: ${response.data.id}")
                Result.success(response.data.toPackageRequest())
            } else {
                Log.e(TAG, "❌ Package request creation failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while creating package request: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getPackageRequest(packageId: Int): Result<PackageRequest> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Fetching package request with ID: $packageId")
            val response = apiService.getPackageRequest(packageId)
            
            if (response.success) {
                Log.d(TAG, "✅ Package request fetched successfully - ID: ${response.data.id}")
                Result.success(response.data.toPackageRequest())
            } else {
                Log.e(TAG, "❌ Package request fetch failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching package request: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updatePackageRequest(packageId: Int, request: CreatePackageRequest): Result<PackageRequest> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Updating package request with ID: $packageId")
            
            // Convert to API format
            val apiRequest = request.toApiRequest()
            val response = apiService.updatePackageRequest(packageId, apiRequest)
            
            if (response.success) {
                Log.d(TAG, "✅ Package request updated successfully - ID: ${response.data.id}")
                Result.success(response.data.toPackageRequest())
            } else {
                Log.e(TAG, "❌ Package request update failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while updating package request: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deletePackageRequest(packageId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Deleting package request with ID: $packageId")
            val response = apiService.deletePackageRequest(packageId)
            
            Log.d(TAG, "✅ Package request deleted successfully - ID: $packageId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while deleting package request: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCompatibleTrips(packageId: Int): Result<List<CompatibleTrip>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Fetching compatible trips for package ID: $packageId")
            val response = apiService.getCompatibleTrips(packageId)
            
            if (response.success) {
                Log.d(TAG, "✅ Compatible trips fetched successfully - Count: ${response.data.size}")
                Result.success(response.data)
            } else {
                Log.e(TAG, "❌ Compatible trips fetch failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching compatible trips: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "PackageRepositoryImpl"
        
        /**
         * Test authentication status
         * Helper method to verify token is valid
         */
        suspend fun testAuthentication(context: Context): Result<Boolean> = withContext(Dispatchers.IO) {
            return@withContext try {
                Log.d(TAG, "🧪 Testing authentication status")
                
                val authService = AuthService.getInstance(context)
                val token = authService.getToken()
                
                if (token == null) {
                    Log.w(TAG, "⚠️ No authentication token found")
                    return@withContext Result.failure(Exception("No authentication token"))
                }
                
                Log.d(TAG, "✅ Authentication token found")
                Log.d(TAG, "   🎫 Token length: ${token.length}")
                Log.d(TAG, "   🎫 Token starts with: ${token.take(20)}...")
                
                // Test token validity by calling a protected endpoint
                val currentUser = authService.getCurrentUser()
                if (currentUser != null) {
                    Log.d(TAG, "✅ User data found: ${currentUser.name}")
                    Result.success(true)
                } else {
                    Log.w(TAG, "⚠️ No user data found")
                    Result.failure(Exception("No user data found"))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Authentication test failed: ${e.message}", e)
                Result.failure(e)
            }
        }
        
        /**
         * Factory method to create PackageRepositoryImpl with APIService
         * Includes authentication interceptor like AuthService
         */
        fun create(context: Context): PackageRepositoryImpl {
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
                    
                    // Add authentication header if available
                    try {
                        val token = kotlinx.coroutines.runBlocking { 
                            // Get token from AuthService singleton
                            AuthService.getInstance(context).getToken()
                        }
                        if (token != null) {
                            requestBuilder.addHeader("Authorization", "Bearer $token")
                            Log.d(TAG, "🔐 Added Authorization header to package request")
                            Log.d(TAG, "   🎫 Token length: ${token.length}")
                            Log.d(TAG, "   🎫 Token starts with: ${token.take(20)}...")
                        } else {
                            Log.w(TAG, "⚠️ No auth token available for package request")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to get token for package request: ${e.message}")
                    }
                    
                    // Add headers to request JSON response
                    requestBuilder.addHeader("Accept", "application/json")
                    requestBuilder.addHeader("Content-Type", "application/json")
                    
                    val response = chain.proceed(requestBuilder.build())
                    
                    // Log response details for debugging
                    Log.d(TAG, "📊 Response Debug Info:")
                    Log.d(TAG, "   🔢 Status Code: ${response.code}")
                    Log.d(TAG, "   📝 Status Message: ${response.message}")
                    Log.d(TAG, "   📋 Content-Type: ${response.header("Content-Type")}")
                    Log.d(TAG, "   📏 Content-Length: ${response.header("Content-Length")}")
                    
                    // Check if we're getting HTML instead of JSON
                    val contentType = response.header("Content-Type")
                    if (contentType?.contains("text/html") == true) {
                        Log.e(TAG, "   🚨 SERVER ERROR: Receiving HTML instead of JSON!")
                        Log.e(TAG, "   💡 This indicates a server-side error or misconfiguration")
                        Log.e(TAG, "   🔧 Check your server's API routes and error handling")
                    }
                    
                    response
                }
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl("${APIService.BASE_URL}/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            
            val apiService = retrofit.create(APIService::class.java)
            return PackageRepositoryImpl(apiService)
        }
    }
} 