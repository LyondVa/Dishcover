package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.domain.model.admin.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AdminRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("USERS")
    private val postsCollection = firestore.collection("POSTS")
    private val recipesCollection = firestore.collection("RECIPES")
    private val moderationActionsCollection = firestore.collection("MODERATION_ACTIONS")

    suspend fun getDashboardStats(): AdminDashboardStats {
        return try {
            // Get user stats
            val usersSnapshot = usersCollection.get().await()
            val totalUsers = usersSnapshot.size()
            val activeUsers = usersSnapshot.documents.count {
                it.getBoolean("isActive") == true
            }

            val today = Timestamp.now()
            val todayStart = Timestamp(today.seconds - 86400, 0) // 24 hours ago
            val newUsersToday = usersSnapshot.documents.count { doc ->
                val createdAt = doc.getTimestamp("createdAt")
                createdAt != null && createdAt.compareTo(todayStart) > 0
            }

            // Get post stats
            val postsSnapshot = postsCollection.get().await()
            val totalPosts = postsSnapshot.size()
            val publicPosts = postsSnapshot.documents.count {
                it.getBoolean("public") != false // Remember Firebase truncates isPublic to public
            }
            val flaggedPosts = postsSnapshot.documents.count {
                it.getBoolean("isFlagged") == true
            }

            // Get recipe stats
            val recipesSnapshot = recipesCollection.get().await()
            val totalRecipes = recipesSnapshot.size()
            val featuredRecipes = recipesSnapshot.documents.count {
                it.getBoolean("isFeatured") == true
            }

            AdminDashboardStats(
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                newUsersToday = newUsersToday,
                totalPosts = totalPosts,
                publicPosts = publicPosts,
                flaggedPosts = flaggedPosts,
                totalRecipes = totalRecipes,
                publicRecipes = totalRecipes, // Assuming all recipes shown in count are public
                featuredRecipes = featuredRecipes,
                pendingReports = flaggedPosts // Simplified: flagged content = pending reports
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting dashboard stats")
            AdminDashboardStats() // Return empty stats on error
        }
    }

    suspend fun getContentItems(
        filters: AdminContentFilters,
        limit: Int,
        lastContentId: String?
    ): List<AdminContentItem> {
        return try {
            val contentItems = mutableListOf<AdminContentItem>()

            // Get posts if no specific content type filter or if filtering for posts
            if (filters.contentType == null || filters.contentType == AdminContentType.POST) {
                val postsQuery = buildPostsQuery(filters, limit, lastContentId)
                val postsSnapshot = postsQuery.get().await()

                postsSnapshot.documents.forEach { doc ->
                    val item = mapPostToAdminContentItem(doc.data ?: emptyMap(), doc.id)
                    contentItems.add(item)
                }
            }

            // Get recipes if no specific content type filter or if filtering for recipes
            if (filters.contentType == null || filters.contentType == AdminContentType.RECIPE) {
                val recipesQuery = buildRecipesQuery(filters, limit, lastContentId)
                val recipesSnapshot = recipesQuery.get().await()

                recipesSnapshot.documents.forEach { doc ->
                    val item = mapRecipeToAdminContentItem(doc.data ?: emptyMap(), doc.id)
                    contentItems.add(item)
                }
            }

            // Sort by lastActivity descending
            contentItems.sortedByDescending { it.lastActivity.seconds }
        } catch (e: Exception) {
            Timber.e(e, "Error getting content items")
            emptyList()
        }
    }

    suspend fun getUsers(
        searchQuery: String,
        status: UserStatus?,
        limit: Int,
        lastUserId: String?
    ): List<AdminUserItem> {
        return try {
            var query = usersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (lastUserId != null) {
                val lastDoc = usersCollection.document(lastUserId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get().await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapUserToAdminUserItem(data, doc.id)
            }.filter { user ->
                // Apply search filter
                if (searchQuery.isNotBlank()) {
                    user.username.contains(searchQuery, ignoreCase = true) ||
                            user.email.contains(searchQuery, ignoreCase = true)
                } else true
            }.filter { user ->
                // Apply status filter
                status?.let {
                    user.status == it
                } ?: true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting users")
            emptyList()
        }
    }

    suspend fun updateContentStatus(
        contentId: String,
        contentType: AdminContentType,
        status: ContentStatus,
        moderatorId: String
    ) {
        try {
            val collection = when (contentType) {
                AdminContentType.POST -> postsCollection
                AdminContentType.RECIPE -> recipesCollection
            }

            val updates = mapOf(
                "status" to status.name,
                "moderatedBy" to moderatorId,
                "moderatedAt" to Timestamp.now()
            )

            collection.document(contentId).update(updates).await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating content status")
            throw e
        }
    }

    suspend fun updateUserStatus(
        userId: String,
        status: UserStatus,
        moderatorId: String
    ) {
        try {
            val updates = mapOf(
                "status" to status.name,
                "isActive" to (status == UserStatus.ACTIVE),
                "moderatedBy" to moderatorId,
                "moderatedAt" to Timestamp.now()
            )

            usersCollection.document(userId).update(updates).await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating user status")
            throw e
        }
    }

    suspend fun updateUserAdminStatus(
        userId: String,
        isAdmin: Boolean,
        moderatorId: String
    ) {
        try {
            val updates = mapOf(
                "admin" to isAdmin, // Firebase truncates isAdmin to admin
                "moderatedBy" to moderatorId,
                "moderatedAt" to Timestamp.now()
            )

            usersCollection.document(userId).update(updates).await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating user admin status")
            throw e
        }
    }

    suspend fun featureRecipe(
        recipeId: String,
        featured: Boolean,
        moderatorId: String
    ) {
        try {
            val updates = mapOf(
                "isFeatured" to featured,
                "featuredBy" to moderatorId,
                "featuredAt" to Timestamp.now()
            )

            recipesCollection.document(recipeId).update(updates).await()
        } catch (e: Exception) {
            Timber.e(e, "Error featuring recipe")
            throw e
        }
    }

    suspend fun logModerationAction(action: ModerationAction) {
        try {
            moderationActionsCollection.add(action).await()
        } catch (e: Exception) {
            Timber.e(e, "Error logging moderation action")
        }
    }

    private fun buildPostsQuery(
        filters: AdminContentFilters,
        limit: Int,
        lastContentId: String?
    ): Query {
        var query = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        // Apply filters
        filters.isFlagged?.let { isFlagged ->
            query = query.whereEqualTo("isFlagged", isFlagged)
        }

        filters.isPublic?.let { isPublic ->
            query = query.whereEqualTo("public", isPublic) // Remember Firebase field name
        }

        if (lastContentId != null) {
            // Implement pagination if needed
        }

        return query
    }

    private fun buildRecipesQuery(
        filters: AdminContentFilters,
        limit: Int,
        lastContentId: String?
    ): Query {
        var query = recipesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        // Apply filters similar to posts
        filters.isPublic?.let { isPublic ->
            query = query.whereEqualTo("public", isPublic) // Remember Firebase field name
        }

        return query
    }

    private fun mapPostToAdminContentItem(data: Map<String, Any>, docId: String): AdminContentItem {
        return AdminContentItem(
            contentId = docId,
            contentType = AdminContentType.POST,
            userId = data["userId"] as? String ?: "",
            username = data["username"] as? String ?: "",
            userEmail = "", // Not stored in posts
            title = "", // Posts don't have titles
            content = data["content"] as? String ?: "",
            imageUrls = (data["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isPublic = data["public"] as? Boolean ?: true, // Firebase field name
            isFlagged = data["isFlagged"] as? Boolean ?: false,
            flagReason = data["flagReason"] as? String,
            reportCount = (data["reportCount"] as? Long)?.toInt() ?: 0,
            status = parseContentStatus(data["status"] as? String),
            moderatedBy = data["moderatedBy"] as? String,
            moderatedAt = data["moderatedAt"] as? Timestamp,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            lastActivity = data["updatedAt"] as? Timestamp ?: data["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    private fun mapRecipeToAdminContentItem(data: Map<String, Any>, docId: String): AdminContentItem {
        return AdminContentItem(
            contentId = docId,
            contentType = AdminContentType.RECIPE,
            userId = data["userId"] as? String ?: "",
            username = "", // Would need to join with users collection
            userEmail = "", // Not stored in recipes
            title = data["title"] as? String ?: "",
            content = data["description"] as? String ?: "",
            imageUrls = listOfNotNull(data["coverImage"] as? String),
            isPublic = data["public"] as? Boolean ?: true, // Firebase field name
            isFlagged = data["isFlagged"] as? Boolean ?: false,
            flagReason = data["flagReason"] as? String,
            reportCount = (data["reportCount"] as? Long)?.toInt() ?: 0,
            status = parseContentStatus(data["status"] as? String),
            moderatedBy = data["moderatedBy"] as? String,
            moderatedAt = data["moderatedAt"] as? Timestamp,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            lastActivity = data["updatedAt"] as? Timestamp ?: data["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    private fun mapUserToAdminUserItem(data: Map<String, Any>, docId: String): AdminUserItem {
        return AdminUserItem(
            userId = docId,
            username = data["username"] as? String ?: "",
            email = data["email"] as? String ?: "",
            profilePicture = data["profilePicture"] as? String,
            isVerified = data["isVerified"] as? Boolean ?: false,
            isActive = data["isActive"] as? Boolean ?: true,
            isAdmin = data["admin"] as? Boolean ?: false, // Firebase field name
            postCount = (data["postCount"] as? Long)?.toInt() ?: 0,
            recipeCount = (data["recipeCount"] as? Long)?.toInt() ?: 0,
            followerCount = (data["followerCount"] as? Long)?.toInt() ?: 0,
            reportCount = (data["reportCount"] as? Long)?.toInt() ?: 0,
            lastLoginAt = data["lastLoginAt"] as? Timestamp,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            status = parseUserStatus(data["status"] as? String)
        )
    }

    private fun parseContentStatus(status: String?): ContentStatus {
        return try {
            ContentStatus.valueOf(status ?: "ACTIVE")
        } catch (e: Exception) {
            ContentStatus.ACTIVE
        }
    }

    private fun parseUserStatus(status: String?): UserStatus {
        return try {
            UserStatus.valueOf(status ?: "ACTIVE")
        } catch (e: Exception) {
            UserStatus.ACTIVE
        }
    }
}