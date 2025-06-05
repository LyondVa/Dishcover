package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedAggregationRepository {
    // Feed Operations
    fun getUserFeed(userId: String, limit: Int = 20, lastPostId: String? = null): Flow<Resource<List<FeedItem>>>
    fun getFollowingFeed(userId: String, limit: Int = 20, lastPostId: String? = null): Flow<Resource<List<FeedItem>>>
    fun getTrendingPosts(limit: Int = 20, timeRange: String = "24h"): Flow<Resource<List<PostListItem>>>
    fun getPopularPosts(limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getDiscoverFeed(userId: String, limit: Int = 20): Flow<Resource<List<FeedItem>>>

    // Hashtag Operations
    fun getPostsByHashtag(hashtag: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getTrendingHashtags(limit: Int = 10): Flow<Resource<List<String>>>
    fun searchHashtags(query: String, limit: Int = 10): Flow<Resource<List<String>>>

    // Feed Algorithm and Personalization
    fun updateFeedPreferences(userId: String, preferences: Map<String, Any>): Flow<Resource<Boolean>>
    fun getFeedPreferences(userId: String): Flow<Resource<Map<String, Any>>>

    // Cache Management
    fun refreshUserFeed(userId: String): Flow<Resource<Boolean>>
    fun clearFeedCache(userId: String): Flow<Resource<Boolean>>
    fun markPostsAsViewed(userId: String, postIds: List<String>): Flow<Resource<Boolean>>
    fun getUnreadPostCount(userId: String): Flow<Resource<Int>>
}