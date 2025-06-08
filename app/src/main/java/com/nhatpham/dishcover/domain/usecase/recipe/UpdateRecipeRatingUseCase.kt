
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateRecipeRatingUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(rating: RecipeRating) = 
        recipeRepository.updateRecipeRating(rating)
}
