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
     * Content management - according to admin flow plan
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

    // POST ACTIONS (3 actions)
    fun hidePost(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun unhidePost(
        postId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun removePost(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    // RECIPE ACTIONS (4 actions)
    fun hideRecipe(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun unhideRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun featureRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun unfeatureRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun removeRecipe(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    /**
     * User management - according to admin flow plan
     */
    fun getUsers(
        filters: AdminUserFilters = AdminUserFilters(),
        limit: Int = 20,
        lastUserId: String? = null
    ): Flow<Resource<List<AdminUserItem>>>

    fun getUser(userId: String): Flow<Resource<AdminUserItem?>>

    // USER ACTIONS (4 actions)
    fun suspendUser(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun unsuspendUser(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun makeAdmin(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun removeAdmin(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun banUser(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    /**
     * Moderation actions and history
     */
    fun logModerationAction(action: ModerationAction): Flow<Resource<Unit>>

    fun getModerationHistory(
        contentId: String? = null,
        userId: String? = null,
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

    fun flagContent(
        contentId: String,
        contentType: AdminContentType,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>>

    fun getPendingReports(limit: Int = 20): Flow<Resource<List<AdminContentItem>>>
}