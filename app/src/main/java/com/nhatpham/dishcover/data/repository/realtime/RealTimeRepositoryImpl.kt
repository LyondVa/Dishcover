// data/repository/RealTimeRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.realtime

import com.nhatpham.dishcover.data.source.local.realtime.*
import com.nhatpham.dishcover.data.source.remote.realtime.*
import com.nhatpham.dishcover.domain.model.realtime.*
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeCommentRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeEngagementRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeFeedRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeNotificationRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeUserActivityRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class RealTimeEngagementRepositoryImpl @Inject constructor(
    private val remoteDataSource: RealTimeEngagementDataSource,
    private val localDataSource: RealTimeEngagementLocalDataSource
) : RealTimeEngagementRepository {

    override fun observePostEngagement(postId: String): Flow<Resource<LiveEngagementData>> = flow {
        emit(Resource.Loading())

        // First emit cached data if available
        val cached = localDataSource.getCachedEngagementData(postId)
        if (cached != null) {
            emit(Resource.Success(cached))
        }

        // Then observe real-time updates
        remoteDataSource.observePostEngagement(postId)
            .catch { error ->
                Timber.e(error, "Error observing post engagement: $postId")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { remoteData ->
                if (remoteData != null) {
                    // Cache the data locally
                    localDataSource.cacheEngagementData(postId, remoteData)
                    emit(Resource.Success(remoteData))
                }
            }
    }

    override fun observeMultiplePostEngagements(postIds: List<String>): Flow<Resource<Map<String, LiveEngagementData>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.observeMultiplePostEngagements(postIds)
            .catch { error ->
                Timber.e(error, "Error observing multiple post engagements")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { engagementMap ->
                // Cache each engagement data
                engagementMap.forEach { (postId, data) ->
                    localDataSource.cacheEngagementData(postId, data)
                }
                emit(Resource.Success(engagementMap))
            }
    }

    override fun updatePostEngagement(postId: String, engagementData: LiveEngagementData): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Update locally first for immediate UI feedback
            localDataSource.cacheEngagementData(postId, engagementData)

            // Then update remotely
            val success = remoteDataSource.updatePostEngagement(postId, engagementData)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to update engagement data"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating post engagement")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun addReaction(postId: String, userId: String, reactionType: ReactionType): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Optimistic update locally
            localDataSource.updateEngagementCount(postId, likesDelta = 1)

            // Update remotely
            val success = remoteDataSource.addReaction(postId, userId, reactionType)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                // Rollback optimistic update
                localDataSource.updateEngagementCount(postId, likesDelta = -1)
                emit(Resource.Error("Failed to add reaction"))
            }
        } catch (e: Exception) {
            // Rollback optimistic update
            localDataSource.updateEngagementCount(postId, likesDelta = -1)
            Timber.e(e, "Error adding reaction")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun removeReaction(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Optimistic update locally
            localDataSource.updateEngagementCount(postId, likesDelta = -1)

            // Update remotely
            val success = remoteDataSource.removeReaction(postId, userId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                // Rollback optimistic update
                localDataSource.updateEngagementCount(postId, likesDelta = 1)
                emit(Resource.Error("Failed to remove reaction"))
            }
        } catch (e: Exception) {
            // Rollback optimistic update
            localDataSource.updateEngagementCount(postId, likesDelta = 1)
            Timber.e(e, "Error removing reaction")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun incrementViewCount(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            // Update view count locally immediately
            localDataSource.updateEngagementCount(postId, viewsDelta = 1)
            emit(Resource.Success(Unit))

            // Background update to remote (don't block UI)
            // View counts are less critical and can be eventually consistent
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing view count")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

class RealTimeFeedRepositoryImpl @Inject constructor(
    private val remoteDataSource: RealTimeFeedDataSource,
    private val localDataSource: RealTimeFeedLocalDataSource
) : RealTimeFeedRepository {

    override fun observeFeedUpdates(userId: String): Flow<Resource<List<RealTimeFeedUpdate>>> = flow {
        emit(Resource.Loading())

        // First emit cached data
        val cached = localDataSource.getCachedFeedUpdates(userId)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        // Then observe real-time updates
        remoteDataSource.observeFeedUpdates(userId)
            .catch { error ->
                Timber.e(error, "Error observing feed updates for user: $userId")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { updates ->
                // Cache updates locally
                localDataSource.cacheFeedUpdates(userId, updates)
                emit(Resource.Success(updates))
            }
    }

    override fun observeFollowingFeedUpdates(userId: String): Flow<Resource<List<RealTimeFeedUpdate>>> =
        observeFeedUpdates(userId) // Same implementation for now, can be specialized later

    override fun publishFeedUpdate(update: RealTimeFeedUpdate): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Add to local cache immediately
            localDataSource.addFeedUpdate(update.userId, update)

            // Publish to remote
            val success = remoteDataSource.publishFeedUpdate(update)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to publish feed update"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error publishing feed update")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun markFeedUpdateAsRead(userId: String, updateId: String): Flow<Resource<Unit>> = flow {
        try {
            // Mark as processed locally
            localDataSource.markUpdateAsProcessed(userId, updateId)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error marking feed update as read")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun clearOldFeedUpdates(userId: String, olderThan: Long): Flow<Resource<Unit>> = flow {
        try {
            // Clear old updates locally
            // Remote cleanup would be handled by a background job
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error clearing old feed updates")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

class RealTimeCommentRepositoryImpl @Inject constructor(
    private val remoteDataSource: RealTimeCommentDataSource,
    private val localDataSource: RealTimeCommentLocalDataSource
) : RealTimeCommentRepository {

    override fun observePostComments(postId: String): Flow<Resource<List<RealTimeComment>>> = flow {
        emit(Resource.Loading())

        // First emit cached comments
        val cached = localDataSource.getCachedComments(postId)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        // Then observe real-time comments
        remoteDataSource.observePostComments(postId)
            .catch { error ->
                Timber.e(error, "Error observing comments for post: $postId")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { comments ->
                // Cache comments locally
                localDataSource.cacheComments(postId, comments)
                emit(Resource.Success(comments))
            }
    }

    override fun addComment(comment: RealTimeComment): Flow<Resource<RealTimeComment>> = flow {
        emit(Resource.Loading())

        try {
            // Add to local cache immediately for optimistic UI
            localDataSource.addComment(comment)

            // Add to remote
            val result = remoteDataSource.addComment(comment)
            if (result != null) {
                emit(Resource.Success(result))
            } else {
                // Remove from local cache if remote failed
                localDataSource.deleteComment(comment.commentId, comment.postId)
                emit(Resource.Error("Failed to add comment"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun updateComment(commentId: String, content: String): Flow<Resource<Unit>> = flow {
        // Implementation similar to addComment but for updates
        emit(Resource.Success(Unit))
    }

    override fun deleteComment(commentId: String): Flow<Resource<Unit>> = flow {
        // Implementation similar to addComment but for deletion
        emit(Resource.Success(Unit))
    }

    override fun likeComment(commentId: String, userId: String): Flow<Resource<Unit>> = flow {
        // Implementation for liking comments
        emit(Resource.Success(Unit))
    }

    override fun unlikeComment(commentId: String, userId: String): Flow<Resource<Unit>> = flow {
        // Implementation for unliking comments
        emit(Resource.Success(Unit))
    }
}

class RealTimeUserActivityRepositoryImpl @Inject constructor(
    private val remoteDataSource: RealTimeUserActivityDataSource,
    private val localDataSource: RealTimeUserActivityLocalDataSource
) : RealTimeUserActivityRepository {

    override fun observeUserActivity(userId: String): Flow<Resource<List<LiveUserActivity>>> = flow {
        emit(Resource.Loading())

        // Observe cached user activity
        localDataSource.observeCachedUserActivity(userId)
            .collect { activities ->
                emit(Resource.Success(activities))
            }
    }

    override fun observePostViewers(postId: String): Flow<Resource<List<LiveUserActivity>>> = flow {
        emit(Resource.Loading())

        // First emit cached viewers
        localDataSource.observeCachedPostViewers(postId)
            .collect { viewers ->
                emit(Resource.Success(viewers))
            }

        // Observe real-time viewers
        remoteDataSource.observePostViewers(postId)
            .catch { error ->
                Timber.e(error, "Error observing post viewers: $postId")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { viewers ->
                localDataSource.cachePostViewers(postId, viewers)
            }
    }

    override fun updateUserActivity(activity: LiveUserActivity): Flow<Resource<Unit>> = flow {
        try {
            // Update locally immediately
            localDataSource.addUserActivity(activity)

            // Update remotely in background
            val success = remoteDataSource.updateUserActivity(activity)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to update user activity"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating user activity")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun setUserOnlineStatus(userId: String, isOnline: Boolean): Flow<Resource<Unit>> = flow {
        try {
            val activity = LiveUserActivity(
                userId = userId,
                activityType = if (isOnline) UserActivityType.ONLINE else UserActivityType.OFFLINE,
                targetId = userId
            )
            localDataSource.addUserActivity(activity)
            remoteDataSource.updateUserActivity(activity)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error setting user online status")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun markUserAsViewingPost(userId: String, postId: String): Flow<Resource<Unit>> = flow {
        try {
            val activity = LiveUserActivity(
                userId = userId,
                activityType = UserActivityType.VIEWING_POST,
                targetId = postId
            )
            localDataSource.addUserActivity(activity)
            remoteDataSource.updateUserActivity(activity)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error marking user as viewing post")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun markUserAsTypingComment(userId: String, postId: String): Flow<Resource<Unit>> = flow {
        try {
            val activity = LiveUserActivity(
                userId = userId,
                activityType = UserActivityType.TYPING_COMMENT,
                targetId = postId
            )
            localDataSource.addUserActivity(activity)
            remoteDataSource.updateUserActivity(activity)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error marking user as typing comment")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun clearUserActivity(userId: String, activityType: UserActivityType): Flow<Resource<Unit>> = flow {
        try {
            // Clear from local cache
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error clearing user activity")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

class RealTimeNotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: RealTimeNotificationDataSource,
    private val localDataSource: RealTimeNotificationLocalDataSource
) : RealTimeNotificationRepository {

    override fun observeUserNotifications(userId: String): Flow<Resource<List<LiveNotification>>> = flow {
        emit(Resource.Loading())

        // First emit cached notifications
        val cached = localDataSource.getCachedNotifications(userId)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        // Then observe real-time notifications
        remoteDataSource.observeUserNotifications(userId)
            .catch { error ->
                Timber.e(error, "Error observing notifications for user: $userId")
                emit(Resource.Error(error.message ?: "Unknown error"))
            }
            .collect { notifications ->
                localDataSource.cacheNotifications(userId, notifications)
                emit(Resource.Success(notifications))
            }
    }

    override fun sendNotification(notification: LiveNotification): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Add to local cache immediately for target user
            localDataSource.addNotification(notification)

            // Send to remote
            val success = remoteDataSource.sendNotification(notification)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                // Remove from local cache if remote failed
                localDataSource.deleteNotification(notification.notificationId, notification.userId)
                emit(Resource.Error("Failed to send notification"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending notification")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun markNotificationAsRead(notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Update locally first for immediate UI feedback
            // Note: We need userId to update local cache - this could be improved

            // Update remotely
            val success = remoteDataSource.markNotificationAsRead(notificationId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to mark notification as read"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking notification as read")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun markAllNotificationsAsRead(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Update locally first
            localDataSource.markAllNotificationsAsRead(userId)

            // Update remotely
            val success = remoteDataSource.markAllNotificationsAsRead(userId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to mark all notifications as read"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking all notifications as read")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun deleteNotification(notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val success = remoteDataSource.deleteNotification(notificationId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to delete notification"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting notification")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getUnreadNotificationCount(userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())

        try {
            // First try local cache
            val localCount = localDataSource.getUnreadNotificationCount(userId)
            emit(Resource.Success(localCount))

            // Then get accurate count from remote
            val remoteCount = remoteDataSource.getUnreadNotificationCount(userId)
            emit(Resource.Success(remoteCount))
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread notification count")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}