package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.RecipeRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(userId: String) =
        recipeRepository.getCategories(userId)
}