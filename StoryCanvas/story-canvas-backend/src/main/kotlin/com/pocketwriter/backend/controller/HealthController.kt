package com.pocketwriter.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Health API", description = "Endpoints for checking system health")
@RestController
@RequestMapping("/api")
class HealthController(
    @Autowired private val jdbcTemplate: JdbcTemplate
) {
    private val logger = LoggerFactory.getLogger(HealthController::class.java)
    
    @Operation(summary = "Ping server", description = "Simple ping endpoint to check if server is up")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Server is up")
    ])
    @GetMapping("/ping")
    fun pingServer(): ResponseEntity<Map<String, String>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val response = mapOf(
            "status" to "up",
            "message" to "Server is running",
            "timestamp" to LocalDateTime.now().format(formatter),
            "version" to "1.0.0"
        )
        return ResponseEntity.ok(response)
    }
    
    @Operation(summary = "Check overall health", description = "Comprehensive health check of all system components")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Health check completed")
    ])
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dbStatus = checkDatabaseHealth()
        val health = mapOf(
            "status" to if (dbStatus.first) "UP" else "DOWN",
            "components" to mapOf(
                "db" to mapOf(
                    "status" to if (dbStatus.first) "UP" else "DOWN",
                    "details" to mapOf(
                        "database" to "PostgreSQL",
                        "validation" to dbStatus.second
                    )
                ),
                "application" to mapOf(
                    "status" to "UP",
                    "details" to mapOf(
                        "name" to "Pocket Writer Backend",
                        "version" to "1.0.0"
                    )
                )
            ),
            "timestamp" to LocalDateTime.now().format(formatter)
        )
        return ResponseEntity.ok(health)
    }
    
    @Operation(summary = "Check database health", description = "Specific health check for the database connection")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Database health check completed")
    ])
    @GetMapping("/health/db")
    fun databaseHealthCheck(): ResponseEntity<Map<String, Any>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dbStatus = checkDatabaseHealth()
        
        val response = mapOf(
            "status" to if (dbStatus.first) "UP" else "DOWN",
            "details" to mapOf(
                "database" to "PostgreSQL",
                "validation" to dbStatus.second,
                "timestamp" to LocalDateTime.now().format(formatter)
            ),
            "metrics" to getConnectionPoolMetrics()
        )
        
        return ResponseEntity.ok(response)
    }
    
    private fun getConnectionPoolMetrics(): Map<String, Any> {
        return try {
            // A more generic query that will work with any database
            val activeConnections = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM information_schema.tables", 
                Int::class.java
            ) ?: 0
            
            mapOf(
                "tables_count" to activeConnections,
                "connection_status" to "active"
            )
        } catch (e: Exception) {
            logger.warn("Could not retrieve database metrics: ${e.message}")
            mapOf("error" to "Metrics unavailable: ${e.message}")
        }
    }
    
    private fun checkDatabaseHealth(): Pair<Boolean, String> {
        return try {
            // Simple database validation query
            val result = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            if (result == 1) {
                Pair(true, "Connection test successful")
            } else {
                Pair(false, "Connection test returned unexpected value")
            }
        } catch (e: Exception) {
            logger.error("Database health check failed", e)
            Pair(false, "Connection test failed: ${e.message}")
        }
    }
} 