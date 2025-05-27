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
fun TemplateBuilderScreen(
    templateId: Long? = null,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // This is a placeholder. In a real implementation, we would load the template
    // if templateId is provided, and allow editing/creation of a template.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val screenTitle = if (templateId == null) {
            "Create New Template"
        } else {
            "Edit Template ID: $templateId"
        }
        
        Text(
            text = screenTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateBuilderScreenPreviewNew() {
    StorycanvasandroidTheme {
        TemplateBuilderScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateBuilderScreenPreviewEdit() {
    StorycanvasandroidTheme {
        TemplateBuilderScreen(templateId = 1)
    }
} 