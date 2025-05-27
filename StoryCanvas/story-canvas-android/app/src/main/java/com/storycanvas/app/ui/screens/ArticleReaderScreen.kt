package com.storycanvas.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.storycanvas.app.ui.theme.StorycanvasandroidTheme

@Composable
fun ArticleReaderScreen(
    articleId: Long,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    // This is a placeholder. In a real implementation, we would have a ViewModel
    // that loads the article content and displays it.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Article Reader Screen\nArticle ID: $articleId",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleReaderScreenPreview() {
    StorycanvasandroidTheme {
        ArticleReaderScreen(articleId = 1)
    }
} 