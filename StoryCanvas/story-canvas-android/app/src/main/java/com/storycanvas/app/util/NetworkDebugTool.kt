package com.storycanvas.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.net.UnknownHostException
import java.util.Enumeration
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Utility class for diagnosing network connectivity issues
 */
object NetworkDebugTool {
    private const val TAG = "NetworkDebugTool"
    
    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Get the device's local IP address
     */
    fun getLocalIpAddress(context: Context): String? {
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
            Log.e(TAG, "Error getting local IP address", e)
        }
        
        return null
    }
    
    /**
     * Check if a host is reachable (ping)
     */
    fun isHostReachable(hostAddress: String, timeout: Int = 2000): Boolean {
        return try {
            val address = InetAddress.getByName(hostAddress)
            address.isReachable(timeout)
        } catch (e: Exception) {
            Log.d(TAG, "Host $hostAddress is not reachable: ${e.message}")
            false
        }
    }
    
    /**
     * Check if a specific port on a host is reachable
     */
    fun isPortReachable(hostAddress: String, port: Int, timeout: Int = 2000): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(hostAddress, port), timeout)
                true
            }
        } catch (e: Exception) {
            Log.d(TAG, "Port $port on host $hostAddress is not reachable: ${e.message}")
            false
        }
    }
    
    /**
     * Run a complete network diagnostics test
     */
    suspend fun runDiagnostics(context: Context, url: String): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, Any>()
            
            // Check if internet is available
            val isInternetAvailable = isNetworkAvailable(context)
            results["internet_available"] = isInternetAvailable
            
            // Get local IP address
            val localIp = getLocalIpAddress(context) ?: "Unknown"
            results["local_ip"] = localIp
            
            // Extract host and port from URL
            val urlObj = URL(url)
            val host = urlObj.host
            val port = if (urlObj.port == -1) urlObj.defaultPort else urlObj.port
            
            // Check if common hosts are reachable
            val hostChecks = mutableMapOf<String, Boolean>()
            
            // Check backend host
            hostChecks[host] = isHostReachable(host)
            
            // Check common backend hosts for development
            if (host == "10.0.2.2") {
                // We're using emulator localhost, check actual device hosts too
                hostChecks["localhost"] = isHostReachable("localhost")
                
                // Try to extract network prefix from device IP
                if (localIp != "Unknown" && !localIp.startsWith("10.0.2")) {
                    val networkPrefix = localIp.substring(0, localIp.lastIndexOf(".") + 1)
                    
                    // Check some common IPs in the same subnet
                    hostChecks[networkPrefix + "1"] = isHostReachable(networkPrefix + "1")
                    hostChecks["172.23.33.24"] = isHostReachable("172.23.33.24")  // Laptop IP from screenshot
                }
            }
            
            // Check if port is open on the host
            val isPortOpen = isPortReachable(host, port)
            hostChecks["$host:$port"] = isPortOpen
            
            results["host_checks"] = hostChecks
            
            // Try to access the server
            var serverAccessible = false
            var serverMessage = "Could not connect to server"
            
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                serverAccessible = responseCode == HttpURLConnection.HTTP_OK
                serverMessage = if (serverAccessible) {
                    "Server responded with code $responseCode"
                } else {
                    "Server returned error code: $responseCode"
                }
            } catch (e: Exception) {
                serverMessage = "Error: ${e.message ?: "Unknown error"}"
            }
            
            results["server_accessible"] = serverAccessible
            results["server_message"] = serverMessage
            
            results
        }
    }
} 