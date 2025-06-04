package com.nhatpham.dishcover.domain.usecase.recipe

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class UploadRecipeImageUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String, imageData: ByteArray) =
        recipeRepository.uploadRecipeImage(recipeId, imageData)
}