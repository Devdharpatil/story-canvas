package com.pocketwriter.backend.service

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.pocketwriter.backend.dto.ArticleCreateRequestDTO
import com.pocketwriter.backend.entity.Article
import com.pocketwriter.backend.entity.Template
import com.pocketwriter.backend.repository.ArticleRepository
import com.pocketwriter.backend.repository.TemplateRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ArticleServiceTests {

    @Mock
    private lateinit var articleRepository: ArticleRepository

    @Mock
    private lateinit var templateRepository: TemplateRepository

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @InjectMocks
    private lateinit var articleService: ArticleService

    private val now = LocalDateTime.now()
    private val validJson = """{"article_elements":[{"element_id":"1","type":"text_block","content":{"text":"Test"}}]}"""
    private val invalidJson = "{invalid json"
    private val complexValidJson = """
        {
          "article_elements": [
            {
              "element_id": "header-1",
              "type": "text_block", 
              "content": {
                "text": "My Amazing Article"
              },
              "properties": {
                "font_size": "24px",
                "text_align": "center"
              }
            },
            {
              "element_id": "img-1",
              "type": "image",
              "content": {
                "image_url": "https://example.com/image.jpg",
                "alt_text": "Beautiful landscape"
              }
            },
            {
              "element_id": "para-1",
              "type": "text_block",
              "content": {
                "text": "This is the main content of my article. It's very informative."
              }
            }
          ]
        }
    """.trimIndent()

    @BeforeEach
    fun setup() {
        // Setup for objectMapper to handle valid and invalid JSON
        `when`(objectMapper.readTree(validJson)).thenReturn(mock(com.fasterxml.jackson.databind.JsonNode::class.java))
        `when`(objectMapper.readTree(invalidJson)).thenThrow(JsonParseException::class.java)
        `when`(objectMapper.readTree(complexValidJson)).thenReturn(mock(com.fasterxml.jackson.databind.JsonNode::class.java))
    }

    @Test
    fun `createArticle should save valid article with valid JSON content`() {
        // Arrange
        val templateId = 1L
        val template = Template(id = templateId, name = "Test Template", structureDescription = "[]")
        val articleRequest = ArticleCreateRequestDTO(
            title = "Test Article",
            contentData = validJson,
            previewText = "Test preview",
            thumbnailUrl = "https://example.com/image.jpg",
            templateId = templateId
        )
        
        val savedArticle = Article(
            id = 1L,
            title = "Test Article",
            contentData = validJson,
            previewText = "Test preview",
            thumbnailUrl = "https://example.com/image.jpg",
            template = template,
            createdAt = now,
            updatedAt = now
        )
        
        `when`(templateRepository.findByIdOrNull(templateId)).thenReturn(template)
        `when`(articleRepository.save(any(Article::class.java))).thenReturn(savedArticle)
        
        // Act
        val result = articleService.createArticle(articleRequest)
        
        // Assert
        assertEquals(1L, result.id)
        assertEquals("Test Article", result.title)
        assertEquals(validJson, result.contentData)
        assertEquals("Test preview", result.previewText)
        assertEquals("https://example.com/image.jpg", result.thumbnailUrl)
        assertEquals(templateId, result.templateId)
        
        verify(objectMapper).readTree(validJson) // Verify JSON validation was called
        verify(templateRepository).findByIdOrNull(templateId)
        verify(articleRepository).save(any(Article::class.java))
    }
    
    @Test
    fun `createArticle should throw exception when JSON content is invalid`() {
        // Arrange
        val articleRequest = ArticleCreateRequestDTO(
            title = "Test Article",
            contentData = invalidJson,
            previewText = "Test preview"
        )
        
        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            articleService.createArticle(articleRequest)
        }
        
        assertTrue(exception.message?.contains("Content data must be a valid JSON") == true)
        verify(objectMapper).readTree(invalidJson) // Verify JSON validation was called
        verify(articleRepository, never()).save(any(Article::class.java)) // Verify save was never called
    }
    
    @Test
    fun `createArticle should handle complex valid JSON structure`() {
        // Arrange
        val articleRequest = ArticleCreateRequestDTO(
            title = "Complex Article",
            contentData = complexValidJson,
            previewText = "Complex article with nested JSON structure"
        )
        
        val savedArticle = Article(
            id = 2L,
            title = "Complex Article",
            contentData = complexValidJson,
            previewText = "Complex article with nested JSON structure",
            thumbnailUrl = null,
            template = null,
            createdAt = now,
            updatedAt = now
        )
        
        `when`(articleRepository.save(any(Article::class.java))).thenReturn(savedArticle)
        
        // Act
        val result = articleService.createArticle(articleRequest)
        
        // Assert
        assertEquals(2L, result.id)
        assertEquals("Complex Article", result.title)
        assertEquals(complexValidJson, result.contentData)
        
        verify(objectMapper).readTree(complexValidJson) // Verify JSON validation was called
        verify(articleRepository).save(any(Article::class.java))
    }
    
    @Test
    fun `getArticleById should return article when it exists`() {
        // Arrange
        val articleId = 1L
        val article = Article(
            id = articleId,
            title = "Test Article",
            contentData = validJson,
            previewText = "Test preview",
            thumbnailUrl = "https://example.com/image.jpg",
            template = null,
            createdAt = now,
            updatedAt = now
        )
        
        `when`(articleRepository.findByIdOrNull(articleId)).thenReturn(article)
        
        // Act
        val result = articleService.getArticleById(articleId)
        
        // Assert
        assertEquals(articleId, result.id)
        assertEquals("Test Article", result.title)
        assertEquals(validJson, result.contentData)
        assertEquals("Test preview", result.previewText)
        assertEquals("https://example.com/image.jpg", result.thumbnailUrl)
        assertNull(result.templateId)
    }
} 