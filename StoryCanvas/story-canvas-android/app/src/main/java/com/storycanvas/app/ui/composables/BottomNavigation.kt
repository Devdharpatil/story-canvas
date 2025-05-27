package com.storycanvas.app.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.storycanvas.app.R
import com.storycanvas.app.navigation.Screen
import com.storycanvas.app.ui.theme.StorycanvasandroidTheme

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    // Add debug logging
    android.util.Log.d("BottomNavigation", "BottomNavigationItems: ${Screen.BottomNavigationItems}")
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Safety check for null list
        if (Screen.BottomNavigationItems.isEmpty()) {
            android.util.Log.e("BottomNavigation", "BottomNavigationItems is empty!")
            return@NavigationBar
        }
        
        Screen.BottomNavigationItems.forEach { screen ->
            // Null safety check for screen
            if (screen == null) {
                android.util.Log.e("BottomNavigation", "Found null screen in BottomNavigationItems")
                return@forEach
            }
            
            // Determine if this item is selected
            val selected = currentRoute == screen.route
            
            // Determine which icon to show based on the screen
            val icon = when (screen) {
                Screen.Feed -> R.drawable.ic_feed
                Screen.MyTemplates -> R.drawable.ic_templates
                Screen.NewArticle -> R.drawable.ic_create
                else -> R.drawable.ic_feed // Default, shouldn't happen with BottomNavigationItems
            }
            
            // Determine the label based on the screen
            val label = when (screen) {
                Screen.Feed -> "Feed"
                Screen.MyTemplates -> "Templates"
                Screen.NewArticle -> "Create"
                else -> "Unknown" // Default, shouldn't happen
            }
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination to avoid building up
                            // a large stack of destinations on the back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when navigating back to a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = icon),
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    StorycanvasandroidTheme {
        BottomNavigationBar(
            navController = rememberNavController(),
            currentRoute = Screen.Feed.route
        )
    }
} 