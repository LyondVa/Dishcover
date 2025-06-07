// RecipeRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.model.*
import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeCategory
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
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
            print("createRecipe" + recipe.title)
            val createdRecipe = recipeRemoteDataSource.createRecipe(recipe)
            if (createdRecipe != null) {
                // Save to local cache
                recipeLocalDataSource.saveRecipe(createdRecipe)

                // Add to user's recipe lists
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
                // Update local cache
                recipeLocalDataSource.saveRecipe(updatedRecipe)

                // Update in user's recipe lists
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
            // Get recipe first to know the user ID
            val recipe = recipeLocalDataSource.getRecipeById(recipeId)

            val success = recipeRemoteDataSource.deleteRecipe(recipeId)
            if (success) {
                // Remove from local cache
                recipeLocalDataSource.deleteRecipe(recipeId)

                // Remove from user's recipe lists
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
            // Check local cache first
            val localRecipe = recipeLocalDataSource.getRecipeById(recipeId)
            localRecipe?.let {
                emit(Resource.Success(it))
            }

            // Fetch from remote to ensure latest data
            val remoteRecipe = recipeRemoteDataSource.getRecipeById(recipeId)
            if (remoteRecipe != null) {
                // Save to local cache
                recipeLocalDataSource.saveRecipe(remoteRecipe)

                // Emit if local was null or if different from local
                if (localRecipe == null) {
                    emit(Resource.Success(remoteRecipe))
                }
            } else if (localRecipe == null) {
                emit(Resource.Error("Recipe not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe")
            // If we have local data, don't emit error
            if (recipeLocalDataSource.getRecipeById(recipeId) == null) {
                emit(Resource.Error(e.message ?: "Failed to load recipe"))
            }
        }
    }

    override fun getFavoriteRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first
            val localRecipes = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            // Fetch from remote
            val remoteRecipes = recipeRemoteDataSource.getFavoriteRecipes(userId, limit)
            recipeLocalDataSource.saveFavoriteRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty() || remoteRecipes != localRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting favorite recipes")
            // Return cached data if available, otherwise emit error
            val cachedData = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (cachedData.isNotEmpty()) {
                emit(Resource.Success(cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load favorite recipes"))
            }
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
            val localRecipes = recipeLocalDataSource.getAllRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getAllRecipes(userId, limit)
            recipeLocalDataSource.saveAllRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty() || remoteRecipes != localRecipes) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all recipes")
            val cachedData = recipeLocalDataSource.getAllRecipes(userId, limit)
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