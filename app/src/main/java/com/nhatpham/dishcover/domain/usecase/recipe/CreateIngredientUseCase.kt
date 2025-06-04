package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.Ingredient
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class CreateIngredientUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(ingredient: Ingredient) =
        recipeRepository.createIngredient(ingredient)
}