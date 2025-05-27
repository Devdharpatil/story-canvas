package com.storycanvas.app.data.repository

import com.storycanvas.app.data.model.TemplateCreateRequest
import com.storycanvas.app.data.model.TemplateResponse
import com.storycanvas.app.data.remote.TemplateRemoteDataSource
import com.storycanvas.app.util.Result

/**
 * Repository interface for Template operations
 */
interface TemplateRepository {
    suspend fun getAllTemplates(): Result<List<TemplateResponse>>
    suspend fun getTemplateById(templateId: Long): Result<TemplateResponse>
    suspend fun createTemplate(name: String, structureDescription: String): Result<TemplateResponse>
}

/**
 * Implementation of the TemplateRepository
 */
class TemplateRepositoryImpl(
    private val remoteDataSource: TemplateRemoteDataSource = TemplateRemoteDataSource()
) : TemplateRepository {
    
    override suspend fun getAllTemplates(): Result<List<TemplateResponse>> {
        return try {
            val response = remoteDataSource.getAllTemplates()
            if (response.isSuccessful) {
                val templates = response.body()
                if (templates != null) {
                    Result.Success(templates)
                } else {
                    Result.Error("Empty response body")
                }
            } else {
                Result.Error("Failed to fetch templates: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    override suspend fun getTemplateById(templateId: Long): Result<TemplateResponse> {
        return try {
            val response = remoteDataSource.getTemplateById(templateId)
            if (response.isSuccessful) {
                val template = response.body()
                if (template != null) {
                    Result.Success(template)
                } else {
                    Result.Error("Template not found")
                }
            } else {
                Result.Error("Failed to fetch template: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    override suspend fun createTemplate(name: String, structureDescription: String): Result<TemplateResponse> {
        return try {
            val request = TemplateCreateRequest(name, structureDescription)
            val response = remoteDataSource.createTemplate(request)
            if (response.isSuccessful) {
                val createdTemplate = response.body()
                if (createdTemplate != null) {
                    Result.Success(createdTemplate)
                } else {
                    Result.Error("Empty response body")
                }
            } else {
                Result.Error("Failed to create template: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
} 