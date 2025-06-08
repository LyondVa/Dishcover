// data/source/remote/RealTimeRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.realtime

import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.nhatpham.dishcover.domain.model.realtime.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealTimeEngagementDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val engagementCollection = firestore.collection("post_engagement")
    private val reactionsCollection = firestore.collection("post_reactions")

    fun observePostEngagement(postId: String): Flow<LiveEngagementData?> = callbackFlow {
        val listener = engagementCollection.document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing post engagement: $postId")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val data = snapshot.toObject(LiveEngagementData::class.java)
                        trySend(data)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing engagement data for post: $postId")
                        trySend(null)
                    }
                } else {
                    // Create default engagement data for new posts
                    trySend(LiveEngagementData(postId = postId))
                }
            }

        awaitClose { listener.remove() }
    }

    fun observeMultiplePostEngagements(postIds: List<String>): Flow<Map<String, LiveEngagementData>> = callbackFlow {
        if (postIds.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val listeners = mutableListOf<ListenerRegistration>()
        val engagementMap = mutableMapOf<String, LiveEngagementData>()

        postIds.forEach { postId ->
            val listener = engagementCollection.document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error observing engagement for post: $postId")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.toObject(LiveEngagementData::class.java)
                        if (data != null) {
                            engagementMap[postId] = data
                            trySend(engagementMap.toMap())
                        }
                    } else {
                        engagementMap[postId] = LiveEngagementData(postId = postId)
                        trySend(engagementMap.toMap())
                    }
                }
            listeners.add(listener)
        }

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    suspend fun updatePostEngagement(postId: String, engagementData: LiveEngagementData): Boolean {
        return try {
            engagementCollection.document(postId)
                .set(engagementData, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating post engagement: $postId")
            false
        }
    }

    suspend fun addReaction(postId: String, userId: String, reactionType: ReactionType): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                // Add reaction to reactions subcollection
                val reactionRef = reactionsCollection
                    .document(postId)
                    .collection("reactions")
                    .document(userId)

                val reactionData = mapOf(
                    "userId" to userId,
                    "reactionType" to reactionType.name,
                    "timestamp" to Timestamp.now()
                )

                transaction.set(reactionRef, reactionData)

                // Update engagement count
                val engagementRef = engagementCollection.document(postId)
                transaction.update(engagementRef, "likeCount", FieldValue.increment(1))
                transaction.update(engagementRef, "lastUpdated", Timestamp.now())
            }.await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error adding reaction to post: $postId")
            false
        }
    }

    suspend fun removeReaction(postId: String, userId: String): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                // Remove reaction
                val reactionRef = reactionsCollection
                    .document(postId)
                    .collection("reactions")
                    .document(userId)

                transaction.delete(reactionRef)

                // Update engagement count
                val engagementRef = engagementCollection.document(postId)
                transaction.update(engagementRef, "likeCount", FieldValue.increment(-1))
                transaction.update(engagementRef, "lastUpdated", Timestamp.now())
            }.await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing reaction from post: $postId")
            false
        }
    }
}

@Singleton
class RealTimeFeedDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val feedUpdatesCollection = firestore.collection("feed_updates")

    fun observeFeedUpdates(userId: String): Flow<List<RealTimeFeedUpdate>> = callbackFlow {
        val listener = feedUpdatesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing feed updates for user: $userId")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val updates = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RealTimeFeedUpdate::class.java)
                        }
                        trySend(updates)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing feed updates for user: $userId")
                        trySend(emptyList())
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun publishFeedUpdate(update: RealTimeFeedUpdate): Boolean {
        return try {
            feedUpdatesCollection.document(update.feedUpdateId)
                .set(update)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error publishing feed update: ${update.feedUpdateId}")
            false
        }
    }
}

@Singleton
class RealTimeCommentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val commentsCollection = firestore.collection("comments")

    fun observePostComments(postId: String): Flow<List<RealTimeComment>> = callbackFlow {
        val listener = commentsCollection
            .whereEqualTo("postId", postId)
            .whereEqualTo("status", CommentStatus.ACTIVE.name)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing comments for post: $postId")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val comments = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(RealTimeComment::class.java)
                        }
                        trySend(comments)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing comments for post: $postId")
                        trySend(emptyList())
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: RealTimeComment): RealTimeComment? {
        return try {
            commentsCollection.document(comment.commentId)
                .set(comment)
                .await()

            // Update post comment count
            firestore.collection("post_engagement")
                .document(comment.postId)
                .update(
                    "commentCount", FieldValue.increment(1),
                    "lastUpdated", Timestamp.now()
                )
                .await()

            comment
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment: ${comment.commentId}")
            null
        }
    }
}

@Singleton
class RealTimeUserActivityDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val userActivityCollection = firestore.collection("user_activity")
    private val postViewersCollection = firestore.collection("post_viewers")

    fun observePostViewers(postId: String): Flow<List<LiveUserActivity>> = callbackFlow {
        val listener = postViewersCollection
            .whereEqualTo("targetId", postId)
            .whereEqualTo("activityType", UserActivityType.VIEWING_POST.name)
            .whereGreaterThan("timestamp", Timestamp(System.currentTimeMillis() / 1000 - 300, 0)) // Last 5 minutes
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing post viewers: $postId")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val viewers = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(LiveUserActivity::class.java)
                        }
                        trySend(viewers)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing post viewers for: $postId")
                        trySend(emptyList())
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserActivity(activity: LiveUserActivity): Boolean {
        return try {
            val activityId = "${activity.userId}_${activity.activityType}_${activity.targetId}"
            userActivityCollection.document(activityId)
                .set(activity, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating user activity: $activity")
            false
        }
    }
}

@Singleton
class RealTimeNotificationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    fun observeUserNotifications(userId: String): Flow<List<LiveNotification>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing notifications for user: $userId")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val notifications = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(LiveNotification::class.java)
                        }
                        trySend(notifications)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing notifications for user: $userId")
                        trySend(emptyList())
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendNotification(notification: LiveNotification): Boolean {
        return try {
            notificationsCollection.document(notification.notificationId)
                .set(notification)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error sending notification: ${notification.notificationId}")
            false
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error marking notification as read: $notificationId")
            false
        }
    }

    suspend fun markAllNotificationsAsRead(userId: String): Boolean {
        return try {
            val batch = firestore.batch()
            val unreadNotifications = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            unreadNotifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error marking all notifications as read for user: $userId")
            false
        }
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting notification: $notificationId")
            false
        }
    }

    suspend fun getUnreadNotificationCount(userId: String): Int {
        return try {
            val query = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            query.size()
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread notification count for user: $userId")
            0
        }
    }
}