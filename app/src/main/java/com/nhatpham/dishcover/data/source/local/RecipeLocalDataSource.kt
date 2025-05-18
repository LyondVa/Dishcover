package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeLocalDataSource @Inject constructor() {
    private val favoriteRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val recentRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val categoriesCache = mutableMapOf<String, List<String>>()
    private val recipesByCategoryCache = mutableMapOf<String, List<RecipeListItem>>()
    private val allRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val searchResultsCache = mutableMapOf<String, List<RecipeListItem>>()
    private val recipeDetailsCache = mutableMapOf<String, Recipe>()

    suspend fun getFavoriteRecipes(userId: String, limit: Int): List<RecipeListItem> = withContext(Dispatchers.IO) {
        return@withContext favoriteRecipesCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveFavoriteRecipes(userId: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        favoriteRecipesCache[userId] = recipes
    }

    suspend fun getRecentRecipes(userId: String, limit: Int): List<RecipeListItem> = withContext(Dispatchers.IO) {
        return@withContext recentRecipesCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveRecentRecipes(userId: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        recentRecipesCache[userId] = recipes
    }

    suspend fun getCategories(userId: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext categoriesCache[userId] ?: emptyList()
    }

    suspend fun saveCategories(userId: String, categories: List<String>) = withContext(Dispatchers.IO) {
        categoriesCache[userId] = categories
    }

    suspend fun getRecipesByCategory(userId: String, category: String, limit: Int): List<RecipeListItem> = withContext(Dispatchers.IO) {
        val key = "$userId:$category"
        return@withContext recipesByCategoryCache[key]?.take(limit) ?: emptyList()
    }

    suspend fun saveRecipesByCategory(userId: String, category: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        val key = "$userId:$category"
        recipesByCategoryCache[key] = recipes
    }

    suspend fun getAllRecipes(userId: String, limit: Int): List<RecipeListItem> = withContext(Dispatchers.IO) {
        return@withContext allRecipesCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveAllRecipes(userId: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        allRecipesCache[userId] = recipes
    }

    suspend fun getSearchResults(query: String, limit: Int): List<RecipeListItem> = withContext(Dispatchers.IO) {
        return@withContext searchResultsCache[query.lowercase()]?.take(limit) ?: emptyList()
    }

    suspend fun saveSearchResults(query: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        searchResultsCache[query.lowercase()] = recipes
    }

    suspend fun getRecipeById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        return@withContext recipeDetailsCache[recipeId]
    }

    suspend fun saveRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        recipeDetailsCache[recipe.recipeId] = recipe
    }
}
