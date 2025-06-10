package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class CheckRecipeFavoriteStatusUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(userId: String, recipeId: String) =
        recipeRepository.checkRecipeFavoriteStatus(userId, recipeId)
}
