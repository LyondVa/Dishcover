package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.ErrorConverter
import com.nhatpham.dishcover.util.error.RecipeError
import com.nhatpham.dishcover.util.error.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource,
    private val recipeLocalDataSource: RecipeLocalDataSource
) : RecipeRepository {

    override fun getRecipe(recipeId: String): Flow<Result<Recipe>> = flow {
        emit(Result.Loading())
        try {
            val localRecipe = recipeLocalDataSource.getRecipeById(recipeId)
            if (localRecipe != null) {
                emit(Result.Success(localRecipe))
            }

            val remoteRecipe = recipeRemoteDataSource.getRecipeById(recipeId)
            if (remoteRecipe != null) {
                recipeLocalDataSource.saveRecipe(remoteRecipe as Recipe)

                if (localRecipe == null) {
                    emit(Result.Success(remoteRecipe))
                }
            } else {
                if (localRecipe == null) {
                    // Use specific NotFoundError for recipes
                    emit(Result.Error(AppError.DomainError.NotFoundError(
                        entityType = "Recipe",
                        identifier = recipeId,
                        message = "Recipe with ID $recipeId not found"
                    )))
                }
            }
        } catch (e: Exception) {
            // Convert general exceptions to appropriate RecipeError types or use ErrorConverter
            emit(Result.Error(ErrorConverter.fromThrowable(e)))
        }
    }

    override fun getFavoriteRecipes(userId: String, limit: Int): Flow<Result<List<RecipeListItem>>> = flow {
        emit(Result.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getFavoriteRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Result.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getFavoriteRecipes(userId, limit)
            recipeLocalDataSource.saveFavoriteRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Result.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            // Use RecipeAccessError with appropriate action
            emit(Result.Error(RecipeError.RecipeAccessError(
                recipeId = "favorites",
                action = "access favorite recipes",
                message = "Failed to load favorite recipes: ${e.message}",
                cause = e
            )))
        }
    }

    override fun getRecentRecipes(userId: String, limit: Int): Flow<Result<List<RecipeListItem>>> = flow {
        emit(Result.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getRecentRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Result.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getRecentRecipes(userId, limit)
            recipeLocalDataSource.saveRecentRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Result.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            // Convert the exception to an appropriate RecipeError
            emit(Result.Error(AppError.DataError.SyncError(
                message = "Failed to load recent recipes",
                entity = "Recipe",
                operation = "getRecentRecipes",
                cause = e
            )))
        }
    }

    override fun getRecipesByCategory(userId: String, category: String, limit: Int): Flow<Result<List<RecipeListItem>>> = flow {
        emit(Result.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getRecipesByCategory(userId, category, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Result.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getRecipesByCategory(userId, category, limit)
            recipeLocalDataSource.saveRecipesByCategory(userId, category, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Result.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            // Use a more specific error type
            emit(Result.Error(AppError.DomainError.NotFoundError(
                entityType = "Category",
                identifier = category,
                message = "Failed to load recipes in category '$category': ${e.message}",
                cause = e
            )))
        }
    }

    override fun getAllRecipes(userId: String, limit: Int): Flow<Result<List<RecipeListItem>>> = flow {
        emit(Result.Loading())
        try {
            val localRecipes = recipeLocalDataSource.getAllRecipes(userId, limit)
            if (localRecipes.isNotEmpty()) {
                emit(Result.Success(localRecipes))
            }

            val remoteRecipes = recipeRemoteDataSource.getAllRecipes(userId, limit)
            recipeLocalDataSource.saveAllRecipes(userId, remoteRecipes)

            if (localRecipes.isEmpty()) {
                emit(Result.Success(remoteRecipes))
            }
        } catch (e: Exception) {
            // Use a more specific error type
            emit(Result.Error(AppError.DataError.SyncError(
                message = "Failed to load recipes",
                entity = "Recipe",
                operation = "getAllRecipes",
                cause = e
            )))
        }
    }

    override fun searchRecipes(query: String, limit: Int): Flow<Result<List<RecipeListItem>>> = flow {
        emit(Result.Loading())
        try {
            val searchResults = recipeRemoteDataSource.searchRecipes(query, limit)
            emit(Result.Success(searchResults))

            recipeLocalDataSource.saveSearchResults(query, searchResults)
        } catch (e: Exception) {
            try {
                val localResults = recipeLocalDataSource.getSearchResults(query, limit)
                if (localResults.isNotEmpty()) {
                    emit(Result.Success(localResults))
                } else {
                    // Fix the incomplete error here
                    emit(Result.Error(AppError.DomainError.ValidationError(
                        message = "No recipes found matching '$query'",
                        field = "searchQuery",
                        value = query
                    )))
                }
            } catch (e2: Exception) {
                emit(Result.Error(AppError.DataError.NetworkError.ConnectionError(
                    message = "Failed to search recipes: ${e.message}",
                    cause = e
                )))
            }
        }
    }

    override fun getCategories(userId: String): Flow<Result<List<String>>> = flow {
        emit(Result.Loading())
        try {
            val localCategories = recipeLocalDataSource.getCategories(userId)
            if (localCategories.isNotEmpty()) {
                emit(Result.Success(localCategories))
            }

            val remoteCategories = recipeRemoteDataSource.getCategories(userId)
            recipeLocalDataSource.saveCategories(userId, remoteCategories)

            if (localCategories.isEmpty()) {
                emit(Result.Success(remoteCategories))
            }
        } catch (e: Exception) {
            // Use more specific error
            emit(Result.Error(AppError.DataError.SyncError(
                message = "Failed to load categories: ${e.message}",
                entity = "Category",
                operation = "getCategories",
                cause = e
            )))
        }
    }
}