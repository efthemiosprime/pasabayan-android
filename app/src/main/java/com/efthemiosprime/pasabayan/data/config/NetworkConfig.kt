package com.efthemiosprime.pasabayan.data.config

/**
 * Centralized Network Configuration
 * Provides consistent URL configuration across all services
 * Handles environment switching and fallback URLs
 */
object NetworkConfig {
    
    // Environment URLs
    private const val PRODUCTION_URL = "https://api.pasabayan.com"
    private const val STAGING_URL = "https://staging.pasabayan.com"
    private const val LOCAL_EMULATOR_URL = "http://10.0.2.2:8001"
    private const val LOCAL_DEVICE_URL = "http://192.168.1.100:8001" // Update with your local IP
    
    // Environment selection
    enum class Environment {
        PRODUCTION,
        STAGING, 
        LOCAL_EMULATOR,
        LOCAL_DEVICE
    }
    
    // Current environment - can be changed based on build variant or runtime config
    // For now, defaulting to PRODUCTION. This can be made configurable later.
    private val currentEnvironment = Environment.PRODUCTION
    
    /**
     * Get the base URL for the current environment
     */
    val baseUrl: String
        get() = when (currentEnvironment) {
            Environment.PRODUCTION -> PRODUCTION_URL
            Environment.STAGING -> STAGING_URL
            Environment.LOCAL_EMULATOR -> LOCAL_EMULATOR_URL
            Environment.LOCAL_DEVICE -> LOCAL_DEVICE_URL
        }
    
    /**
     * Get the full API URL with /api suffix
     */
    val apiUrl: String
        get() = "$baseUrl/api"
    
    /**
     * Check if current environment allows HTTP (for local development)
     */
    val allowHttp: Boolean
        get() = when (currentEnvironment) {
            Environment.LOCAL_EMULATOR, Environment.LOCAL_DEVICE -> true
            else -> false
        }
    
    /**
     * Check if current environment is local development
     */
    val isLocalEnvironment: Boolean
        get() = currentEnvironment in listOf(Environment.LOCAL_EMULATOR, Environment.LOCAL_DEVICE)
    
    /**
     * Get environment name for logging
     */
    val environmentName: String
        get() = currentEnvironment.name
    
    /**
     * Get all available URLs for debugging/testing
     */
    val availableUrls: Map<String, String>
        get() = mapOf(
            "Production" to PRODUCTION_URL,
            "Staging" to STAGING_URL,
            "Local Emulator" to LOCAL_EMULATOR_URL,
            "Local Device" to LOCAL_DEVICE_URL
        )
    
    /**
     * Switch environment at runtime (for testing/debugging)
     * Note: This requires app restart to take effect fully
     */
    fun switchEnvironment(environment: Environment) {
        // For now, we'll keep the environment selection at compile time
        // This method can be enhanced later for dynamic switching
    }
}