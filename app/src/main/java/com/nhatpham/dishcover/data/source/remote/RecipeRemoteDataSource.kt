// RecipeRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.mapper.*
import com.nhatpham.dishcover.data.model.dto.*
import com.nhatpham.dishcover.data.model.dto.recipe.*
import com.nhatpham.dishcover.domain.model.recipe.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecipeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    // Firestore collections
    private val recipesCollection = firestore.collection("RECIPES")
    private val ingredientsCollection = firestore.collection("INGREDIENTS")
    private val recipeIngredientsCollection = firestore.collection("RECIPE_INGREDIENTS")
    private val recipeTagsCollection = firestore.collection("RECIPE_TAGS")
    private val recipeCategoriesCollection = firestore.collection("RECIPE_CATEGORIES")
    private val savedRecipesCollection = firestore.collection("SAVED_RECIPES")
    private val recipeLikesCollection = firestore.collection("RECIPE_LIKES")
    private val recipeViewsCollection = firestore.collection("RECIPE_VIEWS")
    private val recipeStepsCollection = firestore.collection("RECIPE_STEPS")

    // NEW: Additional collections for missing functionality
    private val recipeRatingsCollection = firestore.collection("RECIPE_RATINGS")
    private val recipeReviewsCollection = firestore.collection("RECIPE_REVIEWS")
    private val reviewInteractionsCollection = firestore.collection("REVIEW_INTERACTIONS")
    private val nutritionalInfoCollection = firestore.collection("NUTRITIONAL_INFO")

    // Recipe CRUD operations
    suspend fun createRecipe(recipe: Recipe): Recipe? {
        return try {
            val recipeId = recipe.recipeId.takeIf { it.isNotBlank() }
                ?: recipesCollection.document().id

            val recipeDto = recipe.copy(
                recipeId = recipeId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toDto()

            recipesCollection.document(recipeId)
                .set(recipeDto)
                .await()

            saveRecipeIngredients(recipeId, recipe.ingredients)
            saveRecipeTags(recipeId, recipe.tags)

            recipe.copy(
                recipeId = recipeId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating recipe")
            null
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Recipe? {
        return try {
            val recipeId = recipe.recipeId
            if (recipeId.isBlank()) {
                Timber.e("Cannot update recipe with blank ID")
                return null
            }

            val updatedRecipe = recipe.copy(updatedAt = Timestamp.now())
            val recipeDto = updatedRecipe.toDto()

            recipesCollection.document(recipeId)
                .set(recipeDto)
                .await()

            deleteRecipeIngredients(recipeId)
            saveRecipeIngredients(recipeId, recipe.ingredients)

            deleteRecipeTags(recipeId)
            saveRecipeTags(recipeId, recipe.tags)

            updatedRecipe
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe")
            null
        }
    }

    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            recipesCollection.document(recipeId).delete().await()

            deleteRecipeIngredients(recipeId)
            deleteRecipeTags(recipeId)
            deleteRecipeSteps(recipeId)
            deleteSavedRecipes(recipeId)
            deleteRecipeLikes(recipeId)
            deleteRecipeViews(recipeId)
            deleteRecipeRatings(recipeId)
            deleteRecipeReviews(recipeId)
            deleteNutritionalInfo(recipeId)

            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe")
            false
        }
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        return try {
            val recipeDoc = recipesCollection.document(recipeId).get().await()
            if (!recipeDoc.exists()) return null

            val recipeDto = recipeDoc.toObject(RecipeDto::class.java) ?: return null
            val ingredients = getRecipeIngredients(recipeId)
            val tags = getRecipeTags(recipeId)

            recipeDto.toDomain().copy(
                ingredients = ingredients,
                tags = tags
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe by ID: $recipeId")
            null
        }
    }

    // Recipe query operations
    suspend fun getFavoriteRecipes(userId: String, limit: Int): List<RecipeListItem> {
        return try {
            val savedRecipesSnapshot = savedRecipesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("savedCategory", "favorite")
                .limit(limit.toLong())
                .get()
                .await()

            val recipeIds = savedRecipesSnapshot.documents.mapNotNull {
                it.getString("recipeId")
            }

            if (recipeIds.isEmpty()) return emptyList()

            getRecipesByIds(recipeIds)
        } catch (e: Exception) {
            Timber.e(e, "Error getting favorite recipes")
            emptyList()
        }
    }

    suspend fun getRecentRecipes(userId: String, limit: Int): List<RecipeListItem> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent recipes")
            emptyList()
        }
    }

    suspend fun getRecipesByCategory(userId: String, category: String, limit: Int): List<RecipeListItem> {
        return try {
            val query = if (category == "all") {
                recipesCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isPublic", true)
            } else {
                recipesCollection
                    .whereEqualTo("userId", userId)
                    .whereArrayContains("tags", category)
            }

            val snapshot = query
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipes by category")
            emptyList()
        }
    }

    suspend fun getUserRecipes(userId: String, limit: Int): List<RecipeListItem> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user recipes")
            emptyList()
        }
    }

    suspend fun searchRecipes(query: String, limit: Int): List<RecipeListItem> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("isPublic", true)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching recipes")
            emptyList()
        }
    }

    suspend fun searchUserRecipes(userId: String, query: String, limit: Int): List<RecipeListItem> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("userId", userId)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching user recipes")
            emptyList()
        }
    }

    // Category operations
    suspend fun getCategories(userId: String): List<String> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val categories = mutableSetOf<String>()
            snapshot.documents.forEach { doc ->
                val tags = doc.get("tags") as? List<String>
                tags?.let { categories.addAll(it) }
            }

            categories.toList().sorted()
        } catch (e: Exception) {
            Timber.e(e, "Error getting categories")
            emptyList()
        }
    }

    suspend fun getSystemCategories(): List<RecipeCategory> {
        return try {
            val snapshot = recipeCategoriesCollection
                .whereEqualTo("isSystemCategory", true)
                .orderBy("name")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeCategoryDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting system categories")
            emptyList()
        }
    }

    suspend fun createCustomCategory(userId: String, categoryName: String): RecipeCategory? {
        return try {
            val categoryId = recipeCategoriesCollection.document().id
            val category = RecipeCategory(
                categoryId = categoryId,
                name = categoryName,
                description = null,
                isSystemCategory = false,
                createdBy = userId,
                createdAt = Timestamp.now()
            )

            recipeCategoriesCollection.document(categoryId)
                .set(category.toDto())
                .await()

            category
        } catch (e: Exception) {
            Timber.e(e, "Error creating custom category")
            null
        }
    }

    // Ingredient operations
    suspend fun getSystemIngredients(): List<Ingredient> {
        return try {
            val snapshot = ingredientsCollection
                .whereEqualTo("isSystemIngredient", true)
                .orderBy("name")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(IngredientDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting system ingredients")
            emptyList()
        }
    }

    suspend fun getUserIngredients(userId: String): List<Ingredient> {
        return try {
            val snapshot = ingredientsCollection
                .whereEqualTo("createdBy", userId)
                .orderBy("name")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(IngredientDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user ingredients")
            emptyList()
        }
    }

    suspend fun createIngredient(ingredient: Ingredient): Ingredient? {
        return try {
            val ingredientId = ingredient.ingredientId.takeIf { it.isNotBlank() }
                ?: ingredientsCollection.document().id

            val newIngredient = ingredient.copy(
                ingredientId = ingredientId,
                createdAt = Timestamp.now()
            )

            ingredientsCollection.document(ingredientId)
                .set(newIngredient.toDto())
                .await()

            newIngredient
        } catch (e: Exception) {
            Timber.e(e, "Error creating ingredient")
            null
        }
    }

    suspend fun searchIngredients(query: String, userId: String): List<Ingredient> {
        return try {
            val systemIngredientsTask = ingredientsCollection
                .whereEqualTo("isSystemIngredient", true)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()

            val userIngredientsTask = ingredientsCollection
                .whereEqualTo("createdBy", userId)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()

            val systemResults = systemIngredientsTask.await().documents.mapNotNull { doc ->
                doc.toObject(IngredientDto::class.java)?.toDomain()
            }

            val userResults = userIngredientsTask.await().documents.mapNotNull { doc ->
                doc.toObject(IngredientDto::class.java)?.toDomain()
            }

            (systemResults + userResults).distinctBy { it.name }
        } catch (e: Exception) {
            Timber.e(e, "Error searching ingredients")
            emptyList()
        }
    }

    // Tag operations
    suspend fun getRecipeTags(recipeId: String): List<String> {
        return try {
            val snapshot = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("tagName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe tags")
            emptyList()
        }
    }

    suspend fun addRecipeTag(recipeId: String, tag: String): Boolean {
        return try {
            val existingTag = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("tagName", tag)
                .get()
                .await()

            if (existingTag.isEmpty) {
                val tagDto = RecipeTagDto(
                    recipeTagId = recipeTagsCollection.document().id,
                    recipeId = recipeId,
                    tagName = tag,
                    createdAt = Timestamp.now()
                )

                recipeTagsCollection.document(tagDto.recipeTagId!!)
                    .set(tagDto)
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe tag")
            false
        }
    }

    suspend fun removeRecipeTag(recipeId: String, tag: String): Boolean {
        return try {
            val snapshot = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("tagName", tag)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe tag")
            false
        }
    }

    suspend fun getPopularTags(limit: Int): List<String> {
        return try {
            val snapshot = recipeTagsCollection
                .get()
                .await()

            val tagCounts = mutableMapOf<String, Int>()
            snapshot.documents.forEach { doc ->
                val tagName = doc.getString("tagName")
                if (tagName != null) {
                    tagCounts[tagName] = (tagCounts[tagName] ?: 0) + 1
                }
            }

            tagCounts.toList()
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular tags")
            emptyList()
        }
    }

    // NEW: Rating operations
    suspend fun getRecipeRatingAggregate(recipeId: String): RecipeRatingAggregate? {
        return try {
            val snapshot = recipeRatingsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return RecipeRatingAggregate(recipeId = recipeId)
            }

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeRatingDto::class.java)?.toDomain()
            }

            val totalRatings = ratings.size
            val averageRating = if (totalRatings > 0) {
                ratings.sumOf { it.rating.toDouble() } / totalRatings
            } else 0.0

            val ratingDistribution = mapOf(
                1 to ratings.count { it.rating == 1 },
                2 to ratings.count { it.rating == 2 },
                3 to ratings.count { it.rating == 3 },
                4 to ratings.count { it.rating == 4 },
                5 to ratings.count { it.rating == 5 }
            )

            RecipeRatingAggregate(
                recipeId = recipeId,
                totalRatings = totalRatings,
                averageRating = averageRating,
                ratingDistribution = ratingDistribution
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe rating aggregate")
            null
        }
    }

    suspend fun addRecipeRating(rating: RecipeRating): RecipeRating? {
        return try {
            val ratingId = rating.ratingId.takeIf { it.isNotBlank() }
                ?: recipeRatingsCollection.document().id

            val newRating = rating.copy(
                ratingId = ratingId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            recipeRatingsCollection.document(ratingId)
                .set(newRating.toDto())
                .await()

            newRating
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe rating")
            null
        }
    }

    suspend fun getUserRecipeRating(recipeId: String, userId: String): RecipeRating? {
        return try {
            val snapshot = recipeRatingsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()
                ?.toObject(RecipeRatingDto::class.java)
                ?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user recipe rating")
            null
        }
    }

    suspend fun updateRecipeRating(rating: RecipeRating): RecipeRating? {
        return try {
            val updatedRating = rating.copy(updatedAt = Timestamp.now())

            recipeRatingsCollection.document(rating.ratingId)
                .set(updatedRating.toDto())
                .await()

            updatedRating
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe rating")
            null
        }
    }

    suspend fun deleteRecipeRating(recipeId: String, userId: String): Boolean {
        return try {
            val snapshot = recipeRatingsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe rating")
            false
        }
    }

    // NEW: Review operations
    suspend fun getRecipeReviews(recipeId: String, limit: Int, offset: Int): List<RecipeReview> {
        return try {
            val snapshot = recipeReviewsCollection
                .whereEqualTo("recipeId", recipeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents
                .drop(offset)
                .mapNotNull { doc ->
                    doc.toObject(RecipeReviewDto::class.java)?.toDomain()
                }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe reviews")
            emptyList()
        }
    }

    suspend fun addRecipeReview(review: RecipeReview): RecipeReview? {
        return try {
            val reviewId = review.reviewId.takeIf { it.isNotBlank() }
                ?: recipeReviewsCollection.document().id

            val newReview = review.copy(
                reviewId = reviewId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            recipeReviewsCollection.document(reviewId)
                .set(newReview.toDto())
                .await()

            newReview
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe review")
            null
        }
    }

    suspend fun updateRecipeReview(review: RecipeReview): RecipeReview? {
        return try {
            val updatedReview = review.copy(updatedAt = Timestamp.now())

            recipeReviewsCollection.document(review.reviewId)
                .set(updatedReview.toDto())
                .await()

            updatedReview
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe review")
            null
        }
    }

    suspend fun deleteRecipeReview(reviewId: String, userId: String): Boolean {
        return try {
            val reviewDoc = recipeReviewsCollection.document(reviewId).get().await()
            val review = reviewDoc.toObject(RecipeReviewDto::class.java)

            if (review?.userId == userId) {
                recipeReviewsCollection.document(reviewId).delete().await()

                // Also delete associated interactions
                val interactions = reviewInteractionsCollection
                    .whereEqualTo("reviewId", reviewId)
                    .get()
                    .await()

                interactions.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe review")
            false
        }
    }

    suspend fun markReviewHelpful(reviewId: String, userId: String, helpful: Boolean): Boolean {
        return try {
            val existingInteraction = reviewInteractionsCollection
                .whereEqualTo("reviewId", reviewId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (helpful) {
                if (existingInteraction.isEmpty) {
                    val interaction = RecipeReviewInteraction(
                        interactionId = reviewInteractionsCollection.document().id,
                        reviewId = reviewId,
                        userId = userId,
                        type = ReviewInteractionType.HELPFUL,
                        createdAt = Timestamp.now()
                    )

                    reviewInteractionsCollection.document(interaction.interactionId)
                        .set(interaction.toDto())
                        .await()

                    // Update helpful count in review
                    recipeReviewsCollection.document(reviewId)
                        .update("helpful", FieldValue.increment(1))
                        .await()
                }
            } else {
                existingInteraction.documents.forEach { doc ->
                    doc.reference.delete().await()

                    // Decrement helpful count
                    recipeReviewsCollection.document(reviewId)
                        .update("helpful", FieldValue.increment(-1))
                        .await()
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error marking review helpful")
            false
        }
    }

    suspend fun getUserReviewForRecipe(recipeId: String, userId: String): RecipeReview? {
        return try {
            val snapshot = recipeReviewsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()
                ?.toObject(RecipeReviewDto::class.java)
                ?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user review for recipe")
            null
        }
    }

    // NEW: Nutritional information operations
    suspend fun getNutritionalInfo(recipeId: String): NutritionalInfo? {
        return try {
            val doc = nutritionalInfoCollection.document(recipeId).get().await()
            doc.toObject(NutritionalInfoDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting nutritional info")
            null
        }
    }

    suspend fun calculateNutritionalInfo(recipe: Recipe): NutritionalInfo? {
        return try {
            // This would typically involve calling a nutrition API or database
            // For now, we'll create a placeholder implementation
            val estimatedCalories = recipe.ingredients.size * 50 // Simple estimation

            val nutritionalInfo = NutritionalInfo(
                recipeId = recipe.recipeId,
                calories = estimatedCalories,
                protein = 10.0,
                carbohydrates = 20.0,
                fat = 5.0,
                fiber = 3.0,
                sugar = 5.0,
                sodium = 200.0,
                cholesterol = 0.0,
                iron = 2.0,
                calcium = 100.0,
                vitaminC = 10.0,
                perServing = true,
                servingSize = "1 serving",
                isEstimated = true
            )

            nutritionalInfoCollection.document(recipe.recipeId)
                .set(nutritionalInfo.toDto())
                .await()

            nutritionalInfo
        } catch (e: Exception) {
            Timber.e(e, "Error calculating nutritional info")
            null
        }
    }

    suspend fun updateNutritionalInfo(nutritionalInfo: NutritionalInfo): NutritionalInfo? {
        return try {
            nutritionalInfoCollection.document(nutritionalInfo.recipeId)
                .set(nutritionalInfo.toDto())
                .await()

            nutritionalInfo
        } catch (e: Exception) {
            Timber.e(e, "Error updating nutritional info")
            null
        }
    }

    // Recipe interaction operations
    suspend fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Boolean {
        return try {
            val existingSave = savedRecipesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("savedCategory", "favorite")
                .get()
                .await()

            if (isFavorite) {
                if (existingSave.isEmpty) {
                    val savedRecipe = SavedRecipeDto(
                        savedRecipeId = savedRecipesCollection.document().id,
                        userId = userId,
                        recipeId = recipeId,
                        savedCategory = "favorite",
                        savedAt = Timestamp.now()
                    )

                    savedRecipesCollection.document(savedRecipe.savedRecipeId!!)
                        .set(savedRecipe)
                        .await()
                }
            } else {
                existingSave.documents.forEach { doc ->
                    doc.reference.delete().await()
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error marking recipe as favorite")
            false
        }
    }

    suspend fun incrementViewCount(recipeId: String): Boolean {
        return try {
            val viewDoc = recipeViewsCollection.document(recipeId)
            val doc = viewDoc.get().await()

            if (doc.exists()) {
                viewDoc.update("viewCount", FieldValue.increment(1)).await()
            } else {
                viewDoc.set(mapOf("viewCount" to 1, "updatedAt" to Timestamp.now())).await()
            }

            // Also update the recipe document
            recipesCollection.document(recipeId)
                .update("viewCount", FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing view count")
            false
        }
    }

    suspend fun likeRecipe(userId: String, recipeId: String): Boolean {
        return try {
            val existingLike = recipeLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            if (existingLike.isEmpty) {
                val recipeLike = RecipeLikeDto(
                    likeId = recipeLikesCollection.document().id,
                    userId = userId,
                    recipeId = recipeId,
                    likedAt = Timestamp.now()
                )

                recipeLikesCollection.document(recipeLike.likeId!!)
                    .set(recipeLike)
                    .await()

                // Update like count in recipe
                recipesCollection.document(recipeId)
                    .update("likeCount", FieldValue.increment(1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking recipe")
            false
        }
    }

    suspend fun unlikeRecipe(userId: String, recipeId: String): Boolean {
        return try {
            val existingLike = recipeLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            existingLike.documents.forEach { doc ->
                doc.reference.delete().await()

                // Decrement like count
                recipesCollection.document(recipeId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking recipe")
            false
        }
    }

    // Image operations
    suspend fun uploadRecipeImage(recipeId: String, imageData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("recipe_images")
                .child("$recipeId/${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putBytes(imageData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading recipe image")
            null
        }
    }

    suspend fun deleteRecipeImage(imageUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe image")
            false
        }
    }

    // Helper methods
    private suspend fun getRecipesByIds(recipeIds: List<String>): List<RecipeListItem> {
        return try {
            if (recipeIds.isEmpty()) return emptyList()

            val batches = recipeIds.chunked(10) // Firestore 'in' query limit is 10
            val allRecipes = mutableListOf<RecipeListItem>()

            batches.forEach { batch ->
                val snapshot = recipesCollection
                    .whereIn("recipeId", batch)
                    .get()
                    .await()

                val recipes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RecipeDto::class.java)?.toListItem()
                }
                allRecipes.addAll(recipes)
            }

            allRecipes
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipes by IDs")
            emptyList()
        }
    }

    private suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient> {
        return try {
            val snapshot = recipeIngredientsCollection
                .whereEqualTo("recipeId", recipeId)
                .orderBy("displayOrder")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val recipeIngredientDto = doc.toObject(RecipeIngredientDto::class.java)
                if (recipeIngredientDto != null) {
                    val ingredient = getIngredientById(recipeIngredientDto.ingredientId ?: "")
                    if (ingredient != null) {
                        recipeIngredientDto.toDomain(ingredient)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe ingredients")
            emptyList()
        }
    }

    private suspend fun getIngredientById(ingredientId: String): Ingredient? {
        return try {
            val doc = ingredientsCollection.document(ingredientId).get().await()
            doc.toObject(IngredientDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting ingredient by ID")
            null
        }
    }

    private suspend fun saveRecipeIngredients(recipeId: String, ingredients: List<RecipeIngredient>) {
        ingredients.forEachIndexed { index, ingredient ->
            try {
                val ingredientEntity = getOrCreateIngredient(ingredient.ingredient)

                val recipeIngredientDto = ingredient.copy(
                    recipeIngredientId = if (ingredient.recipeIngredientId.isBlank())
                        recipeIngredientsCollection.document().id
                    else ingredient.recipeIngredientId,
                    recipeId = recipeId,
                    ingredientId = ingredientEntity.ingredientId,
                    displayOrder = index
                ).toDto()

                recipeIngredientsCollection.document(recipeIngredientDto.recipeIngredientId!!)
                    .set(recipeIngredientDto)
                    .await()
            } catch (e: Exception) {
                Timber.e(e, "Error saving recipe ingredient: ${ingredient.ingredient.name}")
            }
        }
    }

    private suspend fun saveRecipeTags(recipeId: String, tags: List<String>) {
        tags.forEach { tag ->
            try {
                addRecipeTag(recipeId, tag)
            } catch (e: Exception) {
                Timber.e(e, "Error saving recipe tag: $tag")
            }
        }
    }

    private suspend fun getOrCreateIngredient(ingredient: Ingredient): Ingredient {
        return try {
            val existingIngredient = ingredientsCollection
                .whereEqualTo("name", ingredient.name)
                .limit(1)
                .get()
                .await()

            if (!existingIngredient.isEmpty) {
                existingIngredient.documents.first()
                    .toObject(IngredientDto::class.java)?.toDomain()
                    ?: ingredient
            } else {
                createIngredient(ingredient) ?: ingredient
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting or creating ingredient")
            ingredient
        }
    }

    // Cleanup helper methods
    private suspend fun deleteRecipeIngredients(recipeId: String) {
        try {
            val snapshot = recipeIngredientsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe ingredients")
        }
    }

    private suspend fun deleteRecipeTags(recipeId: String) {
        try {
            val snapshot = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe tags")
        }
    }

    private suspend fun deleteRecipeSteps(recipeId: String) {
        try {
            val snapshot = recipeStepsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe steps")
        }
    }

    private suspend fun deleteSavedRecipes(recipeId: String) {
        try {
            val snapshot = savedRecipesCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting saved recipes")
        }
    }

    private suspend fun deleteRecipeLikes(recipeId: String) {
        try {
            val snapshot = recipeLikesCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe likes")
        }
    }

    private suspend fun deleteRecipeViews(recipeId: String) {
        try {
            recipeViewsCollection.document(recipeId).delete().await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe views")
        }
    }

    private suspend fun deleteRecipeRatings(recipeId: String) {
        try {
            val snapshot = recipeRatingsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe ratings")
        }
    }

    private suspend fun deleteRecipeReviews(recipeId: String) {
        try {
            val snapshot = recipeReviewsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe reviews")
        }
    }

    private suspend fun deleteNutritionalInfo(recipeId: String) {
        try {
            nutritionalInfoCollection.document(recipeId).delete().await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting nutritional info")
        }
    }
}