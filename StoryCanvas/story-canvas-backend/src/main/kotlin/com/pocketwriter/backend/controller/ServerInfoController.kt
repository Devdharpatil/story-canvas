package com.pocketwriter.backend.controller

import com.pocketwriter.backend.config.ServerConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress

@RestController
@RequestMapping("/api/server-info")
class ServerInfoController {

    data class ServerInfoResponse(
        val ip: String,
        val port: Int,
        val status: String = "running",
        val timestamp: Long = System.currentTimeMillis(),
        val hostname: String = InetAddress.getLocalHost().hostName,
        val availableAddresses: List<String> = ServerConfig.getAllServerAddresses(),
        val preferredAddress: String = ServerConfig.getAllServerAddresses().firstOrNull { 
            !it.startsWith("127.") && !it.startsWith("169.254")
        } ?: "127.0.0.1"
    )
    
    @GetMapping
    fun getServerInfo(): ServerInfoResponse {
        return ServerInfoResponse(
            ip = ServerConfig.serverIp,
            port = ServerConfig.serverPort
        )
    }
    
    @GetMapping("/detailed")
    fun getDetailedServerInfo(): Map<String, Any> {
        val serverInfo = mutableMapOf<String, Any>()
        
        // Basic server info
        serverInfo["status"] = "running"
        serverInfo["timestamp"] = System.currentTimeMillis()
        serverInfo["port"] = ServerConfig.serverPort
        serverInfo["bindAddress"] = ServerConfig.serverIp
        
        // Host information
        val hostname = InetAddress.getLocalHost().hostName
        serverInfo["hostname"] = hostname
        serverInfo["canonicalHostName"] = InetAddress.getLocalHost().canonicalHostName
        
        // Network interfaces and addresses
        val addresses = ServerConfig.getAllServerAddresses()
        serverInfo["availableAddresses"] = addresses
        
        // Recommended address for clients
        val preferredAddress = addresses.firstOrNull { 
            !it.startsWith("127.") && !it.startsWith("169.254")
        } ?: "127.0.0.1"
        serverInfo["preferredAddress"] = preferredAddress
        
        // Connection URLs
        val urls = addresses.map { "http://$it:${ServerConfig.serverPort}/" }
        serverInfo["accessUrls"] = urls
        
        return serverInfo
    }
} 