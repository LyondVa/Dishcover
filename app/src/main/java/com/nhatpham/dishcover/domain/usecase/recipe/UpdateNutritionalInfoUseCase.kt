
package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateNutritionalInfoUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(nutritionalInfo: NutritionalInfo) = 
        recipeRepository.updateNutritionalInfo(nutritionalInfo)
}
