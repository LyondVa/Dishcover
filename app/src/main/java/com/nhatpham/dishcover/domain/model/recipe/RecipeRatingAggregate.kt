// RecipeRatingAggregate.kt
package com.nhatpham.dishcover.domain.model.recipe

data class RecipeRatingAggregate(
    val recipeId: String = "",
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val ratingDistribution: Map<Int, Int> = mapOf(
        1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0
    )
)