package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecipesByCategoryUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(userId: String, category: String, limit: Int = 10) =
        recipeRepository.getRecipesByCategory(userId, category, limit)
}