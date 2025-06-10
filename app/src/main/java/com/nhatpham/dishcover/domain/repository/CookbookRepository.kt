// CookbookRepository.kt
package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface CookbookRepository {

    // Core Cookbook CRUD operations
    fun createCookbook(cookbook: Cookbook): Flow<Resource<Cookbook>>
    fun updateCookbook(cookbook: Cookbook): Flow<Resource<Cookbook>>
    fun deleteCookbook(cookbookId: String): Flow<Resource<Boolean>>
    fun getCookbook(cookbookId: String): Flow<Resource<Cookbook>>

    // Cookbook queries
    fun getUserCookbooks(userId: String, limit: Int = 10): Flow<Resource<List<CookbookListItem>>>
    fun getPublicCookbooks(limit: Int = 20, lastCookbookId: String? = null): Flow<Resource<List<CookbookListItem>>>
    fun getFeaturedCookbooks(limit: Int = 10): Flow<Resource<List<CookbookListItem>>>
    fun getFollowedCookbooks(userId: String, limit: Int = 10): Flow<Resource<List<CookbookListItem>>>
    fun searchCookbooks(query: String, limit: Int = 20): Flow<Resource<List<CookbookListItem>>>

    // Recipe management in cookbooks
    fun addRecipeToCookbook(cookbookRecipe: CookbookRecipe): Flow<Resource<CookbookRecipe>>
    fun removeRecipeFromCookbook(cookbookRecipeId: String): Flow<Resource<Boolean>>
    fun getCookbookRecipes(cookbookId: String, limit: Int = 20): Flow<Resource<List<RecipeListItem>>>
    fun reorderCookbookRecipes(cookbookId: String, recipeOrders: List<Pair<String, Int>>): Flow<Resource<Boolean>>

    // Collaboration operations
    fun inviteCollaborator(collaborator: CookbookCollaborator): Flow<Resource<CookbookCollaborator>>
    fun respondToInvitation(collaboratorId: String, accept: Boolean): Flow<Resource<Boolean>>
    fun removeCollaborator(collaboratorId: String): Flow<Resource<Boolean>>
    fun updateCollaboratorRole(collaboratorId: String, role: CookbookRole): Flow<Resource<Boolean>>
    fun getCookbookCollaborators(cookbookId: String): Flow<Resource<List<CookbookCollaborator>>>
    fun getUserCookbookInvitations(userId: String): Flow<Resource<List<CookbookCollaborator>>>

    // Follow operations
    fun followCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun unfollowCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun isCookbookFollowedByUser(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun getCookbookFollowers(cookbookId: String, limit: Int = 20): Flow<Resource<List<CookbookFollower>>>

    // Like operations
    fun likeCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun unlikeCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun isCookbookLikedByUser(userId: String, cookbookId: String): Flow<Resource<Boolean>>
    fun getCookbookLikes(cookbookId: String, limit: Int = 20): Flow<Resource<List<CookbookLike>>>

    // Analytics
    fun incrementCookbookView(cookbookId: String, userId: String? = null): Flow<Resource<Boolean>>
    fun getCookbookStats(cookbookId: String): Flow<Resource<CookbookStats>>
}

/**
 * Cookbook statistics model
 */
data class CookbookStats(
    val cookbookId: String = "",
    val totalViews: Int = 0,
    val totalLikes: Int = 0,
    val totalFollowers: Int = 0,
    val totalRecipes: Int = 0,
    val totalCollaborators: Int = 0,
    val viewsThisWeek: Int = 0,
    val viewsThisMonth: Int = 0
)