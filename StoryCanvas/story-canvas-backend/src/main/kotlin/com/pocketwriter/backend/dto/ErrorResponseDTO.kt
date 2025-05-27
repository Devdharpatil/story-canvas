package com.pocketwriter.backend.dto

import java.time.LocalDateTime

data class ErrorResponseDTO(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String, // General error category e.g., "Not Found", "Validation Error"
    val message: String?, // Specific error message
    val path: String? = null // The path where the error occurred (optional)
)