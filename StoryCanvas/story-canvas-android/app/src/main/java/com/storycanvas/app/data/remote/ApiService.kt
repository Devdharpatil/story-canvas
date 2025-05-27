package com.storycanvas.app.data.remote

import com.storycanvas.app.data.model.ArticleCreateRequest
import com.storycanvas.app.data.model.ArticleFeedPage
import com.storycanvas.app.data.model.ArticleResponse
import com.storycanvas.app.data.model.TemplateCreateRequest
import com.storycanvas.app.data.model.TemplateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Health Check ---
    @GET("ping")
    suspend fun getPingStatus(): Response<Map<String, String>>

    // --- Template Endpoints ---

    @POST("templates")
    suspend fun createTemplate(@Body templateCreateRequest: TemplateCreateRequest): Response<TemplateResponse>

    @GET("templates")
    suspend fun getAllTemplates(): Response<List<TemplateResponse>>

    @GET("templates/{templateId}")
    suspend fun getTemplateById(@Path("templateId") templateId: Long): Response<TemplateResponse>

    // --- Article Endpoints ---

    @POST("articles")
    suspend fun createArticle(@Body articleCreateRequest: ArticleCreateRequest): Response<ArticleResponse>

    @GET("articles")
    suspend fun getAllArticlesForFeed(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sortBy") sortBy: String? = "created_at",
        @Query("sortDir") sortDir: String? = "desc"
    ): Response<ArticleFeedPage>

    @GET("articles/{articleId}")
    suspend fun getArticleById(@Path("articleId") articleId: Long): Response<ArticleResponse>
} 