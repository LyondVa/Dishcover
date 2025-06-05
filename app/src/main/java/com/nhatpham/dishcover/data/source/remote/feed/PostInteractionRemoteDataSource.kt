// PostInteractionRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.toDomain
import com.nhatpham.dishcover.data.mapper.toDto
import com.nhatpham.dishcover.data.model.dto.feed.PostLikeDto
import com.nhatpham.dishcover.data.model.dto.feed.PostShareDto
import com.nhatpham.dishcover.domain.model.feed.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class PostInteractionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postsCollection = firestore.collection("POSTS")
    private val postLikesCollection = firestore.collection("POST_LIKES")
    private val postSharesCollection = firestore.collection("POST_SHARES")
    private val savedPostsCollection = firestore.collection("SAVED_POSTS")

    // Like Operations
    suspend fun likePost(userId: String, postId: String, likeType: LikeType): Boolean {
        return try {
            val existingLike = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (existingLike.isEmpty) {
                val likeDto = PostLikeDto(
                    likeId = postLikesCollection.document().id,
                    postId = postId,
                    userId = userId,
                    likeType = likeType.name,
                    createdAt = Timestamp.now()
                )

                postLikesCollection.document(likeDto.likeId!!)
                    .set(likeDto)
                    .await()

                // Increment like count
                postsCollection.document(postId)
                    .update("likeCount", FieldValue.increment(1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking post")
            false
        }
    }

    suspend fun unlikePost(userId: String, postId: String): Boolean {
        return try {
            val existingLike = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (!existingLike.isEmpty) {
                existingLike.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement like count
                postsCollection.document(postId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking post")
            false
        }
    }

    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> {
        return try {
            val snapshot = postLikesCollection
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostLikeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post likes")
            emptyList()
        }
    }

    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is liked")
            false
        }
    }

    // Share Operations
    suspend fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): PostShare? {
        return try {
            val shareId = postSharesCollection.document().id
            val postShare = PostShare(
                shareId = shareId,
                originalPostId = postId,
                sharedByUserId = userId,
                shareMessage = shareMessage,
                shareType = shareType,
                createdAt = Timestamp.now()
            )

            postSharesCollection.document(shareId)
                .set(postShare.toDto())
                .await()

            // Increment share count
            postsCollection.document(postId)
                .update("shareCount", FieldValue.increment(1))
                .await()

            postShare
        } catch (e: Exception) {
            Timber.e(e, "Error sharing post")
            null
        }
    }

    suspend fun unsharePost(userId: String, postId: String): Boolean {
        return try {
            val existingShare = postSharesCollection
                .whereEqualTo("sharedByUserId", userId)
                .whereEqualTo("originalPostId", postId)
                .get()
                .await()

            if (!existingShare.isEmpty) {
                existingShare.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement share count
                postsCollection.document(postId)
                    .update("shareCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unsharing post")
            false
        }
    }

    suspend fun getPostShares(postId: String, limit: Int): List<PostShare> {
        return try {
            val snapshot = postSharesCollection
                .whereEqualTo("originalPostId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostShareDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post shares")
            emptyList()
        }
    }

    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postSharesCollection
                .whereEqualTo("sharedByUserId", userId)
                .whereEqualTo("originalPostId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is shared")
            false
        }
    }

    // Save Operations
    suspend fun savePost(userId: String, postId: String): Boolean {
        return try {
            val saveData = mapOf(
                "userId" to userId,
                "postId" to postId,
                "savedAt" to Timestamp.now()
            )

            savedPostsCollection.add(saveData).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving post")
            false
        }
    }

    suspend fun unsavePost(userId: String, postId: String): Boolean {
        return try {
            val snapshot = savedPostsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unsaving post")
            false
        }
    }

    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = savedPostsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is saved")
            false
        }
    }

    // Cleanup operations for post deletion
    suspend fun deletePostLikes(postId: String) {
        try {
            val snapshot = postLikesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post likes")
        }
    }

    suspend fun deletePostShares(postId: String) {
        try {
            val snapshot = postSharesCollection
                .whereEqualTo("originalPostId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post shares")
        }
    }

    suspend fun deleteSavedPosts(postId: String) {
        try {
            val snapshot = savedPostsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting saved posts")
        }
    }
}