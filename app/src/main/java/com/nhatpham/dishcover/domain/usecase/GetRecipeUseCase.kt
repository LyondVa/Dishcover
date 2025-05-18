package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String) =
        recipeRepository.getRecipe(recipeId)
}
