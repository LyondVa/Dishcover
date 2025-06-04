// RecipeRepository.kt
package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.domain.model.Ingredient
import com.nhatpham.dishcover.domain.model.RecipeCategory
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    // Recipe CRUD operations
    fun createRecipe(recipe: Recipe): Flow<Resource<Recipe>>
    fun updateRecipe(recipe: Recipe): Flow<Resource<Recipe>>
    fun deleteRecipe(recipeId: String): Flow<Resource<Boolean>>
    fun getRecipe(recipeId: String): Flow<Resource<Recipe>>

    // Recipe queries
    fun getFavoriteRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecentRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecipesByCategory(userId: String, category: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getAllRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun searchRecipes(query: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>>

    // Category operations
    fun getCategories(userId: String): Flow<Resource<List<String>>>
    fun getSystemCategories(): Flow<Resource<List<RecipeCategory>>>
    fun createCustomCategory(userId: String, categoryName: String): Flow<Resource<RecipeCategory>>

    // Ingredient operations
    fun getSystemIngredients(): Flow<Resource<List<Ingredient>>>
    fun getUserIngredients(userId: String): Flow<Resource<List<Ingredient>>>
    fun createIngredient(ingredient: Ingredient): Flow<Resource<Ingredient>>
    fun searchIngredients(query: String, userId: String): Flow<Resource<List<Ingredient>>>

    // Tag operations
    fun getRecipeTags(recipeId: String): Flow<Resource<List<String>>>
    fun addRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>>
    fun removeRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>>
    fun getPopularTags(limit: Int = 20): Flow<Resource<List<String>>>

    // Recipe interactions
    fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Flow<Resource<Boolean>>
    fun incrementViewCount(recipeId: String): Flow<Resource<Boolean>>
    fun likeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>>
    fun unlikeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>>

    // Image operations
    fun uploadRecipeImage(recipeId: String, imageData: ByteArray): Flow<Resource<String>>
    fun deleteRecipeImage(imageUrl: String): Flow<Resource<Boolean>>
}