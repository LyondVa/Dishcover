package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class CreateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipe: Recipe) =
        recipeRepository.createRecipe(recipe)
}