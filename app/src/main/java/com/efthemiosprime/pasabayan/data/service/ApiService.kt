package com.efthemiosprime.pasabayan.data.service

import android.util.Log
import com.efthemiosprime.pasabayan.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ApiInterface {
    @POST("auth/{provider}/login")
    suspend fun loginWithToken(
        @Path("provider") provider: String,
        @Body request: Map<String, String>
    ): Response<okhttp3.ResponseBody>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    @POST("auth/logout")
    suspend fun logout(): Response<APIResponse>

    @GET("delivery-requests")
    suspend fun getDeliveryRequests(): Response<DeliveryRequestsResponse>

    @POST("delivery-requests")
    suspend fun createDeliveryRequest(@Body request: CreateDeliveryRequest): Response<DeliveryRequestResponse>

    @PUT("delivery-requests/{id}/status")
    suspend fun updateDeliveryRequestStatus(
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): Response<DeliveryRequestResponse>

    @POST("delivery-requests/{id}/accept")
    suspend fun acceptDeliveryRequest(@Path("id") id: Int): Response<DeliveryRequestResponse>

    @GET("profile")
    suspend fun getUserProfile(): Response<UserProfile>
}

class ApiService private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: ApiService? = null
        
        fun getInstance(): ApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiService().also { INSTANCE = it }
            }
        }
    }

    private val baseUrl = "http://10.0.2.2:8001/api/"
    private var authToken: String? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("ApiService", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            authToken?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(ApiInterface::class.java)

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    // Authentication
    suspend fun loginWithGoogleAuthCode(authCode: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ApiService", "🔍 Attempting Google login with auth code...")
                
                val response = api.loginWithToken(
                    provider = "google",
                    request = mapOf("auth_code" to authCode)
                )
                
                Log.d("ApiService", "🔍 Backend response status: ${response.code()}")
                
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    Log.d("ApiService", "🔍 Backend response: $responseBodyString")
                    
                    responseBodyString?.let { jsonString ->
                        // Parse JSON manually to handle the response structure
                        val jsonResponse = kotlinx.serialization.json.Json.parseToJsonElement(jsonString).jsonObject
                        val success = jsonResponse["success"]?.jsonPrimitive?.boolean ?: false
                        
                        if (success) {
                            val data = jsonResponse["data"]?.jsonObject
                            val token = data?.get("token")?.jsonPrimitive?.content
                            val userJson = data?.get("user")?.jsonObject
                            
                            if (token != null && userJson != null) {
                                // Parse user data
                                val userId = userJson["id"]?.jsonPrimitive?.int ?: 0
                                val userName = userJson["name"]?.jsonPrimitive?.content ?: ""
                                val userEmail = userJson["email"]?.jsonPrimitive?.content ?: ""
                                val userAvatar = userJson["avatar"]?.jsonPrimitive?.content
                                val userProvider = userJson["provider"]?.jsonPrimitive?.content
                                
                                val user = User(
                                    id = userId,
                                    name = userName,
                                    email = userEmail,
                                    avatar = userAvatar,
                                    phone = null,
                                    provider = userProvider,
                                    providerId = null,
                                    emailVerifiedAt = null,
                                    createdAt = "",
                                    updatedAt = ""
                                )
                                
                                val authResponse = AuthResponse(
                                    token = token,
                                    user = user,
                                    profile = null
                                )
                                
                                Log.d("ApiService", "🔍 Google login successful")
                                Result.success(authResponse)
                            } else {
                                Log.e("ApiService", "🔍 Missing token or user data in response")
                                Result.failure(Exception("Missing token or user data in response"))
                            }
                        } else {
                            val message = jsonResponse["message"]?.jsonPrimitive?.content ?: "Authentication failed"
                            Log.e("ApiService", "🔍 Backend returned unsuccessful response: $message")
                            Result.failure(Exception("Authentication failed: $message"))
                        }
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ApiService", "🔍 Backend error: ${response.code()} - $errorBody")
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "🔍 Google login error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCurrentUser()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Result.success(user)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Get current user error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.logout()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Logout error", e)
                Result.failure(e)
            }
        }
    }

    // Delivery requests
    suspend fun getDeliveryRequests(): Result<List<DeliveryRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDeliveryRequests()
                if (response.isSuccessful) {
                    response.body()?.let { deliveryResponse ->
                        Result.success(deliveryResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Get delivery requests error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createDeliveryRequest(request: CreateDeliveryRequest): Result<DeliveryRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createDeliveryRequest(request)
                if (response.isSuccessful) {
                    response.body()?.let { deliveryResponse ->
                        Result.success(deliveryResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Create delivery request error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateDeliveryRequestStatus(id: Int, status: DeliveryStatus): Result<DeliveryRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateDeliveryRequestStatus(
                    id = id,
                    request = mapOf("status" to status.value)
                )
                if (response.isSuccessful) {
                    response.body()?.let { deliveryResponse ->
                        Result.success(deliveryResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Update delivery request status error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun acceptDeliveryRequest(id: Int): Result<DeliveryRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.acceptDeliveryRequest(id)
                if (response.isSuccessful) {
                    response.body()?.let { deliveryResponse ->
                        Result.success(deliveryResponse.data)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Accept delivery request error", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUserProfile()
                if (response.isSuccessful) {
                    response.body()?.let { profile ->
                        Result.success(profile)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Get user profile error", e)
                Result.failure(e)
            }
        }
    }
} 