// RecipeLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeCategory
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeLocalDataSource @Inject constructor() {

    // Cache maps for different data types
    private val recipesCache = mutableMapOf<String, Recipe>()
    private val favoriteRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val recentRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val categoriesCache = mutableMapOf<String, List<String>>()
    private val recipesByCategoryCache = mutableMapOf<String, List<RecipeListItem>>()
    private val allRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val searchResultsCache = mutableMapOf<String, List<RecipeListItem>>()
    private val systemIngredientsCache = mutableListOf<Ingredient>()
    private val userIngredientsCache = mutableMapOf<String, List<Ingredient>>()
    private val systemCategoriesCache = mutableListOf<RecipeCategory>()
    private val popularTagsCache = mutableListOf<String>()
    private val userSearchResultsCache = mutableMapOf<String, List<RecipeListItem>>()

    // Recipe operations
    suspend fun saveRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        recipesCache[recipe.recipeId] = recipe
    }

    suspend fun getRecipeById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        return@withContext recipesCache[recipeId]
    }

    suspend fun deleteRecipe(recipeId: String) = withContext(Dispatchers.IO) {
        recipesCache.remove(recipeId)

        // Also clean up from other caches where this recipe might be referenced
        allRecipesCache.forEach { (userId, recipes) ->
            val updatedList = recipes.filter { it.recipeId != recipeId }
            if (updatedList.size != recipes.size) {
                allRecipesCache[userId] = updatedList
            }
        }

        favoriteRecipesCache.forEach { (userId, recipes) ->
            val updatedList = recipes.filter { it.recipeId != recipeId }
            if (updatedList.size != recipes.size) {
                favoriteRecipesCache[userId] = updatedList
            }
        }

        recentRecipesCache.forEach { (userId, recipes) ->
            val updatedList = recipes.filter { it.recipeId != recipeId }
            if (updatedList.size != recipes.size) {
                recentRecipesCache[userId] = updatedList
            }
        }

        recipesByCategoryCache.forEach { (key, recipes) ->
            val updatedList = recipes.filter { it.recipeId != recipeId }
            if (updatedList.size != recipes.size) {
                recipesByCategoryCache[key] = updatedList
            }
        }
    }

    // Recipe list operations
    suspend fun getFavoriteRecipes(userId: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            return@withContext favoriteRecipesCache[userId]?.take(limit) ?: emptyList()
        }

    suspend fun saveFavoriteRecipes(userId: String, recipes: List<RecipeListItem>) =
        withContext(Dispatchers.IO) {
            favoriteRecipesCache[userId] = recipes
        }

    suspend fun getRecentRecipes(userId: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            return@withContext recentRecipesCache[userId]?.take(limit) ?: emptyList()
        }

    suspend fun saveRecentRecipes(userId: String, recipes: List<RecipeListItem>) =
        withContext(Dispatchers.IO) {
            recentRecipesCache[userId] = recipes
        }

    suspend fun getRecipesByCategory(
        userId: String,
        category: String,
        limit: Int
    ): List<RecipeListItem> = withContext(Dispatchers.IO) {
        val key = "$userId:$category"
        return@withContext recipesByCategoryCache[key]?.take(limit) ?: emptyList()
    }

    suspend fun saveRecipesByCategory(
        userId: String,
        category: String,
        recipes: List<RecipeListItem>
    ) = withContext(Dispatchers.IO) {
        val key = "$userId:$category"
        recipesByCategoryCache[key] = recipes
    }

    suspend fun getAllRecipes(userId: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            return@withContext allRecipesCache[userId]?.take(limit) ?: emptyList()
        }

    suspend fun saveAllRecipes(userId: String, recipes: List<RecipeListItem>) =
        withContext(Dispatchers.IO) {
            allRecipesCache[userId] = recipes
        }

    suspend fun getSearchResults(query: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            return@withContext searchResultsCache[query.lowercase()]?.take(limit) ?: emptyList()
        }

    suspend fun saveSearchResults(query: String, recipes: List<RecipeListItem>) =
        withContext(Dispatchers.IO) {
            searchResultsCache[query.lowercase()] = recipes
        }

    // Category operations
    suspend fun getCategories(userId: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext categoriesCache[userId] ?: emptyList()
    }

    suspend fun saveCategories(userId: String, categories: List<String>) =
        withContext(Dispatchers.IO) {
            categoriesCache[userId] = categories
        }

    suspend fun getSystemCategories(): List<RecipeCategory> = withContext(Dispatchers.IO) {
        return@withContext systemCategoriesCache.toList()
    }

    suspend fun saveSystemCategories(categories: List<RecipeCategory>) =
        withContext(Dispatchers.IO) {
            systemCategoriesCache.clear()
            systemCategoriesCache.addAll(categories)
        }

    suspend fun saveCustomCategory(category: RecipeCategory) = withContext(Dispatchers.IO) {
        // For custom categories, we might want to add them to a separate cache
        // or append to system categories - depending on requirements
        systemCategoriesCache.add(category)
    }

    // Ingredient operations
    suspend fun getSystemIngredients(): List<Ingredient> = withContext(Dispatchers.IO) {
        return@withContext systemIngredientsCache.toList()
    }

    suspend fun saveSystemIngredients(ingredients: List<Ingredient>) = withContext(Dispatchers.IO) {
        systemIngredientsCache.clear()
        systemIngredientsCache.addAll(ingredients)
    }

    suspend fun getUserIngredients(userId: String): List<Ingredient> = withContext(Dispatchers.IO) {
        return@withContext userIngredientsCache[userId] ?: emptyList()
    }

    suspend fun saveUserIngredients(userId: String, ingredients: List<Ingredient>) =
        withContext(Dispatchers.IO) {
            userIngredientsCache[userId] = ingredients
        }

    suspend fun saveIngredient(ingredient: Ingredient) = withContext(Dispatchers.IO) {
        if (ingredient.isSystemIngredient) {
            // Add to system ingredients if not already present
            if (!systemIngredientsCache.any { it.ingredientId == ingredient.ingredientId }) {
                systemIngredientsCache.add(ingredient)
            }
        } else {
            // Add to user ingredients
            val userId = ingredient.createdBy ?: return@withContext
            val userIngredients = userIngredientsCache[userId]?.toMutableList() ?: mutableListOf()

            // Update existing or add new
            val existingIndex =
                userIngredients.indexOfFirst { it.ingredientId == ingredient.ingredientId }
            if (existingIndex >= 0) {
                userIngredients[existingIndex] = ingredient
            } else {
                userIngredients.add(ingredient)
            }

            userIngredientsCache[userId] = userIngredients
        }
    }

    suspend fun searchIngredients(query: String, userId: String): List<Ingredient> =
        withContext(Dispatchers.IO) {
            val allIngredients = mutableListOf<Ingredient>()

            // Search system ingredients
            allIngredients.addAll(
                systemIngredientsCache.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )

            // Search user ingredients
            val userIngredients = userIngredientsCache[userId] ?: emptyList()
            allIngredients.addAll(
                userIngredients.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )

            return@withContext allIngredients.distinctBy { it.name }.take(30)
        }

    // Tag operations
    suspend fun getPopularTags(): List<String> = withContext(Dispatchers.IO) {
        return@withContext popularTagsCache.toList()
    }

    suspend fun savePopularTags(tags: List<String>) = withContext(Dispatchers.IO) {
        popularTagsCache.clear()
        popularTagsCache.addAll(tags)
    }

    // Favorite status operations
    suspend fun updateFavoriteStatus(userId: String, recipeId: String, isFavorite: Boolean) =
        withContext(Dispatchers.IO) {
            val userFavorites = favoriteRecipesCache[userId]?.toMutableList() ?: mutableListOf()

            if (isFavorite) {
                // If marking as favorite and not in list, try to find recipe in other caches to add
                if (userFavorites.none { it.recipeId == recipeId }) {
                    // Try to find in all recipes
                    val recipe = allRecipesCache[userId]?.find { it.recipeId == recipeId }
                        ?: recipesCache[recipeId]?.let { fullRecipe ->
                            RecipeListItem(
                                recipeId = fullRecipe.recipeId,
                                title = fullRecipe.title,
                                description = fullRecipe.description,
                                coverImage = fullRecipe.coverImage,
                                prepTime = fullRecipe.prepTime,
                                cookTime = fullRecipe.cookTime,
                                servings = fullRecipe.servings,
                                difficultyLevel = fullRecipe.difficultyLevel,
                                likeCount = fullRecipe.likeCount,
                                viewCount = fullRecipe.viewCount,
                                isPublic = fullRecipe.isPublic,
                                isFeatured = fullRecipe.isFeatured,
                                userId = fullRecipe.userId,
                                createdAt = fullRecipe.createdAt,
                                tags = fullRecipe.tags
                            )
                        }

                    recipe?.let {
                        userFavorites.add(it)
                        favoriteRecipesCache[userId] = userFavorites
                    }
                }
            } else {
                // If removing from favorites
                val updatedFavorites = userFavorites.filter { it.recipeId != recipeId }
                favoriteRecipesCache[userId] = updatedFavorites
            }
        }

    // Cache management operations
    suspend fun clearUserCache(userId: String) = withContext(Dispatchers.IO) {
        favoriteRecipesCache.remove(userId)
        recentRecipesCache.remove(userId)
        allRecipesCache.remove(userId)
        categoriesCache.remove(userId)
        userIngredientsCache.remove(userId)

        // Clear category-specific caches for this user
        val keysToRemove = recipesByCategoryCache.keys.filter { it.startsWith("$userId:") }
        keysToRemove.forEach { recipesByCategoryCache.remove(it) }
    }

    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        recipesCache.clear()
        favoriteRecipesCache.clear()
        recentRecipesCache.clear()
        categoriesCache.clear()
        recipesByCategoryCache.clear()
        allRecipesCache.clear()
        searchResultsCache.clear()
        systemIngredientsCache.clear()
        userIngredientsCache.clear()
        systemCategoriesCache.clear()
        popularTagsCache.clear()
    }

    suspend fun getCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "recipes" to recipesCache.size,
            "favoriteRecipes" to favoriteRecipesCache.values.sumOf { it.size },
            "recentRecipes" to recentRecipesCache.values.sumOf { it.size },
            "categories" to categoriesCache.values.sumOf { it.size },
            "recipesByCategory" to recipesByCategoryCache.values.sumOf { it.size },
            "allRecipes" to allRecipesCache.values.sumOf { it.size },
            "searchResults" to searchResultsCache.values.sumOf { it.size },
            "systemIngredients" to systemIngredientsCache.size,
            "userIngredients" to userIngredientsCache.values.sumOf { it.size },
            "systemCategories" to systemCategoriesCache.size,
            "popularTags" to popularTagsCache.size
        )
    }

    // Helper methods for data consistency
    suspend fun addRecipeToUserLists(userId: String, recipe: RecipeListItem) =
        withContext(Dispatchers.IO) {
            // Add to recent recipes
            val recentRecipes = recentRecipesCache[userId]?.toMutableList() ?: mutableListOf()
            recentRecipes.add(0, recipe) // Add at the beginning
            if (recentRecipes.size > 20) { // Keep only recent 20
                recentRecipes.removeAt(recentRecipes.size - 1)
            }
            recentRecipesCache[userId] = recentRecipes

            // Add to all recipes
            val allRecipes = allRecipesCache[userId]?.toMutableList() ?: mutableListOf()
            allRecipes.add(0, recipe)
            allRecipesCache[userId] = allRecipes

            // Add to category-specific caches if the recipe has tags
            recipe.tags.forEach { tag ->
                val key = "$userId:$tag"
                val categoryRecipes =
                    recipesByCategoryCache[key]?.toMutableList() ?: mutableListOf()
                categoryRecipes.add(0, recipe)
                recipesByCategoryCache[key] = categoryRecipes
            }
        }

    suspend fun updateRecipeInUserLists(userId: String, updatedRecipe: RecipeListItem) =
        withContext(Dispatchers.IO) {
            // Update in all relevant caches
            val caches = mapOf(
                "recent" to recentRecipesCache[userId],
                "all" to allRecipesCache[userId],
                "favorites" to favoriteRecipesCache[userId]
            )

            caches.forEach { (cacheType, recipes) ->
                recipes?.let { recipeList ->
                    val updatedList = recipeList.map { recipe ->
                        if (recipe.recipeId == updatedRecipe.recipeId) updatedRecipe else recipe
                    }

                    when (cacheType) {
                        "recent" -> recentRecipesCache[userId] = updatedList
                        "all" -> allRecipesCache[userId] = updatedList
                        "favorites" -> favoriteRecipesCache[userId] = updatedList
                    }
                }
            }

            // Update in category caches
            recipesByCategoryCache.keys.filter { it.startsWith("$userId:") }.forEach { key ->
                val categoryRecipes = recipesByCategoryCache[key]
                categoryRecipes?.let { recipes ->
                    val updatedList = recipes.map { recipe ->
                        if (recipe.recipeId == updatedRecipe.recipeId) updatedRecipe else recipe
                    }
                    recipesByCategoryCache[key] = updatedList
                }
            }
        }

    suspend fun removeRecipeFromUserLists(userId: String, recipeId: String) =
        withContext(Dispatchers.IO) {
            // Remove from all user-specific caches
            recentRecipesCache[userId] =
                recentRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()
            allRecipesCache[userId] =
                allRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()
            favoriteRecipesCache[userId] =
                favoriteRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()

            // Remove from category caches
            recipesByCategoryCache.keys.filter { it.startsWith("$userId:") }.forEach { key ->
                recipesByCategoryCache[key] =
                    recipesByCategoryCache[key]?.filter { it.recipeId != recipeId } ?: emptyList()
            }
        }

    // Data validation methods
    suspend fun isRecipeCached(recipeId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext recipesCache.containsKey(recipeId)
    }

    suspend fun isUserDataCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext favoriteRecipesCache.containsKey(userId) ||
                recentRecipesCache.containsKey(userId) ||
                allRecipesCache.containsKey(userId) ||
                categoriesCache.containsKey(userId)
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }

    suspend fun searchUserRecipes(userId: String, query: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            val cacheKey = "${userId}_search_${query}"
            return@withContext userSearchResultsCache[cacheKey]?.take(limit) ?: emptyList()
        }

    suspend fun saveUserSearchResults(
        userId: String,
        query: String,
        recipes: List<RecipeListItem>
    ) = withContext(Dispatchers.IO) {
        val cacheKey = "${userId}_search_${query}"
        userSearchResultsCache[cacheKey] = recipes
    }
}