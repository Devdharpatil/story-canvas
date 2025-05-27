package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * Data Transfer Object representing a template response from the API
 */
data class TemplateResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("structure_description")
    val structureDescription: String, // JSON string
    
    @SerializedName("created_at")
    val createdAt: String, // ISO-8601 format
    
    @SerializedName("updated_at")
    val updatedAt: String // ISO-8601 format
) 