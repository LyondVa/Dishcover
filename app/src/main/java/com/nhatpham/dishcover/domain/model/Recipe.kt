package com.nhatpham.dishcover.domain.model

import com.google.firebase.Timestamp
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
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val isPublic: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val isFeatured: Boolean,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val tags: List<String> = emptyList()
)
