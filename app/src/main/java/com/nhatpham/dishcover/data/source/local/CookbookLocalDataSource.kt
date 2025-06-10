// CookbookLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.repository.CookbookStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CookbookLocalDataSource @Inject constructor() {

    // Cache maps for different data types
    private val cookbooksCache = mutableMapOf<String, Cookbook>()
    private val userCookbooksCache = mutableMapOf<String, List<CookbookListItem>>()
    private val publicCookbooksCache = mutableMapOf<String, List<CookbookListItem>>()
    private val featuredCookbooksCache = mutableListOf<CookbookListItem>()
    private val followedCookbooksCache = mutableMapOf<String, List<CookbookListItem>>()
    private val cookbookRecipesCache = mutableMapOf<String, List<RecipeListItem>>()
    private val cookbookCollaboratorsCache = mutableMapOf<String, List<CookbookCollaborator>>()
    private val cookbookFollowStatusCache = mutableMapOf<String, Boolean>()
    private val cookbookLikeStatusCache = mutableMapOf<String, Boolean>()
    private val cookbookStatsCache = mutableMapOf<String, CookbookStats>()

    // Core cookbook operations
    suspend fun getCookbook(cookbookId: String): Cookbook? = withContext(Dispatchers.IO) {
        return@withContext cookbooksCache[cookbookId]
    }

    suspend fun saveCookbook(cookbook: Cookbook) = withContext(Dispatchers.IO) {
        cookbooksCache[cookbook.cookbookId] = cookbook
    }

    suspend fun removeCookbook(cookbookId: String) = withContext(Dispatchers.IO) {
        cookbooksCache.remove(cookbookId)
        // Clear related caches
        cookbookRecipesCache.remove(cookbookId)
        cookbookCollaboratorsCache.remove(cookbookId)
        cookbookStatsCache.remove(cookbookId)
    }

    // User cookbooks
    suspend fun getUserCookbooks(userId: String): List<CookbookListItem> = withContext(Dispatchers.IO) {
        return@withContext userCookbooksCache[userId] ?: emptyList()
    }

    suspend fun saveUserCookbooks(userId: String, cookbooks: List<CookbookListItem>) = withContext(Dispatchers.IO) {
        userCookbooksCache[userId] = cookbooks
    }

    suspend fun addToUserCookbooks(userId: String, cookbook: CookbookListItem) = withContext(Dispatchers.IO) {
        val currentList = userCookbooksCache[userId]?.toMutableList() ?: mutableListOf()
        currentList.add(0, cookbook) // Add to beginning
        userCookbooksCache[userId] = currentList
    }

    suspend fun removeFromUserCookbooks(userId: String, cookbookId: String) = withContext(Dispatchers.IO) {
        val currentList = userCookbooksCache[userId]?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.cookbookId == cookbookId }
        userCookbooksCache[userId] = currentList
    }

    // Public cookbooks
    suspend fun getPublicCookbooks(cacheKey: String): List<CookbookListItem> = withContext(Dispatchers.IO) {
        return@withContext publicCookbooksCache[cacheKey] ?: emptyList()
    }

    suspend fun savePublicCookbooks(cacheKey: String, cookbooks: List<CookbookListItem>) = withContext(Dispatchers.IO) {
        publicCookbooksCache[cacheKey] = cookbooks
    }

    // Featured cookbooks
    suspend fun getFeaturedCookbooks(): List<CookbookListItem> = withContext(Dispatchers.IO) {
        return@withContext featuredCookbooksCache.toList()
    }

    suspend fun saveFeaturedCookbooks(cookbooks: List<CookbookListItem>) = withContext(Dispatchers.IO) {
        featuredCookbooksCache.clear()
        featuredCookbooksCache.addAll(cookbooks)
    }

    // Followed cookbooks
    suspend fun getFollowedCookbooks(userId: String): List<CookbookListItem> = withContext(Dispatchers.IO) {
        return@withContext followedCookbooksCache[userId] ?: emptyList()
    }

    suspend fun saveFollowedCookbooks(userId: String, cookbooks: List<CookbookListItem>) = withContext(Dispatchers.IO) {
        followedCookbooksCache[userId] = cookbooks
    }

    suspend fun addToFollowedCookbooks(userId: String, cookbook: CookbookListItem) = withContext(Dispatchers.IO) {
        val currentList = followedCookbooksCache[userId]?.toMutableList() ?: mutableListOf()
        currentList.add(0, cookbook)
        followedCookbooksCache[userId] = currentList
    }

    suspend fun removeFromFollowedCookbooks(userId: String, cookbookId: String) = withContext(Dispatchers.IO) {
        val currentList = followedCookbooksCache[userId]?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.cookbookId == cookbookId }
        followedCookbooksCache[userId] = currentList
    }

    // Cookbook recipes
    suspend fun getCookbookRecipes(cookbookId: String): List<RecipeListItem> = withContext(Dispatchers.IO) {
        return@withContext cookbookRecipesCache[cookbookId] ?: emptyList()
    }

    suspend fun saveCookbookRecipes(cookbookId: String, recipes: List<RecipeListItem>) = withContext(Dispatchers.IO) {
        cookbookRecipesCache[cookbookId] = recipes
    }

    suspend fun addRecipeToCookbook(cookbookId: String, recipe: RecipeListItem) = withContext(Dispatchers.IO) {
        val currentList = cookbookRecipesCache[cookbookId]?.toMutableList() ?: mutableListOf()
        currentList.add(recipe)
        cookbookRecipesCache[cookbookId] = currentList
    }

    suspend fun removeRecipeFromCookbook(cookbookId: String, recipeId: String) = withContext(Dispatchers.IO) {
        val currentList = cookbookRecipesCache[cookbookId]?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.recipeId == recipeId }
        cookbookRecipesCache[cookbookId] = currentList
    }

    // Collaborators
    suspend fun getCookbookCollaborators(cookbookId: String): List<CookbookCollaborator> = withContext(Dispatchers.IO) {
        return@withContext cookbookCollaboratorsCache[cookbookId] ?: emptyList()
    }

    suspend fun saveCookbookCollaborators(cookbookId: String, collaborators: List<CookbookCollaborator>) = withContext(Dispatchers.IO) {
        cookbookCollaboratorsCache[cookbookId] = collaborators
    }

    suspend fun addCollaborator(cookbookId: String, collaborator: CookbookCollaborator) = withContext(Dispatchers.IO) {
        val currentList = cookbookCollaboratorsCache[cookbookId]?.toMutableList() ?: mutableListOf()
        currentList.add(collaborator)
        cookbookCollaboratorsCache[cookbookId] = currentList
    }

    suspend fun removeCollaborator(cookbookId: String, collaboratorId: String) = withContext(Dispatchers.IO) {
        val currentList = cookbookCollaboratorsCache[cookbookId]?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.collaboratorId == collaboratorId }
        cookbookCollaboratorsCache[cookbookId] = currentList
    }

    // Follow status
    suspend fun getCookbookFollowStatus(userId: String, cookbookId: String): Boolean? = withContext(Dispatchers.IO) {
        return@withContext cookbookFollowStatusCache["${userId}_${cookbookId}"]
    }

    suspend fun setCookbookFollowStatus(userId: String, cookbookId: String, following: Boolean) = withContext(Dispatchers.IO) {
        cookbookFollowStatusCache["${userId}_${cookbookId}"] = following
    }

    // Like status
    suspend fun getCookbookLikeStatus(userId: String, cookbookId: String): Boolean? = withContext(Dispatchers.IO) {
        return@withContext cookbookLikeStatusCache["${userId}_${cookbookId}"]
    }

    suspend fun setCookbookLikeStatus(userId: String, cookbookId: String, liked: Boolean) = withContext(Dispatchers.IO) {
        cookbookLikeStatusCache["${userId}_${cookbookId}"] = liked
    }

    // Stats
    suspend fun getCookbookStats(cookbookId: String): CookbookStats? = withContext(Dispatchers.IO) {
        return@withContext cookbookStatsCache[cookbookId]
    }

    suspend fun saveCookbookStats(stats: CookbookStats) = withContext(Dispatchers.IO) {
        cookbookStatsCache[stats.cookbookId] = stats
    }

    // Cache management
    suspend fun clearUserCookbooksCache(userId: String) = withContext(Dispatchers.IO) {
        userCookbooksCache.remove(userId)
        followedCookbooksCache.remove(userId)
    }

    suspend fun clearCookbookCache(cookbookId: String) = withContext(Dispatchers.IO) {
        cookbooksCache.remove(cookbookId)
        cookbookRecipesCache.remove(cookbookId)
        cookbookCollaboratorsCache.remove(cookbookId)
        cookbookStatsCache.remove(cookbookId)
    }

    suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        cookbooksCache.clear()
        userCookbooksCache.clear()
        publicCookbooksCache.clear()
        featuredCookbooksCache.clear()
        followedCookbooksCache.clear()
        cookbookRecipesCache.clear()
        cookbookCollaboratorsCache.clear()
        cookbookFollowStatusCache.clear()
        cookbookLikeStatusCache.clear()
        cookbookStatsCache.clear()
    }

    // Helper methods
    suspend fun hasCachedUserCookbooks(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userCookbooksCache.containsKey(userId)
    }

    suspend fun hasCachedCookbook(cookbookId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext cookbooksCache.containsKey(cookbookId)
    }

    suspend fun hasCachedFeaturedCookbooks(): Boolean = withContext(Dispatchers.IO) {
        return@withContext featuredCookbooksCache.isNotEmpty()
    }
}