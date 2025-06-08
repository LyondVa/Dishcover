// RatingReviewMappers.kt
package com.nhatpham.dishcover.data.mapper

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.model.dto.recipe.*
import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.model.recipe.RecipeReviewInteraction
import com.nhatpham.dishcover.domain.model.recipe.ReviewInteractionType
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo

// RecipeRating mapping
fun RecipeRatingDto.toDomain(): RecipeRating {
    return RecipeRating(
        ratingId = this.ratingId ?: "",
        recipeId = this.recipeId ?: "",
        userId = this.userId ?: "",
        rating = this.rating ?: 0,
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now()
    )
}

fun RecipeRating.toDto(): RecipeRatingDto {
    return RecipeRatingDto(
        ratingId = this.ratingId,
        recipeId = this.recipeId,
        userId = this.userId,
        rating = this.rating,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// RecipeRatingAggregate mapping
fun RecipeRatingAggregateDto.toDomain(): RecipeRatingAggregate {
    return RecipeRatingAggregate(
        recipeId = this.recipeId ?: "",
        averageRating = this.averageRating ?: 0.0,
        totalRatings = this.totalRatings ?: 0,
        ratingDistribution = this.ratingDistribution ?: mapOf(
            1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0
        )
    )
}

fun RecipeRatingAggregate.toDto(): RecipeRatingAggregateDto {
    return RecipeRatingAggregateDto(
        recipeId = this.recipeId,
        averageRating = this.averageRating,
        totalRatings = this.totalRatings,
        ratingDistribution = this.ratingDistribution
    )
}

// RecipeReview mapping
fun RecipeReviewDto.toDomain(): RecipeReview {
    return RecipeReview(
        reviewId = this.reviewId ?: "",
        recipeId = this.recipeId ?: "",
        userId = this.userId ?: "",
        userName = this.userName ?: "",
        userAvatarUrl = this.userAvatarUrl,
        rating = this.rating ?: 0,
        comment = this.comment ?: "",
        images = this.images ?: emptyList(),
        helpful = this.helpful ?: 0,
        verified = this.verified ?: false,
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now()
    )
}

fun RecipeReview.toDto(): RecipeReviewDto {
    return RecipeReviewDto(
        reviewId = this.reviewId,
        recipeId = this.recipeId,
        userId = this.userId,
        userName = this.userName,
        userAvatarUrl = this.userAvatarUrl,
        rating = this.rating,
        comment = this.comment,
        images = this.images,
        helpful = this.helpful,
        verified = this.verified,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// RecipeReviewInteraction mapping
fun RecipeReviewInteractionDto.toDomain(): RecipeReviewInteraction {
    return RecipeReviewInteraction(
        interactionId = this.interactionId ?: "",
        reviewId = this.reviewId ?: "",
        userId = this.userId ?: "",
        type = ReviewInteractionType.valueOf(this.type ?: "HELPFUL"),
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun RecipeReviewInteraction.toDto(): RecipeReviewInteractionDto {
    return RecipeReviewInteractionDto(
        interactionId = this.interactionId,
        reviewId = this.reviewId,
        userId = this.userId,
        type = this.type.name,
        createdAt = this.createdAt
    )
}

// NutritionalInfo mapping
fun NutritionalInfoDto.toDomain(): NutritionalInfo {
    return NutritionalInfo(
        recipeId = this.recipeId ?: "",
        calories = this.calories ?: 0,
        protein = this.protein ?: 0.0,
        carbohydrates = this.carbohydrates ?: 0.0,
        fat = this.fat ?: 0.0,
        fiber = this.fiber ?: 0.0,
        sugar = this.sugar ?: 0.0,
        sodium = this.sodium ?: 0.0,
        cholesterol = this.cholesterol ?: 0.0,
        iron = this.iron ?: 0.0,
        calcium = this.calcium ?: 0.0,
        vitaminC = this.vitaminC ?: 0.0,
        perServing = this.perServing ?: true,
        servingSize = this.servingSize ?: "",
        isEstimated = this.isEstimated ?: true,
        lastCalculated = this.lastCalculated ?: Timestamp.now()
    )
}

fun NutritionalInfo.toDto(): NutritionalInfoDto {
    return NutritionalInfoDto(
        recipeId = this.recipeId,
        calories = this.calories,
        protein = this.protein,
        carbohydrates = this.carbohydrates,
        fat = this.fat,
        fiber = this.fiber,
        sugar = this.sugar,
        sodium = this.sodium,
        cholesterol = this.cholesterol,
        iron = this.iron,
        calcium = this.calcium,
        vitaminC = this.vitaminC,
        perServing = this.perServing,
        servingSize = this.servingSize,
        isEstimated = this.isEstimated,
        lastCalculated = this.lastCalculated
    )
}

// Data Transfer Objects
data class RecipeRatingDto(
    val ratingId: String? = null,
    val recipeId: String? = null,
    val userId: String? = null,
    val rating: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class RecipeRatingAggregateDto(
    val recipeId: String? = null,
    val averageRating: Double? = null,
    val totalRatings: Int? = null,
    val ratingDistribution: Map<Int, Int>? = null
)

data class RecipeReviewDto(
    val reviewId: String? = null,
    val recipeId: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val userAvatarUrl: String? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val images: List<String>? = null,
    val helpful: Int? = null,
    val verified: Boolean? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class RecipeReviewInteractionDto(
    val interactionId: String? = null,
    val reviewId: String? = null,
    val userId: String? = null,
    val type: String? = null,
    val createdAt: Timestamp? = null
)

data class NutritionalInfoDto(
    val recipeId: String? = null,
    val calories: Int? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val cholesterol: Double? = null,
    val iron: Double? = null,
    val calcium: Double? = null,
    val vitaminC: Double? = null,
    val perServing: Boolean? = null,
    val servingSize: String? = null,
    val isEstimated: Boolean? = null,
    val lastCalculated: Timestamp? = null
)