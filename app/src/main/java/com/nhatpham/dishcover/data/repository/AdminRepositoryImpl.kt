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
            val items = adminRemoteDataSource.getContentItems(
                AdminContentFilters(contentType = contentType),
                1,
                null
            )
            val item = items.find { it.contentId == contentId }
            emit(Resource.Success(item))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get content item"))
        }
    }

    override fun updateContentStatus(
        contentId: String,
        contentType: AdminContentType,
        status: ContentStatus,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateContentStatus(contentId, contentType, status, moderatorId)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update content status"))
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
            // Update the content to flagged status
            adminRemoteDataSource.updateContentStatus(
                contentId,
                contentType,
                ContentStatus.UNDER_REVIEW,
                moderatorId
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to flag content"))
        }
    }

    override fun featureRecipe(
        recipeId: String,
        featured: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.featureRecipe(recipeId, featured, moderatorId)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to feature recipe"))
        }
    }

    override fun getUsers(
        searchQuery: String,
        status: UserStatus?,
        limit: Int,
        lastUserId: String?
    ): Flow<Resource<List<AdminUserItem>>> = flow {
        emit(Resource.Loading())
        try {
            val users = adminRemoteDataSource.getUsers(searchQuery, status, limit, lastUserId)
            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get users"))
        }
    }

    override fun getUser(userId: String): Flow<Resource<AdminUserItem?>> = flow {
        emit(Resource.Loading())
        try {
            val users = adminRemoteDataSource.getUsers("", null, 1, null)
            val user = users.find { it.userId == userId }
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get user"))
        }
    }

    override fun updateUserStatus(
        userId: String,
        status: UserStatus,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserStatus(userId, status, moderatorId)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update user status"))
        }
    }

    override fun updateUserAdminStatus(
        userId: String,
        isAdmin: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            adminRemoteDataSource.updateUserAdminStatus(userId, isAdmin, moderatorId)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update admin status"))
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
        moderatorId: String?,
        limit: Int
    ): Flow<Resource<List<ModerationAction>>> = flow {
        emit(Resource.Loading())
        try {
            // Would implement querying moderation actions collection
            emit(Resource.Success(emptyList()))
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

    override fun getPendingReports(limit: Int): Flow<Resource<List<AdminContentItem>>> = flow {
        emit(Resource.Loading())
        try {
            val filters = AdminContentFilters(
                status = ContentStatus.UNDER_REVIEW
            )
            val items = adminRemoteDataSource.getContentItems(filters, limit, null)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get pending reports"))
        }
    }
}