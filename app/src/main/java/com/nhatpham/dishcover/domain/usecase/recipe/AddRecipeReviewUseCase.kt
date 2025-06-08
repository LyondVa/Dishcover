
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class AddRecipeReviewUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(
        recipeId: String,
        userId: String,
        userName: String,
        rating: Int,
        comment: String,
        images: List<String> = emptyList(),
        verified: Boolean = false
    ) = recipeRepository.addRecipeReview(
        RecipeReview(
            recipeId = recipeId,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment,
            images = images,
            verified = verified
        )
    )
}
