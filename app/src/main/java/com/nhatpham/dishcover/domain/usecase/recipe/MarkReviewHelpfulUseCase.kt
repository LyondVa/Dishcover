
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class MarkReviewHelpfulUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(reviewId: String, userId: String, helpful: Boolean) = 
        recipeRepository.markReviewHelpful(reviewId, userId, helpful)
}
