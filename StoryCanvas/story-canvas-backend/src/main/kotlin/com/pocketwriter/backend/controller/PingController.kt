package com.pocketwriter.backend.controller

import com.pocketwriter.backend.config.ServerConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/ping")
class PingController {

    data class PingResponse(
        val status: String = "ok",
        val message: String = "Pocket Writer Backend is running",
        val timestamp: String = LocalDateTime.now().toString(),
        val serverInfo: Map<String, Any> = mapOf(
            "host" to InetAddress.getLocalHost().hostName,
            "ip" to ServerConfig.serverIp,
            "port" to ServerConfig.serverPort,
            "availableAddresses" to ServerConfig.getAllServerAddresses()
        )
    )
    
    @GetMapping
    fun ping(): PingResponse {
        return PingResponse()
    }
} 