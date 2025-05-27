package com.storycanvas.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Responsible for backend server discovery and configuration
 */
class BackendDiscovery(private val context: Context) {
    companion object {
        private const val TAG = "BackendDiscovery"
        private const val PREFS_NAME = "backend_config"
        private const val KEY_HOST = "backend_host"
        private const val KEY_PORT = "backend_port"
        private const val DEFAULT_HOST = "10.0.2.2" // Android emulator reference to host loopback
        private const val DEFAULT_PORT = 8080
        
        // For singleton pattern
        @Volatile
        private var INSTANCE: BackendDiscovery? = null
        
        fun getInstance(context: Context): BackendDiscovery {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackendDiscovery(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Common IP patterns to try during auto-discovery
        private val COMMON_IP_PATTERNS = listOf(
            "10.0.2.2",        // Android Emulator -> Host loopback
            "172.23.33.24",    // Previously seen IP in your network
            "192.168.0",       // Common home network
            "192.168.1",       // Common home network
            "10.0.0",          // Common office network
            "10.0.1",          // Common office network
            "172.16",          // Common office network
            "172.17",          // Common office network
            "172.18",          // Common office network
            "172.19",          // Common office network
            "172.20",          // Common office network
            "172.21",          // Common office network
            "172.22",          // Common office network
            "172.23",          // Common office network
            "172.24",          // Common office network
            "172.25",          // Common office network
            "172.26",          // Common office network
            "172.27",          // Common office network
            "172.28",          // Common office network
            "172.29",          // Common office network
            "172.30",          // Common office network
            "172.31"           // Common office network
        )
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    
    // Current configuration
    var host: String = prefs.getString(KEY_HOST, DEFAULT_HOST) ?: DEFAULT_HOST
        private set
    
    var port: Int = prefs.getInt(KEY_PORT, DEFAULT_PORT)
        private set
    
    // Save configuration to preferences
    private fun saveConfig(host: String, port: Int) {
        this.host = host
        this.port = port
        prefs.edit()
            .putString(KEY_HOST, host)
            .putInt(KEY_PORT, port)
            .apply()
        Log.d(TAG, "Saved backend configuration: $host:$port")
    }
    
    /**
     * Updates configuration and returns true if valid
     */
    fun updateConfig(host: String, port: Int): Boolean {
        if (host.isNotBlank() && port > 0) {
            saveConfig(host, port)
            return true
        }
        return false
    }
    
    /**
     * Gets the base URL for the backend
     */
    fun getBaseUrl(): String {
        return "http://$host:$port"
    }
    
    /**
     * Check if the device has internet connectivity
     */
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Check if the host is reachable (ping)
     */
    suspend fun isHostReachable(hostToCheck: String = host, timeout: Int = 2000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostToCheck, 7), timeout)
                socket.close()
                true
            } catch (e: IOException) {
                try {
                    // Alternative check using Runtime ping
                    val runtime = Runtime.getRuntime()
                    val ipProcess = runtime.exec("/system/bin/ping -c 1 -W 2 $hostToCheck")
                    val exitValue = ipProcess.waitFor()
                    exitValue == 0
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking host reachability", e)
                    false
                }
            }
        }
    }
    
    /**
     * Check if the server is running on the specified port
     */
    suspend fun isServerReachable(hostToCheck: String = host, portToCheck: Int = port, timeout: Int = 2000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostToCheck, portToCheck), timeout)
                socket.close()
                true
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to server at $hostToCheck:$portToCheck", e)
                false
            }
        }
    }
    
    /**
     * Generate a list of potential IP addresses to check based on local network
     */
    private suspend fun generatePotentialHosts(): List<String> {
        val hosts = mutableListOf<String>()
        
        // Add the current configuration first
        hosts.add(host)
        
        // Add localhost equivalent for emulator
        if (host != "10.0.2.2") {
            hosts.add("10.0.2.2")
        }
        
        // Add specific host from the screenshot
        if (host != "172.23.33.24") {
            hosts.add("172.23.33.24")
        }
        
        // Add local device IP-derived hosts
        val deviceIp = getDeviceIp()
        if (deviceIp != "Unknown" && deviceIp != "Error") {
            // Extract the network prefix (e.g., "192.168.1" from "192.168.1.100")
            val lastDotIndex = deviceIp.lastIndexOf('.')
            if (lastDotIndex > 0) {
                val networkPrefix = deviceIp.substring(0, lastDotIndex)
                
                // Add the network gateway (usually .1 or .254)
                hosts.add("$networkPrefix.1")
                hosts.add("$networkPrefix.254")
                
                // Add some other common host numbers
                for (i in 2..10) {
                    hosts.add("$networkPrefix.$i")
                }
            }
        }
        
        // Add common patterns from the companion object
        for (pattern in COMMON_IP_PATTERNS) {
            if (pattern.count { it == '.' } == 3) {
                // Full IP - add directly
                if (!hosts.contains(pattern)) {
                    hosts.add(pattern)
                }
            } else {
                // Partial IP prefix - add some common last octets
                for (i in 1..5) {
                    val fullIp = "$pattern.$i"
                    if (!hosts.contains(fullIp)) {
                        hosts.add(fullIp)
                    }
                }
            }
        }
        
        // Deduplicate
        return hosts.distinct()
    }
    
    /**
     * Attempt to discover the backend server configuration by trying multiple potential hosts
     * @return true if discovery was successful
     */
    suspend fun discoverBackend(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting backend discovery...")
                
                // Generate a list of potential hosts to check
                val potentialHosts = generatePotentialHosts()
                Log.d(TAG, "Generated ${potentialHosts.size} potential hosts to check")
                
                // Try each potential host
                for (potentialHost in potentialHosts) {
                    // Skip if host is not reachable at all
                    if (!isHostReachable(potentialHost)) {
                        Log.d(TAG, "Host $potentialHost is not reachable, skipping")
                        continue
                    }
                    
                    Log.d(TAG, "Host $potentialHost is reachable, checking port $port")
                    
                    // Skip if server port is not reachable
                    if (!isServerReachable(potentialHost, port)) {
                        Log.d(TAG, "Server at $potentialHost:$port is not reachable, skipping")
                        continue
                    }
                    
                    Log.d(TAG, "Server at $potentialHost:$port is reachable, checking API")
                    
                    // Try to get detailed server info
                    try {
                        val url = "http://$potentialHost:$port/api/server-info/detailed"
                        val request = Request.Builder().url(url).build()
                        
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val body = response.body?.string()
                                if (body != null) {
                                    val json = JSONObject(body)
                                    
                                    // Get the server's preferred address
                                    if (json.has("preferredAddress")) {
                                        val preferredAddress = json.getString("preferredAddress")
                                        saveConfig(preferredAddress, port)
                                        Log.d(TAG, "Backend discovery successful using detailed endpoint: $preferredAddress:$port")
                                        return@withContext true
                                    } else {
                                        // Just use this host if it works
                                        saveConfig(potentialHost, port)
                                        Log.d(TAG, "Backend discovery successful (no preferred address): $potentialHost:$port")
                                        return@withContext true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Error getting detailed server info from $potentialHost:$port, trying basic endpoint", e)
                    }
                    
                    // Try basic server-info endpoint as fallback
                    try {
                        val url = "http://$potentialHost:$port/api/server-info"
                        val request = Request.Builder().url(url).build()
                        
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val body = response.body?.string()
                                if (body != null) {
                                    val json = JSONObject(body)
                                    
                                    // For backward compatibility with the original endpoint
                                    if (json.has("ip") && json.has("port")) {
                                        val discoveredIp = json.getString("ip")
                                        val discoveredPort = json.getInt("port")
                                        
                                        // If 0.0.0.0, use the host we successfully connected to
                                        val actualIp = if (discoveredIp == "0.0.0.0") potentialHost else discoveredIp
                                        
                                        saveConfig(actualIp, discoveredPort)
                                        Log.d(TAG, "Backend discovery successful: $actualIp:$discoveredPort")
                                        return@withContext true
                                    } else {
                                        // Just use this host if it works
                                        saveConfig(potentialHost, port)
                                        Log.d(TAG, "Backend discovery successful (using potentialHost): $potentialHost:$port")
                                        return@withContext true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Error getting basic server info from $potentialHost:$port", e)
                    }
                }
                
                Log.e(TAG, "Backend discovery failed after trying all potential hosts")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error during backend discovery", e)
                false
            }
        }
    }
    
    /**
     * Get a full diagnostic report on connectivity
     */
    suspend fun getNetworkDiagnostics(): Map<String, Any> {
        val diagnostics = mutableMapOf<String, Any>()
        
        // Internet connectivity
        val internetAvailable = isInternetAvailable()
        diagnostics["internetAvailable"] = internetAvailable
        
        // Host reachability
        val hostReachable = isHostReachable()
        diagnostics["hostReachable"] = hostReachable
        
        // Server reachability
        val serverReachable = isServerReachable()
        diagnostics["serverReachable"] = serverReachable
        
        // Detailed server status
        if (serverReachable) {
            diagnostics["serverStatus"] = "Available"
            
            // Try to connect to server-info endpoint
            try {
                val url = "http://$host:$port/api/server-info/detailed"
                val request = Request.Builder().url(url).build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        diagnostics["apiStatus"] = "OK"
                        
                        // Parse detailed server info
                        val body = response.body?.string()
                        if (body != null) {
                            try {
                                val json = JSONObject(body)
                                if (json.has("availableAddresses")) {
                                    val addresses = mutableListOf<String>()
                                    val jsonArray = json.getJSONArray("availableAddresses")
                                    for (i in 0 until jsonArray.length()) {
                                        addresses.add(jsonArray.getString(i))
                                    }
                                    diagnostics["serverAddresses"] = addresses
                                }
                                
                                if (json.has("preferredAddress")) {
                                    diagnostics["preferredAddress"] = json.getString("preferredAddress")
                                }
                                
                                if (json.has("accessUrls")) {
                                    val urls = mutableListOf<String>()
                                    val jsonArray = json.getJSONArray("accessUrls")
                                    for (i in 0 until jsonArray.length()) {
                                        urls.add(jsonArray.getString(i))
                                    }
                                    diagnostics["accessUrls"] = urls
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing detailed server info", e)
                            }
                        }
                    } else {
                        // Try basic endpoint as fallback
                        val basicUrl = "http://$host:$port/api/server-info"
                        val basicRequest = Request.Builder().url(basicUrl).build()
                        
                        client.newCall(basicRequest).execute().use { basicResponse ->
                            if (basicResponse.isSuccessful) {
                                diagnostics["apiStatus"] = "OK (basic endpoint)"
                            } else {
                                diagnostics["apiStatus"] = "Error (${response.code})"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                diagnostics["apiStatus"] = "Error: ${e.message}"
            }
        } else {
            diagnostics["serverStatus"] = "Unavailable"
            diagnostics["error"] = "Failed to connect to $host:$port"
        }
        
        diagnostics["deviceIp"] = getDeviceIp()
        
        return diagnostics
    }
    
    /**
     * Get the device's IP address
     */
    private fun getDeviceIp(): String {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error determining device IP", e)
            "Error"
        }
    }
} 