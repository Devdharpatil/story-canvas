package com.storycanvas.app.data.remote

import com.storycanvas.app.data.model.TemplateCreateRequest
import com.storycanvas.app.data.model.TemplateResponse
import retrofit2.Response

/**
 * Remote data source for Template-related API operations
 */
class TemplateRemoteDataSource {
    private val apiService = RetrofitClient.instance

    /**
     * Fetch all templates from the API
     */
    suspend fun getAllTemplates(): Response<List<TemplateResponse>> {
        return apiService.getAllTemplates()
    }

    /**
     * Get a specific template by ID
     */
    suspend fun getTemplateById(templateId: Long): Response<TemplateResponse> {
        return apiService.getTemplateById(templateId)
    }

    /**
     * Create a new template
     */
    suspend fun createTemplate(templateCreateRequest: TemplateCreateRequest): Response<TemplateResponse> {
        return apiService.createTemplate(templateCreateRequest)
    }
} 