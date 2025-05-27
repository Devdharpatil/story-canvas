package com.pocketwriter.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
data class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 255)
    var title: String,

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    var contentData: String, // Stores the JSON string as per data_models.md

    @Column(nullable = true, length = 500) // Preview can be optional
    var previewText: String? = null,

    @Column(nullable = true, length = 2048) // URL can be long
    var thumbnailUrl: String? = null,

    // Many-to-One relationship with Template (Lazy loaded by default)
    // An article can optionally belong to one template.
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetching is generally good practice
    @JoinColumn(name = "template_id", referencedColumnName = "id", nullable = true) // Defines the foreign key column
    var template: Template? = null, // Nullable if an article can exist without a template

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)