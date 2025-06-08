
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.ServingAdjustment
import javax.inject.Inject

class ScaleRecipeIngredientsUseCase @Inject constructor() {
    
    operator fun invoke(recipe: Recipe, targetServings: Int): Recipe {
        if (recipe.servings <= 0 || targetServings <= 0) return recipe
        
        val adjustment = ServingAdjustment(
            originalServings = recipe.servings,
            targetServings = targetServings
        )
        
        val scaledIngredients = recipe.ingredients.map { ingredient ->
            ingredient.copy(
                quantity = adjustment.scaleQuantity(ingredient.quantity)
            )
        }
        
        return recipe.copy(
            ingredients = scaledIngredients,
            servings = targetServings
        )
    }
}
