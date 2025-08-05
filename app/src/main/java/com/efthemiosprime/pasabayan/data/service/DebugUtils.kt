package com.efthemiosprime.pasabayan.data.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.efthemiosprime.pasabayan.data.repository.PackageRepositoryImpl
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl

/**
 * Debug utilities for diagnosing data loading issues
 * Comprehensive testing suite for packages and trips
 */
object DebugUtils {
    private const val TAG = "DebugUtils"
    
    /**
     * Run comprehensive diagnostics for packages and trips loading
     */
    suspend fun runComprehensiveDiagnostics(context: Context): Map<String, Any> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔧 Starting comprehensive diagnostics...")
        
        val results = mutableMapOf<String, Any>()
        
        // 1. Test network connectivity
        Log.d(TAG, "1️⃣ Testing network connectivity...")
        results["networkConnectivity"] = testNetworkConnectivity()
        
        // 2. Test authentication
        Log.d(TAG, "2️⃣ Testing authentication...")
        results["authentication"] = testAuthentication(context)
        
        // 3. Test API endpoints
        Log.d(TAG, "3️⃣ Testing API endpoints...")
        results["apiEndpoints"] = testApiEndpoints(context)
        
        // 4. Test package loading
        Log.d(TAG, "4️⃣ Testing package loading...")
        results["packageLoading"] = testPackageLoading(context)
        
        // 5. Test trip loading
        Log.d(TAG, "5️⃣ Testing trip loading...")
        results["tripLoading"] = testTripLoading(context)
        
        // 6. Summary
        Log.d(TAG, "📋 Diagnostic Summary:")
        results.forEach { (key, value) ->
            Log.d(TAG, "   $key: $value")
        }
        
        return@withContext results
    }
    
    private suspend fun testNetworkConnectivity(): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        try {
            Log.d(TAG, "   🌐 Testing basic internet connectivity...")
            val connection = java.net.URL("https://www.google.com").openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            results["internetConnectivity"] = true
            Log.d(TAG, "   ✅ Internet connectivity: OK")
        } catch (e: Exception) {
            results["internetConnectivity"] = false
            results["internetError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ Internet connectivity: FAILED - ${e.message}")
        }
        
        // Test API server connectivity with enhanced SSL/TLS diagnostics
        try {
            Log.d(TAG, "   🌐 Testing API server connectivity...")
            Log.d(TAG, "   📡 Target: ${APIService.BASE_URL}")
            
            val url = java.net.URL(APIService.BASE_URL)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            // Enhanced SSL diagnostics for HTTPS connections
            if (connection is javax.net.ssl.HttpsURLConnection) {
                Log.d(TAG, "   🔒 HTTPS connection detected - testing SSL...")
                connection.setRequestProperty("User-Agent", "Pasabayan-Android/1.0")
                
                try {
                    connection.connect()
                    val responseCode = connection.responseCode
                    Log.d(TAG, "   📊 HTTP Response Code: $responseCode")
                    
                    // Check SSL certificate info
                    val serverCerts = connection.serverCertificates
                    if (serverCerts.isNotEmpty()) {
                        Log.d(TAG, "   🔒 SSL Certificate: ${serverCerts[0].type}")
                        Log.d(TAG, "   🔒 Certificate Subject: ${(serverCerts[0] as java.security.cert.X509Certificate).subjectDN}")
                    }
                    
                    results["apiServerConnectivity"] = true
                    results["httpResponseCode"] = responseCode
                    results["sslCertificateValid"] = true
                    Log.d(TAG, "   ✅ API server connectivity: OK")
                    Log.d(TAG, "   ✅ SSL certificate: VALID")
                    
                } catch (sslE: javax.net.ssl.SSLException) {
                    results["apiServerConnectivity"] = false
                    results["sslError"] = sslE.message ?: "SSL handshake failed"
                    Log.e(TAG, "   ❌ SSL Error: ${sslE.message}")
                    Log.e(TAG, "   🔍 Check network_security_config.xml for api.pasabayan.com domain")
                    Log.e(TAG, "   🔍 Verify SSL certificate is valid and trusted")
                }
            } else {
                connection.connect()
                results["apiServerConnectivity"] = true
                Log.d(TAG, "   ✅ API server connectivity: OK (HTTP)")
            }
            
        } catch (e: Exception) {
            results["apiServerConnectivity"] = false
            results["apiServerError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ API server connectivity: FAILED - ${e.message}")
            
            when (e) {
                is java.net.UnknownHostException -> {
                    Log.e(TAG, "   🔍 DNS resolution failed for api.pasabayan.com")
                    Log.e(TAG, "   💡 Check internet connection and DNS settings")
                }
                is java.net.ConnectException -> {
                    Log.e(TAG, "   🔍 Connection refused - server may be down")
                    Log.e(TAG, "   💡 Try switching to LOCAL_URL for local testing")
                }
                is javax.net.ssl.SSLException -> {
                    Log.e(TAG, "   🔍 SSL/TLS error - certificate or configuration issue")
                    Log.e(TAG, "   💡 Check network_security_config.xml configuration")
                }
                else -> {
                    Log.e(TAG, "   🔍 Unexpected error: ${e.javaClass.simpleName}")
                }
            }
            
            // Suggest alternative URLs
            Log.d(TAG, "   💡 Try these alternatives:")
            Log.d(TAG, "      - Change BASE_URL to LOCAL_URL for local testing")
            Log.d(TAG, "      - Check if server is running")
            Log.d(TAG, "      - Verify network_security_config.xml")
            Log.d(TAG, "      - Test api.pasabayan.com in browser")
        }
        
        return results
    }
    
    private suspend fun testAuthentication(context: Context): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        try {
            val authService = AuthService.getInstance(context)
            val debugInfo = authService.debugTokenStatus()
            results.putAll(debugInfo)
            
            if (debugInfo["hasToken"] as? Boolean == true) {
                Log.d(TAG, "   ✅ Authentication token exists")
                
                // Test API connectivity with token
                val connectivityTest = authService.testApiConnectivity()
                results["apiConnectivityWithToken"] = connectivityTest.isSuccess
                if (connectivityTest.isFailure) {
                    results["apiConnectivityError"] = connectivityTest.exceptionOrNull()?.message ?: "Unknown error"
                }
            } else {
                Log.w(TAG, "   ⚠️ No authentication token found")
                
                // REMOVED: Mock login no longer available - use real authentication
                Log.w(TAG, "   ⚠️ Mock login disabled - use real authentication methods")
            }
        } catch (e: Exception) {
            results["authenticationError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ Authentication test failed: ${e.message}")
        }
        
        return results
    }
    
    private suspend fun testApiEndpoints(context: Context): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        try {
            Log.d(TAG, "   🔍 Testing API endpoints...")
            
            // Test health endpoint
            try {
                val apiService = TripRepositoryImpl.create(context).let { 
                    // Extract APIService from repository - this is a hack for testing
                    // In production, we'd inject this properly
                    APIService::class.java
                }
                results["healthEndpoint"] = "Not testable without proper DI"
            } catch (e: Exception) {
                results["healthEndpointError"] = e.message ?: "Unknown error"
            }
            
            results["endpointUrls"] = mapOf(
                "packages" to "${APIService.BASE_URL}/api/packages",
                "trips" to "${APIService.BASE_URL}/api/trips",
                "health" to "${APIService.BASE_URL}/api/health"
            )
            
        } catch (e: Exception) {
            results["endpointTestError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ API endpoint test failed: ${e.message}")
        }
        
        return results
    }
    
    private suspend fun testPackageLoading(context: Context): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        try {
            Log.d(TAG, "   📦 Testing package repository...")
            val packageRepository = PackageRepositoryImpl.create(context)
            
            val packageResult = packageRepository.getPackageRequests()
            results["packageLoadingSuccess"] = packageResult.isSuccess
            
            if (packageResult.isSuccess) {
                val packages = packageResult.getOrNull()
                results["packageCount"] = packages?.size ?: 0
                Log.d(TAG, "   ✅ Package loading successful: ${packages?.size} packages")
            } else {
                val error = packageResult.exceptionOrNull()
                results["packageLoadingError"] = error?.message ?: "Unknown error"
                Log.e(TAG, "   ❌ Package loading failed: ${error?.message}")
            }
            
        } catch (e: Exception) {
            results["packageTestError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ Package test exception: ${e.message}")
        }
        
        return results
    }
    
    private suspend fun testTripLoading(context: Context): Map<String, Any> {
        val results = mutableMapOf<String, Any>()
        
        try {
            Log.d(TAG, "   🚛 Testing trip repository...")
            val tripRepository = TripRepositoryImpl.create(context)
            
            // Test getting trips for carrier ID 1
            tripRepository.getTripsForCarrier(1).collect { result ->
                results["tripLoadingSuccess"] = result.isSuccess
                
                if (result.isSuccess) {
                    val trips = result.getOrNull()
                    results["tripCount"] = trips?.size ?: 0
                    Log.d(TAG, "   ✅ Trip loading successful: ${trips?.size} trips")
                } else {
                    val error = (result as? com.efthemiosprime.pasabayan.data.common.Result.Failure)?.error
                    results["tripLoadingError"] = error?.message ?: "Unknown error"
                    Log.e(TAG, "   ❌ Trip loading failed: ${error?.message}")
                }
            }
            
        } catch (e: Exception) {
            results["tripTestError"] = e.message ?: "Unknown error"
            Log.e(TAG, "   ❌ Trip test exception: ${e.message}")
        }
        
        return results
    }
    
    /**
     * Quick connectivity test that can be called from UI
     */
    suspend fun quickConnectivityTest(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val connection = java.net.URL(APIService.BASE_URL).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            "✅ API server is reachable"
        } catch (e: Exception) {
            "❌ Cannot reach API server: ${e.message}"
        }
    }
    
    /**
     * Switch API base URL for testing (requires app restart)
     */
    fun suggestAlternativeConfigurations(): List<String> {
        return listOf(
            "💡 Debugging suggestions:",
            "1. Change APIService.BASE_URL to APIService.LOCAL_URL for local testing",
            "2. Change APIService.BASE_URL to APIService.STAGING_URL for staging",
            "3. Ensure your local server is running on port 8001",
            "4. Check network_security_config.xml for cleartext traffic settings",
            "5. Verify AndroidManifest.xml has INTERNET permission",
            "6. Check device's internet connection",
            "7. Try mock login if authentication fails",
            "8. Check logcat for detailed error messages with tags:",
            "   - PackageRepositoryImpl",
            "   - TripRepositoryImpl", 
            "   - AuthService",
            "   - APIService"
        )
    }
} 