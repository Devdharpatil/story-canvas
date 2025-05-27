package com.pocketwriter.backend.config

import com.pocketwriter.backend.util.NetworkUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import jakarta.annotation.PostConstruct

@Configuration
class ServerConfig(private val environment: Environment) {
    
    private val logger = LoggerFactory.getLogger(ServerConfig::class.java)
    
    // Store the actual port used after server starts
    companion object {
        var serverPort: Int = 8080
        var serverIp: String = "0.0.0.0"  // Changed to 0.0.0.0 to indicate binding to all interfaces
        
        // Get all IP addresses the server is accessible on
        fun getAllServerAddresses(): List<String> {
            return NetworkUtils.getAllLocalIpAddresses()
        }
    }
    
    /**
     * Initialize and log network information at startup
     */
    @PostConstruct
    fun init() {
        // Log all network interfaces for diagnostic purposes
        NetworkUtils.logNetworkInterfaces()
        
        // Log all addresses the server can be accessed on
        val addresses = getAllServerAddresses()
        logger.info("Server will be accessible at these addresses:")
        addresses.forEach { address ->
            logger.info("http://$address:$serverPort/")
        }
    }
    
    /**
     * Customizes the web server to use an available port
     */
    @Bean
    fun webServerFactoryCustomizer(): WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        return WebServerFactoryCustomizer { factory ->
            // Check if port is explicitly set in application properties
            val configuredPort = environment.getProperty("server.port")?.toIntOrNull()
            
            if (configuredPort != null) {
                // Use configured port
                serverPort = configuredPort
                logger.info("Using configured port: $serverPort")
            } else {
                // Find available port
                val availablePort = NetworkUtils.findAvailablePort() ?: 8080
                serverPort = availablePort
                logger.info("Selected available port: $serverPort")
            }
            
            // Get the server's binding address from application.properties
            val configuredAddress = environment.getProperty("server.address")
            if (configuredAddress != null) {
                serverIp = configuredAddress
                logger.info("Using configured address: $serverIp")
            } else {
                // Default to all interfaces
                serverIp = "0.0.0.0"
                logger.info("Using default address (all interfaces): $serverIp")
            }
            
            // Get a specific IP for client connections (not the binding address)
            val clientAccessIp = NetworkUtils.getLocalIpAddress()
            logger.info("Recommended client access IP: $clientAccessIp")
            
            // Apply configuration to the server factory
            factory.setPort(serverPort)
            if (serverIp != "0.0.0.0") {
                factory.setAddress(java.net.InetAddress.getByName(serverIp))
            }
        }
    }
    
    /**
     * Configure CORS to allow requests from Android app
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
            }
        }
    }
} 