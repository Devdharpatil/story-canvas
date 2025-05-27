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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.storycanvas.app.ui.theme.StorycanvasandroidTheme
import com.storycanvas.app.viewmodel.FeedViewModel

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = viewModel(),
    onArticleClick: (Long) -> Unit = {}
) {
    // This is a placeholder. In a real implementation, we would observe the ViewModel's state
    // and display a list of articles using LazyColumn.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Feed Screen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    StorycanvasandroidTheme {
        FeedScreen()
    }
} 