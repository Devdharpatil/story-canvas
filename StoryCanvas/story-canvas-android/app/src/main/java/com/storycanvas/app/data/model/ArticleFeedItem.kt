package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object representing a lightweight article item for display in the feed
 */
data class ArticleFeedItem(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("preview_text")
    val previewText: String?,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String // ISO-8601 format
) 