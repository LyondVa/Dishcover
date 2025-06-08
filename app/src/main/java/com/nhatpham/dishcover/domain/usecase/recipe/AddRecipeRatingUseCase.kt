
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class AddRecipeRatingUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(
        recipeId: String,
        userId: String, 
        rating: Int
    ) = recipeRepository.addRecipeRating(
        RecipeRating(
            recipeId = recipeId,
            userId = userId,
            rating = rating
        )
    )
}
