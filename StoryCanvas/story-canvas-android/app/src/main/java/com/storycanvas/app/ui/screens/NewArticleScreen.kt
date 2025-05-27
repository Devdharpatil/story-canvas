package com.storycanvas.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.storycanvas.app.ui.theme.StorycanvasandroidTheme

@Composable
fun NewArticleScreen(
    modifier: Modifier = Modifier,
    onStartFromScratch: () -> Unit = {},
    onUseTemplate: () -> Unit = {}
) {
    // This is a placeholder. In a real implementation, we would provide options
    // to create a new article from scratch or use a template.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Article",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            Button(
                onClick = onStartFromScratch,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Start from Scratch")
            }
            
            Button(
                onClick = onUseTemplate,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Choose from My Templates")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewArticleScreenPreview() {
    StorycanvasandroidTheme {
        NewArticleScreen()
    }
} 