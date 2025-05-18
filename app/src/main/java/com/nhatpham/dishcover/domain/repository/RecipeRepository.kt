package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getFavoriteRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecentRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecipesByCategory(userId: String, category: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getAllRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecipe(recipeId: String): Flow<Resource<Recipe>>
    fun searchRecipes(query: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>>
    fun getCategories(userId: String): Flow<Resource<List<String>>>
}