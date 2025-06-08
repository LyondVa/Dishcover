// RecipeLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeCategory
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
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

    // NEW: Additional cache maps for missing functionality
    private val recipeRatingsCache = mutableMapOf<String, RecipeRatingAggregate>()
    private val userRatingsCache = mutableMapOf<String, RecipeRating>() // key: "userId:recipeId"
    private val recipeReviewsCache = mutableMapOf<String, List<RecipeReview>>()
    private val userReviewsCache = mutableMapOf<String, RecipeReview>() // key: "userId:recipeId"
    private val nutritionalInfoCache = mutableMapOf<String, NutritionalInfo>()
    private val recipeTagsCache = mutableMapOf<String, List<String>>()

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

    suspend fun getRecipesByCategory(userId: String, category: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
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

    suspend fun getUserRecipes(userId: String, limit: Int): List<RecipeListItem> =
        withContext(Dispatchers.IO) {
            return@withContext allRecipesCache[userId]?.take(limit) ?: emptyList()
        }

    suspend fun saveUserRecipes(userId: String, recipes: List<RecipeListItem>) =
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
            if (!systemIngredientsCache.any { it.ingredientId == ingredient.ingredientId }) {
                systemIngredientsCache.add(ingredient)
            }
        } else {
            val userId = ingredient.createdBy ?: return@withContext
            val userIngredients = userIngredientsCache[userId]?.toMutableList() ?: mutableListOf()

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

            allIngredients.addAll(
                systemIngredientsCache.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )

            val userIngredients = userIngredientsCache[userId] ?: emptyList()
            allIngredients.addAll(
                userIngredients.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )

            return@withContext allIngredients.distinctBy { it.name }.take(30)
        }

    // Tag operations
    suspend fun getRecipeTags(recipeId: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext recipeTagsCache[recipeId] ?: emptyList()
    }

    suspend fun saveRecipeTags(recipeId: String, tags: List<String>) = withContext(Dispatchers.IO) {
        recipeTagsCache[recipeId] = tags
    }

    suspend fun getPopularTags(): List<String> = withContext(Dispatchers.IO) {
        return@withContext popularTagsCache.toList()
    }

    suspend fun savePopularTags(tags: List<String>) = withContext(Dispatchers.IO) {
        popularTagsCache.clear()
        popularTagsCache.addAll(tags)
    }

    // NEW: Rating operations
    suspend fun getRecipeRatingAggregate(recipeId: String): RecipeRatingAggregate? = withContext(Dispatchers.IO) {
        return@withContext recipeRatingsCache[recipeId]
    }

    suspend fun saveRecipeRatingAggregate(aggregate: RecipeRatingAggregate) = withContext(Dispatchers.IO) {
        recipeRatingsCache[aggregate.recipeId] = aggregate
    }

    suspend fun getUserRecipeRating(recipeId: String, userId: String): RecipeRating? = withContext(Dispatchers.IO) {
        val key = "$userId:$recipeId"
        return@withContext userRatingsCache[key]
    }

    suspend fun saveRecipeRating(rating: RecipeRating) = withContext(Dispatchers.IO) {
        val key = "${rating.userId}:${rating.recipeId}"
        userRatingsCache[key] = rating
    }

    suspend fun deleteRecipeRating(recipeId: String, userId: String) = withContext(Dispatchers.IO) {
        val key = "$userId:$recipeId"
        userRatingsCache.remove(key)
    }

    // NEW: Review operations
    suspend fun getRecipeReviews(recipeId: String, limit: Int, offset: Int): List<RecipeReview> = withContext(Dispatchers.IO) {
        val allReviews = recipeReviewsCache[recipeId] ?: emptyList()
        return@withContext allReviews.drop(offset).take(limit)
    }

    suspend fun saveRecipeReviews(recipeId: String, reviews: List<RecipeReview>) = withContext(Dispatchers.IO) {
        recipeReviewsCache[recipeId] = reviews
    }

    suspend fun saveRecipeReview(review: RecipeReview) = withContext(Dispatchers.IO) {
        // Save to recipe-specific cache
        val currentReviews = recipeReviewsCache[review.recipeId]?.toMutableList() ?: mutableListOf()
        val existingIndex = currentReviews.indexOfFirst { it.reviewId == review.reviewId }
        if (existingIndex >= 0) {
            currentReviews[existingIndex] = review
        } else {
            currentReviews.add(0, review) // Add newest reviews first
        }
        recipeReviewsCache[review.recipeId] = currentReviews

        // Save to user-specific cache
        val userKey = "${review.userId}:${review.recipeId}"
        userReviewsCache[userKey] = review
    }

    suspend fun deleteRecipeReview(reviewId: String) = withContext(Dispatchers.IO) {
        // Find and remove from all caches
        recipeReviewsCache.forEach { (recipeId, reviews) ->
            val mutableReviews = reviews.toMutableList()
            mutableReviews.removeAll { it.reviewId == reviewId }
            if (mutableReviews.size != reviews.size) {
                recipeReviewsCache[recipeId] = mutableReviews
            }
        }

        // Remove from user cache
        userReviewsCache.entries.removeAll { it.value.reviewId == reviewId }
    }

    suspend fun updateReviewHelpfulCount(reviewId: String, helpful: Boolean) = withContext(Dispatchers.IO) {
        // Update in all caches where this review exists
        recipeReviewsCache.forEach { (recipeId, reviews) ->
            val mutableReviews = reviews.toMutableList()
            val reviewIndex = mutableReviews.indexOfFirst { it.reviewId == reviewId }
            if (reviewIndex >= 0) {
                val currentReview = mutableReviews[reviewIndex]
                val newHelpfulCount = if (helpful) {
                    currentReview.helpful + 1
                } else {
                    maxOf(0, currentReview.helpful - 1)
                }
                mutableReviews[reviewIndex] = currentReview.copy(helpful = newHelpfulCount)
                recipeReviewsCache[recipeId] = mutableReviews
            }
        }

        // Also update in user cache
        userReviewsCache.forEach { (key, review) ->
            if (review.reviewId == reviewId) {
                val newHelpfulCount = if (helpful) {
                    review.helpful + 1
                } else {
                    maxOf(0, review.helpful - 1)
                }
                userReviewsCache[key] = review.copy(helpful = newHelpfulCount)
            }
        }
    }

    suspend fun getUserReviewForRecipe(recipeId: String, userId: String): RecipeReview? = withContext(Dispatchers.IO) {
        val key = "$userId:$recipeId"
        return@withContext userReviewsCache[key]
    }

    // NEW: Nutritional information operations
    suspend fun getNutritionalInfo(recipeId: String): NutritionalInfo? = withContext(Dispatchers.IO) {
        return@withContext nutritionalInfoCache[recipeId]
    }

    suspend fun saveNutritionalInfo(nutritionalInfo: NutritionalInfo) = withContext(Dispatchers.IO) {
        nutritionalInfoCache[nutritionalInfo.recipeId] = nutritionalInfo
    }

    // Favorite status operations
    suspend fun updateFavoriteStatus(userId: String, recipeId: String, isFavorite: Boolean) =
        withContext(Dispatchers.IO) {
            val userFavorites = favoriteRecipesCache[userId]?.toMutableList() ?: mutableListOf()

            if (isFavorite) {
                if (userFavorites.none { it.recipeId == recipeId }) {
                    val recipe = allRecipesCache[userId]?.find { it.recipeId == recipeId }
                        ?: recentRecipesCache[userId]?.find { it.recipeId == recipeId }

                    recipe?.let { userFavorites.add(0, it) }
                }
            } else {
                userFavorites.removeAll { it.recipeId == recipeId }
            }

            favoriteRecipesCache[userId] = userFavorites
        }

    suspend fun updateRecipeInUserLists(userId: String, updatedRecipe: RecipeListItem) =
        withContext(Dispatchers.IO) {
            // Update in recent recipes
            val recentRecipes = recentRecipesCache[userId]?.toMutableList() ?: return@withContext
            val recentIndex = recentRecipes.indexOfFirst { it.recipeId == updatedRecipe.recipeId }
            if (recentIndex >= 0) {
                recentRecipes[recentIndex] = updatedRecipe
                recentRecipesCache[userId] = recentRecipes
            }

            // Update in all recipes
            val allRecipes = allRecipesCache[userId]?.toMutableList() ?: return@withContext
            val allIndex = allRecipes.indexOfFirst { it.recipeId == updatedRecipe.recipeId }
            if (allIndex >= 0) {
                allRecipes[allIndex] = updatedRecipe
                allRecipesCache[userId] = allRecipes
            }

            // Update in favorites if exists
            val favoriteRecipes = favoriteRecipesCache[userId]?.toMutableList()
            favoriteRecipes?.let { favorites ->
                val favoriteIndex = favorites.indexOfFirst { it.recipeId == updatedRecipe.recipeId }
                if (favoriteIndex >= 0) {
                    favorites[favoriteIndex] = updatedRecipe
                    favoriteRecipesCache[userId] = favorites
                }
            }

            // Update in category caches
            recipesByCategoryCache.keys.filter { it.startsWith("$userId:") }.forEach { key ->
                val categoryRecipes = recipesByCategoryCache[key]?.toMutableList() ?: return@forEach
                val categoryIndex = categoryRecipes.indexOfFirst { it.recipeId == updatedRecipe.recipeId }
                if (categoryIndex >= 0) {
                    categoryRecipes[categoryIndex] = updatedRecipe
                    recipesByCategoryCache[key] = categoryRecipes
                }
            }
        }

    suspend fun removeRecipeFromUserLists(userId: String, recipeId: String) =
        withContext(Dispatchers.IO) {
            recentRecipesCache[userId] =
                recentRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()
            allRecipesCache[userId] =
                allRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()
            favoriteRecipesCache[userId] =
                favoriteRecipesCache[userId]?.filter { it.recipeId != recipeId } ?: emptyList()

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
        return@withContext System.currentTimeMillis()
    }

    // Cache management operations
    suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        recipesCache.clear()
        favoriteRecipesCache.clear()
        recentRecipesCache.clear()
        categoriesCache.clear()
        recipesByCategoryCache.clear()
        allRecipesCache.clear()
        searchResultsCache.clear()
        userSearchResultsCache.clear()
        systemIngredientsCache.clear()
        userIngredientsCache.clear()
        systemCategoriesCache.clear()
        popularTagsCache.clear()

        // Clear new caches
        recipeRatingsCache.clear()
        userRatingsCache.clear()
        recipeReviewsCache.clear()
        userReviewsCache.clear()
        nutritionalInfoCache.clear()
        recipeTagsCache.clear()
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
            "popularTags" to popularTagsCache.size,
            "recipeRatings" to recipeRatingsCache.size,
            "userRatings" to userRatingsCache.size,
            "recipeReviews" to recipeReviewsCache.values.sumOf { it.size },
            "userReviews" to userReviewsCache.size,
            "nutritionalInfo" to nutritionalInfoCache.size,
            "recipeTags" to recipeTagsCache.values.sumOf { it.size }
        )
    }

    // Helper methods for data consistency
    suspend fun addRecipeToUserLists(userId: String, recipe: RecipeListItem) =
        withContext(Dispatchers.IO) {
            // Add to recent recipes
            val recentRecipes = recentRecipesCache[userId]?.toMutableList() ?: mutableListOf()
            recentRecipes.add(0, recipe)
            if (recentRecipes.size > 20) {
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
}