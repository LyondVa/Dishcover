package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class RecipeListItem(
    val recipeId: String = "",
    val title: String = "",
    val description: String? = null,
    val coverImage: String? = null,
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 0,
    val difficultyLevel: String = "Easy",
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val isPublic: Boolean = true,
    val isFeatured: Boolean = false,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val tags: List<String> = emptyList()
)