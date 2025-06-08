// domain/repository/RealTimeRepositories.kt
package com.nhatpham.dishcover.domain.repository.realtime

import com.nhatpham.dishcover.domain.model.realtime.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface RealTimeEngagementRepository {
    fun observePostEngagement(postId: String): Flow<Resource<LiveEngagementData>>
    fun observeMultiplePostEngagements(postIds: List<String>): Flow<Resource<Map<String, LiveEngagementData>>>
    fun updatePostEngagement(postId: String, engagementData: LiveEngagementData): Flow<Resource<Unit>>
    fun addReaction(postId: String, userId: String, reactionType: ReactionType): Flow<Resource<Unit>>
    fun removeReaction(postId: String, userId: String): Flow<Resource<Unit>>
    fun incrementViewCount(postId: String, userId: String): Flow<Resource<Unit>>
}

interface RealTimeFeedRepository {
    fun observeFeedUpdates(userId: String): Flow<Resource<List<RealTimeFeedUpdate>>>
    fun observeFollowingFeedUpdates(userId: String): Flow<Resource<List<RealTimeFeedUpdate>>>
    fun publishFeedUpdate(update: RealTimeFeedUpdate): Flow<Resource<Unit>>
    fun markFeedUpdateAsRead(userId: String, updateId: String): Flow<Resource<Unit>>
    fun clearOldFeedUpdates(userId: String, olderThan: Long): Flow<Resource<Unit>>
}

interface RealTimeCommentRepository {
    fun observePostComments(postId: String): Flow<Resource<List<RealTimeComment>>>
    fun addComment(comment: RealTimeComment): Flow<Resource<RealTimeComment>>
    fun updateComment(commentId: String, content: String): Flow<Resource<Unit>>
    fun deleteComment(commentId: String): Flow<Resource<Unit>>
    fun likeComment(commentId: String, userId: String): Flow<Resource<Unit>>
    fun unlikeComment(commentId: String, userId: String): Flow<Resource<Unit>>
}

interface RealTimeUserActivityRepository {
    fun observeUserActivity(userId: String): Flow<Resource<List<LiveUserActivity>>>
    fun observePostViewers(postId: String): Flow<Resource<List<LiveUserActivity>>>
    fun updateUserActivity(activity: LiveUserActivity): Flow<Resource<Unit>>
    fun setUserOnlineStatus(userId: String, isOnline: Boolean): Flow<Resource<Unit>>
    fun markUserAsViewingPost(userId: String, postId: String): Flow<Resource<Unit>>
    fun markUserAsTypingComment(userId: String, postId: String): Flow<Resource<Unit>>
    fun clearUserActivity(userId: String, activityType: UserActivityType): Flow<Resource<Unit>>
}

interface RealTimeNotificationRepository {
    fun observeUserNotifications(userId: String): Flow<Resource<List<LiveNotification>>>
    fun sendNotification(notification: LiveNotification): Flow<Resource<Unit>>
    fun markNotificationAsRead(notificationId: String): Flow<Resource<Unit>>
    fun markAllNotificationsAsRead(userId: String): Flow<Resource<Unit>>
    fun deleteNotification(notificationId: String): Flow<Resource<Unit>>
    fun getUnreadNotificationCount(userId: String): Flow<Resource<Int>>
}

interface RealTimePostRepository {
    fun observePostUpdates(postId: String): Flow<Resource<List<RealTimePostUpdate>>>
    fun observeUserPostUpdates(userId: String): Flow<Resource<List<RealTimePostUpdate>>>
    fun publishPostUpdate(update: RealTimePostUpdate): Flow<Resource<Unit>>
    fun getPostUpdateHistory(postId: String, limit: Int): Flow<Resource<List<RealTimePostUpdate>>>
}

interface RealTimeSyncRepository {
    fun observeDataSyncStatus(): Flow<Resource<SyncStatus>>
    fun triggerManualSync(): Flow<Resource<Unit>>
    fun resolveConflict(conflictId: String, resolution: ConflictResolution): Flow<Resource<Unit>>
    fun observePendingConflicts(): Flow<Resource<List<DataConflict>>>
}

data class SyncStatus(
    val isOnline: Boolean,
    val lastSyncTime: Long,
    val pendingSyncOperations: Int,
    val failedSyncOperations: Int
)

data class DataConflict(
    val conflictId: String,
    val resourceType: String,
    val resourceId: String,
    val localVersion: Map<String, Any>,
    val remoteVersion: Map<String, Any>,
    val timestamp: Long
)

data class ConflictResolution(
    val useLocal: Boolean = false,
    val useRemote: Boolean = false,
    val mergedData: Map<String, Any>? = null
)