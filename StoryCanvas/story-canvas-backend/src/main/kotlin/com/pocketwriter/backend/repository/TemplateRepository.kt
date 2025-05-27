package com.pocketwriter.backend.repository

import com.pocketwriter.backend.entity.Template
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository // Optional but good practice to denote it as a Spring-managed repository bean
interface TemplateRepository : JpaRepository<Template, Long> {
    // Spring Data JPA will automatically provide methods like:
    // - save(template: Template): Template
    // - findById(id: Long): Optional<Template>
    // - findAll(): List<Template>
    // - deleteById(id: Long)
    // - etc.

    // You can add custom query methods here if needed later
    // For example:
    // fun findByName(name: String): Template?
}