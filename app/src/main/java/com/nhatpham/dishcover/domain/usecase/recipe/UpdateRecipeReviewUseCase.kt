
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateRecipeReviewUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(review: RecipeReview) = 
        recipeRepository.updateRecipeReview(review)
}
