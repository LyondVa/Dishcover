// PostAnalyticsRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.PostActivity
import com.nhatpham.dishcover.domain.repository.feed.PostAnalyticsRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class PostAnalyticsRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : PostAnalyticsRepository {

    override fun trackPostActivity(activity: PostActivity): Flow<Resource<Boolean>> = flow {
        try {
            val success = feedRemoteDataSource.trackPostActivity(activity)
            emit(Resource.Success(success))
        } catch (e: Exception) {
            Timber.e(e, "Error tracking post activity")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostAnalytics(postId: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedAnalytics = feedLocalDataSource.getPostAnalytics(postId)
            if (cachedAnalytics != null) {
                emit(Resource.Success(cachedAnalytics))
            }

            // Fetch from remote
            val remoteAnalytics = feedRemoteDataSource.getPostAnalytics(postId)
            if (remoteAnalytics.isNotEmpty()) {
                feedLocalDataSource.savePostAnalytics(postId, remoteAnalytics)
                emit(Resource.Success(remoteAnalytics))
            } else if (cachedAnalytics == null) {
                emit(Resource.Success(emptyMap()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post analytics")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserPostAnalytics(userId: String, dateRange: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedAnalytics = feedLocalDataSource.getUserPostAnalytics(userId)
            if (cachedAnalytics != null) {
                emit(Resource.Success(cachedAnalytics))
            }

            // Fetch from remote
            val remoteAnalytics = feedRemoteDataSource.getUserPostAnalytics(userId, dateRange)
            if (remoteAnalytics.isNotEmpty()) {
                feedLocalDataSource.saveUserPostAnalytics(userId, remoteAnalytics)
                emit(Resource.Success(remoteAnalytics))
            } else if (cachedAnalytics == null) {
                emit(Resource.Success(emptyMap()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user post analytics")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}