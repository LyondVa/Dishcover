package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipe: Recipe) =
        recipeRepository.updateRecipe(recipe)
}