package com.pocketwriter.backend.controller

import com.pocketwriter.backend.dto.ErrorResponseDTO
import com.pocketwriter.backend.dto.TemplateCreateRequestDTO
import com.pocketwriter.backend.dto.TemplateResponseDTO
import com.pocketwriter.backend.service.TemplateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Templates API", description = "Endpoints for managing content templates")
@RestController // Marks this class as a REST controller, combines @Controller and @ResponseBody
@RequestMapping("/api/templates") // Base path for all endpoints in this controller
class TemplateController(
    private val templateService: TemplateService // Inject the service
) {

    @Operation(summary = "Create a new template", description = "Allows creation of a new content template by providing a name and its structure description in JSON format.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Template created successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TemplateResponseDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid input data (e.g., blank name or structure)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseDTO::class))])
    ])
    @PostMapping // Handles HTTP POST requests to /api/templates
    fun createTemplate(@Valid @RequestBody request: TemplateCreateRequestDTO): ResponseEntity<TemplateResponseDTO> {
        val createdTemplate = templateService.createTemplate(request)
        return ResponseEntity(createdTemplate, HttpStatus.CREATED) // Return 201 Created status
    }

    @Operation(summary = "Get all templates", description = "Retrieves a list of all available templates.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved list of templates",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TemplateResponseDTO::class))])
    ])
    @GetMapping // Handles HTTP GET requests to /api/templates
    fun getAllTemplates(): ResponseEntity<List<TemplateResponseDTO>> {
        val templates = templateService.getAllTemplates()
        return ResponseEntity.ok(templates) // Return 200 OK status
    }

    @Operation(summary = "Get a template by its ID", description = "Retrieves a specific template if it exists using its unique identifier.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved template",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TemplateResponseDTO::class))]),
        ApiResponse(responseCode = "404", description = "Template not found with the given ID",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseDTO::class))])
    ])
    @GetMapping("/{templateId}") // Handles HTTP GET requests to /api/templates/{templateId}
    fun getTemplateById(@PathVariable templateId: Long): ResponseEntity<TemplateResponseDTO> {
        // Service method now throws ResourceNotFoundException, which GlobalExceptionHandler handles
        val template = templateService.getTemplateById(templateId)
        return ResponseEntity.ok(template)
    }
}