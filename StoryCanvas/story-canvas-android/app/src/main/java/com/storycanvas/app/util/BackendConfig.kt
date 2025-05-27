package com.storycanvas.app.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles backend configuration and environment selection
 * Uses a combination of build configuration, automatic discovery, and fallback mechanisms
 */
object BackendConfig {
    private const val TAG = "BackendConfig"
    
    // Default backend connection details
    private const val DEFAULT_HOST = "10.0.2.2" // Android emulator localhost
    private const val DEFAULT_PORT = 8080
    private const val DEFAULT_API_PATH = "/api/"
    
    // Shared preferences keys
    private const val PREFS_NAME = "backend_config"
    private const val KEY_HOST = "backend_host"
    private const val KEY_PORT = "backend_port"
    private const val KEY_ENVIRONMENT = "environment"
    
    // Available environments
    enum class Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
    
    // Current environment - defaults to DEVELOPMENT
    private var currentEnvironment = Environment.DEVELOPMENT
    
    // Connection state flow
    private val _connectionState = MutableStateFlow(ConnectionState.UNKNOWN)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    // Auto-discovery in progress flag
    private val isDiscoveryInProgress = AtomicBoolean(false)
    
    /**
     * Initialize the backend configuration
     */
    fun init(context: Context) {
        // Load saved configuration
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSavedConfig(prefs)
        
        // Set initial connection state
        updateConnectionState(ConnectionState.UNKNOWN)
        
        Log.d(TAG, "BackendConfig initialized with environment: $currentEnvironment")
    }
    
    /**
     * Get the base URL for API requests
     */
    fun getBaseUrl(): String {
        val host = getHost()
        val port = getPort()
        
        return when (currentEnvironment) {
            Environment.DEVELOPMENT -> "http://$host:$port$DEFAULT_API_PATH"
            Environment.STAGING -> "https://staging-api.storycanvas.com/api/"
            Environment.PRODUCTION -> "https://api.storycanvas.com/api/"
        }
    }
    
    /**
     * Get the current host
     */
    fun getHost(): String {
        return BackendPreferences.getHost() ?: DEFAULT_HOST
    }
    
    /**
     * Get the current port
     */
    fun getPort(): Int {
        return BackendPreferences.getPort() ?: DEFAULT_PORT
    }
    
    /**
     * Set a custom host and port
     */
    fun setCustomBackend(context: Context, host: String, port: Int) {
        BackendPreferences.saveHost(context, host)
        BackendPreferences.savePort(context, port)
        Log.d(TAG, "Custom backend set to $host:$port")
    }
    
    /**
     * Switch to a different environment
     */
    fun setEnvironment(context: Context, environment: Environment) {
        currentEnvironment = environment
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENVIRONMENT, environment.name)
            .apply()
        Log.d(TAG, "Environment switched to $environment")
    }
    
    /**
     * Auto-discover the backend server
     * Returns true if successful, false otherwise
     */
    suspend fun autoDiscoverBackend(context: Context): Boolean {
        if (isDiscoveryInProgress.getAndSet(true)) {
            Log.d(TAG, "Auto-discovery already in progress")
            return false
        }
        
        try {
            updateConnectionState(ConnectionState.DISCOVERING)
            
            // Try common local development ports
            val commonPorts = listOf(8080, 8081, 8082, 8090, 9000)
            val commonHosts = listOf("10.0.2.2", "localhost", "127.0.0.1")
            
            // For physical devices, try to find the server on the local network
            if (!isEmulator()) {
                // Add local network address to the hosts list
                val localNetworkHost = getWifiIpAddress(context)
                if (localNetworkHost != null) {
                    commonHosts + localNetworkHost
                }
            }
            
            withContext(Dispatchers.IO) {
                for (host in commonHosts) {
                    for (port in commonPorts) {
                        if (isServerReachable(host, port)) {
                            Log.d(TAG, "Found backend server at $host:$port")
                            BackendPreferences.saveHost(context, host)
                            BackendPreferences.savePort(context, port)
                            updateConnectionState(ConnectionState.CONNECTED)
                            return@withContext true
                        }
                    }
                }
                
                // If we couldn't find the server, update the connection state
                updateConnectionState(ConnectionState.DISCONNECTED)
                return@withContext false
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error during auto-discovery", e)
            updateConnectionState(ConnectionState.ERROR)
            return false
        } finally {
            isDiscoveryInProgress.set(false)
        }
    }
    
    /**
     * Test the connection to the backend
     */
    suspend fun testConnection(): Boolean {
        val host = getHost()
        val port = getPort()
        
        updateConnectionState(ConnectionState.TESTING)
        
        return try {
            val isReachable = withContext(Dispatchers.IO) {
                isServerReachable(host, port)
            }
            
            updateConnectionState(
                if (isReachable) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
            )
            
            isReachable
        } catch (e: Exception) {
            Log.e(TAG, "Error testing connection", e)
            updateConnectionState(ConnectionState.ERROR)
            false
        }
    }
    
    /**
     * Check if a server is reachable at the given host and port
     */
    private fun isServerReachable(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 1000)
                true
            }
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Load saved configuration from SharedPreferences
     */
    private fun loadSavedConfig(prefs: SharedPreferences) {
        // Load environment
        val savedEnvironment = prefs.getString(KEY_ENVIRONMENT, null)
        if (savedEnvironment != null) {
            try {
                currentEnvironment = Environment.valueOf(savedEnvironment)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid environment value in preferences: $savedEnvironment")
            }
        }
    }
    
    /**
     * Update the connection state
     */
    private fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        Log.d(TAG, "Connection state updated to $state")
    }
    
    /**
     * Get the WiFi IP address of the device
     */
    private fun getWifiIpAddress(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            // Get local IP (this is just a placeholder - actual implementation would be more complex)
            return "192.168.1.1" // This would need to be determined dynamically in a real app
        }
        
        return null
    }
    
    /**
     * Check if running on an emulator
     */
    private fun isEmulator(): Boolean {
        return (android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK"))
    }
    
    /**
     * Connection state enum
     */
    enum class ConnectionState {
        UNKNOWN,
        DISCOVERING,
        TESTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }
} 