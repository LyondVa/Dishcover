
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class CalculateNutritionalInfoUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipe: Recipe) = 
        recipeRepository.calculateNutritionalInfo(recipe)
}
