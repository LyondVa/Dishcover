
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetUserReviewForRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String, userId: String) = 
        recipeRepository.getUserReviewForRecipe(recipeId, userId)
}
