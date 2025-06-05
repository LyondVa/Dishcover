// PostLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostLocalDataSource @Inject constructor() {

    // Cache maps for posts
    private val postsCache = mutableMapOf<String, Post>()
    private val userPostsCache = mutableMapOf<String, List<PostListItem>>()
    private val searchResultsCache = mutableMapOf<String, List<PostListItem>>()

    // Post operations
    suspend fun savePost(post: Post) = withContext(Dispatchers.IO) {
        postsCache[post.postId] = post
    }

    suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        return@withContext postsCache[postId]
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        postsCache.remove(postId)

        // Clean up from other caches
        userPostsCache.forEach { (userId, posts) ->
            val updatedList = posts.filter { it.postId != postId }
            if (updatedList.size != posts.size) {
                userPostsCache[userId] = updatedList
            }
        }

        // Clean up search results
        searchResultsCache.forEach { (query, posts) ->
            val updatedList = posts.filter { it.postId != postId }
            if (updatedList.size != posts.size) {
                searchResultsCache[query] = updatedList
            }
        }
    }

    // User posts operations
    suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext userPostsCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveUserPosts(userId: String, posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        userPostsCache[userId] = posts
    }

    suspend fun addPostToUserLists(userId: String, post: PostListItem) = withContext(Dispatchers.IO) {
        val userPosts = userPostsCache[userId]?.toMutableList() ?: mutableListOf()
        userPosts.add(0, post) // Add at the beginning
        userPostsCache[userId] = userPosts
    }

    suspend fun updatePostInUserLists(userId: String, updatedPost: PostListItem) = withContext(Dispatchers.IO) {
        val userPosts = userPostsCache[userId]?.toMutableList()
        userPosts?.let { posts ->
            val index = posts.indexOfFirst { it.postId == updatedPost.postId }
            if (index >= 0) {
                posts[index] = updatedPost
                userPostsCache[userId] = posts
            }
        }
    }

    suspend fun removePostFromUserLists(userId: String, postId: String) = withContext(Dispatchers.IO) {
        userPostsCache[userId] = userPostsCache[userId]?.filter { it.postId != postId } ?: emptyList()
    }

    // Search operations
    suspend fun getSearchResults(query: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext searchResultsCache[query.lowercase()]?.take(limit) ?: emptyList()
    }

    suspend fun saveSearchResults(query: String, posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        searchResultsCache[query.lowercase()] = posts
    }

    // Multiple posts operation
    suspend fun getMultiplePosts(postIds: List<String>): List<Post> = withContext(Dispatchers.IO) {
        return@withContext postIds.mapNotNull { postId -> postsCache[postId] }
    }

    // Cache management
    suspend fun clearUserPostsCache(userId: String) = withContext(Dispatchers.IO) {
        userPostsCache.remove(userId)
    }

    suspend fun clearSearchCache() = withContext(Dispatchers.IO) {
        searchResultsCache.clear()
    }

    suspend fun clearAllPostsCache() = withContext(Dispatchers.IO) {
        postsCache.clear()
        userPostsCache.clear()
        searchResultsCache.clear()
    }

    // Validation methods
    suspend fun isPostCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postsCache.containsKey(postId)
    }

    suspend fun areUserPostsCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userPostsCache.containsKey(userId) &&
                userPostsCache[userId]?.isNotEmpty() == true
    }

    // Cache size information
    suspend fun getPostsCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "posts" to postsCache.size,
            "userPosts" to userPostsCache.values.sumOf { it.size },
            "searchResults" to searchResultsCache.values.sumOf { it.size }
        )
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }
}