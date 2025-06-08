// data/source/local/RealTimeLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.realtime

import com.nhatpham.dishcover.domain.model.realtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealTimeEngagementLocalDataSource @Inject constructor() {

    // In-memory cache for real-time engagement data
    private val engagementCache = mutableMapOf<String, LiveEngagementData>()
    private val engagementFlows = mutableMapOf<String, MutableStateFlow<LiveEngagementData?>>()

    // Recent reactions cache (limited size for performance)
    private val recentReactionsCache = mutableMapOf<String, List<RecentReaction>>()

    suspend fun cacheEngagementData(postId: String, data: LiveEngagementData) = withContext(Dispatchers.IO) {
        engagementCache[postId] = data

        // Update flow if exists
        engagementFlows[postId]?.value = data
    }

    suspend fun getCachedEngagementData(postId: String): LiveEngagementData? = withContext(Dispatchers.IO) {
        engagementCache[postId]
    }

    fun observeCachedEngagementData(postId: String): Flow<LiveEngagementData?> {
        return engagementFlows.getOrPut(postId) {
            MutableStateFlow(engagementCache[postId])
        }.asStateFlow()
    }

    suspend fun updateEngagementCount(
        postId: String,
        likesDelta: Int = 0,
        commentsDelta: Int = 0,
        sharesDelta: Int = 0,
        viewsDelta: Int = 0
    ) = withContext(Dispatchers.IO) {
        val current = engagementCache[postId] ?: LiveEngagementData(postId = postId)
        val updated = current.copy(
            likeCount = maxOf(0, current.likeCount + likesDelta),
            commentCount = maxOf(0, current.commentCount + commentsDelta),
            shareCount = maxOf(0, current.shareCount + sharesDelta),
            viewCount = maxOf(0, current.viewCount + viewsDelta),
            lastUpdated = com.google.firebase.Timestamp.now()
        )

        engagementCache[postId] = updated
        engagementFlows[postId]?.value = updated
    }

    suspend fun addRecentReaction(postId: String, reaction: RecentReaction) = withContext(Dispatchers.IO) {
        val currentReactions = recentReactionsCache[postId]?.toMutableList() ?: mutableListOf()

        // Remove existing reaction from same user
        currentReactions.removeAll { it.userId == reaction.userId }

        // Add new reaction at beginning
        currentReactions.add(0, reaction)

        // Keep only last 20 reactions for performance
        if (currentReactions.size > 20) {
            currentReactions.removeAt(currentReactions.size - 1)
        }

        recentReactionsCache[postId] = currentReactions

        // Update engagement data with recent reactions
        val current = engagementCache[postId] ?: LiveEngagementData(postId = postId)
        val updated = current.copy(
            recentReactions = currentReactions,
            lastUpdated = com.google.firebase.Timestamp.now()
        )

        engagementCache[postId] = updated
        engagementFlows[postId]?.value = updated
    }

    suspend fun clearOldData(olderThanMillis: Long) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - olderThanMillis

        engagementCache.entries.removeAll { (_, data) ->
            data.lastUpdated.toDate().time < cutoffTime
        }

        recentReactionsCache.clear()
    }
}

@Singleton
class RealTimeFeedLocalDataSource @Inject constructor() {

    private val feedUpdatesCache = mutableMapOf<String, MutableList<RealTimeFeedUpdate>>()
    private val feedFlows = mutableMapOf<String, MutableStateFlow<List<RealTimeFeedUpdate>>>()
    private val maxCacheSize = 100 // Limit cache size per user

    suspend fun cacheFeedUpdates(userId: String, updates: List<RealTimeFeedUpdate>) = withContext(Dispatchers.IO) {
        val cached = feedUpdatesCache.getOrPut(userId) { mutableListOf() }

        // Merge and sort by timestamp
        cached.clear()
        cached.addAll(updates)
        cached.sortByDescending { it.timestamp.toDate().time }

        // Limit cache size
        if (cached.size > maxCacheSize) {
            cached.subList(maxCacheSize, cached.size).clear()
        }

        feedFlows[userId]?.value = cached.toList()
    }

    suspend fun addFeedUpdate(userId: String, update: RealTimeFeedUpdate) = withContext(Dispatchers.IO) {
        val cached = feedUpdatesCache.getOrPut(userId) { mutableListOf() }

        // Remove duplicate if exists
        cached.removeAll { it.feedUpdateId == update.feedUpdateId }

        // Add at beginning (newest first)
        cached.add(0, update)

        // Limit cache size
        if (cached.size > maxCacheSize) {
            cached.removeAt(cached.size - 1)
        }

        feedFlows[userId]?.value = cached.toList()
    }

    fun observeCachedFeedUpdates(userId: String): Flow<List<RealTimeFeedUpdate>> {
        return feedFlows.getOrPut(userId) {
            MutableStateFlow(feedUpdatesCache[userId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    suspend fun getCachedFeedUpdates(userId: String): List<RealTimeFeedUpdate> = withContext(Dispatchers.IO) {
        feedUpdatesCache[userId]?.toList() ?: emptyList()
    }

    suspend fun markUpdateAsProcessed(userId: String, updateId: String) = withContext(Dispatchers.IO) {
        feedUpdatesCache[userId]?.removeAll { it.feedUpdateId == updateId }
        feedFlows[userId]?.value = feedUpdatesCache[userId]?.toList() ?: emptyList()
    }
}

@Singleton
class RealTimeCommentLocalDataSource @Inject constructor() {

    private val commentsCache = mutableMapOf<String, MutableList<RealTimeComment>>()
    private val commentFlows = mutableMapOf<String, MutableStateFlow<List<RealTimeComment>>>()

    suspend fun cacheComments(postId: String, comments: List<RealTimeComment>) = withContext(Dispatchers.IO) {
        val cached = commentsCache.getOrPut(postId) { mutableListOf() }
        cached.clear()
        cached.addAll(comments.sortedBy { it.timestamp.toDate().time })

        commentFlows[postId]?.value = cached.toList()
    }

    suspend fun addComment(comment: RealTimeComment) = withContext(Dispatchers.IO) {
        val cached = commentsCache.getOrPut(comment.postId) { mutableListOf() }

        // Remove if already exists (for updates)
        cached.removeAll { it.commentId == comment.commentId }

        // Insert in chronological order
        val insertIndex = cached.indexOfFirst {
            it.timestamp.toDate().time > comment.timestamp.toDate().time
        }

        if (insertIndex == -1) {
            cached.add(comment)
        } else {
            cached.add(insertIndex, comment)
        }

        commentFlows[comment.postId]?.value = cached.toList()
    }

    suspend fun updateComment(commentId: String, postId: String, newContent: String) = withContext(Dispatchers.IO) {
        val cached = commentsCache[postId]
        cached?.find { it.commentId == commentId }?.let { comment ->
            val index = cached.indexOf(comment)
            cached[index] = comment.copy(content = newContent)
            commentFlows[postId]?.value = cached.toList()
        }
    }

    suspend fun deleteComment(commentId: String, postId: String) = withContext(Dispatchers.IO) {
        val cached = commentsCache[postId]
        cached?.removeAll { it.commentId == commentId }
        commentFlows[postId]?.value = cached?.toList() ?: emptyList()
    }

    fun observeCachedComments(postId: String): Flow<List<RealTimeComment>> {
        return commentFlows.getOrPut(postId) {
            MutableStateFlow(commentsCache[postId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    suspend fun getCachedComments(postId: String): List<RealTimeComment> = withContext(Dispatchers.IO) {
        commentsCache[postId]?.toList() ?: emptyList()
    }
}

@Singleton
class RealTimeUserActivityLocalDataSource @Inject constructor() {

    private val userActivityCache = mutableMapOf<String, MutableList<LiveUserActivity>>()
    private val postViewersCache = mutableMapOf<String, MutableList<LiveUserActivity>>()
    private val activityFlows = mutableMapOf<String, MutableStateFlow<List<LiveUserActivity>>>()

    suspend fun cacheUserActivity(userId: String, activities: List<LiveUserActivity>) = withContext(Dispatchers.IO) {
        val cached = userActivityCache.getOrPut(userId) { mutableListOf() }
        cached.clear()
        cached.addAll(activities.sortedByDescending { it.timestamp.toDate().time })

        activityFlows["user_$userId"]?.value = cached.toList()
    }

    suspend fun cachePostViewers(postId: String, viewers: List<LiveUserActivity>) = withContext(Dispatchers.IO) {
        val cached = postViewersCache.getOrPut(postId) { mutableListOf() }
        cached.clear()
        cached.addAll(viewers.filter {
            // Only keep recent viewers (last 5 minutes)
            System.currentTimeMillis() - it.timestamp.toDate().time < 300_000
        })

        activityFlows["post_$postId"]?.value = cached.toList()
    }

    suspend fun addUserActivity(activity: LiveUserActivity) = withContext(Dispatchers.IO) {
        // Update user activity cache
        val userCached = userActivityCache.getOrPut(activity.userId) { mutableListOf() }
        userCached.removeAll {
            it.activityType == activity.activityType && it.targetId == activity.targetId
        }
        userCached.add(0, activity)

        // Limit cache size
        if (userCached.size > 50) {
            userCached.removeAt(userCached.size - 1)
        }

        activityFlows["user_${activity.userId}"]?.value = userCached.toList()

        // If viewing post, update post viewers cache
        if (activity.activityType == UserActivityType.VIEWING_POST) {
            val postCached = postViewersCache.getOrPut(activity.targetId) { mutableListOf() }
            postCached.removeAll { it.userId == activity.userId }
            postCached.add(activity)

            activityFlows["post_${activity.targetId}"]?.value = postCached.toList()
        }
    }

    fun observeCachedUserActivity(userId: String): Flow<List<LiveUserActivity>> {
        return activityFlows.getOrPut("user_$userId") {
            MutableStateFlow(userActivityCache[userId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    fun observeCachedPostViewers(postId: String): Flow<List<LiveUserActivity>> {
        return activityFlows.getOrPut("post_$postId") {
            MutableStateFlow(postViewersCache[postId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    suspend fun cleanupOldActivity() = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - 300_000 // 5 minutes

        userActivityCache.values.forEach { activities ->
            activities.removeAll { it.timestamp.toDate().time < cutoffTime }
        }

        postViewersCache.values.forEach { viewers ->
            viewers.removeAll { it.timestamp.toDate().time < cutoffTime }
        }
    }
}

@Singleton
class RealTimeNotificationLocalDataSource @Inject constructor() {

    private val notificationsCache = mutableMapOf<String, MutableList<LiveNotification>>()
    private val notificationFlows = mutableMapOf<String, MutableStateFlow<List<LiveNotification>>>()
    private val maxCacheSize = 100 // Limit cache size per user

    suspend fun cacheNotifications(userId: String, notifications: List<LiveNotification>) = withContext(Dispatchers.IO) {
        val cached = notificationsCache.getOrPut(userId) { mutableListOf() }
        cached.clear()
        cached.addAll(notifications.sortedByDescending { it.timestamp.toDate().time })

        // Limit cache size
        if (cached.size > maxCacheSize) {
            cached.subList(maxCacheSize, cached.size).clear()
        }

        notificationFlows[userId]?.value = cached.toList()
    }

    suspend fun addNotification(notification: LiveNotification) = withContext(Dispatchers.IO) {
        val cached = notificationsCache.getOrPut(notification.userId) { mutableListOf() }

        // Remove duplicate if exists
        cached.removeAll { it.notificationId == notification.notificationId }

        // Add at beginning (newest first)
        cached.add(0, notification)

        // Limit cache size
        if (cached.size > maxCacheSize) {
            cached.removeAt(cached.size - 1)
        }

        notificationFlows[notification.userId]?.value = cached.toList()
    }

    fun observeCachedNotifications(userId: String): Flow<List<LiveNotification>> {
        return notificationFlows.getOrPut(userId) {
            MutableStateFlow(notificationsCache[userId]?.toList() ?: emptyList())
        }.asStateFlow()
    }

    suspend fun getCachedNotifications(userId: String): List<LiveNotification> = withContext(Dispatchers.IO) {
        notificationsCache[userId]?.toList() ?: emptyList()
    }

    suspend fun markNotificationAsRead(notificationId: String, userId: String) = withContext(Dispatchers.IO) {
        val cached = notificationsCache[userId]
        cached?.find { it.notificationId == notificationId }?.let { notification ->
            val index = cached.indexOf(notification)
            cached[index] = notification.copy(isRead = true)
            notificationFlows[userId]?.value = cached.toList()
        }
    }

    suspend fun markAllNotificationsAsRead(userId: String) = withContext(Dispatchers.IO) {
        val cached = notificationsCache[userId]
        cached?.forEachIndexed { index, notification ->
            cached[index] = notification.copy(isRead = true)
        }
        notificationFlows[userId]?.value = cached?.toList() ?: emptyList()
    }

    suspend fun deleteNotification(notificationId: String, userId: String) = withContext(Dispatchers.IO) {
        val cached = notificationsCache[userId]
        cached?.removeAll { it.notificationId == notificationId }
        notificationFlows[userId]?.value = cached?.toList() ?: emptyList()
    }

    suspend fun getUnreadNotificationCount(userId: String): Int = withContext(Dispatchers.IO) {
        notificationsCache[userId]?.count { !it.isRead } ?: 0
    }

    suspend fun clearOldNotifications(userId: String, olderThanMillis: Long) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - olderThanMillis
        val cached = notificationsCache[userId]

        cached?.removeAll { notification ->
            notification.timestamp.toDate().time < cutoffTime
        }

        notificationFlows[userId]?.value = cached?.toList() ?: emptyList()
    }
}