package com.pocketwriter.backend.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.pocketwriter.backend.dto.ArticleCreateRequestDTO
import com.pocketwriter.backend.dto.ArticleFeedItemResponseDTO
import com.pocketwriter.backend.dto.ArticleResponseDTO
import com.pocketwriter.backend.entity.Article
import com.pocketwriter.backend.repository.ArticleRepository
import com.pocketwriter.backend.repository.TemplateRepository // To fetch Template if templateId is provided
import com.pocketwriter.backend.exception.ResourceNotFoundException // Add this import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val templateRepository: TemplateRepository, // Inject if needed to validate templateId or fetch template info
    private val objectMapper: ObjectMapper // Inject ObjectMapper for JSON validation
) {
    private val logger = LoggerFactory.getLogger(ArticleService::class.java)

    @Transactional
    fun createArticle(request: ArticleCreateRequestDTO): ArticleResponseDTO {
        // Validate that contentData is a valid JSON
        validateContentDataJson(request.contentData)
        
        val templateEntity = request.templateId?.let { templateId ->
            templateRepository.findByIdOrNull(templateId)
                ?: throw ResourceNotFoundException("Template with ID $templateId not found while creating article")
        }

        val newArticle = Article(
            title = request.title,
            contentData = request.contentData,
            previewText = request.previewText,
            thumbnailUrl = request.thumbnailUrl,
            template = templateEntity // Assign the fetched Template entity, or null
        )

        val savedArticle = articleRepository.save(newArticle)
        return mapToArticleResponseDTO(savedArticle)
    }

    // Validate that the content data is a valid JSON
    private fun validateContentDataJson(contentData: String) {
        try {
            // Parse the JSON string to verify it's valid JSON
            objectMapper.readTree(contentData)
        } catch (e: JsonProcessingException) {
            logger.error("Invalid JSON content data: ${e.message}")
            throw IllegalArgumentException("Content data must be a valid JSON: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun getAllArticlesForFeed(pageable: Pageable): Page<ArticleFeedItemResponseDTO> {
        // For now, just fetching all and mapping. Pagination is handled by Spring Data JPA.
        // We might add sorting later (e.g., by createdAt desc)
        val articlesPage: Page<Article> = articleRepository.findAll(pageable) //findAll already supports Pageable
        return articlesPage.map { mapToArticleFeedItemResponseDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getArticleById(id: Long): ArticleResponseDTO {
        val article = articleRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Article with ID $id not found")
        return mapToArticleResponseDTO(article)
    }


    // Helper mapping functions
    private fun mapToArticleResponseDTO(article: Article): ArticleResponseDTO {
        return ArticleResponseDTO(
            id = article.id!!,
            title = article.title,
            contentData = article.contentData,
            previewText = article.previewText,
            thumbnailUrl = article.thumbnailUrl,
            templateId = article.template?.id, // Safely access template's id
            createdAt = article.createdAt!!,
            updatedAt = article.updatedAt!!
        )
    }

    private fun mapToArticleFeedItemResponseDTO(article: Article): ArticleFeedItemResponseDTO {
        return ArticleFeedItemResponseDTO(
            id = article.id!!,
            title = article.title,
            previewText = article.previewText,
            thumbnailUrl = article.thumbnailUrl,
            createdAt = article.createdAt!!
        )
    }
}