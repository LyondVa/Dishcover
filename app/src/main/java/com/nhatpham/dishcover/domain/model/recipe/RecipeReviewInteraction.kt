// RecipeReviewInteraction.kt
package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class RecipeReviewInteraction(
    val interactionId: String = "",
    val reviewId: String = "",
    val userId: String = "",
    val type: ReviewInteractionType = ReviewInteractionType.HELPFUL,
    val createdAt: Timestamp = Timestamp.now()
)