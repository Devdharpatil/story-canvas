package com.pocketwriter.backend.service

import com.pocketwriter.backend.dto.TemplateCreateRequestDTO
import com.pocketwriter.backend.dto.TemplateResponseDTO
import com.pocketwriter.backend.entity.Template
import com.pocketwriter.backend.repository.TemplateRepository
import org.springframework.data.repository.findByIdOrNull // Good utility for Optionals
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional // For methods that modify data
import com.pocketwriter.backend.exception.ResourceNotFoundException

@Service // Marks this class as a Spring service component
class TemplateService(
    private val templateRepository: TemplateRepository // Inject the repository via constructor
) {

    @Transactional // Methods that create, update, or delete should be transactional
    fun createTemplate(request: TemplateCreateRequestDTO): TemplateResponseDTO {
        // 1. Convert DTO to Entity
        val newTemplate = Template(
            name = request.name,
            structureDescription = request.structureDescription
            // createdAt and updatedAt will be set by @CreationTimestamp and @UpdateTimestamp
        )

        // 2. Save Entity using Repository
        val savedTemplate = templateRepository.save(newTemplate)

        // 3. Convert saved Entity back to Response DTO
        return mapToTemplateResponseDTO(savedTemplate)
    }

    @Transactional(readOnly = true) // readOnly = true can optimize read operations
    fun getAllTemplates(): List<TemplateResponseDTO> {
        return templateRepository.findAll().map { mapToTemplateResponseDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getTemplateById(id: Long): TemplateResponseDTO { // Return non-nullable
        val template = templateRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Template with ID $id not found")
        return mapToTemplateResponseDTO(template)
    }

    // Helper function to map Entity to DTO (can be private or in a separate mapper class/file)
    private fun mapToTemplateResponseDTO(template: Template): TemplateResponseDTO {
        return TemplateResponseDTO(
            id = template.id!!, // id will not be null for a persisted entity
            name = template.name,
            structureDescription = template.structureDescription,
            createdAt = template.createdAt!!, // Timestamps will not be null
            updatedAt = template.updatedAt!!
        )
    }
}