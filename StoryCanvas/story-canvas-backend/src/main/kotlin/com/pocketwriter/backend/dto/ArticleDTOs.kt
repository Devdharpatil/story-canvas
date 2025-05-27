package com.pocketwriter.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// DTO for creating a new Article
data class ArticleCreateRequestDTO(
    @field:NotBlank(message = "Article title cannot be blank")
    @field:Size(max = 255, message = "Article title cannot exceed 255 characters")
    val title: String,

    @field:NotBlank(message = "Article content data cannot be blank")
    val contentData: String, // JSON String

    @field:Size(max = 500, message = "Preview text cannot exceed 500 characters")
    val previewText: String? = null,

    @field:Size(max = 2048, message = "Thumbnail URL cannot exceed 2048 characters")
    val thumbnailUrl: String? = null,

    val templateId: Long? = null // Optional: ID of the template used
)

// DTO for representing full Article details in API responses
// (e.g., for GET /api/articles/{id} or as response to POST)
data class ArticleResponseDTO(
    val id: Long,
    val title: String,
    val contentData: String, // JSON String
    val previewText: String?,
    val thumbnailUrl: String?,
    val templateId: Long?, // Can be null if not based on a template
    // val templateName: String? // Optional: could be added by service if templateId is present
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// DTO for representing an Article item in a feed list
// (e.g., for GET /api/articles)
// This is more lightweight, excluding full contentData for efficiency
data class ArticleFeedItemResponseDTO(
    val id: Long,
    val title: String,
    val previewText: String?,
    val thumbnailUrl: String?,
    val createdAt: LocalDateTime
    // val authorName: String? // Example: if you had user information
    // val estimatedReadTime: Int? // Example: for advanced feed features
)