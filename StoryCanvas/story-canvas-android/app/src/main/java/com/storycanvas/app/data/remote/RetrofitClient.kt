package com.storycanvas.app.data.remote

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.storycanvas.app.util.BackendConfig
import com.storycanvas.app.util.BackendPortDiscovery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client that automatically uses the dynamically discovered backend
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Default timeout values in seconds
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 15L

    private var retrofit: Retrofit? = null
    private var backendDiscovery: BackendDiscovery? = null
    
    /**
     * Initialize the RetrofitClient with the application context
     * Must be called before using getClient() or any API service
     */
    fun initialize(context: Context) {
        backendDiscovery = BackendDiscovery.getInstance(context.applicationContext)
    }
    
    /**
     * Get the Retrofit client instance configured with the current backend URL
     */
    fun getClient(): Retrofit {
        if (backendDiscovery == null) {
            throw IllegalStateException("RetrofitClient must be initialized with a Context before use")
        }
        
        val baseUrl = backendDiscovery!!.getBaseUrl()
        
        if (retrofit == null || getBaseUrl() != baseUrl) {
            val client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl("$baseUrl/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        
        return retrofit!!
    }
    
    /**
     * Get the current base URL being used
     */
    fun getBaseUrl(): String {
        return retrofit?.baseUrl()?.toString() ?: "http://unknown/"
    }
    
    /**
     * Create an API service interface
     */
    inline fun <reified T> createService(): T {
        return getClient().create(T::class.java)
    }
} 