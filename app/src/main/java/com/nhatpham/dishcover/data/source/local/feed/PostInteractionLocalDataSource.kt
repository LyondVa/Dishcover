// PostInteractionLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import com.nhatpham.dishcover.domain.model.feed.PostLike
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.feed.PostShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostInteractionLocalDataSource @Inject constructor() {

    // Cache maps for interactions
    private val postLikesCache = mutableMapOf<String, List<PostLike>>()
    private val postSharesCache = mutableMapOf<String, List<PostShare>>()

    // Status caches (using primitives)
    private val postLikeStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean
    private val postShareStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean
    private val postSaveStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean

    // Like operations
    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> = withContext(Dispatchers.IO) {
        return@withContext postLikesCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostLikes(postId: String, likes: List<PostLike>) = withContext(Dispatchers.IO) {
        postLikesCache[postId] = likes
    }

    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postLikeStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostLikeStatus(userId: String, postId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        postLikeStatusCache["$userId:$postId"] = isLiked
    }

    // Share operations
    suspend fun getPostShares(postId: String, limit: Int): List<PostShare> = withContext(Dispatchers.IO) {
        return@withContext postSharesCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostShares(postId: String, shares: List<PostShare>) = withContext(Dispatchers.IO) {
        postSharesCache[postId] = shares
    }

    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postShareStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostShareStatus(userId: String, postId: String, isShared: Boolean) = withContext(Dispatchers.IO) {
        postShareStatusCache["$userId:$postId"] = isShared
    }

    // Save operations
    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postSaveStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostSaveStatus(userId: String, postId: String, isSaved: Boolean) = withContext(Dispatchers.IO) {
        postSaveStatusCache["$userId:$postId"] = isSaved
    }

    // User-specific post lists
    suspend fun getUserLikedPosts(userId: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        // This would require a more complex implementation in a real app
        // For now, return empty list as this data would typically come from remote
        return@withContext emptyList()
    }

    suspend fun getUserSharedPosts(userId: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        // This would require a more complex implementation in a real app
        return@withContext emptyList()
    }

    suspend fun getUserSavedPosts(userId: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        // This would require a more complex implementation in a real app
        return@withContext emptyList()
    }

    // Cache management for post deletion
    suspend fun deletePostInteractions(postId: String) = withContext(Dispatchers.IO) {
        postLikesCache.remove(postId)
        postSharesCache.remove(postId)

        // Remove status entries for this post
        val keysToRemove = mutableListOf<String>()

        postLikeStatusCache.keys.forEach { key ->
            if (key.endsWith(":$postId")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postLikeStatusCache.remove(it) }

        keysToRemove.clear()
        postShareStatusCache.keys.forEach { key ->
            if (key.endsWith(":$postId")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postShareStatusCache.remove(it) }

        keysToRemove.clear()
        postSaveStatusCache.keys.forEach { key ->
            if (key.endsWith(":$postId")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postSaveStatusCache.remove(it) }
    }

    // Cache management for user
    suspend fun clearUserInteractionCache(userId: String) = withContext(Dispatchers.IO) {
        val keysToRemove = mutableListOf<String>()

        // Clear user-specific status caches
        postLikeStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postLikeStatusCache.remove(it) }

        keysToRemove.clear()
        postShareStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postShareStatusCache.remove(it) }

        keysToRemove.clear()
        postSaveStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postSaveStatusCache.remove(it) }
    }

    suspend fun clearAllInteractionCache() = withContext(Dispatchers.IO) {
        postLikesCache.clear()
        postSharesCache.clear()
        postLikeStatusCache.clear()
        postShareStatusCache.clear()
        postSaveStatusCache.clear()
    }

    // Cache size information
    suspend fun getInteractionCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "postLikes" to postLikesCache.values.sumOf { it.size },
            "postShares" to postSharesCache.values.sumOf { it.size },
            "statusCaches" to (postLikeStatusCache.size + postShareStatusCache.size + postSaveStatusCache.size)
        )
    }
}