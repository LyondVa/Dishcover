package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class SearchRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(query: String, limit: Int = 20) =
        recipeRepository.searchRecipes(query, limit)
}