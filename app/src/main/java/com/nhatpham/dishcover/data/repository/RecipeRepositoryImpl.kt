// RecipeRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.model.*
import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeCategory
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource,
    private val recipeLocalDataSource: RecipeLocalDataSource
) : RecipeRepository {

    override fun createRecipe(recipe: Recipe): Flow<Resource<Recipe>> = flow {
        emit(Resource.Loading())
        try {
            val createdRecipe = recipeRemoteDataSource.createRecipe(recipe)
            if (createdRecipe != null) {
                recipeLocalDataSource.saveRecipe(createdRecipe)
                recipeLocalDataSource.addRecipeToUserLists(
                    createdRecipe.userId,
                    createdRecipe.toListItem()
                )
                emit(Resource.Success(createdRecipe))
            } else {
                emit(Resource.Error("Failed to create recipe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating recipe")
            emit(Resource.Error(e.message ?: "Failed to create recipe"))
        }
    }

    override fun updateRecipe(recipe: Recipe): Flow<Resource<Recipe>> = flow {
        emit(Resource.Loading())
        try {
            val updatedRecipe = recipeRemoteDataSource.updateRecipe(recipe)
            if (updatedRecipe != null) {
                recipeLocalDataSource.saveRecipe(updatedRecipe)
                recipeLocalDataSource.updateRecipeInUserLists(
                    updatedRecipe.userId,
                    updatedRecipe.toListItem()
                )
                emit(Resource.Success(updatedRecipe))
            } else {
                emit(Resource.Error("Failed to update recipe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe")
            emit(Resource.Error(e.message ?: "Failed to update recipe"))
        }
    }

    override fun deleteRecipe(recipeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val recipe = recipeLocalDataSource.getRecipeById(recipeId)
            val success = recipeRemoteDataSource.deleteRecipe(recipeId)
            if (success) {
                recipeLocalDataSource.deleteRecipe(recipeId)
                recipe?.let {
                    recipeLocalDataSource.removeRecipeFromUserLists(it.userId, recipeId)
                }
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete recipe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe")
            emit(Resource.Error(e.message ?: "Failed to delete recipe"))
        }
    }

    override fun getRecipe(recipeId: String): Flow<Resource<Recipe>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipe = recipeLocalDataSource.getRecipeById(recipeId)
            localRecipe?.let {
                emit(Resource.Success(it))
            }

            val remoteRecipe = recipeRemoteDataSource.getRecipeById(recipeId)
            if (remoteRecipe != null) {
                recipeLocalDataSource.saveRecipe(remoteRecipe)
                if (localRecipe == null) {
                    emit(Resource.Success(remoteRecipe))
                }
            } else if (localRecipe == null) {
                emit(Resource.Error("Recipe not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe")
            if (recipeLocalDataSource.getRecipeById(recipeId) == null) {
                emit(Resource.Error(e.message ?: "Failed to load recipe"))
            }
        }
    }

    override fun getFavoriteRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val cachedRecipes = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (cachedRecipes.isNotEmpty()) {
                emit(Resource.Success(cachedRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getFavoriteRecipes(userId, limit)
            recipeLocalDataSource.saveFavoriteRecipes(userId, remoteRecipes)

            if (cachedRecipes.isEmpty() || remoteRecipes != cachedRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting favorite recipes")
            val cachedData = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load favorite recipes"))
            }
        }
    }

    override fun getRecipeRatings(recipeId: String): Flow<Resource<RecipeRatingAggregate>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val cachedAggregate = recipeLocalDataSource.getRecipeRatingAggregate(recipeId)
            cachedAggregate?.let {
                emit(Resource.Success(it))
            }

            // Fetch from remote
            val remoteAggregate = recipeRemoteDataSource.getRecipeRatingAggregate(recipeId)
            if (remoteAggregate != null) {
                recipeLocalDataSource.saveRecipeRatingAggregate(remoteAggregate)

                if (cachedAggregate == null || remoteAggregate != cachedAggregate) {
                    emit(Resource.Success(remoteAggregate))
                }
            } else if (cachedAggregate == null) {
                // Return empty aggregate for new recipes
                emit(Resource.Success(RecipeRatingAggregate(recipeId = recipeId)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe ratings")
            val cachedData = recipeLocalDataSource.getRecipeRatingAggregate(recipeId)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load ratings"))
            }
        }
    }

    override fun addRecipeRating(rating: RecipeRating): Flow<Resource<RecipeRating>> = flow {
        emit(Resource.Loading())
        try {
            val savedRating = recipeRemoteDataSource.addRecipeRating(rating)
            if (savedRating != null) {
                recipeLocalDataSource.saveRecipeRating(savedRating)
                emit(Resource.Success(savedRating))
            } else {
                emit(Resource.Error("Failed to save rating"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe rating")
            emit(Resource.Error(e.message ?: "Failed to add rating"))
        }
    }

    override fun getUserRecipeRating(recipeId: String, userId: String): Flow<Resource<RecipeRating?>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val localRating = recipeLocalDataSource.getUserRecipeRating(recipeId, userId)
            if (localRating != null) {
                emit(Resource.Success(localRating))
            }

            // Fetch from remote
            val remoteRating = recipeRemoteDataSource.getUserRecipeRating(recipeId, userId)
            if (remoteRating != null) {
                recipeLocalDataSource.saveRecipeRating(remoteRating)
                if (localRating == null || remoteRating != localRating) {
                    emit(Resource.Success(remoteRating))
                }
            } else if (localRating == null) {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user recipe rating")
            val cachedData = recipeLocalDataSource.getUserRecipeRating(recipeId, userId)
            emit(Resource.Success(cachedData))
        }
    }

    override fun updateRecipeRating(rating: RecipeRating): Flow<Resource<RecipeRating>> = flow {
        emit(Resource.Loading())
        try {
            val updatedRating = recipeRemoteDataSource.updateRecipeRating(rating)
            if (updatedRating != null) {
                recipeLocalDataSource.saveRecipeRating(updatedRating)
                emit(Resource.Success(updatedRating))
            } else {
                emit(Resource.Error("Failed to update rating"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe rating")
            emit(Resource.Error(e.message ?: "Failed to update rating"))
        }
    }

    override fun deleteRecipeRating(recipeId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.deleteRecipeRating(recipeId, userId)
            if (success) {
                recipeLocalDataSource.deleteRecipeRating(recipeId, userId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete rating"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe rating")
            emit(Resource.Error(e.message ?: "Failed to delete rating"))
        }
    }

    // NEW: Recipe Review operations
    override fun getRecipeReviews(recipeId: String, limit: Int, offset: Int): Flow<Resource<List<RecipeReview>>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val cachedReviews = recipeLocalDataSource.getRecipeReviews(recipeId, limit, offset)
            if (cachedReviews.isNotEmpty()) {
                emit(Resource.Success(cachedReviews))
            }

            // Fetch from remote
            val remoteReviews = recipeRemoteDataSource.getRecipeReviews(recipeId, limit, offset)
            recipeLocalDataSource.saveRecipeReviews(recipeId, remoteReviews)

            if (cachedReviews.isEmpty() || remoteReviews != cachedReviews) {
                emit(Resource.Success(remoteReviews))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe reviews")
            val cachedData = recipeLocalDataSource.getRecipeReviews(recipeId, limit, offset)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load reviews"))
            }
        }
    }

    override fun addRecipeReview(review: RecipeReview): Flow<Resource<RecipeReview>> = flow {
        emit(Resource.Loading())
        try {
            val savedReview = recipeRemoteDataSource.addRecipeReview(review)
            if (savedReview != null) {
                recipeLocalDataSource.saveRecipeReview(savedReview)
                emit(Resource.Success(savedReview))
            } else {
                emit(Resource.Error("Failed to save review"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe review")
            emit(Resource.Error(e.message ?: "Failed to add review"))
        }
    }

    override fun updateRecipeReview(review: RecipeReview): Flow<Resource<RecipeReview>> = flow {
        emit(Resource.Loading())
        try {
            val updatedReview = recipeRemoteDataSource.updateRecipeReview(review)
            if (updatedReview != null) {
                recipeLocalDataSource.saveRecipeReview(updatedReview)
                emit(Resource.Success(updatedReview))
            } else {
                emit(Resource.Error("Failed to update review"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe review")
            emit(Resource.Error(e.message ?: "Failed to update review"))
        }
    }

    override fun deleteRecipeReview(reviewId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.deleteRecipeReview(reviewId, userId)
            if (success) {
                recipeLocalDataSource.deleteRecipeReview(reviewId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete review"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe review")
            emit(Resource.Error(e.message ?: "Failed to delete review"))
        }
    }

    override fun markReviewHelpful(reviewId: String, userId: String, helpful: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.markReviewHelpful(reviewId, userId, helpful)
            if (success) {
                recipeLocalDataSource.updateReviewHelpfulCount(reviewId, helpful)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to mark review as helpful"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking review as helpful")
            emit(Resource.Error(e.message ?: "Failed to mark review as helpful"))
        }
    }

    override fun getUserReviewForRecipe(recipeId: String, userId: String): Flow<Resource<RecipeReview?>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val localReview = recipeLocalDataSource.getUserReviewForRecipe(recipeId, userId)
            if (localReview != null) {
                emit(Resource.Success(localReview))
            }

            // Fetch from remote
            val remoteReview = recipeRemoteDataSource.getUserReviewForRecipe(recipeId, userId)
            if (remoteReview != null) {
                recipeLocalDataSource.saveRecipeReview(remoteReview)
                if (localReview == null || remoteReview != localReview) {
                    emit(Resource.Success(remoteReview))
                }
            } else if (localReview == null) {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user review for recipe")
            val cachedData = recipeLocalDataSource.getUserReviewForRecipe(recipeId, userId)
            emit(Resource.Success(cachedData))
        }
    }

    // NEW: Nutritional Information operations
    override fun getNutritionalInfo(recipeId: String): Flow<Resource<NutritionalInfo>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val cachedInfo = recipeLocalDataSource.getNutritionalInfo(recipeId)
            if (cachedInfo != null) {
                emit(Resource.Success(cachedInfo))
            }

            // Fetch from remote
            val remoteInfo = recipeRemoteDataSource.getNutritionalInfo(recipeId)
            if (remoteInfo != null) {
                recipeLocalDataSource.saveNutritionalInfo(remoteInfo)
                if (cachedInfo == null || remoteInfo != cachedInfo) {
                    emit(Resource.Success(remoteInfo))
                }
            } else if (cachedInfo == null) {
                emit(Resource.Error("Nutritional information not available"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting nutritional info")
            val cachedData = recipeLocalDataSource.getNutritionalInfo(recipeId)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load nutritional information"))
            }
        }
    }

    override fun calculateNutritionalInfo(recipe: Recipe): Flow<Resource<NutritionalInfo>> = flow {
        emit(Resource.Loading())
        try {
            val calculatedInfo = recipeRemoteDataSource.calculateNutritionalInfo(recipe)
            if (calculatedInfo != null) {
                recipeLocalDataSource.saveNutritionalInfo(calculatedInfo)
                emit(Resource.Success(calculatedInfo))
            } else {
                emit(Resource.Error("Failed to calculate nutritional information"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating nutritional info")
            emit(Resource.Error(e.message ?: "Failed to calculate nutritional information"))
        }
    }

    override fun updateNutritionalInfo(nutritionalInfo: NutritionalInfo): Flow<Resource<NutritionalInfo>> = flow {
        emit(Resource.Loading())
        try {
            val updatedInfo = recipeRemoteDataSource.updateNutritionalInfo(nutritionalInfo)
            if (updatedInfo != null) {
                recipeLocalDataSource.saveNutritionalInfo(updatedInfo)
                emit(Resource.Success(updatedInfo))
            } else {
                emit(Resource.Error("Failed to update nutritional information"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating nutritional info")
            emit(Resource.Error(e.message ?: "Failed to update nutritional information"))
        }
    }

    override fun getRecentRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getRecentRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getRecentRecipes(userId, limit)
            recipeLocalDataSource.saveRecentRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty() || remoteRecipes != localRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent recipes")
            val cachedData = recipeLocalDataSource.getRecentRecipes(userId, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load recent recipes"))
            }
        }
    }

    override fun getRecipesByCategory(userId: String, category: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getRecipesByCategory(userId, category, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getRecipesByCategory(userId, category, limit)
            recipeLocalDataSource.saveRecipesByCategory(userId, category, remoteRecipes)

            if (localRecipes.isEmpty() || remoteRecipes != localRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipes by category")
            val cachedData = recipeLocalDataSource.getRecipesByCategory(userId, category, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load recipes by category"))
            }
        }
    }

    override fun getUserRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getUserRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getUserRecipes(userId, limit)
            recipeLocalDataSource.saveUserRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty() || remoteRecipes != localRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all recipes")
            val cachedData = recipeLocalDataSource.getUserRecipes(userId, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load all recipes"))
            }
        }
    }

    override fun searchRecipes(query: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            // Check cache first for this search query
            val cachedResults = recipeLocalDataSource.getSearchResults(query, limit)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Always search remote for fresh results
            val searchResults = recipeRemoteDataSource.searchRecipes(query, limit)
            recipeLocalDataSource.saveSearchResults(query, searchResults)

            if (cachedResults.isEmpty() || searchResults != cachedResults) {
                emit(Resource.Success(searchResults))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching recipes")
            val cachedData = recipeLocalDataSource.getSearchResults(query, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to search recipes"))
            }
        }
    }

    override fun getCategories(userId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val localCategories = recipeLocalDataSource.getCategories(userId)
            if (localCategories.isNotEmpty()) {
                emit(Resource.Success(localCategories))
            }

            val remoteCategories = recipeRemoteDataSource.getCategories(userId)
            recipeLocalDataSource.saveCategories(userId, remoteCategories)

            if (localCategories.isEmpty() || remoteCategories != localCategories) {
                emit(Resource.Success(remoteCategories))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting categories")
            val cachedData = recipeLocalDataSource.getCategories(userId)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load categories"))
            }
        }
    }

    override fun getSystemCategories(): Flow<Resource<List<RecipeCategory>>> = flow {
        emit(Resource.Loading())
        try {
            val localCategories = recipeLocalDataSource.getSystemCategories()
            if (localCategories.isNotEmpty()) {
                emit(Resource.Success(localCategories))
            }

            val remoteCategories = recipeRemoteDataSource.getSystemCategories()
            recipeLocalDataSource.saveSystemCategories(remoteCategories)

            if (localCategories.isEmpty() || remoteCategories != localCategories) {
                emit(Resource.Success(remoteCategories))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting system categories")
            val cachedData = recipeLocalDataSource.getSystemCategories()
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load system categories"))
            }
        }
    }

    override fun createCustomCategory(userId: String, categoryName: String): Flow<Resource<RecipeCategory>> = flow {
        emit(Resource.Loading())
        try {
            val category = recipeRemoteDataSource.createCustomCategory(userId, categoryName)
            if (category != null) {
                recipeLocalDataSource.saveCustomCategory(category)
                emit(Resource.Success(category))
            } else {
                emit(Resource.Error("Failed to create custom category"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating custom category")
            emit(Resource.Error(e.message ?: "Failed to create custom category"))
        }
    }

    override fun getSystemIngredients(): Flow<Resource<List<Ingredient>>> = flow {
        emit(Resource.Loading())
        try {
            val localIngredients = recipeLocalDataSource.getSystemIngredients()
            if (localIngredients.isNotEmpty()) {
                emit(Resource.Success(localIngredients))
            }

            val remoteIngredients = recipeRemoteDataSource.getSystemIngredients()
            recipeLocalDataSource.saveSystemIngredients(remoteIngredients)

            if (localIngredients.isEmpty() || remoteIngredients != localIngredients) {
                emit(Resource.Success(remoteIngredients))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting system ingredients")
            val cachedData = recipeLocalDataSource.getSystemIngredients()
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load system ingredients"))
            }
        }
    }

    override fun getUserIngredients(userId: String): Flow<Resource<List<Ingredient>>> = flow {
        emit(Resource.Loading())
        try {
            val localIngredients = recipeLocalDataSource.getUserIngredients(userId)
            if (localIngredients.isNotEmpty()) {
                emit(Resource.Success(localIngredients))
            }

            val remoteIngredients = recipeRemoteDataSource.getUserIngredients(userId)
            recipeLocalDataSource.saveUserIngredients(userId, remoteIngredients)

            if (localIngredients.isEmpty() || remoteIngredients != localIngredients) {
                emit(Resource.Success(remoteIngredients))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user ingredients")
            val cachedData = recipeLocalDataSource.getUserIngredients(userId)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load user ingredients"))
            }
        }
    }

    override fun createIngredient(ingredient: Ingredient): Flow<Resource<Ingredient>> = flow {
        emit(Resource.Loading())
        try {
            val createdIngredient = recipeRemoteDataSource.createIngredient(ingredient)
            if (createdIngredient != null) {
                recipeLocalDataSource.saveIngredient(createdIngredient)
                emit(Resource.Success(createdIngredient))
            } else {
                emit(Resource.Error("Failed to create ingredient"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating ingredient")
            emit(Resource.Error(e.message ?: "Failed to create ingredient"))
        }
    }

    override fun searchIngredients(query: String, userId: String): Flow<Resource<List<Ingredient>>> = flow {
        emit(Resource.Loading())
        try {
            // Try local search first for quick response
            val localResults = recipeLocalDataSource.searchIngredients(query, userId)
            if (localResults.isNotEmpty()) {
                emit(Resource.Success(localResults))
            }

            // Also search remote for more comprehensive results
            val remoteResults = recipeRemoteDataSource.searchIngredients(query, userId)

            // Merge and emit unique results
            val combinedResults = (localResults + remoteResults)
                .distinctBy { it.name.lowercase() }
                .take(30)

            if (combinedResults != localResults) {
                emit(Resource.Success(combinedResults))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching ingredients")
            val cachedData = recipeLocalDataSource.searchIngredients(query, userId)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to search ingredients"))
            }
        }
    }

    override fun getRecipeTags(recipeId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val tags = recipeRemoteDataSource.getRecipeTags(recipeId)
            emit(Resource.Success(tags))
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe tags")
            emit(Resource.Error(e.message ?: "Failed to load recipe tags"))
        }
    }

    override fun addRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.addRecipeTag(recipeId, tag)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to add tag"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe tag")
            emit(Resource.Error(e.message ?: "Failed to add tag"))
        }
    }

    override fun removeRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.removeRecipeTag(recipeId, tag)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove tag"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe tag")
            emit(Resource.Error(e.message ?: "Failed to remove tag"))
        }
    }

    override fun getPopularTags(limit: Int): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val localTags = recipeLocalDataSource.getPopularTags()
            if (localTags.isNotEmpty()) {
                emit(Resource.Success(localTags.take(limit)))
            }

            val remoteTags = recipeRemoteDataSource.getPopularTags(limit)
            recipeLocalDataSource.savePopularTags(remoteTags)

            if (localTags.isEmpty() || remoteTags != localTags) {
                emit(Resource.Success(remoteTags))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular tags")
            val cachedData = recipeLocalDataSource.getPopularTags()
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData.take(limit)))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load popular tags"))
            }
        }
    }

    override fun checkRecipeFavoriteStatus(userId: String, recipeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try{
            val isFavorite = recipeRemoteDataSource.checkRecipeFavoriteStatus(userId, recipeId)
            emit(Resource.Success(isFavorite))
        } catch (e: Exception) {
            Timber.e(e, "Error checking favorite status")
            emit(Resource.Error(e.message ?: "Failed to check favorite status"))
        }
    }

    override fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.markRecipeAsFavorite(userId, recipeId, isFavorite)
            if (success) {
                // Update local cache
                recipeLocalDataSource.updateFavoriteStatus(userId, recipeId, isFavorite)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to update favorite status"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating favorite status")
            emit(Resource.Error(e.message ?: "Failed to update favorite status"))
        }
    }

    override fun incrementViewCount(recipeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.incrementViewCount(recipeId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to update view count"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing view count")
            emit(Resource.Error(e.message ?: "Failed to update view count"))
        }
    }

    override fun likeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.likeRecipe(userId, recipeId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to like recipe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error liking recipe")
            emit(Resource.Error(e.message ?: "Failed to like recipe"))
        }
    }

    override fun unlikeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.unlikeRecipe(userId, recipeId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to unlike recipe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error unliking recipe")
            emit(Resource.Error(e.message ?: "Failed to unlike recipe"))
        }
    }

    override fun uploadRecipeImage(recipeId: String, imageData: ByteArray): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val imageUrl = recipeRemoteDataSource.uploadRecipeImage(recipeId, imageData)
            if (imageUrl != null) {
                emit(Resource.Success(imageUrl))
            } else {
                emit(Resource.Error("Failed to upload image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading recipe image")
            emit(Resource.Error(e.message ?: "Failed to upload image"))
        }
    }

    override fun deleteRecipeImage(imageUrl: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val success = recipeRemoteDataSource.deleteRecipeImage(imageUrl)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe image")
            emit(Resource.Error(e.message ?: "Failed to delete image"))
        }
    }

    override fun searchUserRecipes(userId: String, query: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            // Check cache first for this user's search query
            val cachedResults = recipeLocalDataSource.searchUserRecipes(userId, query, limit)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Search remote for user's recipes only
            val searchResults = recipeRemoteDataSource.searchUserRecipes(userId, query, limit)
            recipeLocalDataSource.saveUserSearchResults(userId, query, searchResults)

            if (cachedResults.isEmpty() || searchResults != cachedResults) {
                emit(Resource.Success(searchResults))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching user recipes")
            val cachedData = recipeLocalDataSource.searchUserRecipes(userId, query, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to search user recipes"))
            }
        }
    }

    // Helper extension function to convert Recipe to RecipeListItem
    private fun Recipe.toListItem(): RecipeListItem {
        return RecipeListItem(
            recipeId = this.recipeId,
            title = this.title,
            description = this.description,
            coverImage = this.coverImage,
            prepTime = this.prepTime,
            cookTime = this.cookTime,
            servings = this.servings,
            difficultyLevel = this.difficultyLevel,
            likeCount = this.likeCount,
            viewCount = this.viewCount,
            isPublic = this.isPublic,
            isFeatured = this.isFeatured,
            userId = this.userId,
            createdAt = this.createdAt,
            tags = this.tags
        )
    }
}