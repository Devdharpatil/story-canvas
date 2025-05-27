package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for paginated article feed responses
 */
data class ArticleFeedPage(
    @SerializedName("content")
    val content: List<ArticleFeedItem>,
    
    @SerializedName("pageable")
    val pageable: Pageable,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("totalElements")
    val totalElements: Int,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int, // Current page number
    
    @SerializedName("sort")
    val sort: Sort,
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("empty")
    val empty: Boolean
) {
    /**
     * Pagination information from Spring Boot's Page response
     */
    data class Pageable(
        @SerializedName("pageNumber")
        val pageNumber: Int,
        
        @SerializedName("pageSize")
        val pageSize: Int,
        
        @SerializedName("sort")
        val sort: Sort
    )
    
    /**
     * Sorting information from Spring Boot's Page response
     */
    data class Sort(
        @SerializedName("sorted")
        val sorted: Boolean,
        
        @SerializedName("unsorted")
        val unsorted: Boolean,
        
        @SerializedName("empty")
        val empty: Boolean
    )
} 