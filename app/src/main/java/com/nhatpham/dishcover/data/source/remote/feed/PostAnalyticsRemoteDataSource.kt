// PostAnalyticsRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.mapper.toDto
import com.nhatpham.dishcover.data.model.dto.feed.PostDto
import com.nhatpham.dishcover.domain.model.feed.PostActivity
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class PostAnalyticsRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postActivityCollection = firestore.collection("POST_ACTIVITY")
    private val postViewsCollection = firestore.collection("POST_VIEWS")
    private val postsCollection = firestore.collection("POSTS")

    suspend fun trackPostActivity(activity: PostActivity): Boolean {
        return try {
            val activityId = activity.activityId.takeIf { it.isNotBlank() }
                ?: postActivityCollection.document().id

            val activityDto = activity.copy(
                activityId = activityId,
                createdAt = Timestamp.now()
            ).toDto()

            postActivityCollection.document(activityId)
                .set(activityDto)
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error tracking post activity")
            false
        }
    }

    suspend fun getPostAnalytics(postId: String): Map<String, Any> {
        return try {
            val post = getPostById(postId)
            val viewCount = getPostViewCount(postId)
            val uniqueViewers = getUniqueViewerCount(postId)
            val engagementRate = calculateEngagementRate(postId)

            mapOf(
                "likeCount" to (post?.likeCount ?: 0),
                "commentCount" to (post?.commentCount ?: 0),
                "shareCount" to (post?.shareCount ?: 0),
                "viewCount" to viewCount,
                "uniqueViewers" to uniqueViewers,
                "engagementRate" to engagementRate
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting post analytics")
            emptyMap()
        }
    }

    suspend fun getUserPostAnalytics(userId: String, dateRange: String): Map<String, Any> {
        return try {
            val posts = getUserPosts(userId, 100) // Get recent posts

            val totalLikes = posts.sumOf { it.likeCount ?: 0 }
            val totalComments = posts.sumOf { it.commentCount ?: 0 }
            val totalShares = posts.sumOf { it.shareCount ?: 0 }
            val avgEngagement = if (posts.isNotEmpty()) {
                (totalLikes + totalComments + totalShares).toDouble() / posts.size
            } else 0.0

            mapOf(
                "totalPosts" to posts.size,
                "totalLikes" to totalLikes,
                "totalComments" to totalComments,
                "totalShares" to totalShares,
                "averageEngagement" to avgEngagement
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting user post analytics")
            emptyMap()
        }
    }

    // Cleanup operations for post deletion
    suspend fun deletePostActivity(postId: String) {
        try {
            val snapshot = postActivityCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post activity")
        }
    }

    suspend fun deletePostViews(postId: String) {
        try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post views")
        }
    }

    // Helper methods
    private suspend fun getPostById(postId: String): PostDto? {
        return try {
            val doc = postsCollection.document(postId).get().await()
            if (doc.exists()) {
                doc.toObject(PostDto::class.java)
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Error getting post by ID")
            null
        }
    }

    private suspend fun getUserPosts(userId: String, limit: Int): List<PostDto> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user posts for analytics")
            emptyList()
        }
    }

    private suspend fun getPostViewCount(postId: String): Int {
        return try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()

            snapshot.count.toInt()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getUniqueViewerCount(postId: String): Int {
        return try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("userId") }.distinct().size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun calculateEngagementRate(postId: String): Double {
        return try {
            val post = getPostById(postId)
            val viewCount = getPostViewCount(postId)

            if (post != null && viewCount > 0) {
                val totalEngagements = (post.likeCount ?: 0) + (post.commentCount ?: 0) + (post.shareCount ?: 0)
                (totalEngagements.toDouble() / viewCount) * 100
            } else 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}