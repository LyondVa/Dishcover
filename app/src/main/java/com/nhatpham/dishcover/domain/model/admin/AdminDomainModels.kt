package com.nhatpham.dishcover.domain.model.admin

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.feed.PostType

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
    val status: ContentStatus = ContentStatus.ACTIVE,
    val moderatedBy: String? = null,
    val moderatedAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val lastActivity: Timestamp = Timestamp.now()
)

enum class AdminContentType {
    POST, RECIPE
}

enum class ContentStatus {
    ACTIVE, HIDDEN, REMOVED, UNDER_REVIEW
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

enum class UserStatus {
    ACTIVE, SUSPENDED, BANNED
}

/**
 * Content moderation action
 */
data class ModerationAction(
    val actionId: String = "",
    val contentId: String = "",
    val contentType: AdminContentType = AdminContentType.POST,
    val actionType: ModerationActionType = ModerationActionType.APPROVE,
    val reason: String = "",
    val moderatorId: String = "",
    val moderatorUsername: String = "",
    val targetUserId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

enum class ModerationActionType {
    APPROVE, HIDE, REMOVE, FLAG, FEATURE, UNFEATURE
}

/**
 * Admin dashboard statistics
 */
data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val newUsersToday: Int = 0,
    val totalPosts: Int = 0,
    val publicPosts: Int = 0,
    val flaggedPosts: Int = 0,
    val totalRecipes: Int = 0,
    val publicRecipes: Int = 0,
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
    val dateRange: DateRange? = null,
    val userId: String? = null
)

data class DateRange(
    val startDate: Timestamp,
    val endDate: Timestamp
)