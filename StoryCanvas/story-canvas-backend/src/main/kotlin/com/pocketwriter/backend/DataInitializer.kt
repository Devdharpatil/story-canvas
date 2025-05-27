package com.pocketwriter.backend

import com.pocketwriter.backend.dto.ArticleCreateRequestDTO
import com.pocketwriter.backend.dto.TemplateCreateRequestDTO
import com.pocketwriter.backend.service.ArticleService
import com.pocketwriter.backend.service.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.dao.DataAccessException

@Component
class DataInitializer(
    private val templateService: TemplateService,
    private val articleService: ArticleService
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        try {
            logger.info("Checking if initial data needs to be seeded...")

            if (templateService.getAllTemplates().isEmpty()) {
                logger.info("Seeding initial templates...")
                seedInitialData()
                logger.info("Data seeding completed successfully.")
            } else {
                logger.info("Initial data already exists. Skipping seed.")
            }
        } catch (e: DataAccessException) {
            logger.error("Database error during data initialization: ${e.message}", e)
            // No need to rethrow - CommandLineRunner errors are logged but don't prevent app startup
        } catch (e: Exception) {
            logger.error("Unexpected error during data initialization: ${e.message}", e)
            // No need to rethrow - CommandLineRunner errors are logged but don't prevent app startup
        }
    }
    
    @Transactional
    private fun seedInitialData() {
        try {
            // Template 1
            val template1DTO = TemplateCreateRequestDTO(
                name = "Simple Blog Post",
                structureDescription = """
                [
                  {"element_id":"title1","type":"text_block","position":{"x_percentage":5,"y_percentage":5},"size":{"width_percentage":90,"height_percentage":10},"default_properties":{"placeholder_text":"Main Title..."}},
                  {"element_id":"image1","type":"image_placeholder","position":{"x_percentage":5,"y_percentage":20},"size":{"width_percentage":90,"height_percentage":30}},
                  {"element_id":"content1","type":"text_block","position":{"x_percentage":5,"y_percentage":55},"size":{"width_percentage":90,"height_percentage":40},"default_properties":{"placeholder_text":"Start writing content..."}}
                ]
                """.trimIndent()
            )
            val createdTemplate1 = templateService.createTemplate(template1DTO)
            logger.info("Created template: ${createdTemplate1.name} with ID ${createdTemplate1.id}")

            // Template 2
            val template2DTO = TemplateCreateRequestDTO(
                name = "Two Column Layout",
                structureDescription = """
                [
                  {"element_id":"col_text_1","type":"text_block","position":{"x_percentage":5,"y_percentage":5},"size":{"width_percentage":43,"height_percentage":90},"default_properties":{"placeholder_text":"Left column text"}},
                  {"element_id":"col_text_2","type":"text_block","position":{"x_percentage":52,"y_percentage":5},"size":{"width_percentage":43,"height_percentage":90},"default_properties":{"placeholder_text":"Right column text"}}
                ]
                """.trimIndent()
            )
            val createdTemplate2 = templateService.createTemplate(template2DTO)
            logger.info("Created template: ${createdTemplate2.name} with ID ${createdTemplate2.id}")

            // Sample Article
            seedSampleArticle(createdTemplate1.id)
            
        } catch (e: Exception) {
            logger.error("Error in seedInitialData()", e)
            throw e  // Rethrow to trigger transaction rollback
        }
    }
    
    @Transactional
    private fun seedSampleArticle(templateId: Long) {
        try {
            // Check if articles already exist
            if (articleService.getAllArticlesForFeed(org.springframework.data.domain.PageRequest.of(0,1)).isEmpty) {
                logger.info("Seeding initial article...")
                val article1DTO = ArticleCreateRequestDTO(
                    title = "Welcome to Pocket Writer!",
                    contentData = """
                    {"article_elements":[
                        {"template_element_id_ref":"title1","type":"text_block","content":{"text":"Hello World!"}},
                        {"template_element_id_ref":"image1","type":"image","content":{"image_url":"https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e"}},
                        {"template_element_id_ref":"content1","type":"text_block","content":{"text":"This is a sample article seeded on startup. You can edit or delete it."}}
                    ]}
                    """.trimIndent(),
                    previewText = "Get started with your first AI-powered content creation experience.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e",
                    templateId = templateId
                )
                val createdArticle1 = articleService.createArticle(article1DTO)
                logger.info("Created article: ${createdArticle1.title} with ID ${createdArticle1.id}")
            } else {
                logger.info("Articles already exist. Skipping article seed.")
            }
        } catch (e: Exception) {
            logger.error("Error in seedSampleArticle()", e)
            throw e  // Rethrow to trigger transaction rollback
        }
    }
}