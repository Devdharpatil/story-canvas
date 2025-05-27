package com.pocketwriter.backend.util

import java.net.ServerSocket
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import org.slf4j.LoggerFactory

object NetworkUtils {
    private val logger = LoggerFactory.getLogger(NetworkUtils::class.java)
    
    // Default port range
    private const val MIN_PORT = 8080
    private const val MAX_PORT = 8180
    
    /**
     * Finds an available port in the specified range
     * @return available port number or null if none available
     */
    fun findAvailablePort(startPort: Int = MIN_PORT, endPort: Int = MAX_PORT): Int? {
        for (port in startPort..endPort) {
            try {
                // Use 0.0.0.0 to bind to all interfaces
                ServerSocket(port, 50, InetAddress.getByName("0.0.0.0")).use { 
                    logger.info("Found available port: $port")
                    return port 
                }
            } catch (e: IOException) {
                logger.debug("Port $port is in use, trying next")
                // Port is in use, try the next one
            }
        }
        logger.error("No available ports found in range $startPort-$endPort")
        return null
    }
    
    /**
     * Returns all non-loopback IPv4 addresses for the server
     */
    fun getAllLocalIpAddresses(): List<String> {
        val addresses = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                
                // Skip loopback, virtual and non-running interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }
                
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val address = inetAddresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        addresses.add(address.hostAddress)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error determining local IP addresses", e)
        }
        
        // Always add localhost as fallback
        if (addresses.isEmpty()) {
            addresses.add("127.0.0.1")
        }
        
        return addresses
    }
    
    /**
     * Returns the server's primary IP address for external connections
     * Will prefer non-loopback addresses on active network interfaces
     */
    fun getLocalIpAddress(): String {
        val addresses = getAllLocalIpAddresses()
        
        // Prefer addresses that are likely to be accessible from other devices
        val preferredAddress = addresses.firstOrNull { 
            !it.startsWith("127.") && !it.startsWith("169.254")
        }
        
        return preferredAddress ?: "127.0.0.1" // Fallback to localhost
    }
    
    /**
     * Logs all available network interfaces and their addresses
     * Useful for debugging connection issues
     */
    fun logNetworkInterfaces() {
        try {
            logger.info("Available network interfaces:")
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                logger.info("Interface: ${networkInterface.displayName} (${networkInterface.name})")
                logger.info("  - Is up: ${networkInterface.isUp}")
                logger.info("  - Is loopback: ${networkInterface.isLoopback}")
                logger.info("  - Is virtual: ${networkInterface.isVirtual}")
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    logger.info("  - Address: ${address.hostAddress} (${if (address is java.net.Inet4Address) "IPv4" else "IPv6"})")
                }
            }
        } catch (e: Exception) {
            logger.error("Error logging network interfaces", e)
        }
    }
} 