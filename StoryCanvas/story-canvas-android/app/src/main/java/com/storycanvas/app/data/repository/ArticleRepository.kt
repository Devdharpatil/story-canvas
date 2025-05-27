package com.storycanvas.app.data.repository

import com.storycanvas.app.data.model.ArticleCreateRequest
import com.storycanvas.app.data.model.ArticleFeedItem
import com.storycanvas.app.data.model.ArticleResponse
import com.storycanvas.app.data.remote.ArticleRemoteDataSource
import com.storycanvas.app.util.Result

/**
 * Repository interface for Article operations
 */
interface ArticleRepository {
    suspend fun getArticlesForFeed(
        page: Int = 0, 
        size: Int = 10
    ): Result<List<ArticleFeedItem>>
    
    suspend fun getArticleById(articleId: Long): Result<ArticleResponse>
    
    suspend fun createArticle(
        title: String,
        contentData: String,
        previewText: String? = null,
        thumbnailUrl: String? = null,
        templateId: Long? = null
    ): Result<ArticleResponse>
}

/**
 * Implementation of the ArticleRepository
 */
class ArticleRepositoryImpl(
    private val remoteDataSource: ArticleRemoteDataSource = ArticleRemoteDataSource()
) : ArticleRepository {
    
    override suspend fun getArticlesForFeed(page: Int, size: Int): Result<List<ArticleFeedItem>> {
        return try {
            val response = remoteDataSource.getArticlesForFeed(page, size)
            if (response.isSuccessful) {
                val articlePage = response.body()
                if (articlePage != null) {
                    Result.Success(articlePage.content)
                } else {
                    Result.Error("Empty response body")
                }
            } else {
                Result.Error("Failed to fetch articles: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    override suspend fun getArticleById(articleId: Long): Result<ArticleResponse> {
        return try {
            val response = remoteDataSource.getArticleById(articleId)
            if (response.isSuccessful) {
                val article = response.body()
                if (article != null) {
                    Result.Success(article)
                } else {
                    Result.Error("Article not found")
                }
            } else {
                Result.Error("Failed to fetch article: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    override suspend fun createArticle(
        title: String,
        contentData: String,
        previewText: String?,
        thumbnailUrl: String?,
        templateId: Long?
    ): Result<ArticleResponse> {
        return try {
            val request = ArticleCreateRequest(title, contentData, previewText, thumbnailUrl, templateId)
            val response = remoteDataSource.createArticle(request)
            if (response.isSuccessful) {
                val createdArticle = response.body()
                if (createdArticle != null) {
                    Result.Success(createdArticle)
                } else {
                    Result.Error("Empty response body")
                }
            } else {
                Result.Error("Failed to create article: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
} 