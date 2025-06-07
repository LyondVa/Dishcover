package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRecipesForSelectionUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(
        userId: String,
        searchQuery: String = "",
        limit: Int = 20
    ): Flow<Resource<List<RecipeListItem>>> {
        return if (searchQuery.isBlank()) {
            recipeRepository.getUserRecipes(userId, limit)
        } else {
            recipeRepository.searchUserRecipes(userId, searchQuery, limit)
        }
    }
}