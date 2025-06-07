// FeedAggregationRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.buildFeedItem
import com.nhatpham.dishcover.data.mapper.toDomain
import com.nhatpham.dishcover.data.mapper.toListItem
import com.nhatpham.dishcover.data.model.dto.feed.PostDto
import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FeedAggregationRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postsCollection = firestore.collection("POSTS")
    private val userFollowsCollection = firestore.collection("USER_FOLLOWS")
    private val hashtagCountsCollection = firestore.collection("HASHTAG_COUNTS")
    private val postLikesCollection = firestore.collection("POST_LIKES")
    private val postSharesCollection = firestore.collection("POST_SHARES")

    // Feed Operations
    suspend fun getUserFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> {
        return try {
            // Get user's following list
            val followingIds = getUserFollowingIds(userId)
            if (followingIds.isEmpty()) return emptyList()

            var query = postsCollection
                .whereIn("userId", followingIds.take(10)) // Firestore limit
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            // Handle pagination
            lastPostId?.let { lastId ->
                val lastDoc = postsCollection.document(lastId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc) as Query
                }
            }

            val snapshot = query.get().await()
            buildFeedItems(snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }, userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user feed")
            emptyList()
        }
    }

    suspend fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> {
        return try {
            Timber.d("Getting following feed for user: $userId, limit: $limit, lastPostId: $lastPostId")

            // Get user's following list first
            val followingIds = getUserFollowingIds(userId)

            if (followingIds.isEmpty()) {
                Timber.d("User $userId is not following anyone")
                return emptyList()
            }

            Timber.d("User $userId is following ${followingIds.size} users: $followingIds")

            // Due to Firestore's whereIn limitation (max 10 items), we need to handle large following lists
            // by chunking them into groups of 10
            val allFeedItems = mutableListOf<FeedItem>()

            for (followingChunk in followingIds.chunked(10)) {
                var query = postsCollection
                    .whereIn("userId", followingChunk)
                    .whereEqualTo("public", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                // Handle pagination
                lastPostId?.let { lastId ->
                    val lastDoc = postsCollection.document(lastId).get().await()
                    if (lastDoc.exists()) {
                        query = query.startAfter(lastDoc) as Query
                    }
                }

                val snapshot = query.get().await()
                val chunkFeedItems = buildFeedItems(
                    snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) },
                    userId
                )

                allFeedItems.addAll(chunkFeedItems)
            }

            // Sort all feed items by creation date (descending) and limit the results
            val sortedFeedItems = allFeedItems
                .sortedByDescending { it.createdAt }
                .take(limit)

            Timber.d("Retrieved ${sortedFeedItems.size} feed items for following feed")
            sortedFeedItems

        } catch (e: Exception) {
            Timber.e(e, "Error getting following feed for user: $userId")
            emptyList()
        }
    }

    suspend fun getTrendingPosts(limit: Int, timeRange: String): List<PostListItem> {
        return try {
            val cutoffTime = when (timeRange) {
                "24h" -> Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }.time
                "7d" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
                "30d" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
                else -> Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }.time
            }

            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .whereGreaterThan("createdAt", Timestamp(cutoffTime))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val postDto = doc.toObject(PostDto::class.java)
                postDto?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending posts")
            emptyList()
        }
    }

    suspend fun getPopularPosts(limit: Int): List<PostListItem> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val postDto = doc.toObject(PostDto::class.java)
                postDto?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular posts")
            emptyList()
        }
    }

    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> {
        return try {
            // Get posts from users the current user is not following
            val followingIds = getUserFollowingIds(userId)

            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                val postDto = doc.toObject(PostDto::class.java)
                // Filter out posts from users already followed
                if (postDto != null && !followingIds.contains(postDto.userId)) {
                    postDto
                } else null
            }

            buildFeedItems(posts, userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting discover feed")
            emptyList()
        }
    }

    // Hashtag Operations
    suspend fun getTrendingHashtags(limit: Int): List<String> {
        return try {
            val snapshot = hashtagCountsCollection
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("hashtag")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending hashtags")
            emptyList()
        }
    }

    suspend fun updateHashtagCounts(hashtags: List<String>, increment: Boolean) {
        hashtags.forEach { hashtag ->
            try {
                val hashtagRef = hashtagCountsCollection.document(hashtag)
                val field = if (increment) FieldValue.increment(1) else FieldValue.increment(-1)

                hashtagRef.update("count", field).await()
            } catch (e: Exception) {
                // Document might not exist, create it
                if (increment) {
                    try {
                        hashtagCountsCollection.document(hashtag)
                            .set(mapOf("hashtag" to hashtag, "count" to 1))
                            .await()
                    } catch (createException: Exception) {
                        Timber.e(createException, "Error creating hashtag count")
                    }
                }
            }
        }
    }

    suspend fun searchHashtags(query: String, limit: Int): List<String> {
        return try {
            val snapshot = hashtagCountsCollection
                .whereGreaterThanOrEqualTo("hashtag", query)
                .whereLessThan("hashtag", query + '\uf8ff')
                .orderBy("hashtag")
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("hashtag")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching hashtags")
            emptyList()
        }
    }

    suspend fun getPostsByHashtag(hashtag: String, limit: Int): List<PostListItem> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .whereArrayContains("hashtags", hashtag)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val postDto = doc.toObject(PostDto::class.java)
                postDto?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting posts by hashtag")
            emptyList()
        }
    }

    // User Following Operations
    suspend fun followUser(followerId: String, followingId: String): Boolean {
        return try {
            val followData = mapOf(
                "followerId" to followerId,
                "followingId" to followingId,
                "createdAt" to FieldValue.serverTimestamp()
            )

            userFollowsCollection.add(followData).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error following user")
            false
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Boolean {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unfollowing user")
            false
        }
    }

    suspend fun getUserFollowingIds(userId: String): List<String> {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("followingId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user following IDs")
            emptyList()
        }
    }

    suspend fun isUserFollowing(followerId: String, followingId: String): Boolean {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Helper methods
    private suspend fun buildFeedItems(posts: List<PostDto>, currentUserId: String): List<FeedItem> {
        return posts.mapNotNull { postDto ->
            try {
                val post = postDto.toDomain()
                val isLiked = isPostLikedByUser(currentUserId, post.postId)
                val isShared = isPostSharedByUser(currentUserId, post.postId)
                val isFollowing = isUserFollowing(currentUserId, post.userId)

                buildFeedItem(
                    post = post,
                    author = null, // No longer needed since username is in post
                    isLikedByCurrentUser = isLiked,
                    isSharedByCurrentUser = isShared,
                    isFollowingAuthor = isFollowing
                )
            } catch (e: Exception) {
                Timber.e(e, "Error building feed item")
                null
            }
        }
    }

    private suspend fun isPostLikedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun isPostSharedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postSharesCollection
                .whereEqualTo("sharedByUserId", userId)
                .whereEqualTo("originalPostId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}