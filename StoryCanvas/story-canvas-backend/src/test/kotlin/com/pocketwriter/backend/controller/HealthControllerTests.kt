package com.pocketwriter.backend.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@WebMvcTest(HealthController::class)
class HealthControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `ping endpoint should return status up`() {
        mockMvc.perform(get("/api/ping")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("up"))
            .andExpect(jsonPath("$.message").value("Server is running"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.version").value("1.0.0"))
    }

    @Test
    fun `health endpoint should return UP status when database is healthy`() {
        // Mock database check to return success
        `when`(jdbcTemplate.queryForObject(anyString(), eq(Int::class.java))).thenReturn(1)

        mockMvc.perform(get("/api/health")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components.db.status").value("UP"))
            .andExpect(jsonPath("$.components.db.details.validation").value("Connection test successful"))
            .andExpect(jsonPath("$.components.application.status").value("UP"))
    }

    @Test
    fun `health endpoint should return DOWN status when database check fails`() {
        // Mock database check to throw exception
        `when`(jdbcTemplate.queryForObject(anyString(), eq(Int::class.java)))
            .thenThrow(RuntimeException("Database connection failed"))

        mockMvc.perform(get("/api/health")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk) // Still returns 200 OK, but with DOWN status
            .andExpect(jsonPath("$.status").value("DOWN"))
            .andExpect(jsonPath("$.components.db.status").value("DOWN"))
            .andExpect(jsonPath("$.components.db.details.validation").value("Connection test failed: Database connection failed"))
    }

    @Test
    fun `health db endpoint should return database status`() {
        // Mock database check to return success
        `when`(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Int::class.java))).thenReturn(1)
        `when`(jdbcTemplate.queryForObject(eq("SELECT count(1) FROM information_schema.tables"), eq(Int::class.java))).thenReturn(42)

        mockMvc.perform(get("/api/health/db")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.details.database").value("PostgreSQL"))
            .andExpect(jsonPath("$.details.validation").value("Connection test successful"))
            .andExpect(jsonPath("$.details.timestamp").exists())
            .andExpect(jsonPath("$.metrics.tables_count").value(42))
    }

    @Test
    fun `health db endpoint should handle connection pool metrics failure gracefully`() {
        // Mock database validation to succeed but metrics query to fail
        `when`(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Int::class.java))).thenReturn(1)
        `when`(jdbcTemplate.queryForObject(
            eq("SELECT count(1) FROM information_schema.tables"), 
            eq(Int::class.java)
        )).thenThrow(RuntimeException("Metrics query failed"))

        mockMvc.perform(get("/api/health/db")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.metrics.error").exists())
    }
} 