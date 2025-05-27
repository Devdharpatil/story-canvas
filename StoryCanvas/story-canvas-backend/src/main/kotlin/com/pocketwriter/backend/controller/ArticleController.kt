package com.pocketwriter.backend.controller

import com.pocketwriter.backend.dto.ArticleCreateRequestDTO
import com.pocketwriter.backend.dto.ArticleFeedItemResponseDTO
import com.pocketwriter.backend.dto.ArticleResponseDTO
import com.pocketwriter.backend.dto.ErrorResponseDTO
import com.pocketwriter.backend.service.ArticleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault // For default pagination settings
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Sort

@Tag(name = "Articles API", description = "Endpoints for managing articles")
@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val articleService: ArticleService
) {

    @Operation(summary = "Create a new article", description = "Allows creation of a new article with title, content, and optional template linkage.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Article created successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ArticleResponseDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid input data",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseDTO::class))]),
        ApiResponse(responseCode = "404", description = "Referenced template not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseDTO::class))])
    ])
    @PostMapping
    fun createArticle(@Valid @RequestBody request: ArticleCreateRequestDTO): ResponseEntity<ArticleResponseDTO> {
        val createdArticle = articleService.createArticle(request)
        return ResponseEntity(createdArticle, HttpStatus.CREATED)
    }

    @Operation(summary = "Get all articles (for feed)", description = "Retrieves a paginated list of articles suitable for a feed display.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved list of articles for feed",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Page::class))]) // Page<ArticleFeedItemResponseDTO>
    ])
    @GetMapping // For the feed
    fun getAllArticlesForFeed(
        @PageableDefault(
            size = 10,
            sort = ["createdAt"], // Property name
            direction = Sort.Direction.DESC // Sort direction
        ) pageable: Pageable
    ): ResponseEntity<Page<ArticleFeedItemResponseDTO>> {
        // Spring MVC will automatically construct a Pageable object from request parameters
        // e.g., /api/articles?page=0&size=5&sort=title,asc
        val articlesPage = articleService.getAllArticlesForFeed(pageable)
        return ResponseEntity.ok(articlesPage)
    }

    @Operation(summary = "Get an article by its ID", description = "Retrieves detailed information for a specific article if it exists.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved article",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ArticleResponseDTO::class))]),
        ApiResponse(responseCode = "404", description = "Article not found with the given ID",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseDTO::class))])
    ])
    @GetMapping("/{articleId}") // For getting a single detailed article
    fun getArticleById(@PathVariable articleId: Long): ResponseEntity<ArticleResponseDTO> {
        // Service method now throws ResourceNotFoundException
        val article = articleService.getArticleById(articleId)
        return ResponseEntity.ok(article)
    }
}