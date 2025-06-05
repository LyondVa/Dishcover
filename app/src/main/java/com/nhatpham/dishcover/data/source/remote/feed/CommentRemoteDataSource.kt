// CommentRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.toDomain
import com.nhatpham.dishcover.data.mapper.toDto
import com.nhatpham.dishcover.data.model.dto.feed.CommentDto
import com.nhatpham.dishcover.data.model.dto.feed.CommentLikeDto
import com.nhatpham.dishcover.domain.model.feed.Comment
import com.nhatpham.dishcover.domain.model.feed.CommentLike
import com.nhatpham.dishcover.domain.model.feed.LikeType
import com.nhatpham.dishcover.domain.model.user.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class CommentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val commentsCollection = firestore.collection("COMMENTS")
    private val commentLikesCollection = firestore.collection("COMMENT_LIKES")
    private val postsCollection = firestore.collection("POSTS")
    private val usersCollection = firestore.collection("USERS")

    // Comment CRUD Operations
    suspend fun addComment(comment: Comment): Comment? {
        return try {
            val commentId = comment.commentId.takeIf { it.isNotBlank() }
                ?: commentsCollection.document().id

            // Get user's username if not provided
            val username = if (comment.username.isBlank()) {
                getUserById(comment.userId)?.username ?: "Unknown User"
            } else {
                comment.username
            }

            val commentDto = comment.copy(
                commentId = commentId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toDto()

            commentsCollection.document(commentId)
                .set(commentDto)
                .await()

            // Increment comment count on post
            postsCollection.document(comment.postId)
                .update("commentCount", FieldValue.increment(1))
                .await()

            // If it's a reply, increment reply count on parent comment
            comment.parentCommentId?.let { parentId ->
                commentsCollection.document(parentId)
                    .update("replyCount", FieldValue.increment(1))
                    .await()
            }

            comment.copy(
                commentId = commentId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment")
            null
        }
    }

    suspend fun updateComment(comment: Comment): Comment? {
        return try {
            val updatedComment = comment.copy(
                updatedAt = Timestamp.now(),
                isEdited = true
            )

            commentsCollection.document(comment.commentId)
                .set(updatedComment.toDto())
                .await()

            updatedComment
        } catch (e: Exception) {
            Timber.e(e, "Error updating comment")
            null
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            val comment = getComment(commentId)
            if (comment != null) {
                // Soft delete - mark as deleted
                commentsCollection.document(commentId)
                    .update("deleted", true)
                    .await()

                // Decrement comment count on post
                postsCollection.document(comment.postId)
                    .update("commentCount", FieldValue.increment(-1))
                    .await()

                // If it's a reply, decrement reply count on parent comment
                comment.parentCommentId?.let { parentId ->
                    commentsCollection.document(parentId)
                        .update("replyCount", FieldValue.increment(-1))
                        .await()
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting comment")
            false
        }
    }

    suspend fun getComment(commentId: String): Comment? {
        return try {
            val doc = commentsCollection.document(commentId).get().await()
            if (doc.exists()) {
                doc.toObject(CommentDto::class.java)?.toDomain()
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment")
            null
        }
    }

    suspend fun getPostComments(postId: String, limit: Int, lastCommentId: String?): List<Comment> {
        return try {
            var query = commentsCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("parentCommentId", null) // Top-level comments only
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            lastCommentId?.let { lastId ->
                val lastDoc = commentsCollection.document(lastId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc) as Query
                }
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post comments")
            emptyList()
        }
    }

    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("parentCommentId", commentId)
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment replies")
            emptyList()
        }
    }

    // Comment Interaction Operations
    suspend fun likeComment(userId: String, commentId: String, likeType: LikeType): Boolean {
        return try {
            val existingLike = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            if (existingLike.isEmpty) {
                val likeDto = CommentLikeDto(
                    likeId = commentLikesCollection.document().id,
                    commentId = commentId,
                    userId = userId,
                    likeType = likeType.name,
                    createdAt = Timestamp.now()
                )

                commentLikesCollection.document(likeDto.likeId!!)
                    .set(likeDto)
                    .await()

                // Increment like count
                commentsCollection.document(commentId)
                    .update("likeCount", FieldValue.increment(1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking comment")
            false
        }
    }

    suspend fun unlikeComment(userId: String, commentId: String): Boolean {
        return try {
            val existingLike = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            if (!existingLike.isEmpty) {
                existingLike.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement like count
                commentsCollection.document(commentId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking comment")
            false
        }
    }

    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> {
        return try {
            val snapshot = commentLikesCollection
                .whereEqualTo("commentId", commentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentLikeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment likes")
            emptyList()
        }
    }

    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean {
        return try {
            val snapshot = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if comment is liked")
            false
        }
    }

    // Cleanup operations for post deletion
    suspend fun deletePostComments(postId: String) {
        try {
            val snapshot = commentsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post comments")
        }
    }

    suspend fun deleteCommentLikes(commentId: String) {
        try {
            val snapshot = commentLikesCollection
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting comment likes")
        }
    }

    // Helper method
    private suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java)
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID")
            null
        }
    }
}