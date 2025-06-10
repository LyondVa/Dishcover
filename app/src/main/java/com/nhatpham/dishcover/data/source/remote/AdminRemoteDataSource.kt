package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.domain.model.admin.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val POSTS_COLLECTION = "POSTS"
        private const val RECIPES_COLLECTION = "RECIPES"
        private const val USERS_COLLECTION = "USERS"
        private const val MODERATION_ACTIONS_COLLECTION = "MODERATION_ACTIONS"
    }

    suspend fun getDashboardStats(): AdminDashboardStats {
        // Get user stats
        val usersSnapshot = firestore.collection(USERS_COLLECTION).get().await()
        val totalUsers = usersSnapshot.size()

        val activeUsers = usersSnapshot.documents.count { doc ->
            (doc.get("status") as? String) != "BANNED" && (doc.get("status") as? String) != "SUSPENDED"
        }

        val suspendedUsers = usersSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "SUSPENDED"
        }

        val bannedUsers = usersSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "BANNED"
        }

        // Get posts stats
        val postsSnapshot = firestore.collection(POSTS_COLLECTION).get().await()
        val totalPosts = postsSnapshot.size()

        val visiblePosts = postsSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "VISIBLE" || doc.get("status") == null
        }

        val hiddenPosts = postsSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "HIDDEN"
        }

        val removedPosts = postsSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "REMOVED"
        }

        val flaggedPosts = postsSnapshot.documents.count { doc ->
            (doc.get("isFlagged") as? Boolean) == true
        }

        // Get recipes stats
        val recipesSnapshot = firestore.collection(RECIPES_COLLECTION).get().await()
        val totalRecipes = recipesSnapshot.size()

        val visibleRecipes = recipesSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "VISIBLE" || doc.get("status") == null
        }

        val hiddenRecipes = recipesSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "HIDDEN"
        }

        val removedRecipes = recipesSnapshot.documents.count { doc ->
            (doc.get("status") as? String) == "REMOVED"
        }

        val featuredRecipes = recipesSnapshot.documents.count { doc ->
            (doc.get("isFeatured") as? Boolean) == true || (doc.get("featured") as? Boolean) == true
        }

        // Get new users today
        val todayStart = Timestamp.now().let { now ->
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = now.toDate().time
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            Timestamp(calendar.time)
        }

        val newUsersToday = usersSnapshot.documents.count { doc ->
            val createdAt = doc.get("createdAt") as? Timestamp
            createdAt != null && createdAt >= todayStart
        }

        return AdminDashboardStats(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            suspendedUsers = suspendedUsers,
            bannedUsers = bannedUsers,
            newUsersToday = newUsersToday,
            totalPosts = totalPosts,
            visiblePosts = visiblePosts,
            hiddenPosts = hiddenPosts,
            removedPosts = removedPosts,
            flaggedPosts = flaggedPosts,
            totalRecipes = totalRecipes,
            visibleRecipes = visibleRecipes,
            hiddenRecipes = hiddenRecipes,
            removedRecipes = removedRecipes,
            featuredRecipes = featuredRecipes,
            pendingReports = flaggedPosts, // Simplified: flagged content = pending reports
            updatedAt = Timestamp.now()
        )
    }

    suspend fun getContentItems(
        filters: AdminContentFilters,
        limit: Int,
        lastContentId: String?
    ): List<AdminContentItem> {
        val collection = when (filters.contentType) {
            AdminContentType.POST -> POSTS_COLLECTION
            AdminContentType.RECIPE -> RECIPES_COLLECTION
            null -> POSTS_COLLECTION // Default to posts if no type specified
        }

        var query: Query = firestore.collection(collection)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        // Apply filters
        filters.status?.let { status ->
            query = query.whereEqualTo("status", status.name)
        }

        filters.isFlagged?.let { flagged ->
            query = query.whereEqualTo("isFlagged", flagged)
        }

        filters.isPublic?.let { public ->
            query = query.whereEqualTo("public", public) // Firebase field name
        }

        filters.isFeatured?.let { featured ->
            if (collection == RECIPES_COLLECTION) {
                query = query.whereEqualTo("featured", featured) // Firebase field name
            }
        }

        filters.userId?.let { userId ->
            query = query.whereEqualTo("userId", userId)
        }

        // Apply pagination
        lastContentId?.let { lastId ->
            val lastDoc = firestore.collection(collection).document(lastId).get().await()
            if (lastDoc.exists()) {
                query = query.startAfter(lastDoc)
            }
        }

        query = query.limit(limit.toLong())

        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            if (collection == POSTS_COLLECTION) {
                mapPostToAdminContentItem(data, doc.id)
            } else {
                mapRecipeToAdminContentItem(data, doc.id)
            }
        }
    }

    suspend fun getContentItem(
        contentId: String,
        contentType: AdminContentType
    ): AdminContentItem? {
        val collection = when (contentType) {
            AdminContentType.POST -> POSTS_COLLECTION
            AdminContentType.RECIPE -> RECIPES_COLLECTION
        }

        val doc = firestore.collection(collection).document(contentId).get().await()
        val data = doc.data ?: return null

        return if (contentType == AdminContentType.POST) {
            mapPostToAdminContentItem(data, doc.id)
        } else {
            mapRecipeToAdminContentItem(data, doc.id)
        }
    }

    suspend fun updateContentStatus(
        contentId: String,
        contentType: AdminContentType,
        status: ContentStatus,
        moderatorId: String
    ) {
        val collection = when (contentType) {
            AdminContentType.POST -> POSTS_COLLECTION
            AdminContentType.RECIPE -> RECIPES_COLLECTION
        }

        val updates = mapOf(
            "status" to status.name,
            "moderatedBy" to moderatorId,
            "moderatedAt" to Timestamp.now()
        )

        firestore.collection(collection)
            .document(contentId)
            .update(updates)
            .await()
    }

    suspend fun updateRecipeFeatureStatus(
        recipeId: String,
        featured: Boolean,
        moderatorId: String
    ) {
        val updates = mapOf(
            "featured" to featured, // Firebase field name (will be "featured", not "isFeatured")
            "moderatedBy" to moderatorId,
            "moderatedAt" to Timestamp.now()
        )

        firestore.collection(RECIPES_COLLECTION)
            .document(recipeId)
            .update(updates)
            .await()
    }

    suspend fun flagContent(
        contentId: String,
        contentType: AdminContentType,
        reason: String,
        moderatorId: String
    ) {
        val collection = when (contentType) {
            AdminContentType.POST -> POSTS_COLLECTION
            AdminContentType.RECIPE -> RECIPES_COLLECTION
        }

        val updates = mapOf(
            "isFlagged" to true,
            "flagReason" to reason,
            "flaggedBy" to moderatorId,
            "flaggedAt" to Timestamp.now()
        )

        firestore.collection(collection)
            .document(contentId)
            .update(updates)
            .await()
    }

    suspend fun getUsers(
        filters: AdminUserFilters,
        limit: Int,
        lastUserId: String?
    ): List<AdminUserItem> {
        var query: Query = firestore.collection(USERS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        // Apply filters
        filters.status?.let { status ->
            query = query.whereEqualTo("status", status.name)
        }

        filters.isAdmin?.let { admin ->
            query = query.whereEqualTo("admin", admin) // Firebase field name
        }

        // Apply pagination
        lastUserId?.let { lastId ->
            val lastDoc = firestore.collection(USERS_COLLECTION).document(lastId).get().await()
            if (lastDoc.exists()) {
                query = query.startAfter(lastDoc)
            }
        }

        query = query.limit(limit.toLong())

        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapUserToAdminUserItem(data, doc.id)
        }
    }

    suspend fun getUser(userId: String): AdminUserItem? {
        val doc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
        val data = doc.data ?: return null
        return mapUserToAdminUserItem(data, doc.id)
    }

    suspend fun updateUserStatus(
        userId: String,
        status: UserStatus,
        moderatorId: String
    ) {
        val updates = mapOf(
            "status" to status.name,
            "moderatedBy" to moderatorId,
            "moderatedAt" to Timestamp.now()
        )

        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(updates)
            .await()
    }

    suspend fun updateUserAdminStatus(
        userId: String,
        isAdmin: Boolean,
        moderatorId: String
    ) {
        val updates = mapOf(
            "admin" to isAdmin, // Firebase field name (will be "admin", not "isAdmin")
            "moderatedBy" to moderatorId,
            "moderatedAt" to Timestamp.now()
        )

        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(updates)
            .await()
    }

    suspend fun logModerationAction(action: ModerationAction) {
        val data = mapOf(
            "contentId" to action.contentId,
            "contentType" to action.contentType.name,
            "actionType" to action.actionType.name,
            "reason" to action.reason,
            "moderatorId" to action.moderatorId,
            "moderatorUsername" to action.moderatorUsername,
            "targetUserId" to action.targetUserId,
            "createdAt" to action.createdAt
        )

        firestore.collection(MODERATION_ACTIONS_COLLECTION)
            .add(data)
            .await()
    }

    suspend fun getModerationHistory(
        contentId: String?,
        userId: String?,
        moderatorId: String?,
        limit: Int
    ): List<ModerationAction> {
        var query: Query = firestore.collection(MODERATION_ACTIONS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        contentId?.let { id ->
            query = query.whereEqualTo("contentId", id)
        }

        userId?.let { id ->
            query = query.whereEqualTo("targetUserId", id)
        }

        moderatorId?.let { id ->
            query = query.whereEqualTo("moderatorId", id)
        }

        query = query.limit(limit.toLong())

        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapToModerationAction(data, doc.id)
        }
    }

    suspend fun getPendingReports(limit: Int): List<AdminContentItem> {
        // Get flagged posts
        val flaggedPosts = firestore.collection(POSTS_COLLECTION)
            .whereEqualTo("isFlagged", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit((limit / 2).toLong())
            .get()
            .await()

        // Get flagged recipes
        val flaggedRecipes = firestore.collection(RECIPES_COLLECTION)
            .whereEqualTo("isFlagged", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit((limit / 2).toLong())
            .get()
            .await()

        val posts = flaggedPosts.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapPostToAdminContentItem(data, doc.id)
        }

        val recipes = flaggedRecipes.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapRecipeToAdminContentItem(data, doc.id)
        }

        return (posts + recipes).sortedByDescending { it.createdAt.toDate() }.take(limit)
    }

    // Mapping functions
    private fun mapPostToAdminContentItem(data: Map<String, Any>, docId: String): AdminContentItem {
        return AdminContentItem(
            contentId = docId,
            contentType = AdminContentType.POST,
            userId = data["userId"] as? String ?: "",
            username = data["username"] as? String ?: "",
            userEmail = "", // Would need to join with users collection
            title = "",
            content = data["content"] as? String ?: "",
            imageUrls = (data["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isPublic = data["public"] as? Boolean ?: true, // Firebase field name
            isFlagged = data["isFlagged"] as? Boolean ?: false,
            flagReason = data["flagReason"] as? String,
            reportCount = (data["reportCount"] as? Long)?.toInt() ?: 0,
            status = parseContentStatus(data["status"] as? String),
            isFeatured = false, // Posts don't have featured status
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
            username = data["username"] as? String ?: "",
            userEmail = "", // Would need to join with users collection
            title = data["title"] as? String ?: "",
            content = data["description"] as? String ?: "",
            imageUrls = listOfNotNull(data["coverImage"] as? String),
            isPublic = data["public"] as? Boolean ?: true, // Firebase field name
            isFlagged = data["isFlagged"] as? Boolean ?: false,
            flagReason = data["flagReason"] as? String,
            reportCount = (data["reportCount"] as? Long)?.toInt() ?: 0,
            status = parseContentStatus(data["status"] as? String),
            isFeatured = data["featured"] as? Boolean ?: false, // Firebase field name
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

    private fun mapToModerationAction(data: Map<String, Any>, docId: String): ModerationAction {
        return ModerationAction(
            actionId = docId,
            contentId = data["contentId"] as? String ?: "",
            contentType = parseContentType(data["contentType"] as? String),
            actionType = parseModerationActionType(data["actionType"] as? String),
            reason = data["reason"] as? String ?: "",
            moderatorId = data["moderatorId"] as? String ?: "",
            moderatorUsername = data["moderatorUsername"] as? String ?: "",
            targetUserId = data["targetUserId"] as? String ?: "",
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
        )
    }

    private fun parseContentStatus(status: String?): ContentStatus {
        return try {
            ContentStatus.valueOf(status ?: "VISIBLE")
        } catch (e: Exception) {
            ContentStatus.VISIBLE
        }
    }

    private fun parseUserStatus(status: String?): UserStatus {
        return try {
            UserStatus.valueOf(status ?: "ACTIVE")
        } catch (e: Exception) {
            UserStatus.ACTIVE
        }
    }

    private fun parseContentType(type: String?): AdminContentType {
        return try {
            AdminContentType.valueOf(type ?: "POST")
        } catch (e: Exception) {
            AdminContentType.POST
        }
    }

    private fun parseModerationActionType(type: String?): ModerationActionType {
        return try {
            ModerationActionType.valueOf(type ?: "HIDE")
        } catch (e: Exception) {
            ModerationActionType.HIDE
        }
    }
}