package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.util.error.Result
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getFavoriteRecipes(userId: String, limit: Int = 10): Flow<Result<List<RecipeListItem>>>
    fun getRecentRecipes(userId: String, limit: Int = 10): Flow<Result<List<RecipeListItem>>>
    fun getRecipesByCategory(userId: String, category: String, limit: Int = 10): Flow<Result<List<RecipeListItem>>>
    fun getAllRecipes(userId: String, limit: Int = 10): Flow<Result<List<RecipeListItem>>>
    fun getRecipe(recipeId: String): Flow<Result<Recipe>>
    fun searchRecipes(query: String, limit: Int = 20): Flow<Result<List<RecipeListItem>>>
    fun getCategories(userId: String): Flow<Result<List<String>>>
}
