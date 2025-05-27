package com.pocketwriter.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// DTO for creating a new Template
data class TemplateCreateRequestDTO(
    @field:NotBlank(message = "Template name cannot be blank")
    @field:Size(max = 255, message = "Template name cannot exceed 255 characters")
    val name: String,

    @field:NotBlank(message = "Template structure description cannot be blank")
    // No specific size constraint here as it's JSON text, DB will handle TEXT type
    val structureDescription: String // JSON String
)

// DTO for representing a Template in API responses
data class TemplateResponseDTO(
    val id: Long,
    val name: String,
    val structureDescription: String, // JSON String
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)