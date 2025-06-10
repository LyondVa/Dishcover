package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.remote.AdminRemoteDataSource
import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.domain.repository.AdminRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val adminRemoteDataSource: AdminRemoteDataSource
) : AdminRepository {

    override fun getDashboardStats(): Flow<Resource<AdminDashboardStats>> = flow {
        emit(Resource.Loading())
        try {
            val stats = adminRemoteDataSource.getDashboardStats()
            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get dashboard stats"))
        }
    }

    override fun getContentItems(
        filters: AdminContentFilters,
        limit: Int,
        lastContentId: String?
    ): Flow<Resource<List<AdminContentItem>>> = flow {
        emit(Resource.Loading())
        try {
            val items = adminRemoteDataSource.getContentItems(filters, limit, lastContentId)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get content items"))
        }
    }

    override fun getContentItem(
        contentId: String,
        contentType: AdminContentType
    ): Flow<Resource<AdminContentItem?>> = flow {
        emit(Resource.Loading())
        try {
            val item = adminRemoteDataSource.getContentItem(contentId, contentType)
            emit(Resource.Success(item))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get content item"))
        }
    }

    // POST ACTIONS (3 actions) - according to admin flow plan
    override fun hidePost(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = postId,
                contentType = AdminContentType.POST,
                status = ContentStatus.HIDDEN,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = postId,
                contentType = AdminContentType.POST,
                actionType = ModerationActionType.HIDE,
                reason = reason,
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to hide post"))
        }
    }

    override fun unhidePost(
        postId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = postId,
                contentType = AdminContentType.POST,
                status = ContentStatus.VISIBLE,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = postId,
                contentType = AdminContentType.POST,
                actionType = ModerationActionType.UNHIDE,
                reason = "",
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to unhide post"))
        }
    }

    override fun removePost(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = postId,
                contentType = AdminContentType.POST,
                status = ContentStatus.REMOVED,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = postId,
                contentType = AdminContentType.POST,
                actionType = ModerationActionType.REMOVE,
                reason = reason,
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove post"))
        }
    }

    // RECIPE ACTIONS (4 actions) - according to admin flow plan
    override fun hideRecipe(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                status = ContentStatus.HIDDEN,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                actionType = ModerationActionType.HIDE,
                reason = reason,
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to hide recipe"))
        }
    }

    override fun unhideRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                status = ContentStatus.VISIBLE,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                actionType = ModerationActionType.UNHIDE,
                reason = "",
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to unhide recipe"))
        }
    }

    override fun featureRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateRecipeFeatureStatus(recipeId, true, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                actionType = ModerationActionType.FEATURE,
                reason = "",
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to feature recipe"))
        }
    }

    override fun unfeatureRecipe(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateRecipeFeatureStatus(recipeId, false, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                actionType = ModerationActionType.UNFEATURE,
                reason = "",
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to unfeature recipe"))
        }
    }

    override fun removeRecipe(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                status = ContentStatus.REMOVED,
                moderatorId = moderatorId
            )

            // Log moderation action
            val action = ModerationAction(
                contentId = recipeId,
                contentType = AdminContentType.RECIPE,
                actionType = ModerationActionType.REMOVE,
                reason = reason,
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove recipe"))
        }
    }

    override fun getUsers(
        filters: AdminUserFilters,
        limit: Int,
        lastUserId: String?
    ): Flow<Resource<List<AdminUserItem>>> = flow {
        emit(Resource.Loading())
        try {
            val users = adminRemoteDataSource.getUsers(filters, limit, lastUserId)
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get users"))
        }
    }

    override fun getUser(userId: String): Flow<Resource<AdminUserItem?>> = flow {
        emit(Resource.Loading())
        try {
            val user = adminRemoteDataSource.getUser(userId)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get user"))
        }
    }

    // USER ACTIONS (4 actions) - according to admin flow plan
    override fun suspendUser(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserStatus(userId, UserStatus.SUSPENDED, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = "",
                contentType = AdminContentType.POST, // Default, not relevant for user actions
                actionType = ModerationActionType.SUSPEND,
                reason = reason,
                moderatorId = moderatorId,
                targetUserId = userId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to suspend user"))
        }
    }

    override fun unsuspendUser(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserStatus(userId, UserStatus.ACTIVE, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = "",
                contentType = AdminContentType.POST, // Default, not relevant for user actions
                actionType = ModerationActionType.UNSUSPEND,
                reason = "",
                moderatorId = moderatorId,
                targetUserId = userId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to unsuspend user"))
        }
    }

    override fun makeAdmin(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserAdminStatus(userId, true, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = "",
                contentType = AdminContentType.POST, // Default, not relevant for user actions
                actionType = ModerationActionType.MAKE_ADMIN,
                reason = "",
                moderatorId = moderatorId,
                targetUserId = userId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to make user admin"))
        }
    }

    override fun removeAdmin(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserAdminStatus(userId, false, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = "",
                contentType = AdminContentType.POST, // Default, not relevant for user actions
                actionType = ModerationActionType.REMOVE_ADMIN,
                reason = "",
                moderatorId = moderatorId,
                targetUserId = userId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove admin status"))
        }
    }

    override fun banUser(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserStatus(userId, UserStatus.BANNED, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = "",
                contentType = AdminContentType.POST, // Default, not relevant for user actions
                actionType = ModerationActionType.BAN,
                reason = reason,
                moderatorId = moderatorId,
                targetUserId = userId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to ban user"))
        }
    }

    override fun logModerationAction(action: ModerationAction): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.logModerationAction(action)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to log moderation action"))
        }
    }

    override fun getModerationHistory(
        contentId: String?,
        userId: String?,
        moderatorId: String?,
        limit: Int
    ): Flow<Resource<List<ModerationAction>>> = flow {
        emit(Resource.Loading())
        try {
            val history = adminRemoteDataSource.getModerationHistory(contentId, userId, moderatorId, limit)
            emit(Resource.Success(history))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get moderation history"))
        }
    }

    override fun getFlaggedContent(
        contentType: AdminContentType?,
        limit: Int
    ): Flow<Resource<List<AdminContentItem>>> = flow {
        emit(Resource.Loading())
        try {
            val filters = AdminContentFilters(
                contentType = contentType,
                isFlagged = true
            )
            val items = adminRemoteDataSource.getContentItems(filters, limit, null)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get flagged content"))
        }
    }

    override fun flagContent(
        contentId: String,
        contentType: AdminContentType,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.flagContent(contentId, contentType, reason, moderatorId)

            // Log moderation action
            val action = ModerationAction(
                contentId = contentId,
                contentType = contentType,
                actionType = ModerationActionType.FLAG,
                reason = reason,
                moderatorId = moderatorId,
                createdAt = com.google.firebase.Timestamp.now()
            )
            adminRemoteDataSource.logModerationAction(action)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to flag content"))
        }
    }

    override fun getPendingReports(limit: Int): Flow<Resource<List<AdminContentItem>>> = flow {
        emit(Resource.Loading())
        try {
            val reports = adminRemoteDataSource.getPendingReports(limit)
            emit(Resource.Success(reports))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get pending reports"))
        }
    }
}