// CookbookUseCases.kt
package com.nhatpham.dishcover.domain.usecase.cookbook

import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.repository.CookbookRepository
import com.nhatpham.dishcover.domain.repository.CookbookStats
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Core Cookbook CRUD Use Cases
class CreateCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbook: Cookbook): Flow<Resource<Cookbook>> =
        cookbookRepository.createCookbook(cookbook)
}

class UpdateCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbook: Cookbook): Flow<Resource<Cookbook>> =
        cookbookRepository.updateCookbook(cookbook)
}

class DeleteCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.deleteCookbook(cookbookId)
}

class GetCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String): Flow<Resource<Cookbook>> =
        cookbookRepository.getCookbook(cookbookId)
}

// Cookbook Query Use Cases
class GetUserCookbooksUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, limit: Int = 10): Flow<Resource<List<CookbookListItem>>> =
        cookbookRepository.getUserCookbooks(userId, limit)
}

class GetPublicCookbooksUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(limit: Int = 20, lastCookbookId: String? = null): Flow<Resource<List<CookbookListItem>>> =
        cookbookRepository.getPublicCookbooks(limit, lastCookbookId)
}

class GetFeaturedCookbooksUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(limit: Int = 10): Flow<Resource<List<CookbookListItem>>> =
        cookbookRepository.getFeaturedCookbooks(limit)
}

class GetFollowedCookbooksUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, limit: Int = 10): Flow<Resource<List<CookbookListItem>>> =
        cookbookRepository.getFollowedCookbooks(userId, limit)
}

class SearchCookbooksUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(query: String, limit: Int = 20): Flow<Resource<List<CookbookListItem>>> =
        cookbookRepository.searchCookbooks(query, limit)
}

// Recipe Management Use Cases
class AddRecipeToCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookRecipe: CookbookRecipe): Flow<Resource<CookbookRecipe>> =
        cookbookRepository.addRecipeToCookbook(cookbookRecipe)
}

class RemoveRecipeFromCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookRecipeId: String): Flow<Resource<Boolean>> =
        cookbookRepository.removeRecipeFromCookbook(cookbookRecipeId)
}

class GetCookbookRecipesUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>> =
        cookbookRepository.getCookbookRecipes(cookbookId, limit)
}

class ReorderCookbookRecipesUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(
        cookbookId: String,
        recipeOrders: List<Pair<String, Int>>
    ): Flow<Resource<Boolean>> =
        cookbookRepository.reorderCookbookRecipes(cookbookId, recipeOrders)
}

// Collaboration Use Cases
class InviteCollaboratorUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(collaborator: CookbookCollaborator): Flow<Resource<CookbookCollaborator>> =
        cookbookRepository.inviteCollaborator(collaborator)
}

class RespondToInvitationUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(collaboratorId: String, accept: Boolean): Flow<Resource<Boolean>> =
        cookbookRepository.respondToInvitation(collaboratorId, accept)
}

class RemoveCollaboratorUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(collaboratorId: String): Flow<Resource<Boolean>> =
        cookbookRepository.removeCollaborator(collaboratorId)
}

class UpdateCollaboratorRoleUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(collaboratorId: String, role: CookbookRole): Flow<Resource<Boolean>> =
        cookbookRepository.updateCollaboratorRole(collaboratorId, role)
}

class GetCookbookCollaboratorsUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String): Flow<Resource<List<CookbookCollaborator>>> =
        cookbookRepository.getCookbookCollaborators(cookbookId)
}

class GetUserCookbookInvitationsUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<CookbookCollaborator>>> =
        cookbookRepository.getUserCookbookInvitations(userId)
}

// Follow Use Cases
class FollowCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.followCookbook(userId, cookbookId)
}

class UnfollowCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.unfollowCookbook(userId, cookbookId)
}

class IsCookbookFollowedByUserUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.isCookbookFollowedByUser(userId, cookbookId)
}

// Like Use Cases
class LikeCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.likeCookbook(userId, cookbookId)
}

class UnlikeCookbookUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.unlikeCookbook(userId, cookbookId)
}

class IsCookbookLikedByUserUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(userId: String, cookbookId: String): Flow<Resource<Boolean>> =
        cookbookRepository.isCookbookLikedByUser(userId, cookbookId)
}

// Analytics Use Cases
class IncrementCookbookViewUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String, userId: String? = null): Flow<Resource<Boolean>> =
        cookbookRepository.incrementCookbookView(cookbookId, userId)
}

class GetCookbookStatsUseCase @Inject constructor(
    private val cookbookRepository: CookbookRepository
) {
    operator fun invoke(cookbookId: String): Flow<Resource<CookbookStats>> =
        cookbookRepository.getCookbookStats(cookbookId)
}