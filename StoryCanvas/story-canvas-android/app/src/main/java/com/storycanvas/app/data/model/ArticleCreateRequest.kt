package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for creating a new article
 */
data class ArticleCreateRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("content_data")
    val contentData: String, // JSON string
    
    @SerializedName("preview_text")
    val previewText: String? = null,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("template_id")
    val templateId: Long? = null // Optional, can be null if not based on a template
) 