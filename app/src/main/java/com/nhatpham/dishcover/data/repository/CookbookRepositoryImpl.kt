// CookbookRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.mapper.toListItem
import com.nhatpham.dishcover.data.source.local.CookbookLocalDataSource
import com.nhatpham.dishcover.data.source.remote.CookbookRemoteDataSource
import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.repository.CookbookRepository
import com.nhatpham.dishcover.domain.repository.CookbookStats
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class CookbookRepositoryImpl @Inject constructor(
    private val cookbookRemoteDataSource: CookbookRemoteDataSource,
    private val cookbookLocalDataSource: CookbookLocalDataSource,
    private val recipeRepository: RecipeRepository
) : CookbookRepository {

    // Core Cookbook CRUD operations
    override fun createCookbook(cookbook: Cookbook): Flow<Resource<Cookbook>> = flow {
        try {
            emit(Resource.Loading())

            val createdCookbook = cookbookRemoteDataSource.createCookbook(cookbook)
            if (createdCookbook != null) {
                // Update local cache
                cookbookLocalDataSource.saveCookbook(createdCookbook)
                cookbookLocalDataSource.addToUserCookbooks(
                    createdCookbook.userId,
                    createdCookbook.toListItem()
                )

                emit(Resource.Success(createdCookbook))
            } else {
                emit(Resource.Error("Failed to create cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateCookbook(cookbook: Cookbook): Flow<Resource<Cookbook>> = flow {
        try {
            emit(Resource.Loading())

            val updatedCookbook = cookbookRemoteDataSource.updateCookbook(cookbook)
            if (updatedCookbook != null) {
                // Update local cache
                cookbookLocalDataSource.saveCookbook(updatedCookbook)

                emit(Resource.Success(updatedCookbook))
            } else {
                emit(Resource.Error("Failed to update cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deleteCookbook(cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val deleted = cookbookRemoteDataSource.deleteCookbook(cookbookId)
            if (deleted) {
                // Clear from local cache
                cookbookLocalDataSource.removeCookbook(cookbookId)

                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbook(cookbookId: String): Flow<Resource<Cookbook>> = flow {
        try {
            emit(Resource.Loading())

            // Check local cache first
            val cachedCookbook = cookbookLocalDataSource.getCookbook(cookbookId)
            if (cachedCookbook != null) {
                emit(Resource.Success(cachedCookbook))
                return@flow
            }

            // Fetch from remote
            val remoteCookbook = cookbookRemoteDataSource.getCookbook(cookbookId)
            if (remoteCookbook != null) {
                // Update cache
                cookbookLocalDataSource.saveCookbook(remoteCookbook)
                emit(Resource.Success(remoteCookbook))
            } else {
                emit(Resource.Error("Cookbook not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Cookbook queries
    override fun getUserCookbooks(userId: String, limit: Int): Flow<Resource<List<CookbookListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            if (cookbookLocalDataSource.hasCachedUserCookbooks(userId)) {
                val cached = cookbookLocalDataSource.getUserCookbooks(userId)
                emit(Resource.Success(cached))
                return@flow
            }

            // Fetch from remote
            val remoteCookbooks = cookbookRemoteDataSource.getUserCookbooks(userId, limit)
            cookbookLocalDataSource.saveUserCookbooks(userId, remoteCookbooks)
            emit(Resource.Success(remoteCookbooks))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPublicCookbooks(limit: Int, lastCookbookId: String?): Flow<Resource<List<CookbookListItem>>> = flow {
        try {
            emit(Resource.Loading())

            val cacheKey = "public_${limit}_${lastCookbookId ?: "start"}"

            // Check cache first (for initial page only)
            if (lastCookbookId == null) {
                val cached = cookbookLocalDataSource.getPublicCookbooks(cacheKey)
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached))
                    return@flow
                }
            }

            // Fetch from remote
            val remoteCookbooks = cookbookRemoteDataSource.getPublicCookbooks(limit, lastCookbookId)
            if (lastCookbookId == null) {
                cookbookLocalDataSource.savePublicCookbooks(cacheKey, remoteCookbooks)
            }
            emit(Resource.Success(remoteCookbooks))
        } catch (e: Exception) {
            Timber.e(e, "Error getting public cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFeaturedCookbooks(limit: Int): Flow<Resource<List<CookbookListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            if (cookbookLocalDataSource.hasCachedFeaturedCookbooks()) {
                val cached = cookbookLocalDataSource.getFeaturedCookbooks()
                emit(Resource.Success(cached))
                return@flow
            }

            // Fetch from remote
            val remoteCookbooks = cookbookRemoteDataSource.getFeaturedCookbooks(limit)
            cookbookLocalDataSource.saveFeaturedCookbooks(remoteCookbooks)
            emit(Resource.Success(remoteCookbooks))
        } catch (e: Exception) {
            Timber.e(e, "Error getting featured cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFollowedCookbooks(userId: String, limit: Int): Flow<Resource<List<CookbookListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cached = cookbookLocalDataSource.getFollowedCookbooks(userId)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
                return@flow
            }

            // Fetch from remote using join operation
            val followedCookbooks = cookbookRemoteDataSource.getFollowedCookbooks(userId, limit)
            cookbookLocalDataSource.saveFollowedCookbooks(userId, followedCookbooks)
            emit(Resource.Success(followedCookbooks))
        } catch (e: Exception) {
            Timber.e(e, "Error getting followed cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun searchCookbooks(query: String, limit: Int): Flow<Resource<List<CookbookListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Always fetch from remote for search (no caching)
            val searchResults = cookbookRemoteDataSource.searchCookbooks(query, limit)
            emit(Resource.Success(searchResults))
        } catch (e: Exception) {
            Timber.e(e, "Error searching cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Recipe management in cookbooks
    override fun addRecipeToCookbook(cookbookRecipe: CookbookRecipe): Flow<Resource<CookbookRecipe>> = flow {
        try {
            emit(Resource.Loading())

            val addedRecipe = cookbookRemoteDataSource.addRecipeToCookbook(cookbookRecipe)
            if (addedRecipe != null) {
                // Get the recipe details and add to local cache
                val recipeResult = recipeRepository.getRecipe(cookbookRecipe.recipeId).first()
                if (recipeResult is Resource.Success) {
                    val recipeListItem = recipeResult.data?.let { recipe ->
                        RecipeListItem(
                            recipeId = recipe.recipeId,
                            title = recipe.title,
                            description = recipe.description,
                            coverImage = recipe.coverImage,
                            prepTime = recipe.prepTime,
                            cookTime = recipe.cookTime,
                            servings = recipe.servings,
                            difficultyLevel = recipe.difficultyLevel,
                            likeCount = recipe.likeCount,
                            viewCount = recipe.viewCount,
                            isPublic = recipe.isPublic,
                            isFeatured = recipe.isFeatured,
                            userId = recipe.userId,
                            createdAt = recipe.createdAt,
                            tags = recipe.tags
                        )
                    }

                    if (recipeListItem != null) {
                        cookbookLocalDataSource.addRecipeToCookbook(cookbookRecipe.cookbookId, recipeListItem)
                    }
                }

                emit(Resource.Success(addedRecipe))
            } else {
                emit(Resource.Error("Failed to add recipe to cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe to cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeRecipeFromCookbook(cookbookRecipeId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val removed = cookbookRemoteDataSource.removeRecipeFromCookbook(cookbookRecipeId)
            if (removed) {
                // Note: We'd need to track cookbook-recipe mapping to remove from local cache
                // For now, we'll clear the entire cookbook recipes cache
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove recipe from cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe from cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbookRecipes(cookbookId: String, limit: Int): Flow<Resource<List<RecipeListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cached = cookbookLocalDataSource.getCookbookRecipes(cookbookId)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
                return@flow
            }

            // Get recipe IDs from remote
            val recipeIds = cookbookRemoteDataSource.getCookbookRecipeIds(cookbookId, limit)

            // Fetch recipe details
            val recipes = mutableListOf<RecipeListItem>()
            for (recipeId in recipeIds) {
                val recipeResult = recipeRepository.getRecipe(recipeId).first()
                if (recipeResult is Resource.Success && recipeResult.data != null) {
                    val recipe = recipeResult.data
                    recipes.add(RecipeListItem(
                        recipeId = recipe.recipeId,
                        title = recipe.title,
                        description = recipe.description,
                        coverImage = recipe.coverImage,
                        prepTime = recipe.prepTime,
                        cookTime = recipe.cookTime,
                        servings = recipe.servings,
                        difficultyLevel = recipe.difficultyLevel,
                        likeCount = recipe.likeCount,
                        viewCount = recipe.viewCount,
                        isPublic = recipe.isPublic,
                        isFeatured = recipe.isFeatured,
                        userId = recipe.userId,
                        createdAt = recipe.createdAt,
                        tags = recipe.tags
                    ))
                }
            }

            // Update cache
            cookbookLocalDataSource.saveCookbookRecipes(cookbookId, recipes)
            emit(Resource.Success(recipes))
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook recipes")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun reorderCookbookRecipes(cookbookId: String, recipeOrders: List<Pair<String, Int>>): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val reordered = cookbookRemoteDataSource.reorderCookbookRecipes(cookbookId, recipeOrders)
            if (reordered) {
                // Clear cookbook recipes cache to refresh with new order
                cookbookLocalDataSource.saveCookbookRecipes(cookbookId, emptyList())
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to reorder cookbook recipes"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reordering cookbook recipes")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Collaboration operations
    override fun inviteCollaborator(collaborator: CookbookCollaborator): Flow<Resource<CookbookCollaborator>> = flow {
        try {
            emit(Resource.Loading())

            val invitedCollaborator = cookbookRemoteDataSource.inviteCollaborator(collaborator)
            if (invitedCollaborator != null) {
                // Update local cache
                cookbookLocalDataSource.addCollaborator(collaborator.cookbookId, invitedCollaborator)
                emit(Resource.Success(invitedCollaborator))
            } else {
                emit(Resource.Error("Failed to invite collaborator"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inviting collaborator")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun respondToInvitation(collaboratorId: String, accept: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val responded = cookbookRemoteDataSource.respondToInvitation(collaboratorId, accept)
            if (responded) {
                // Clear invitations cache to refresh
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to respond to invitation"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error responding to invitation")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeCollaborator(collaboratorId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val removed = cookbookRemoteDataSource.removeCollaborator(collaboratorId)
            if (removed) {
                // Note: Would need to track which cookbook to update cache properly
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove collaborator"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing collaborator")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateCollaboratorRole(collaboratorId: String, role: CookbookRole): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val updated = cookbookRemoteDataSource.updateCollaboratorRole(collaboratorId, role)
            if (updated) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to update collaborator role"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating collaborator role")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbookCollaborators(cookbookId: String): Flow<Resource<List<CookbookCollaborator>>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cached = cookbookLocalDataSource.getCookbookCollaborators(cookbookId)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
                return@flow
            }

            // Fetch from remote
            val collaborators = cookbookRemoteDataSource.getCookbookCollaborators(cookbookId)
            cookbookLocalDataSource.saveCookbookCollaborators(cookbookId, collaborators)
            emit(Resource.Success(collaborators))
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook collaborators")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserCookbookInvitations(userId: String): Flow<Resource<List<CookbookCollaborator>>> = flow {
        try {
            emit(Resource.Loading())

            // Always fetch from remote for invitations (no caching)
            val invitations = cookbookRemoteDataSource.getUserCookbookInvitations(userId)
            emit(Resource.Success(invitations))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user cookbook invitations")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Follow operations
    override fun followCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val followed = cookbookRemoteDataSource.followCookbook(userId, cookbookId)
            if (followed) {
                // Update local cache
                cookbookLocalDataSource.setCookbookFollowStatus(userId, cookbookId, true)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to follow cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error following cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unfollowCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val unfollowed = cookbookRemoteDataSource.unfollowCookbook(userId, cookbookId)
            if (unfollowed) {
                // Update local cache
                cookbookLocalDataSource.setCookbookFollowStatus(userId, cookbookId, false)
                cookbookLocalDataSource.removeFromFollowedCookbooks(userId, cookbookId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to unfollow cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error unfollowing cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isCookbookFollowedByUser(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = cookbookLocalDataSource.getCookbookFollowStatus(userId, cookbookId)
            if (cachedStatus != null) {
                emit(Resource.Success(cachedStatus))
                return@flow
            }

            // Check remote
            val isFollowed = cookbookRemoteDataSource.isCookbookFollowedByUser(userId, cookbookId)
            cookbookLocalDataSource.setCookbookFollowStatus(userId, cookbookId, isFollowed)
            emit(Resource.Success(isFollowed))
        } catch (e: Exception) {
            Timber.e(e, "Error checking if cookbook is followed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbookFollowers(cookbookId: String, limit: Int): Flow<Resource<List<CookbookFollower>>> = flow {
        try {
            emit(Resource.Loading())

            // Always fetch from remote (no caching for follower lists)
            val followers = cookbookRemoteDataSource.getCookbookFollowers(cookbookId, limit)
            emit(Resource.Success(followers))
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook followers")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Like operations
    override fun likeCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val liked = cookbookRemoteDataSource.likeCookbook(userId, cookbookId)
            if (liked) {
                // Update local cache
                cookbookLocalDataSource.setCookbookLikeStatus(userId, cookbookId, true)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to like cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error liking cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unlikeCookbook(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val unliked = cookbookRemoteDataSource.unlikeCookbook(userId, cookbookId)
            if (unliked) {
                // Update local cache
                cookbookLocalDataSource.setCookbookLikeStatus(userId, cookbookId, false)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to unlike cookbook"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error unliking cookbook")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isCookbookLikedByUser(userId: String, cookbookId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = cookbookLocalDataSource.getCookbookLikeStatus(userId, cookbookId)
            if (cachedStatus != null) {
                emit(Resource.Success(cachedStatus))
                return@flow
            }

            // Check remote
            val isLiked = cookbookRemoteDataSource.isCookbookLikedByUser(userId, cookbookId)
            cookbookLocalDataSource.setCookbookLikeStatus(userId, cookbookId, isLiked)
            emit(Resource.Success(isLiked))
        } catch (e: Exception) {
            Timber.e(e, "Error checking if cookbook is liked")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbookLikes(cookbookId: String, limit: Int): Flow<Resource<List<CookbookLike>>> = flow {
        try {
            emit(Resource.Loading())

            // Always fetch from remote (no caching for like lists)
            val likes = cookbookRemoteDataSource.getCookbookLikes(cookbookId, limit)
            emit(Resource.Success(likes))
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook likes")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Analytics
    override fun incrementCookbookView(cookbookId: String, userId: String?): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val incremented = cookbookRemoteDataSource.incrementCookbookView(cookbookId, userId)
            emit(Resource.Success(incremented))
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing cookbook view")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCookbookStats(cookbookId: String): Flow<Resource<CookbookStats>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStats = cookbookLocalDataSource.getCookbookStats(cookbookId)
            if (cachedStats != null) {
                emit(Resource.Success(cachedStats))
                return@flow
            }

            // Fetch from remote
            val remoteStats = cookbookRemoteDataSource.getCookbookStats(cookbookId)
            if (remoteStats != null) {
                cookbookLocalDataSource.saveCookbookStats(remoteStats)
                emit(Resource.Success(remoteStats))
            } else {
                emit(Resource.Error("Cookbook stats not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cookbook stats")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}