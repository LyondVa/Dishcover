package com.nhatpham.dishcover.domain.model

import java.util.Date

data class Recipe(
    val recipeId: String,
    val userId: String,
    val title: String,
    val description: String?,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val instructions: String,
    val difficultyLevel: String,
    val coverImage: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val isPublic: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val isFeatured: Boolean,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val tags: List<String> = emptyList()
)
