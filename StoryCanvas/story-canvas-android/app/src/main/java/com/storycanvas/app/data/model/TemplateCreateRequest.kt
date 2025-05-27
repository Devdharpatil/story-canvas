package com.storycanvas.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for creating a new template
 */
data class TemplateCreateRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("structure_description")
    val structureDescription: String // JSON string representing the template structure
) 