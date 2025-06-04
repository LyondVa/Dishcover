package com.nhatpham.dishcover.domain.model.recipe

data class RecipeStep(
    val stepId: String = "",
    val recipeId: String = "",
    val stepNumber: Int = 0,
    val instruction: String = "",
    val duration: Int? = null, // in minutes
    val temperature: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null
)