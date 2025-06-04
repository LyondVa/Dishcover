package com.nhatpham.dishcover.data.model.dto.recipe

data class RecipeStepDto(
    val stepId: String? = null,
    val recipeId: String? = null,
    val stepNumber: Int? = null,
    val instruction: String? = null,
    val duration: Int? = null,
    val temperature: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null
)