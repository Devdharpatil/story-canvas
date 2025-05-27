package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object representing a complete article response from the API
 */
data class ArticleResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("content_data")
    val contentData: String, // JSON string containing article content structure
    
    @SerializedName("preview_text")
    val previewText: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    
    @SerializedName("template_id")
    val templateId: Long?,
    
    @SerializedName("created_at")
    val createdAt: String, // ISO-8601 format
    
    @SerializedName("updated_at")
    val updatedAt: String // ISO-8601 format
) 