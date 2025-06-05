// FeedAggregationLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedAggregationLocalDataSource @Inject constructor() {

    // Cache maps for feed aggregation
    private val userFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val followingFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val discoverFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val trendingPostsCache = mutableListOf<PostListItem>()
    private val popularPostsCache = mutableListOf<PostListItem>()
    private val trendingHashtagsCache = mutableListOf<String>()
    private val userFollowingCache = mutableMapOf<String, List<String>>() // userId -> list of following IDs

    // Feed operations
    suspend fun getUserFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext userFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveUserFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        userFeedCache[userId] = feedItems
    }

    suspend fun getFollowingFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext followingFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveFollowingFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        followingFeedCache[userId] = feedItems
    }

    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext discoverFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveDiscoverFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        discoverFeedCache[userId] = feedItems
    }

    // Trending and popular posts
    suspend fun getTrendingPosts(limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext trendingPostsCache.take(limit)
    }

    suspend fun saveTrendingPosts(posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        trendingPostsCache.clear()
        trendingPostsCache.addAll(posts)
    }

    suspend fun getPopularPosts(limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext popularPostsCache.take(limit)
    }

    suspend fun savePopularPosts(posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        popularPostsCache.clear()
        popularPostsCache.addAll(posts)
    }

    // Hashtag operations
    suspend fun getTrendingHashtags(limit: Int): List<String> = withContext(Dispatchers.IO) {
        return@withContext trendingHashtagsCache.take(limit)
    }

    suspend fun saveTrendingHashtags(hashtags: List<String>) = withContext(Dispatchers.IO) {
        trendingHashtagsCache.clear()
        trendingHashtagsCache.addAll(hashtags)
    }

    // User following operations
    suspend fun getUserFollowing(userId: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext userFollowingCache[userId] ?: emptyList()
    }

    suspend fun saveUserFollowing(userId: String, followingIds: List<String>) = withContext(Dispatchers.IO) {
        userFollowingCache[userId] = followingIds
    }

    // Feed management with post updates
    suspend fun updatePostInFeeds(postId: String, updateFunction: (FeedItem) -> FeedItem) = withContext(Dispatchers.IO) {
        userFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            userFeedCache[userId] = updatedItems
        }

        followingFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            followingFeedCache[userId] = updatedItems
        }

        discoverFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            discoverFeedCache[userId] = updatedItems
        }
    }

    suspend fun removePostFromFeeds(postId: String) = withContext(Dispatchers.IO) {
        userFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.filter { it.post?.postId != postId }
            if (updatedItems.size != feedItems.size) {
                userFeedCache[userId] = updatedItems
            }
        }

        followingFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.filter { it.post?.postId != postId }
            if (updatedItems.size != feedItems.size) {
                followingFeedCache[userId] = updatedItems
            }
        }

        discoverFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.filter { it.post?.postId != postId }
            if (updatedItems.size != feedItems.size) {
                discoverFeedCache[userId] = updatedItems
            }
        }

        // Remove from trending and popular
        trendingPostsCache.removeAll { it.postId == postId }
        popularPostsCache.removeAll { it.postId == postId }
    }

    suspend fun updatePostInTrendingAndPopular(updatedPost: PostListItem) = withContext(Dispatchers.IO) {
        val trendingIndex = trendingPostsCache.indexOfFirst { it.postId == updatedPost.postId }
        if (trendingIndex >= 0) {
            trendingPostsCache[trendingIndex] = updatedPost
        }

        val popularIndex = popularPostsCache.indexOfFirst { it.postId == updatedPost.postId }
        if (popularIndex >= 0) {
            popularPostsCache[popularIndex] = updatedPost
        }
    }

    // Cache management for user
    suspend fun clearUserFeedCache(userId: String) = withContext(Dispatchers.IO) {
        userFeedCache.remove(userId)
        followingFeedCache.remove(userId)
        discoverFeedCache.remove(userId)
        userFollowingCache.remove(userId)
    }

    suspend fun clearGlobalFeedCache() = withContext(Dispatchers.IO) {
        trendingPostsCache.clear()
        popularPostsCache.clear()
        trendingHashtagsCache.clear()
    }

    suspend fun clearAllFeedCache() = withContext(Dispatchers.IO) {
        userFeedCache.clear()
        followingFeedCache.clear()
        discoverFeedCache.clear()
        trendingPostsCache.clear()
        popularPostsCache.clear()
        trendingHashtagsCache.clear()
        userFollowingCache.clear()
    }

    // Validation methods
    suspend fun isUserFeedCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userFeedCache.containsKey(userId) &&
                userFeedCache[userId]?.isNotEmpty() == true
    }

    suspend fun isFollowingFeedCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext followingFeedCache.containsKey(userId) &&
                followingFeedCache[userId]?.isNotEmpty() == true
    }

    suspend fun areTrendingPostsCached(): Boolean = withContext(Dispatchers.IO) {
        return@withContext trendingPostsCache.isNotEmpty()
    }

    suspend fun arePopularPostsCached(): Boolean = withContext(Dispatchers.IO) {
        return@withContext popularPostsCache.isNotEmpty()
    }

    // Cache size information
    suspend fun getFeedCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "userFeeds" to userFeedCache.values.sumOf { it.size },
            "followingFeeds" to followingFeedCache.values.sumOf { it.size },
            "discoverFeeds" to discoverFeedCache.values.sumOf { it.size },
            "trendingPosts" to trendingPostsCache.size,
            "popularPosts" to popularPostsCache.size,
            "trendingHashtags" to trendingHashtagsCache.size,
            "userFollowing" to userFollowingCache.values.sumOf { it.size }
        )
    }

    // Advanced operations for feed management
    suspend fun refreshUserFeeds(userId: String) = withContext(Dispatchers.IO) {
        userFeedCache.remove(userId)
        followingFeedCache.remove(userId)
        discoverFeedCache.remove(userId)
    }

    suspend fun addFeedItemToUserFeeds(userId: String, feedItem: FeedItem, addToTop: Boolean = true) = withContext(Dispatchers.IO) {
        // Add to user feed
        val userFeed = userFeedCache[userId]?.toMutableList() ?: mutableListOf()
        if (addToTop) {
            userFeed.add(0, feedItem)
        } else {
            userFeed.add(feedItem)
        }
        userFeedCache[userId] = userFeed

        // Add to following feed if applicable
        val followingFeed = followingFeedCache[userId]?.toMutableList() ?: mutableListOf()
        if (addToTop) {
            followingFeed.add(0, feedItem)
        } else {
            followingFeed.add(feedItem)
        }
        followingFeedCache[userId] = followingFeed
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }
}