package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.model.dto.IngredientDto
import com.nhatpham.dishcover.domain.model.Ingredient
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.data.model.dto.RecipeDto
import com.nhatpham.dishcover.domain.model.RecipeIngredient
import com.nhatpham.dishcover.data.model.dto.RecipeIngredientDto
import com.nhatpham.dishcover.data.model.dto.RecipeTagDto
import com.nhatpham.dishcover.domain.model.RecipeListItem
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class RecipeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val recipesCollection = firestore.collection("RECIPES")
    private val ingredientsCollection = firestore.collection("INGREDIENTS")
    private val recipeIngredientsCollection = firestore.collection("RECIPE_INGREDIENTS")
    private val recipeTagsCollection = firestore.collection("RECIPE_TAGS")
    private val savedItemsCollection = firestore.collection("SAVED_ITEMS")

    suspend fun getFavoriteRecipes(userId: String, limit: Int): List<RecipeListItem> {
        val snapshot = firestore.collection("SAVED_ITEMS")
            .whereEqualTo("userId", userId)
            .whereEqualTo("contentType", "recipe")
            .whereEqualTo("savedCategory", "favorite")
            .limit(limit.toLong())
            .get()
            .await()

        val recipeIds = snapshot.documents.mapNotNull { it.getString("contentId") }

        if (recipeIds.isEmpty()) return emptyList()

        val recipeListItems = mutableListOf<RecipeListItem>()

        for (recipeId in recipeIds) {
            val recipeDoc = recipesCollection.document(recipeId).get().await()
            if (recipeDoc.exists()) {
                val recipeDto = recipeDoc.toObject(RecipeDto::class.java)
                recipeDto?.let {
                    recipeListItems.add(
                        RecipeListItem(
                            recipeId = it.recipeId ?: recipeId,
                            title = it.title ?: "",
                            description = it.description,
                            coverImage = it.coverImage,
                            prepTime = it.prepTime ?: 0,
                            cookTime = it.cookTime ?: 0,
                            likeCount = it.likeCount ?: 0,
                            isPublic = it.isPublic ?: false
                        )
                    )
                }
            }
        }

        return recipeListItems
    }

    suspend fun getRecentRecipes(userId: String, limit: Int): List<RecipeListItem> {
        val snapshot = recipesCollection
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val recipeDto = doc.toObject(RecipeDto::class.java)
            recipeDto?.let {
                RecipeListItem(
                    recipeId = it.recipeId ?: doc.id,
                    title = it.title ?: "",
                    description = it.description,
                    coverImage = it.coverImage,
                    prepTime = it.prepTime ?: 0,
                    cookTime = it.cookTime ?: 0,
                    likeCount = it.likeCount ?: 0,
                    isPublic = it.isPublic ?: false
                )
            }
        }
    }

    suspend fun getRecipesByCategory(userId: String, category: String, limit: Int): List<RecipeListItem> {
        val query = if (category == "all_categories") {
            recipesCollection
                .whereEqualTo("userId", userId)
                .limit(limit.toLong())
        } else {
            recipesCollection
                .whereEqualTo("userId", userId)
                .whereArrayContains("tags", category)
                .limit(limit.toLong())
        }

        val snapshot = query.get().await()

        return snapshot.documents.mapNotNull { doc ->
            val recipeDto = doc.toObject(RecipeDto::class.java)
            recipeDto?.let {
                RecipeListItem(
                    recipeId = it.recipeId ?: doc.id,
                    title = it.title ?: "",
                    description = it.description,
                    coverImage = it.coverImage,
                    prepTime = it.prepTime ?: 0,
                    cookTime = it.cookTime ?: 0,
                    likeCount = it.likeCount ?: 0,
                    isPublic = it.isPublic ?: false
                )
            }
        }
    }

    suspend fun getAllRecipes(userId: String, limit: Int): List<RecipeListItem> {
        val snapshot = recipesCollection
            .whereEqualTo("userId", userId)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val recipeDto = doc.toObject(RecipeDto::class.java)
            recipeDto?.let {
                RecipeListItem(
                    recipeId = it.recipeId ?: doc.id,
                    title = it.title ?: "",
                    description = it.description,
                    coverImage = it.coverImage,
                    prepTime = it.prepTime ?: 0,
                    cookTime = it.cookTime ?: 0,
                    likeCount = it.likeCount ?: 0,
                    isPublic = it.isPublic ?: false
                )
            }
        }
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        val recipeDoc = recipesCollection.document(recipeId).get().await()

        if (!recipeDoc.exists()) return null

        val recipeDto = recipeDoc.toObject(RecipeDto::class.java) ?: return null

        val ingredientsSnapshot = recipeIngredientsCollection
            .whereEqualTo("recipeId", recipeId)
            .orderBy("displayOrder")
            .get()
            .await()

        val recipeIngredients = mutableListOf<RecipeIngredient>()

        for (doc in ingredientsSnapshot.documents) {
            val recipeIngredientDto = doc.toObject(RecipeIngredientDto::class.java)
            recipeIngredientDto?.let { riDto ->
                val ingredientDoc = ingredientsCollection.document(riDto.ingredientId ?: "").get().await()
                val ingredientDto = ingredientDoc.toObject(IngredientDto::class.java)

                ingredientDto?.let { iDto ->
                    val ingredient = Ingredient(
                        ingredientId = iDto.ingredientId ?: ingredientDoc.id,
                        name = iDto.name ?: "",
                        description = iDto.description
                    )

                    val recipeIngredient = RecipeIngredient(
                        recipeIngredientId = riDto.recipeIngredientId ?: doc.id,
                        recipeId = riDto.recipeId ?: recipeId,
                        ingredientId = riDto.ingredientId ?: "",
                        quantity = riDto.quantity ?: "",
                        unit = riDto.unit ?: "",
                        notes = riDto.notes,
                        displayOrder = riDto.displayOrder ?: 0,
                        ingredient = ingredient
                    )

                    recipeIngredients.add(recipeIngredient)
                }
            }
        }

        val tagsSnapshot = recipeTagsCollection
            .whereEqualTo("recipeId", recipeId)
            .get()
            .await()

        val tags = tagsSnapshot.documents.mapNotNull { doc ->
            doc.getString("tagName")
        }

        return Recipe(
            recipeId = recipeDto.recipeId ?: recipeDoc.id,
            userId = recipeDto.userId ?: "",
            title = recipeDto.title ?: "",
            description = recipeDto.description,
            prepTime = recipeDto.prepTime ?: 0,
            cookTime = recipeDto.cookTime ?: 0,
            servings = recipeDto.servings ?: 0,
            instructions = recipeDto.instructions ?: "",
            difficultyLevel = recipeDto.difficultyLevel ?: "Easy",
            coverImage = recipeDto.coverImage,
            createdAt = (recipeDto.createdAt as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (recipeDto.updatedAt as? Timestamp)?.toDate() ?: Date(),
            isPublic = recipeDto.isPublic ?: false,
            viewCount = recipeDto.viewCount ?: 0,
            likeCount = recipeDto.likeCount ?: 0,
            isFeatured = recipeDto.isFeatured ?: false,
            ingredients = recipeIngredients,
            tags = tags
        )
    }

    suspend fun searchRecipes(query: String, limit: Int): List<RecipeListItem> {
        val snapshot = recipesCollection
            .orderBy("title")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val recipeDto = doc.toObject(RecipeDto::class.java)
            recipeDto?.let {
                RecipeListItem(
                    recipeId = it.recipeId ?: doc.id,
                    title = it.title ?: "",
                    description = it.description,
                    coverImage = it.coverImage,
                    prepTime = it.prepTime ?: 0,
                    cookTime = it.cookTime ?: 0,
                    likeCount = it.likeCount ?: 0,
                    isPublic = it.isPublic ?: false
                )
            }
        }
    }

    suspend fun getCategories(userId: String): List<String> {
        val snapshot = recipesCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val tagQueries = mutableListOf<String>()
        snapshot.documents.forEach { doc ->
            val recipeId = doc.id
            tagQueries.add(recipeId)
        }

        val categories = mutableSetOf<String>()

        if (tagQueries.isNotEmpty()) {
            val tagsSnapshot = recipeTagsCollection
                .whereIn("recipeId", tagQueries.take(10))
                .get()
                .await()

            tagsSnapshot.documents.forEach { doc ->
                doc.getString("tagName")?.let {
                    categories.add(it)
                }
            }
        }

        return categories.toList()
    }


    suspend fun createRecipe(recipe: Recipe): Recipe? {
        return try {
            // Create recipe document with auto ID if not provided
            val recipeId = recipe.recipeId.takeIf { it.isNotBlank() } ?: recipesCollection.document().id

            val recipeDto = RecipeDto(
                recipeId = recipeId,
                userId = recipe.userId,
                title = recipe.title,
                description = recipe.description,
                prepTime = recipe.prepTime,
                cookTime = recipe.cookTime,
                servings = recipe.servings,
                instructions = recipe.instructions,
                difficultyLevel = recipe.difficultyLevel,
                coverImage = recipe.coverImage,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                isPublic = recipe.isPublic,
                viewCount = 0,
                likeCount = 0,
                isFeatured = false
            )

            // Save recipe document
            recipesCollection.document(recipeId)
                .set(recipeDto)
                .await()

            // Save ingredients
            for ((index, ingredient) in recipe.ingredients.withIndex()) {
                val recipeIngredientDto = RecipeIngredientDto(
                    recipeIngredientId = ingredient.recipeIngredientId.takeIf { it.isNotBlank() }
                        ?: recipeIngredientsCollection.document().id,
                    recipeId = recipeId,
                    ingredientId = ingredient.ingredientId,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    notes = ingredient.notes,
                    displayOrder = index
                )

                recipeIngredientsCollection.document(recipeIngredientDto.recipeIngredientId!!)
                    .set(recipeIngredientDto)
                    .await()
            }

            // Save tags
            for (tag in recipe.tags) {
                val tagDto = RecipeTagDto(
                    recipeId = recipeId,
                    tagName = tag
                )

                recipeTagsCollection.document()
                    .set(tagDto)
                    .await()
            }

            // Return with updated ID and timestamps
            recipe.copy(
                recipeId = recipeId,
                createdAt = Date(),
                updatedAt = Date()
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

            val recipeData = mapOf(
                "title" to recipe.title,
                "description" to recipe.description,
                "prepTime" to recipe.prepTime,
                "cookTime" to recipe.cookTime,
                "servings" to recipe.servings,
                "instructions" to recipe.instructions,
                "difficultyLevel" to recipe.difficultyLevel,
                "coverImage" to recipe.coverImage,
                "updatedAt" to Timestamp.now(),
                "isPublic" to recipe.isPublic
            )

            // Update recipe document
            recipesCollection.document(recipeId)
                .update(recipeData)
                .await()

            // Delete existing ingredients
            val existingIngredients = recipeIngredientsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            for (doc in existingIngredients.documents) {
                doc.reference.delete().await()
            }

            // Save updated ingredients
            for ((index, ingredient) in recipe.ingredients.withIndex()) {
                val recipeIngredientDto = RecipeIngredientDto(
                    recipeIngredientId = recipeIngredientsCollection.document().id,
                    recipeId = recipeId,
                    ingredientId = ingredient.ingredientId,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    notes = ingredient.notes,
                    displayOrder = index
                )

                recipeIngredientsCollection.document(recipeIngredientDto.recipeIngredientId!!)
                    .set(recipeIngredientDto)
                    .await()
            }

            // Handle tags (optional - based on whether tags are updated)
            if (recipe.tags.isNotEmpty()) {
                // Delete existing tags
                val existingTags = recipeTagsCollection
                    .whereEqualTo("recipeId", recipeId)
                    .get()
                    .await()

                for (doc in existingTags.documents) {
                    doc.reference.delete().await()
                }

                // Save updated tags
                for (tag in recipe.tags) {
                    val tagDto = RecipeTagDto(
                        recipeId = recipeId,
                        tagName = tag
                    )

                    recipeTagsCollection.document()
                        .set(tagDto)
                        .await()
                }
            }

            // Return the updated recipe
            recipe.copy(updatedAt = Date())
        } catch (e: Exception) {
            Timber.e(e, "Error updating recipe")
            null
        }
    }

    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            // Delete the recipe document
            recipesCollection.document(recipeId).delete().await()

            // Delete associated ingredients
            val ingredientsQuery = recipeIngredientsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            for (doc in ingredientsQuery.documents) {
                doc.reference.delete().await()
            }

            // Delete associated tags
            val tagsQuery = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            for (doc in tagsQuery.documents) {
                doc.reference.delete().await()
            }

            // Delete associated saved items (favorites)
            val savedItemsQuery = savedItemsCollection
                .whereEqualTo("contentId", recipeId)
                .whereEqualTo("contentType", "recipe")
                .get()
                .await()

            for (doc in savedItemsQuery.documents) {
                doc.reference.delete().await()
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe")
            false
        }
    }

    suspend fun markRecipeAsFavorite(userId: String, recipeId: String, isFavorite: Boolean): Boolean {
        return try {
            val savedItemQuery = savedItemsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("contentId", recipeId)
                .whereEqualTo("contentType", "recipe")
                .whereEqualTo("savedCategory", "favorite")
                .get()
                .await()

            if (isFavorite) {
                // Add to favorites if not already saved
                if (savedItemQuery.isEmpty) {
                    val savedItem = hashMapOf(
                        "userId" to userId,
                        "contentId" to recipeId,
                        "contentType" to "recipe",
                        "savedCategory" to "favorite",
                        "savedAt" to Timestamp.now()
                    )
                    savedItemsCollection.document().set(savedItem).await()
                }
            } else {
                // Remove from favorites
                for (doc in savedItemQuery.documents) {
                    doc.reference.delete().await()
                }
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating favorite status")
            false
        }
    }

    suspend fun getRecipeTags(recipeId: String): List<String> {
        return try {
            val tagsQuery = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            tagsQuery.documents.mapNotNull { doc ->
                doc.getString("tagName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recipe tags")
            emptyList()
        }
    }

    suspend fun addRecipeTag(recipeId: String, tag: String): Boolean {
        return try {
            // Check if tag already exists
            val existingTag = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("tagName", tag)
                .get()
                .await()

            if (existingTag.isEmpty) {
                val tagDto = RecipeTagDto(
                    recipeId = recipeId,
                    tagName = tag
                )

                recipeTagsCollection.document()
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
            val tagQuery = recipeTagsCollection
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("tagName", tag)
                .get()
                .await()

            for (doc in tagQuery.documents) {
                doc.reference.delete().await()
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe tag")
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

}