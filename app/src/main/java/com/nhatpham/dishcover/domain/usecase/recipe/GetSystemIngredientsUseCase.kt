package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetSystemIngredientsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke() = recipeRepository.getSystemIngredients()
}