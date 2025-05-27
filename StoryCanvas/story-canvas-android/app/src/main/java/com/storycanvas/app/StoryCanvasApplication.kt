package com.storycanvas.app

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.storycanvas.app.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StoryCanvasApplication : Application() {
    
    companion object {
        const val TAG = "StoryCanvasApp"
        const val DEBUG = true // Hardcoded for now, replace with BuildConfig when available
    }
    
    // Application scope for coroutines that should live as long as the application
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize crash reporting
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
            // You could add Firebase Crashlytics or other crash reporting here
        }
        
        // Enable StrictMode for development to catch common issues
        if (DEBUG) {
            enableStrictMode()
        }
        
        // Initialize RetrofitClient
        RetrofitClient.init(this)
        
        // Start backend discovery in the background
        applicationScope.launch {
            try {
                // This will attempt to discover and update the backend connection
                // The result is a new ApiService if successful, which will be used for future requests
                val discoveredService = RetrofitClient.discoverBackendService(applicationContext)
                if (discoveredService != null) {
                    Log.i(TAG, "Backend service discovered and configured successfully")
                } else {
                    Log.w(TAG, "Backend service discovery was unsuccessful, using fallback settings")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during backend service discovery", e)
            }
        }
        
        Log.i(TAG, "Application initialized successfully")
    }
    
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        
        Log.d(TAG, "StrictMode enabled")
    }
} 