package com.storycanvas.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.storycanvas.app.ui.composables.BottomNavigationBar
import com.storycanvas.app.ui.screens.ArticleCreationScreen
import com.storycanvas.app.ui.screens.ArticleReaderScreen
import com.storycanvas.app.ui.screens.FeedScreen
import com.storycanvas.app.ui.screens.MyTemplatesScreen
import com.storycanvas.app.ui.screens.NewArticleScreen
import com.storycanvas.app.ui.screens.TemplateBuilderScreen

private const val TAG = "AppNavigation"

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Get the current route
    val currentRoute = navBackStackEntry?.destination?.route
    
    // State to track if bottom nav items are valid
    var bottomNavItemsValid by remember { mutableStateOf(true) }
    
    // Check BottomNavigationItems - do this early to prevent late NPE
    val bottomNavItems = remember {
        try {
            // Force initialization and check for null values
            if (Screen.BottomNavigationItems.isEmpty()) {
                Log.e(TAG, "BottomNavigationItems list is empty")
                bottomNavItemsValid = false
            }
            
            // Check each item for null (shouldn't happen with a sealed class but being defensive)
            Screen.BottomNavigationItems.forEach { screen ->
                if (screen == null) {
                    Log.e(TAG, "Found null screen in BottomNavigationItems")
                    bottomNavItemsValid = false
                }
            }
            
            // Return the items if all checks pass
            Screen.BottomNavigationItems
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BottomNavigationItems", e)
            bottomNavItemsValid = false
            // Create a fallback list in case of problems
            listOf(
                Screen.Feed,
                Screen.MyTemplates,
                Screen.NewArticle
            )
        }
    }
    
    // Determine if the current screen is a top-level destination (should show bottom nav)
    val shouldShowBottomNav = when (currentRoute) {
        Screen.Feed.route, Screen.MyTemplates.route, Screen.NewArticle.route -> true
        else -> false
    }
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomNav) {
                // Use a safe pattern instead of try-catch around composable
                SafeBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main screens (visible in bottom navigation)
            composable(Screen.Feed.route) {
                FeedScreen(
                    onArticleClick = { articleId ->
                        navController.navigate(Screen.ArticleReader.createRoute(articleId))
                    }
                )
            }
            
            composable(Screen.MyTemplates.route) {
                MyTemplatesScreen(
                    onTemplateClick = { templateId ->
                        navController.navigate(Screen.TemplateBuilder.createRoute(templateId))
                    },
                    onCreateTemplateClick = {
                        navController.navigate(Screen.TemplateBuilder.DEFAULT_ROUTE)
                    }
                )
            }
            
            composable(Screen.NewArticle.route) {
                NewArticleScreen(
                    onStartFromScratch = {
                        navController.navigate(Screen.ArticleCreation.DEFAULT_ROUTE)
                    },
                    onUseTemplate = {
                        navController.navigate(Screen.MyTemplates.route)
                    }
                )
            }
            
            // Detail screens (not in bottom navigation)
            composable(
                route = Screen.ArticleReader.route,
                arguments = listOf(
                    navArgument("articleId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getLong("articleId") ?: -1L
                ArticleReaderScreen(
                    articleId = articleId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // For template builder - use -1 as a sentinel value for "no template" instead of null
            composable(
                route = Screen.TemplateBuilder.route,
                arguments = listOf(
                    navArgument("templateId") {
                        type = NavType.LongType
                        defaultValue = -1L  // Use -1 as sentinel value for "no template"
                    }
                )
            ) { backStackEntry ->
                val templateIdArg = backStackEntry.arguments?.getLong("templateId") ?: -1L
                // Convert -1 back to null for the screen
                val templateId = if (templateIdArg == -1L) null else templateIdArg
                
                Log.d(TAG, "TemplateBuilder screen with templateId: $templateId")
                
                TemplateBuilderScreen(
                    templateId = templateId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSave = {
                        navController.popBackStack()
                    }
                )
            }
            
            // For article creation - use -1 as a sentinel value for "no template" instead of null
            composable(
                route = Screen.ArticleCreation.route,
                arguments = listOf(
                    navArgument("templateId") {
                        type = NavType.LongType
                        defaultValue = -1L  // Use -1 as sentinel value for "no template"
                    }
                )
            ) { backStackEntry ->
                val templateIdArg = backStackEntry.arguments?.getLong("templateId") ?: -1L
                // Convert -1 back to null for the screen
                val templateId = if (templateIdArg == -1L) null else templateIdArg
                
                Log.d(TAG, "ArticleCreation screen with templateId: $templateId")
                
                ArticleCreationScreen(
                    templateId = templateId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onPublish = {
                        // Navigate to Feed to see the published article
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * A safe wrapper around BottomNavigationBar that handles exceptions gracefully
 * without using try-catch around composable function calls
 */
@Composable
private fun SafeBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    // Use state to track if an error occurred
    var hasError by remember { mutableStateOf(false) }
    
    if (!hasError) {
        try {
            // This is NOT a try-catch around a composable call - this is initialization code
            // that runs before the composable function is called
            // Validate inputs if necessary
            if (navController == null) {
                Log.e(TAG, "NavController is null")
                hasError = true
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during validation for BottomNavigationBar", e)
            hasError = true
            return
        }
        
        // Now we can call the composable function directly without surrounding it with try-catch
        BottomNavigationBar(
            navController = navController,
            currentRoute = currentRoute
        )
    } else {
        // Optionally show a fallback UI if there was an error
        // Or just leave it empty if you prefer
    }
} 