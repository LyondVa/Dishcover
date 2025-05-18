package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.model.dto.IngredientDto
import com.nhatpham.dishcover.domain.model.Ingredient
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.data.model.dto.RecipeDto
import com.nhatpham.dishcover.domain.model.RecipeIngredient
import com.nhatpham.dishcover.data.model.dto.RecipeIngredientDto
import com.nhatpham.dishcover.domain.model.RecipeListItem
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class RecipeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val recipesCollection = firestore.collection("RECIPES")
    private val ingredientsCollection = firestore.collection("INGREDIENTS")
    private val recipeIngredientsCollection = firestore.collection("RECIPE_INGREDIENTS")
    private val recipeTagsCollection = firestore.collection("RECIPE_TAGS")

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
}