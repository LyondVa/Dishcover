package com.nhatpham.dishcover.domain.model.admin

import com.google.firebase.Timestamp

/**
 * Admin view of content items for moderation
 */
data class AdminContentItem(
    val contentId: String = "",
    val contentType: AdminContentType = AdminContentType.POST,
    val userId: String = "",
    val username: String = "",
    val userEmail: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val isFlagged: Boolean = false,
    val flagReason: String? = null,
    val reportCount: Int = 0,
    val status: ContentStatus = ContentStatus.VISIBLE,
    val isFeatured: Boolean = false, // For recipes
    val moderatedBy: String? = null,
    val moderatedAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val lastActivity: Timestamp = Timestamp.now()
)

enum class AdminContentType {
    POST, RECIPE
}

/**
 * Content status according to admin flow plan:
 * - VISIBLE: Default state, content is live
 * - HIDDEN: Temporarily hidden, reversible
 * - REMOVED: Permanently deleted, irreversible
 */
enum class ContentStatus {
    VISIBLE, HIDDEN, REMOVED
}

/**
 * Admin view of users for management
 */
data class AdminUserItem(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicture: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val isAdmin: Boolean = false, // Will map to "admin" in Firebase
    val postCount: Int = 0,
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val reportCount: Int = 0,
    val lastLoginAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val status: UserStatus = UserStatus.ACTIVE
)

/**
 * User status according to admin flow plan:
 * - ACTIVE: Default state, full access
 * - SUSPENDED: Temporarily restricted, reversible
 * - BANNED: Permanently terminated, irreversible
 */
enum class UserStatus {
    ACTIVE, SUSPENDED, BANNED
}

/**
 * Content moderation action for logging
 */
data class ModerationAction(
    val actionId: String = "",
    val contentId: String = "",
    val contentType: AdminContentType = AdminContentType.POST,
    val actionType: ModerationActionType = ModerationActionType.HIDE,
    val reason: String = "",
    val moderatorId: String = "",
    val moderatorUsername: String = "",
    val targetUserId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Moderation action types according to admin flow plan
 */
enum class ModerationActionType {
    // Content actions
    HIDE, UNHIDE, REMOVE,
    FEATURE, UNFEATURE,

    // User actions
    SUSPEND, UNSUSPEND, BAN,
    MAKE_ADMIN, REMOVE_ADMIN,

    // General actions
    FLAG
}

/**
 * Admin dashboard statistics
 */
data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val suspendedUsers: Int = 0,
    val bannedUsers: Int = 0,
    val newUsersToday: Int = 0,
    val totalPosts: Int = 0,
    val visiblePosts: Int = 0,
    val hiddenPosts: Int = 0,
    val removedPosts: Int = 0,
    val flaggedPosts: Int = 0,
    val totalRecipes: Int = 0,
    val visibleRecipes: Int = 0,
    val hiddenRecipes: Int = 0,
    val removedRecipes: Int = 0,
    val featuredRecipes: Int = 0,
    val pendingReports: Int = 0,
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Content filters for admin queries
 */
data class AdminContentFilters(
    val contentType: AdminContentType? = null,
    val status: ContentStatus? = null,
    val isFlagged: Boolean? = null,
    val isPublic: Boolean? = null,
    val isFeatured: Boolean? = null,
    val userId: String? = null,
    val searchQuery: String = ""
)

/**
 * User filters for admin queries
 */
data class AdminUserFilters(
    val status: UserStatus? = null,
    val isAdmin: Boolean? = null,
    val searchQuery: String = ""
)