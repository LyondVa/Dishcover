// RecipeRepository.kt - Updated with new methods
package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.RecipeCategory
import com.nhatpham.dishcover.domain.model.recipe.RecipeRating
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    // Existing Recipe CRUD operations
    fun createRecipe(recipe: Recipe): Flow<Resource<Recipe>>
    fun updateRecipe(recipe: Recipe): Flow<Resource<Recipe>>
    fun deleteRecipe(recipeId: String): Flow<Resource<Boolean>>
    fun getRecipe(recipeId: String): Flow<Resource<Recipe>>

    // Existing Recipe queries
    fun getFavoriteRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecentRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getRecipesByCategory(userId: String, category: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun getUserRecipes(userId: String, limit: Int = 10): Flow<Resource<List<RecipeListItem>>>
    fun searchRecipes(query: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>>
    fun searchUserRecipes(userId: String, query: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>>

    // Existing Category operations
    fun getCategories(userId: String): Flow<Resource<List<String>>>
    fun getSystemCategories(): Flow<Resource<List<RecipeCategory>>>
    fun createCustomCategory(userId: String, categoryName: String): Flow<Resource<RecipeCategory>>

    // Existing Ingredient operations
    fun getSystemIngredients(): Flow<Resource<List<Ingredient>>>
    fun getUserIngredients(userId: String): Flow<Resource<List<Ingredient>>>
    fun createIngredient(ingredient: Ingredient): Flow<Resource<Ingredient>>
    fun searchIngredients(query: String, userId: String): Flow<Resource<List<Ingredient>>>

    // Existing Tag operations
    fun getRecipeTags(recipeId: String): Flow<Resource<List<String>>>
    fun addRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>>
    fun removeRecipeTag(recipeId: String, tag: String): Flow<Resource<Boolean>>
    fun getPopularTags(limit: Int = 20): Flow<Resource<List<String>>>

    // Existing Recipe interactions
    fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Flow<Resource<Boolean>>
    fun checkRecipeFavoriteStatus(userId: String, recipeId: String): Flow<Resource<Boolean>>
    fun incrementViewCount(recipeId: String): Flow<Resource<Boolean>>
    fun likeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>>
    fun unlikeRecipe(userId: String, recipeId: String): Flow<Resource<Boolean>>

    // Existing Image operations
    fun uploadRecipeImage(recipeId: String, imageData: ByteArray): Flow<Resource<String>>
    fun deleteRecipeImage(imageUrl: String): Flow<Resource<Boolean>>

    // NEW: Recipe Rating operations
    fun getRecipeRatings(recipeId: String): Flow<Resource<RecipeRatingAggregate>>
    fun addRecipeRating(rating: RecipeRating): Flow<Resource<RecipeRating>>
    fun getUserRecipeRating(recipeId: String, userId: String): Flow<Resource<RecipeRating?>>
    fun updateRecipeRating(rating: RecipeRating): Flow<Resource<RecipeRating>>
    fun deleteRecipeRating(recipeId: String, userId: String): Flow<Resource<Boolean>>

    // NEW: Recipe Review operations
    fun getRecipeReviews(recipeId: String, limit: Int = 20, offset: Int = 0): Flow<Resource<List<RecipeReview>>>
    fun addRecipeReview(review: RecipeReview): Flow<Resource<RecipeReview>>
    fun updateRecipeReview(review: RecipeReview): Flow<Resource<RecipeReview>>
    fun deleteRecipeReview(reviewId: String, userId: String): Flow<Resource<Boolean>>
    fun markReviewHelpful(reviewId: String, userId: String, helpful: Boolean): Flow<Resource<Boolean>>
    fun getUserReviewForRecipe(recipeId: String, userId: String): Flow<Resource<RecipeReview?>>

    // NEW: Nutritional Information operations
    fun getNutritionalInfo(recipeId: String): Flow<Resource<NutritionalInfo>>
    fun calculateNutritionalInfo(recipe: Recipe): Flow<Resource<NutritionalInfo>>
    fun updateNutritionalInfo(nutritionalInfo: NutritionalInfo): Flow<Resource<NutritionalInfo>>
}