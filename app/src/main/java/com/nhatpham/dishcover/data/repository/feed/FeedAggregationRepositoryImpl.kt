// FeedAggregationRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.feed.FeedAggregationRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class FeedAggregationRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : FeedAggregationRepository {

    override fun getUserFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first (only if no pagination)
            if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getUserFeed(userId, limit)
                if (cachedFeed.isNotEmpty()) {
                    emit(Resource.Success(cachedFeed))
                }
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getUserFeed(userId, limit, lastPostId)
            if (remoteFeed.isNotEmpty()) {
                if (lastPostId == null) {
                    // Fresh feed, replace cache
                    feedLocalDataSource.saveUserFeed(userId, remoteFeed)
                }
                emit(Resource.Success(remoteFeed))
            } else if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getUserFeed(userId, limit)
                emit(Resource.Success(cachedFeed))
            } else {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first (only if no pagination)
            if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getFollowingFeed(userId, limit)
                if (cachedFeed.isNotEmpty()) {
                    emit(Resource.Success(cachedFeed))
                }
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getFollowingFeed(userId, limit, lastPostId)
            if (remoteFeed.isNotEmpty()) {
                if (lastPostId == null) {
                    feedLocalDataSource.saveFollowingFeed(userId, remoteFeed)
                }
                emit(Resource.Success(remoteFeed))
            } else if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getFollowingFeed(userId, limit)
                emit(Resource.Success(cachedFeed))
            } else {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting following feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getTrendingPosts(limit: Int, timeRange: String): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPosts = feedLocalDataSource.getTrendingPosts(limit)
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }

            // Fetch from remote
            val remotePosts = feedRemoteDataSource.getTrendingPosts(limit, timeRange)
            if (remotePosts.isNotEmpty()) {
                feedLocalDataSource.saveTrendingPosts(remotePosts)
                emit(Resource.Success(remotePosts))
            } else if (cachedPosts.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPopularPosts(limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPosts = feedLocalDataSource.getPopularPosts(limit)
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }

            // Fetch from remote
            val remotePosts = feedRemoteDataSource.getPopularPosts(limit)
            if (remotePosts.isNotEmpty()) {
                feedLocalDataSource.savePopularPosts(remotePosts)
                emit(Resource.Success(remotePosts))
            } else if (cachedPosts.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getDiscoverFeed(userId: String, limit: Int): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedFeed = feedLocalDataSource.getDiscoverFeed(userId, limit)
            if (cachedFeed.isNotEmpty()) {
                emit(Resource.Success(cachedFeed))
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getDiscoverFeed(userId, limit)
            if (remoteFeed.isNotEmpty()) {
                feedLocalDataSource.saveDiscoverFeed(userId, remoteFeed)
                emit(Resource.Success(remoteFeed))
            } else if (cachedFeed.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting discover feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostsByHashtag(hashtag: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // For hashtag search, go directly to remote (no cache for now)
            val posts = feedRemoteDataSource.searchPosts("#$hashtag", null, limit)
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            Timber.e(e, "Error getting posts by hashtag")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getTrendingHashtags(limit: Int): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedHashtags = feedLocalDataSource.getTrendingHashtags(limit)
            if (cachedHashtags.isNotEmpty()) {
                emit(Resource.Success(cachedHashtags))
            }

            // Fetch from remote
            val remoteHashtags = feedRemoteDataSource.getTrendingHashtags(limit)
            if (remoteHashtags.isNotEmpty()) {
                feedLocalDataSource.saveTrendingHashtags(remoteHashtags)
                emit(Resource.Success(remoteHashtags))
            } else if (cachedHashtags.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending hashtags")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun searchHashtags(query: String, limit: Int): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            // For hashtag search, this would typically be a remote operation
            // For now, return empty list (can be implemented later)
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error searching hashtags")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateFeedPreferences(userId: String, preferences: Map<String, Any>): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error updating feed preferences")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFeedPreferences(userId: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(emptyMap())) // Return empty map for now
        } catch (e: Exception) {
            Timber.e(e, "Error getting feed preferences")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun refreshUserFeed(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Clear cache and fetch fresh data
            feedLocalDataSource.clearUserCache(userId)

            // Fetch fresh feed
            val freshFeed = feedRemoteDataSource.getUserFeed(userId, 20, null)
            if (freshFeed.isNotEmpty()) {
                feedLocalDataSource.saveUserFeed(userId, freshFeed)
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing user feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun clearFeedCache(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            feedLocalDataSource.clearUserCache(userId)
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error clearing feed cache")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun markPostsAsViewed(userId: String, postIds: List<String>): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error marking posts as viewed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUnreadPostCount(userId: String): Flow<Resource<Int>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(0)) // Return 0 for now
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread post count")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}