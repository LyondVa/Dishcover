
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecipeReviewsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String, limit: Int = 20, offset: Int = 0) = 
        recipeRepository.getRecipeReviews(recipeId, limit, offset)
}
