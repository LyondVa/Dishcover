package com.nhatpham.dishcover.domain.usecase.admin

import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.domain.repository.AdminRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Dashboard Use Cases
class GetDashboardStatsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<AdminDashboardStats>> =
        adminRepository.getDashboardStats()
}

// Content Management Use Cases
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

class GetContentItemUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        contentId: String,
        contentType: AdminContentType
    ): Flow<Resource<AdminContentItem?>> =
        adminRepository.getContentItem(contentId, contentType)
}

// POST ACTIONS (3 actions) - according to admin flow plan
class HidePostUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.hidePost(postId, reason, moderatorId)
}

class UnhidePostUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        postId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.unhidePost(postId, moderatorId)
}

class RemovePostUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        postId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.removePost(postId, reason, moderatorId)
}

// RECIPE ACTIONS (4 actions) - according to admin flow plan
class HideRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.hideRecipe(recipeId, reason, moderatorId)
}

class UnhideRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.unhideRecipe(recipeId, moderatorId)
}

class FeatureRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.featureRecipe(recipeId, moderatorId)
}

class UnfeatureRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.unfeatureRecipe(recipeId, moderatorId)
}

class RemoveRecipeUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        recipeId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.removeRecipe(recipeId, reason, moderatorId)
}

// User Management Use Cases
class GetUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        filters: AdminUserFilters = AdminUserFilters(),
        limit: Int = 20,
        lastUserId: String? = null
    ): Flow<Resource<List<AdminUserItem>>> =
        adminRepository.getUsers(filters, limit, lastUserId)
}

class GetUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(userId: String): Flow<Resource<AdminUserItem?>> =
        adminRepository.getUser(userId)
}

// USER ACTIONS (4 actions) - according to admin flow plan
class SuspendUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.suspendUser(userId, reason, moderatorId)
}

class UnsuspendUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.unsuspendUser(userId, moderatorId)
}

class MakeAdminUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.makeAdmin(userId, moderatorId)
}

class RemoveAdminUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.removeAdmin(userId, moderatorId)
}

class BanUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        userId: String,
        reason: String,
        moderatorId: String
    ): Flow<Resource<Unit>> =
        adminRepository.banUser(userId, reason, moderatorId)
}

// Moderation and Reports Use Cases
class LogModerationActionUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(action: ModerationAction): Flow<Resource<Unit>> =
        adminRepository.logModerationAction(action)
}

class GetModerationHistoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(
        contentId: String? = null,
        userId: String? = null,
        moderatorId: String? = null,
        limit: Int = 50
    ): Flow<Resource<List<ModerationAction>>> =
        adminRepository.getModerationHistory(contentId, userId, moderatorId, limit)
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

class GetPendingReportsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    operator fun invoke(limit: Int = 20): Flow<Resource<List<AdminContentItem>>> =
        adminRepository.getPendingReports(limit)
}