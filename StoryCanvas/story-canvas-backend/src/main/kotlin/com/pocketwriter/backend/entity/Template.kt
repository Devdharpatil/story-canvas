package com.pocketwriter.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "templates") // Optional: specifies the table name, otherwise it defaults based on class name
data class Template(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY for auto-increment with PostgreSQL
    var id: Long? = null, // Nullable because it's null before being persisted and generated

    @Column(nullable = false, length = 255)
    var name: String,

    @Lob // For potentially large JSON string, maps to TEXT or CLOB
    @Column(nullable = false, columnDefinition = "TEXT") // Explicitly define as TEXT for large strings
    var structureDescription: String, // Stores the JSON string as per data_models.md

    @CreationTimestamp // Automatically sets the value on creation
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp // Automatically sets the value on creation and on each update
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)