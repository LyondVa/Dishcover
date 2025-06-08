
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeReviewUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(reviewId: String, userId: String) = 
        recipeRepository.deleteRecipeReview(reviewId, userId)
}
