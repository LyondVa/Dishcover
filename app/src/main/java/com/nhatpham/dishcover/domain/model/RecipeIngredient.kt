package com.nhatpham.dishcover.domain.model

data class RecipeIngredient(
    val recipeIngredientId: String,
    val recipeId: String,
    val ingredientId: String,
    val quantity: String,
    val unit: String,
    val notes: String?,
    val displayOrder: Int,
    val ingredient: Ingredient
)
