package com.storycanvas.app.data.remote

import com.storycanvas.app.data.model.ArticleCreateRequest
import com.storycanvas.app.data.model.ArticleFeedPage
import com.storycanvas.app.data.model.ArticleResponse
import retrofit2.Response

/**
 * Remote data source for Article-related API operations
 */
class ArticleRemoteDataSource {
    private val apiService = RetrofitClient.instance

    /**
     * Fetch articles for the feed with pagination
     */
    suspend fun getArticlesForFeed(
        page: Int = 0,
        size: Int = 10,
        sortBy: String? = "created_at",
        sortDir: String? = "desc"
    ): Response<ArticleFeedPage> {
        return apiService.getAllArticlesForFeed(page, size, sortBy, sortDir)
    }

    /**
     * Get a specific article by ID
     */
    suspend fun getArticleById(articleId: Long): Response<ArticleResponse> {
        return apiService.getArticleById(articleId)
    }

    /**
     * Create a new article
     */
    suspend fun createArticle(articleCreateRequest: ArticleCreateRequest): Response<ArticleResponse> {
        return apiService.createArticle(articleCreateRequest)
    }
} 