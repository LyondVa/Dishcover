package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class MarkRecipeAsFavoriteUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(userId: String, recipeId: String, isFavorite: Boolean) =
        recipeRepository.markRecipeAsFavorite(userId, recipeId, isFavorite)
}