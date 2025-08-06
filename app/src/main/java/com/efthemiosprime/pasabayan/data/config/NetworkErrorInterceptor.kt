package com.efthemiosprime.pasabayan.data.config

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Network Error Interceptor
 * Provides enhanced error handling and logging for network issues
 * Helps with debugging connectivity problems on different devices
 */
class NetworkErrorInterceptor : Interceptor {
    
    private companion object {
        const val TAG = "NetworkErrorInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            // Log successful responses in debug mode
            if (response.isSuccessful) {
                Log.d(TAG, "✅ Success: ${request.method} ${request.url} -> ${response.code}")
            } else {
                Log.w(TAG, "⚠️ HTTP Error: ${request.method} ${request.url} -> ${response.code} ${response.message}")
                
                // Log response body for debugging (only for non-successful responses)
                response.peekBody(1024).let { peekBody ->
                    val bodyString = peekBody.string()
                    if (bodyString.isNotEmpty()) {
                        Log.w(TAG, "   Response body preview: ${bodyString.take(200)}...")
                    }
                }
            }
            
            response
            
        } catch (e: Exception) {
            // Enhanced error logging with device-specific guidance
            logNetworkError(request.url.toString(), e)
            throw e
        }
    }
    
    private fun logNetworkError(url: String, exception: Exception) {
        Log.e(TAG, "❌ Network error for: $url")
        Log.e(TAG, "   Error type: ${exception.javaClass.simpleName}")
        Log.e(TAG, "   Message: ${exception.message}")
        
        when (exception) {
            is UnknownHostException -> {
                Log.e(TAG, "   🌐 DNS Resolution Failed")
                Log.e(TAG, "   💡 Possible causes:")
                Log.e(TAG, "      - No internet connection")
                Log.e(TAG, "      - DNS server issues")
                Log.e(TAG, "      - Hostname typos in URL")
                Log.e(TAG, "   🔧 Solutions:")
                Log.e(TAG, "      - Check device internet connectivity")
                Log.e(TAG, "      - Try switching between WiFi/Mobile data")
                Log.e(TAG, "      - Verify the server hostname is correct")
            }
            
            is ConnectException -> {
                Log.e(TAG, "   🔌 Connection Failed")
                Log.e(TAG, "   💡 Possible causes:")
                Log.e(TAG, "      - Server is down or unreachable")
                Log.e(TAG, "      - Firewall blocking connection")
                Log.e(TAG, "      - Wrong port number")
                Log.e(TAG, "   🔧 Solutions:")
                Log.e(TAG, "      - Check if server is running")
                Log.e(TAG, "      - Verify correct port (80 for HTTP, 443 for HTTPS)")
                Log.e(TAG, "      - Check network security config allows the domain")
            }
            
            is SocketTimeoutException -> {
                Log.e(TAG, "   ⏱️ Request Timeout")
                Log.e(TAG, "   💡 Possible causes:")
                Log.e(TAG, "      - Slow network connection")
                Log.e(TAG, "      - Server overloaded")
                Log.e(TAG, "      - Large response taking too long")
                Log.e(TAG, "   🔧 Solutions:")
                Log.e(TAG, "      - Increase timeout values")
                Log.e(TAG, "      - Check network speed")
                Log.e(TAG, "      - Implement retry logic")
            }
            
            is SSLException -> {
                Log.e(TAG, "   🔒 SSL/TLS Error")
                Log.e(TAG, "   💡 Possible causes:")
                Log.e(TAG, "      - Certificate validation failed")
                Log.e(TAG, "      - Unsupported TLS version")
                Log.e(TAG, "      - Certificate pinning issues")
                Log.e(TAG, "   🔧 Solutions:")
                Log.e(TAG, "      - Check network security config")
                Log.e(TAG, "      - Verify server certificates")
                Log.e(TAG, "      - Update certificate pinning")
            }
            
            is IOException -> {
                Log.e(TAG, "   📡 General I/O Error")
                Log.e(TAG, "   💡 Could be various network issues")
                Log.e(TAG, "   🔧 Check device network settings and try again")
            }
            
            else -> {
                Log.e(TAG, "   ❓ Unknown network error")
                Log.e(TAG, "   💡 Stack trace: ${exception.stackTraceToString()}")
            }
        }
        
        // Additional device-specific debugging info
        Log.e(TAG, "   📱 Device Info:")
        Log.e(TAG, "      - Environment: ${NetworkConfig.environmentName}")
        Log.e(TAG, "      - Base URL: ${NetworkConfig.baseUrl}")
        Log.e(TAG, "      - Local Environment: ${NetworkConfig.isLocalEnvironment}")
    }
}