// CookbookRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.*
import com.nhatpham.dishcover.data.model.dto.cookbook.*
import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.repository.CookbookStats
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CookbookRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Firestore collections
    private val cookbooksCollection = firestore.collection("COOKBOOKS")
    private val cookbookRecipesCollection = firestore.collection("COOKBOOK_RECIPES")
    private val cookbookCollaboratorsCollection = firestore.collection("COOKBOOK_COLLABORATORS")
    private val cookbookFollowersCollection = firestore.collection("COOKBOOK_FOLLOWERS")
    private val cookbookLikesCollection = firestore.collection("COOKBOOK_LIKES")
    private val cookbookViewsCollection = firestore.collection("COOKBOOK_VIEWS")
    private val cookbookAnalyticsCollection = firestore.collection("COOKBOOK_ANALYTICS")

    // Core Cookbook CRUD operations
    suspend fun createCookbook(cookbook: Cookbook): Cookbook? {
        return try {
            val cookbookId = cookbook.cookbookId.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            val cookbookDto = cookbook.copy(
                cookbookId = cookbookId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toDto()

            cookbooksCollection.document(cookbookId).set(cookbookDto).await()

            // Initialize analytics document
            val analyticsDto = CookbookAnalyticsDto(
                cookbookId = cookbookId,
                totalViews = 0,
                totalLikes = 0,
                totalFollowers = 0,
                totalRecipes = 0,
                totalCollaborators = 0,
                viewsThisWeek = 0,
                viewsThisMonth = 0,
                lastUpdated = Timestamp.now()
            )
            cookbookAnalyticsCollection.document(cookbookId).set(analyticsDto).await()

            cookbookDto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error creating cookbook")
            null
        }
    }

    suspend fun updateCookbook(cookbook: Cookbook): Cookbook? {
        return try {
            val cookbookDto = cookbook.copy(updatedAt = Timestamp.now()).toDto()
            cookbooksCollection.document(cookbook.cookbookId).set(cookbookDto).await()
            cookbookDto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error updating cookbook")
            null
        }
    }

    suspend fun deleteCookbook(cookbookId: String): Boolean {
        return try {
            // Delete in batch
            val batch = firestore.batch()

            // Delete cookbook
            batch.delete(cookbooksCollection.document(cookbookId))

            // Delete associated data
            val cookbookRecipes = cookbookRecipesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .get().await()
            cookbookRecipes.documents.forEach { batch.delete(it.reference) }

            val collaborators = cookbookCollaboratorsCollection
                .whereEqualTo("cookbookId", cookbookId)
                .get().await()
            collaborators.documents.forEach { batch.delete(it.reference) }

            val followers = cookbookFollowersCollection
                .whereEqualTo("cookbookId", cookbookId)
                .get().await()
            followers.documents.forEach { batch.delete(it.reference) }

            val likes = cookbookLikesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .get().await()
            likes.documents.forEach { batch.delete(it.reference) }

            // Delete analytics
            batch.delete(cookbookAnalyticsCollection.document(cookbookId))

            batch.commit().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting cookbook")
            false
        }
    }

    suspend fun getCookbook(cookbookId: String): Cookbook? {
        return try {
            val document = cookbooksCollection.document(cookbookId).get().await()
            document.toObject(CookbookDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook")
            null
        }
    }

    // Cookbook queries
    suspend fun getUserCookbooks(userId: String, limit: Int): List<CookbookListItem> {
        return try {
            val querySnapshot = cookbooksCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user cookbooks")
            emptyList()
        }
    }

    suspend fun getPublicCookbooks(limit: Int, lastCookbookId: String?): List<CookbookListItem> {
        return try {
            var query = cookbooksCollection
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (!lastCookbookId.isNullOrBlank()) {
                val lastDoc = cookbooksCollection.document(lastCookbookId).get().await()
                query = query.startAfter(lastDoc)
            }

            val querySnapshot = query.get().await()
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting public cookbooks")
            emptyList()
        }
    }

    suspend fun getFeaturedCookbooks(limit: Int): List<CookbookListItem> {
        return try {
            val querySnapshot = cookbooksCollection
                .whereEqualTo("featured", true)
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting featured cookbooks")
            emptyList()
        }
    }

    suspend fun searchCookbooks(query: String, limit: Int): List<CookbookListItem> {
        return try {
            // Simple text search - in production you'd use a search service
            val querySnapshot = cookbooksCollection
                .whereEqualTo("public", true)
                .orderBy("title")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .limit(limit.toLong())
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching cookbooks")
            emptyList()
        }
    }

    // Recipe management
    suspend fun addRecipeToCookbook(cookbookRecipe: CookbookRecipe): CookbookRecipe? {
        return try {
            val recipeId = cookbookRecipe.cookbookRecipeId.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            val dto = cookbookRecipe.copy(
                cookbookRecipeId = recipeId,
                addedAt = Timestamp.now()
            ).toDto()

            cookbookRecipesCollection.document(recipeId).set(dto).await()

            // Update cookbook recipe count
            cookbooksCollection.document(cookbookRecipe.cookbookId)
                .update("recipeCount", FieldValue.increment(1))
                .await()

            dto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe to cookbook")
            null
        }
    }

    suspend fun removeRecipeFromCookbook(cookbookRecipeId: String): Boolean {
        return try {
            // Get the cookbook recipe to find the cookbook ID
            val cookbookRecipeDoc = cookbookRecipesCollection.document(cookbookRecipeId).get().await()
            val cookbookId = cookbookRecipeDoc.getString("cookbookId")

            // Delete the recipe association
            cookbookRecipesCollection.document(cookbookRecipeId).delete().await()

            // Update cookbook recipe count
            if (cookbookId != null) {
                cookbooksCollection.document(cookbookId)
                    .update("recipeCount", FieldValue.increment(-1))
                    .await()
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe from cookbook")
            false
        }
    }

    suspend fun getCookbookRecipeIds(cookbookId: String, limit: Int): List<String> {
        return try {
            val querySnapshot = cookbookRecipesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .orderBy("addedAt", Query.Direction.ASCENDING) // Changed from displayOrder to addedAt
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.getString("recipeId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook recipe IDs for cookbook: $cookbookId")
            emptyList()
        }
    }

    suspend fun getCookbookRecipesWithMetadata(cookbookId: String, limit: Int): List<CookbookRecipe> {
        return try {
            val querySnapshot = cookbookRecipesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .orderBy("addedAt", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookRecipeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook recipes with metadata for cookbook: $cookbookId")
            emptyList()
        }
    }

    // Follow operations
    suspend fun followCookbook(userId: String, cookbookId: String): Boolean {
        return try {
            val followId = UUID.randomUUID().toString()
            val followerDto = CookbookFollowerDto(
                followId = followId,
                cookbookId = cookbookId,
                userId = userId,
                followedAt = Timestamp.now()
            )

            cookbookFollowersCollection.document(followId).set(followerDto).await()

            // Update cookbook follower count
            cookbooksCollection.document(cookbookId)
                .update("followerCount", FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error following cookbook")
            false
        }
    }

    suspend fun unfollowCookbook(userId: String, cookbookId: String): Boolean {
        return try {
            val querySnapshot = cookbookFollowersCollection
                .whereEqualTo("cookbookId", cookbookId)
                .whereEqualTo("userId", userId)
                .get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.first().reference.delete().await()

                // Update cookbook follower count
                cookbooksCollection.document(cookbookId)
                    .update("followerCount", FieldValue.increment(-1))
                    .await()
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error unfollowing cookbook")
            false
        }
    }

    suspend fun isCookbookFollowedByUser(userId: String, cookbookId: String): Boolean {
        return try {
            val querySnapshot = cookbookFollowersCollection
                .whereEqualTo("cookbookId", cookbookId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get().await()

            querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Error checking if cookbook is followed")
            false
        }
    }

    // Like operations
    suspend fun likeCookbook(userId: String, cookbookId: String): Boolean {
        return try {
            val likeId = UUID.randomUUID().toString()
            val likeDto = CookbookLikeDto(
                likeId = likeId,
                cookbookId = cookbookId,
                userId = userId,
                likedAt = Timestamp.now()
            )

            cookbookLikesCollection.document(likeId).set(likeDto).await()

            // Update cookbook like count
            cookbooksCollection.document(cookbookId)
                .update("likeCount", FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking cookbook")
            false
        }
    }

    suspend fun unlikeCookbook(userId: String, cookbookId: String): Boolean {
        return try {
            val querySnapshot = cookbookLikesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .whereEqualTo("userId", userId)
                .get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.first().reference.delete().await()

                // Update cookbook like count
                cookbooksCollection.document(cookbookId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking cookbook")
            false
        }
    }

    suspend fun isCookbookLikedByUser(userId: String, cookbookId: String): Boolean {
        return try {
            val querySnapshot = cookbookLikesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get().await()

            querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Error checking if cookbook is liked")
            false
        }
    }

    // Analytics
    suspend fun incrementCookbookView(cookbookId: String, userId: String?): Boolean {
        return try {
            // Record view
            val viewId = UUID.randomUUID().toString()
            val viewDto = CookbookViewDto(
                viewId = viewId,
                cookbookId = cookbookId,
                userId = userId,
                viewedAt = Timestamp.now()
            )
            cookbookViewsCollection.document(viewId).set(viewDto).await()

            // Update cookbook view count
            cookbooksCollection.document(cookbookId)
                .update("viewCount", FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing cookbook view")
            false
        }
    }

    suspend fun getCookbookStats(cookbookId: String): CookbookStats? {
        return try {
            val document = cookbookAnalyticsCollection.document(cookbookId).get().await()
            document.toObject(CookbookAnalyticsDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook stats")
            null
        }
    }

    // Collaboration operations
    suspend fun inviteCollaborator(collaborator: CookbookCollaborator): CookbookCollaborator? {
        return try {
            val collaboratorId = collaborator.collaboratorId.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            val collaboratorDto = collaborator.copy(
                collaboratorId = collaboratorId,
                invitedAt = Timestamp.now(),
                status = CollaboratorStatus.PENDING
            ).toDto()

            cookbookCollaboratorsCollection.document(collaboratorId).set(collaboratorDto).await()
            collaboratorDto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Error inviting collaborator")
            null
        }
    }

    suspend fun respondToInvitation(collaboratorId: String, accept: Boolean): Boolean {
        return try {
            val updates = if (accept) {
                mapOf(
                    "status" to CollaboratorStatus.ACCEPTED.name,
                    "acceptedAt" to Timestamp.now()
                )
            } else {
                mapOf(
                    "status" to CollaboratorStatus.DECLINED.name
                )
            }

            cookbookCollaboratorsCollection.document(collaboratorId).update(updates).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error responding to invitation")
            false
        }
    }

    suspend fun removeCollaborator(collaboratorId: String): Boolean {
        return try {
            cookbookCollaboratorsCollection.document(collaboratorId).delete().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing collaborator")
            false
        }
    }

    suspend fun updateCollaboratorRole(collaboratorId: String, role: CookbookRole): Boolean {
        return try {
            cookbookCollaboratorsCollection.document(collaboratorId)
                .update("role", role.name)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating collaborator role")
            false
        }
    }

    suspend fun getCookbookCollaborators(cookbookId: String): List<CookbookCollaborator> {
        return try {
            val querySnapshot = cookbookCollaboratorsCollection
                .whereEqualTo("cookbookId", cookbookId)
                .whereEqualTo("status", CollaboratorStatus.ACCEPTED.name)
                .orderBy("acceptedAt", Query.Direction.DESCENDING)
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookCollaboratorDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook collaborators")
            emptyList()
        }
    }

    suspend fun getUserCookbookInvitations(userId: String): List<CookbookCollaborator> {
        return try {
            val querySnapshot = cookbookCollaboratorsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", CollaboratorStatus.PENDING.name)
                .orderBy("invitedAt", Query.Direction.DESCENDING)
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookCollaboratorDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user cookbook invitations")
            emptyList()
        }
    }

    // Follower operations
    suspend fun getCookbookFollowers(cookbookId: String, limit: Int): List<CookbookFollower> {
        return try {
            val querySnapshot = cookbookFollowersCollection
                .whereEqualTo("cookbookId", cookbookId)
                .orderBy("followedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookFollowerDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook followers")
            emptyList()
        }
    }

    suspend fun getFollowedCookbooks(userId: String, limit: Int): List<CookbookListItem> {
        return try {
            // Get cookbook IDs that user follows
            val followersSnapshot = cookbookFollowersCollection
                .whereEqualTo("userId", userId)
                .orderBy("followedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            val cookbookIds = followersSnapshot.documents.mapNotNull { doc ->
                doc.getString("cookbookId")
            }

            if (cookbookIds.isEmpty()) {
                return emptyList()
            }

            // Batch get cookbook details
            val cookbooks = mutableListOf<CookbookListItem>()
            for (cookbookId in cookbookIds) {
                val cookbookDoc = cookbooksCollection.document(cookbookId).get().await()
                cookbookDoc.toObject(CookbookDto::class.java)?.toListItem()?.let { cookbook ->
                    cookbooks.add(cookbook)
                }
            }

            cookbooks
        } catch (e: Exception) {
            Timber.e(e, "Error getting followed cookbooks")
            emptyList()
        }
    }

    // Like operations
    suspend fun getCookbookLikes(cookbookId: String, limit: Int): List<CookbookLike> {
        return try {
            val querySnapshot = cookbookLikesCollection
                .whereEqualTo("cookbookId", cookbookId)
                .orderBy("likedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(CookbookLikeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook likes")
            emptyList()
        }
    }

    // Recipe reordering
    suspend fun reorderCookbookRecipes(cookbookId: String, recipeOrders: List<Pair<String, Int>>): Boolean {
        return try {
            // Use batch write for atomic updates
            val batch = firestore.batch()

            for ((recipeId, displayOrder) in recipeOrders) {
                // Find the cookbook recipe document
                val querySnapshot = cookbookRecipesCollection
                    .whereEqualTo("cookbookId", cookbookId)
                    .whereEqualTo("recipeId", recipeId)
                    .limit(1)
                    .get().await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val docRef = querySnapshot.documents.first().reference
                    batch.update(docRef, "displayOrder", displayOrder)
                }
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error reordering cookbook recipes")
            false
        }
    }
}