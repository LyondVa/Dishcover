package com.nhatpham.dishcover.data.model.dto.recipe

data class RecipeIngredientDto(
    val recipeIngredientId: String? = null,
    val recipeId: String? = null,
    val ingredientId: String? = null,
    val quantity: String? = null,
    val unit: String? = null,
    val notes: String? = null,
    val displayOrder: Int? = null
)