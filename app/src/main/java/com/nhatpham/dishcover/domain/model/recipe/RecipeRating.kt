// RecipeRating.kt
package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class RecipeRating(
    val ratingId: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val rating: Int = 0, // 1-5 stars
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)