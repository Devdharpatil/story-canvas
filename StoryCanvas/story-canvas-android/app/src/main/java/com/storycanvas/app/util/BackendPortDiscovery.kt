package com.storycanvas.app.util

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.Enumeration
import java.util.concurrent.TimeUnit

// Define DataStore for the app
private val Context.dataStore by preferencesDataStore(name = "backend_settings")

/**
 * Utility class to discover and manage backend port settings
 */
object BackendPortDiscovery {
    private const val TAG = "BackendPortDiscovery"
    
    // Keys for DataStore
    private val BACKEND_PORT = intPreferencesKey("backend_port")
    private val BACKEND_HOST = stringPreferencesKey("backend_host")
    
    // Default values
    private const val DEFAULT_PORT = 8080
    private const val DEFAULT_EMULATOR_HOST = "10.0.2.2"
    
    // Common ports to check
    private val COMMON_PORTS = listOf(8080, 8081, 8082, 8090, 8000, 9000)
    
    /**
     * Get the currently saved backend host
     */
    suspend fun getSavedHost(context: Context): String {
        return context.dataStore.data.map { preferences ->
            preferences[BACKEND_HOST] ?: DEFAULT_EMULATOR_HOST
        }.firstOrNull() ?: DEFAULT_EMULATOR_HOST
    }
    
    /**
     * Get the currently saved backend port
     */
    suspend fun getSavedPort(context: Context): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BACKEND_PORT] ?: DEFAULT_PORT
        }.firstOrNull() ?: DEFAULT_PORT
    }
    
    /**
     * Save backend host
     */
    suspend fun saveHost(context: Context, host: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKEND_HOST] = host
        }
        Log.d(TAG, "Saved backend host: $host")
    }
    
    /**
     * Save backend port
     */
    suspend fun savePort(context: Context, port: Int) {
        context.dataStore.edit { preferences ->
            preferences[BACKEND_PORT] = port
        }
        Log.d(TAG, "Saved backend port: $port")
    }
    
    /**
     * Get a Flow of backend URL that updates when host or port changes
     */
    fun getBackendUrlFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val host = preferences[BACKEND_HOST] ?: DEFAULT_EMULATOR_HOST
            val port = preferences[BACKEND_PORT] ?: DEFAULT_PORT
            "http://$host:$port/api/"
        }
    }
    
    /**
     * Get the current backend URL
     */
    suspend fun getBackendUrl(context: Context): String {
        val host = getSavedHost(context)
        val port = getSavedPort(context)
        return "http://$host:$port/api/"
    }
    
    /**
     * Get device's IP address
     */
    fun getDeviceIpAddress(context: Context): String? {
        try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device IP address", e)
        }
        
        return null
    }
    
    /**
     * Generate possible host IPs based on device's network
     */
    fun generatePossibleHostIps(context: Context): List<String> {
        val hosts = mutableListOf<String>()
        
        // Always add emulator localhost
        hosts.add(DEFAULT_EMULATOR_HOST)
        
        // Add common development machine IPs
        hosts.add("172.23.33.24") // From the user's screenshot
        hosts.add("172.23.39.254") // Gateway from network details
        
        // Try to infer from device IP
        getDeviceIpAddress(context)?.let { deviceIp ->
            // Add device IP itself (for loopback test)
            hosts.add(deviceIp)
            
            // Extract network prefix
            val lastDotIndex = deviceIp.lastIndexOf('.')
            if (lastDotIndex > 0) {
                val networkPrefix = deviceIp.substring(0, lastDotIndex + 1)
                
                // Add common last octet values
                hosts.add(networkPrefix + "1") // Common gateway
                hosts.add(networkPrefix + "100") // Common static IP
                hosts.add(networkPrefix + "101")
                hosts.add(networkPrefix + "102")
                
                // Try a few more values around the device's IP
                val lastOctet = deviceIp.substring(lastDotIndex + 1).toIntOrNull() ?: 0
                if (lastOctet > 1) {
                    hosts.add(networkPrefix + (lastOctet - 1))
                }
                if (lastOctet < 254) {
                    hosts.add(networkPrefix + (lastOctet + 1))
                }
            }
        }
        
        // Remove duplicates and return
        return hosts.distinct()
    }
    
    /**
     * Auto-discover the backend port by trying common ports
     * Returns the port found or null if none were successful
     */
    suspend fun discoverBackendPort(context: Context, host: String = DEFAULT_EMULATOR_HOST): Int? {
        return withContext(Dispatchers.IO) {
            // Check saved port first
            val savedPort = getSavedPort(context)
            if (isPortAccessible(host, savedPort)) {
                Log.d(TAG, "Saved port $savedPort is accessible")
                return@withContext savedPort
            }
            
            // Try common ports
            Log.d(TAG, "Checking common ports on host $host")
            for (port in COMMON_PORTS) {
                if (isPortAccessible(host, port)) {
                    Log.d(TAG, "Found accessible port: $port")
                    savePort(context, port)
                    return@withContext port
                }
            }
            
            // Try scanning a range of ports (more expensive operation)
            Log.d(TAG, "No common port found, scanning port range")
            for (port in 8000..9000) {
                // Skip already checked common ports
                if (port in COMMON_PORTS) continue
                
                if (isPortAccessible(host, port)) {
                    Log.d(TAG, "Found accessible port in range: $port")
                    savePort(context, port)
                    return@withContext port
                }
            }
            
            Log.e(TAG, "No accessible port found on host $host")
            null
        }
    }
    
    /**
     * Check if a specific port is accessible on the given host
     */
    private suspend fun isPortAccessible(host: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://$host:$port/api/ping")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = TimeUnit.SECONDS.toMillis(2).toInt() // Short timeout for quick checks
                connection.readTimeout = TimeUnit.SECONDS.toMillis(2).toInt()
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                val success = responseCode == HttpURLConnection.HTTP_OK
                
                if (success) {
                    Log.d(TAG, "Port $port is accessible on $host (response: $responseCode)")
                } else {
                    Log.d(TAG, "Port $port returned status $responseCode on $host")
                }
                
                success
            } catch (e: Exception) {
                Log.d(TAG, "Port $port is not accessible on $host: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Attempt to discover the backend on multiple hosts
     * Returns a Pair of (host, port) if found, null otherwise
     */
    suspend fun discoverBackend(context: Context): Pair<String, Int>? {
        return withContext(Dispatchers.IO) {
            // Get saved host and port to try first
            val savedHost = getSavedHost(context)
            val savedPort = getSavedPort(context)
            
            // Try saved configuration first
            if (isPortAccessible(savedHost, savedPort)) {
                return@withContext Pair(savedHost, savedPort)
            }
            
            // Generate possible host IPs based on device's network
            val possibleHosts = generatePossibleHostIps(context)
            
            // Try each host with the saved port first (port is more likely to be correct)
            for (host in possibleHosts) {
                if (isPortAccessible(host, savedPort)) {
                    return@withContext Pair(host, savedPort)
                }
            }
            
            // Try each host with common ports
            for (host in possibleHosts) {
                discoverBackendPort(context, host)?.let { port ->
                    return@withContext Pair(host, port)
                }
            }
            
            // No backend found
            null
        }
    }
} 