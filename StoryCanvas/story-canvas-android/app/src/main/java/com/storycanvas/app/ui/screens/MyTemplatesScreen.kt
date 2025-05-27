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
fun MyTemplatesScreen(
    modifier: Modifier = Modifier,
    onTemplateClick: (Long) -> Unit = {},
    onCreateTemplateClick: () -> Unit = {}
) {
    // This is a placeholder. In a real implementation, we would display a list of templates
    // and provide a way to create a new template.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "My Templates Screen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyTemplatesScreenPreview() {
    StorycanvasandroidTheme {
        MyTemplatesScreen()
    }
} 