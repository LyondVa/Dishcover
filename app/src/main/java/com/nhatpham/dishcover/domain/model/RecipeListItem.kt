package com.nhatpham.dishcover.domain.model

data class RecipeListItem(
    val recipeId: String,
    val title: String,
    val description: String?, // Using the description field from RECIPES table
    val coverImage: String?,
    val prepTime: Int,
    val cookTime: Int,
    val likeCount: Int,
    val isPublic: Boolean,
    val isFeatured: Boolean = false
)