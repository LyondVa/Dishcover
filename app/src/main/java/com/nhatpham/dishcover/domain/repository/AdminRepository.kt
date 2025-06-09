package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface AdminRepository {

    /**
     * Dashboard and analytics
     */
    fun getDashboardStats(): Flow<Resource<AdminDashboardStats>>

    /**
     * Content management
     */
    fun getContentItems(
        filters: AdminContentFilters = AdminContentFilters(),
        limit: Int = 20,
        lastContentId: String? = null
    ): Flow<Resource<List<AdminContentItem>>>

    fun getContentItem(
        contentId: String,
        contentType: AdminContentType
    ): Flow<Resource<AdminContentItem?>>

    fun updateContentStatus(
        contentId: String,
        contentType: AdminContentType,
        status: ContentStatus,
        reason: String = "",
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun flagContent(
        contentId: String,
        contentType: AdminContentType,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun featureRecipe(
        recipeId: String,
        featured: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>>

    /**
     * User management
     */
    fun getUsers(
        searchQuery: String = "",
        status: UserStatus? = null,
        limit: Int = 20,
        lastUserId: String? = null
    ): Flow<Resource<List<AdminUserItem>>>

    fun getUser(userId: String): Flow<Resource<AdminUserItem?>>

    fun updateUserStatus(
        userId: String,
        status: UserStatus,
        reason: String = "",
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun updateUserAdminStatus(
        userId: String,
        isAdmin: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>>

    /**
     * Moderation actions
     */
    fun logModerationAction(action: ModerationAction): Flow<Resource<Unit>>

    fun getModerationHistory(
        contentId: String? = null,
        moderatorId: String? = null,
        limit: Int = 50
    ): Flow<Resource<List<ModerationAction>>>

    /**
     * Reports and flags
     */
    fun getFlaggedContent(
        contentType: AdminContentType? = null,
        limit: Int = 20
    ): Flow<Resource<List<AdminContentItem>>>

    fun getPendingReports(limit: Int = 20): Flow<Resource<List<AdminContentItem>>>
}