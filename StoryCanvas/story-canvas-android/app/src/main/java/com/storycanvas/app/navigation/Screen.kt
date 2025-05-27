package com.storycanvas.app.navigation

import android.util.Log

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    // Main navigation destinations
    object Feed : Screen("feed")
    object MyTemplates : Screen("my_templates")
    object NewArticle : Screen("new_article")
    
    // Detail screens
    object ArticleReader : Screen("article_reader/{articleId}") {
        fun createRoute(articleId: Long) = "article_reader/$articleId"
    }
    
    object TemplateBuilder : Screen("template_builder/{templateId}") {
        const val DEFAULT_ROUTE = "template_builder/-1"
        fun createRoute(templateId: Long? = null) = "template_builder/${templateId ?: -1L}"
    }
    
    object ArticleCreation : Screen("article_creation/{templateId}") {
        const val DEFAULT_ROUTE = "article_creation/-1"
        fun createRoute(templateId: Long? = null) = "article_creation/${templateId ?: -1L}"
    }
    
    // Bottom navigation items (subset of main destinations that appear in bottom nav)
    companion object {
        // Initialize with non-null values
        val BottomNavigationItems = listOf(
            Feed,
            MyTemplates,
            NewArticle
        ).also {
            // Log for debugging
            Log.d("Screen", "BottomNavigationItems initialized with ${it.size} items")
        }
    }
} 