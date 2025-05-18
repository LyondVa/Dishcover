package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetFavoriteRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(userId: String, limit: Int = 10) =
        recipeRepository.getFavoriteRecipes(userId, limit)
}