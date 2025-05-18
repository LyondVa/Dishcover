package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource,
    private val recipeLocalDataSource: RecipeLocalDataSource
) : RecipeRepository {

    override fun getRecipe(recipeId: String): Flow<Resource<Recipe>> = flow {
        emit(Resource.Loading())
        try {
            // First check local cache
            val localRecipe = recipeLocalDataSource.getRecipeById(recipeId)
            localRecipe?.let {
                emit(Resource.Success(it))
            }

            // Then fetch from remote to update local cache
            val remoteRecipe = recipeRemoteDataSource.getRecipeById(recipeId)

            // Handle the remote recipe result
            remoteRecipe?.let {
                // Save to local cache
                recipeLocalDataSource.saveRecipe(it)

                // Emit remote recipe if local was null
                if (localRecipe == null) {
                    emit(Resource.Success(it))
                }
            } ?: emit(Resource.Error("Recipe not found"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load recipe"))
        }
    }

    // Other repository methods implementation...
    override fun getFavoriteRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getFavoriteRecipes(userId, limit)
            recipeLocalDataSource.saveFavoriteRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load favorite recipes"))
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

            if (localRecipes.isEmpty()) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load recent recipes"))
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

            if (localRecipes.isEmpty()) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load recipes by category"))
        }
    }

    override fun getAllRecipes(userId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getAllRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Resource.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getAllRecipes(userId, limit)
            recipeLocalDataSource.saveAllRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Resource.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load all recipes"))
        }
    }

    override fun searchRecipes(query: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val searchResults = recipeRemoteDataSource.searchRecipes(query, limit)
            emit(Resource.Success(searchResults))

            recipeLocalDataSource.saveSearchResults(query, searchResults)
        } catch (e: Exception) {
            try {
                val localResults = recipeLocalDataSource.getSearchResults(query, limit)
                if (localResults.isNotEmpty()) {
                    emit(Resource.Success(localResults))
                } else {
                    emit(Resource.Error(e.message ?: "Failed to search recipes"))
                }
            } catch (e2: Exception) {
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

            if (localCategories.isEmpty()) {
                emit(Resource.Success(remoteCategories))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load categories"))
        }
    }
}