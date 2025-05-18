package com.nhatpham.dishcover.domain.model

import com.google.firebase.Timestamp

data class RecipeDto(
    val recipeId: String? = null,
    val userId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    val difficultyLevel: String? = null,
    val coverImage: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null,
    val isPublic: Boolean? = null,
    val viewCount: Int? = null,
    val likeCount: Int? = null,
    val isFeatured: Boolean? = null
)