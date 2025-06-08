// RecipeReview.kt
package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class RecipeReview(
    val reviewId: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val rating: Int = 0,
    val comment: String = "",
    val images: List<String> = emptyList(),
    val helpful: Int = 0,
    val verified: Boolean = false, // User actually cooked the recipe
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)