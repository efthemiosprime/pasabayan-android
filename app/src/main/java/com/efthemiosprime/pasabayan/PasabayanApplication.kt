package com.efthemiosprime.pasabayan

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

/**
 * Application class for the Pasabayan Android app
 * Basic setup without dependency injection for initial implementation
 * Mirrors iOS PasabayanApp structure
 */
class PasabayanApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
} 