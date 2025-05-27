package com.pocketwriter.backend.repository

import com.pocketwriter.backend.entity.Article
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
// Potentially import Page, Pageable for pagination later
// import org.springframework.data.domain.Page
// import org.springframework.data.domain.Pageable

@Repository
interface ArticleRepository : JpaRepository<Article, Long> {
    // Standard CRUD methods will be provided automatically by Spring Data JPA.

    // You can add custom query methods here for more specific needs, for example:
    // fun findByTitleContainingIgnoreCase(title: String, pageable: Pageable): Page<Article>
    // fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<Article>
    // fun findByTemplateId(templateId: Long): List<Article>
}