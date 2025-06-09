package com.nhatpham.dishcover.domain.usecase.admin

import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.domain.repository.AdminRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<AdminDashboardStats>> =
        adminRepository.getDashboardStats()
}

class GetContentItemsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        filters: AdminContentFilters = AdminContentFilters(),
        limit: Int = 20,
        lastContentId: String? = null
    ): Flow<Resource<List<AdminContentItem>>> =
        adminRepository.getContentItems(filters, limit, lastContentId)
}

class ModerateContentUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        contentId: String,
        contentType: AdminContentType,
        status: ContentStatus,
        reason: String = "",
        moderatorId: String
    ): Flow<Resource<Unit>> {
        return adminRepository.updateContentStatus(
            contentId = contentId,
            contentType = contentType,
            status = status,
            reason = reason,
            moderatorId = moderatorId
        )
    }
}

class FlagContentUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        contentId: String,
        contentType: AdminContentType,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.flagContent(contentId, contentType, reason, moderatorId)
}

class FeatureRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        featured: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.featureRecipe(recipeId, featured, moderatorId)
}

class GetUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        searchQuery: String = "",
        status: UserStatus? = null,
        limit: Int = 20,
        lastUserId: String? = null
    ): Flow<Resource<List<AdminUserItem>>> =
        adminRepository.getUsers(searchQuery, status, limit, lastUserId)
}

class ModerateUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        status: UserStatus,
        reason: String = "",
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.updateUserStatus(userId, status, reason, moderatorId)
}

class UpdateUserAdminStatusUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        isAdmin: Boolean,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.updateUserAdminStatus(userId, isAdmin, moderatorId)
}

class GetFlaggedContentUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        contentType: AdminContentType? = null,
        limit: Int = 20
    ): Flow<Resource<List<AdminContentItem>>> =
        adminRepository.getFlaggedContent(contentType, limit)
}