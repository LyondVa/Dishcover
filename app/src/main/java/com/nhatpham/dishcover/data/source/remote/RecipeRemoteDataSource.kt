// RecipeRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.mapper.*
import com.nhatpham.dishcover.data.model.dto.*
import com.nhatpham.dishcover.domain.model.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecipeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val recipesCollection = firestore.collection("RECIPES")
    private val ingredientsCollection = firestore.collection("INGREDIENTS")
    private val recipeIngredientsCollection = firestore.collection("RECIPE_INGREDIENTS")
    private val recipeTagsCollection = firestore.collection("RECIPE_TAGS")
    private val recipeCategoriesCollection = firestore.collection("RECIPE_CATEGORIES")
    private val savedRecipesCollection = firestore.collection("SAVED_RECIPES")
    private val recipeLikesCollection = firestore.collection("RECIPE_LIKES")
    private val recipeViewsCollection = firestore.collection("RECIPE_VIEWS")
    private val recipeStepsCollection = firestore.collection("RECIPE_STEPS")

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

            // Save recipe document
            recipesCollection.document(recipeId)
                .set(recipeDto)
                .await()

            // Save ingredients
            saveRecipeIngredients(recipeId, recipe.ingredients)

            // Save tags
            saveRecipeTags(recipeId, recipe.tags)

            // Return the created recipe with updated ID and timestamps
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

            // Update recipe document
            recipesCollection.document(recipeId)
                .set(recipeDto)
                .await()

            // Update ingredients
            deleteRecipeIngredients(recipeId)
            saveRecipeIngredients(recipeId, recipe.ingredients)

            // Update tags
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
            // Delete recipe document
            recipesCollection.document(recipeId).delete().await()

            // Delete associated data
            deleteRecipeIngredients(recipeId)
            deleteRecipeTags(recipeId)
            deleteRecipeSteps(recipeId)
            deleteSavedRecipes(recipeId)
            deleteRecipeLikes(recipeId)
            deleteRecipeViews(recipeId)

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
            val query = if (category == "all_categories") {
                recipesCollection
                    .whereEqualTo("userId", userId)
                    .limit(limit.toLong())
            } else {
                // Get recipe IDs with this tag
                val taggedRecipeIds = getRecipeIdsByTag(category)
                if (taggedRecipeIds.isEmpty()) return emptyList()

                recipesCollection
                    .whereEqualTo("userId", userId)
                    .whereIn("recipeId", taggedRecipeIds.take(10)) // Firestore limit
                    .limit(limit.toLong())
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipes by category")
            emptyList()
        }
    }

    suspend fun getAllRecipes(userId: String, limit: Int): List<RecipeListItem> {
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
            Timber.e(e, "Error getting all recipes")
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

    // Category operations
    suspend fun getCategories(userId: String): List<String> {
        return try {
            val userRecipesSnapshot = recipesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val recipeIds = userRecipesSnapshot.documents.map { it.id }
            if (recipeIds.isEmpty()) return emptyList()

            val categories = mutableSetOf<String>()

            // Process in batches due to Firestore 'in' query limit
            recipeIds.chunked(10).forEach { batch ->
                val tagsSnapshot = recipeTagsCollection
                    .whereIn("recipeId", batch)
                    .get()
                    .await()

                tagsSnapshot.documents.forEach { doc ->
                    doc.getString("tagName")?.let { categories.add(it) }
                }
            }

            categories.toList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting categories")
            emptyList()
        }
    }

    suspend fun getSystemCategories(): List<RecipeCategory> {
        return try {
            val snapshot = recipeCategoriesCollection
                .whereEqualTo("isSystemCategory", true)
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
            // This is a simplified implementation
            // In production, you might want to maintain a separate collection for tag analytics
            val snapshot = recipeTagsCollection
                .limit(limit.toLong())
                .get()
                .await()

            val tagCounts = mutableMapOf<String, Int>()
            snapshot.documents.forEach { doc ->
                val tagName = doc.getString("tagName")
                if (tagName != null) {
                    tagCounts[tagName] = tagCounts.getOrDefault(tagName, 0) + 1
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

    // Recipe interaction operations
    suspend fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Boolean {
        return try {
            val existingSaved = savedRecipesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("savedCategory", "favorite")
                .get()
                .await()

            if (isFavorite) {
                if (existingSaved.isEmpty) {
                    val savedRecipeDto = SavedRecipeDto(
                        savedRecipeId = savedRecipesCollection.document().id,
                        userId = userId,
                        recipeId = recipeId,
                        savedCategory = "favorite",
                        savedAt = Timestamp.now()
                    )

                    savedRecipesCollection.document(savedRecipeDto.savedRecipeId!!)
                        .set(savedRecipeDto)
                        .await()
                }
            } else {
                existingSaved.documents.forEach { doc ->
                    doc.reference.delete().await()
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating favorite status")
            false
        }
    }

    suspend fun incrementViewCount(recipeId: String): Boolean {
        return try {
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
                val likeDto = RecipeLikeDto(
                    likeId = recipeLikesCollection.document().id,
                    userId = userId,
                    recipeId = recipeId,
                    likedAt = Timestamp.now()
                )

                recipeLikesCollection.document(likeDto.likeId!!)
                    .set(likeDto)
                    .await()

                // Increment like count
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

            if (!existingLike.isEmpty) {
                existingLike.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

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
    private suspend fun saveRecipeIngredients(recipeId: String, ingredients: List<RecipeIngredient>) {
        ingredients.forEachIndexed { index, ingredient ->
            try {
                // Ensure ingredient exists
                var ingredientEntity = getOrCreateIngredient(ingredient.ingredient)

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
            // First try to find existing ingredient by name
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
                // Create new ingredient
                createIngredient(ingredient) ?: ingredient
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting or creating ingredient")
            ingredient
        }
    }

    private suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient> {
        return try {
            val snapshot = recipeIngredientsCollection
                .whereEqualTo("recipeId", recipeId)
                .orderBy("displayOrder")
                .get()
                .await()

            val recipeIngredients = mutableListOf<RecipeIngredient>()

            for (doc in snapshot.documents) {
                val recipeIngredientDto = doc.toObject(RecipeIngredientDto::class.java)
                if (recipeIngredientDto != null) {
                    val ingredientDoc = ingredientsCollection
                        .document(recipeIngredientDto.ingredientId ?: "")
                        .get()
                        .await()

                    val ingredientDto = ingredientDoc.toObject(IngredientDto::class.java)
                    if (ingredientDto != null) {
                        recipeIngredients.add(
                            recipeIngredientDto.toDomain(ingredientDto.toDomain())
                        )
                    }
                }
            }

            recipeIngredients
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe ingredients")
            emptyList()
        }
    }

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
            val snapshot = recipeViewsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe views")
        }
    }

    private suspend fun getRecipesByIds(recipeIds: List<String>): List<RecipeListItem> {
        return try {
            val recipes = mutableListOf<RecipeListItem>()

            // Process in batches due to Firestore 'in' query limit
            recipeIds.chunked(10).forEach { batch ->
                val snapshot = recipesCollection
                    .whereIn("recipeId", batch)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RecipeDto::class.java)?.toListItem()
                }.let { recipes.addAll(it) }
            }

            recipes
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipes by IDs")
            emptyList()
        }
    }

    private suspend fun getRecipeIdsByTag(tag: String): List<String> {
        return try {
            val snapshot = recipeTagsCollection
                .whereEqualTo("tagName", tag)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("recipeId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe IDs by tag")
            emptyList()
        }
    }
}