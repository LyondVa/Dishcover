// CommentLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import com.nhatpham.dishcover.domain.model.feed.Comment
import com.nhatpham.dishcover.domain.model.feed.CommentLike
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentLocalDataSource @Inject constructor() {

    // Cache maps for comments
    private val commentsCache = mutableMapOf<String, Comment>()
    private val postCommentsCache = mutableMapOf<String, List<Comment>>()
    private val commentRepliesCache = mutableMapOf<String, List<Comment>>()
    private val commentLikesCache = mutableMapOf<String, List<CommentLike>>()

    // Status cache for comment likes
    private val commentLikeStatusCache = mutableMapOf<String, Boolean>() // "userId:commentId" -> Boolean

    // Comment operations
    suspend fun saveComment(comment: Comment) = withContext(Dispatchers.IO) {
        commentsCache[comment.commentId] = comment

        // Update comments list for the post
        val postComments = postCommentsCache[comment.postId]?.toMutableList() ?: mutableListOf()
        val existingIndex = postComments.indexOfFirst { it.commentId == comment.commentId }
        if (existingIndex >= 0) {
            postComments[existingIndex] = comment
        } else {
            postComments.add(0, comment) // Add at the beginning
        }
        postCommentsCache[comment.postId] = postComments

        // If it's a reply, update replies list
        comment.parentCommentId?.let { parentId ->
            val replies = commentRepliesCache[parentId]?.toMutableList() ?: mutableListOf()
            val replyIndex = replies.indexOfFirst { it.commentId == comment.commentId }
            if (replyIndex >= 0) {
                replies[replyIndex] = comment
            } else {
                replies.add(comment)
            }
            commentRepliesCache[parentId] = replies
        }
    }

    suspend fun getCommentById(commentId: String): Comment? = withContext(Dispatchers.IO) {
        return@withContext commentsCache[commentId]
    }

    suspend fun deleteComment(commentId: String) = withContext(Dispatchers.IO) {
        val comment = commentsCache[commentId]
        if (comment != null) {
            // Mark as deleted instead of removing
            val deletedComment = comment.copy(isDeleted = true)
            commentsCache[commentId] = deletedComment

            // Update in post comments
            val postComments = postCommentsCache[comment.postId]?.toMutableList()
            postComments?.let { comments ->
                val index = comments.indexOfFirst { it.commentId == commentId }
                if (index >= 0) {
                    comments[index] = deletedComment
                    postCommentsCache[comment.postId] = comments
                }
            }

            // Update in replies if it's a reply
            comment.parentCommentId?.let { parentId ->
                val replies = commentRepliesCache[parentId]?.toMutableList()
                replies?.let { replyList ->
                    val index = replyList.indexOfFirst { it.commentId == commentId }
                    if (index >= 0) {
                        replyList[index] = deletedComment
                        commentRepliesCache[parentId] = replyList
                    }
                }
            }
        }

        commentLikesCache.remove(commentId)
        commentRepliesCache.remove(commentId)
    }

    // Post comments operations
    suspend fun getPostComments(postId: String, limit: Int): List<Comment> = withContext(Dispatchers.IO) {
        return@withContext postCommentsCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostComments(postId: String, comments: List<Comment>) = withContext(Dispatchers.IO) {
        postCommentsCache[postId] = comments
        comments.forEach { comment ->
            commentsCache[comment.commentId] = comment
        }
    }

    // Comment replies operations
    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> = withContext(Dispatchers.IO) {
        return@withContext commentRepliesCache[commentId]?.take(limit) ?: emptyList()
    }

    suspend fun saveCommentReplies(commentId: String, replies: List<Comment>) = withContext(Dispatchers.IO) {
        commentRepliesCache[commentId] = replies
        replies.forEach { reply ->
            commentsCache[reply.commentId] = reply
        }
    }

    // Comment likes operations
    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> = withContext(Dispatchers.IO) {
        return@withContext commentLikesCache[commentId]?.take(limit) ?: emptyList()
    }

    suspend fun saveCommentLikes(commentId: String, likes: List<CommentLike>) = withContext(Dispatchers.IO) {
        commentLikesCache[commentId] = likes
    }

    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext commentLikeStatusCache["$userId:$commentId"] ?: false
    }

    suspend fun updateCommentLikeStatus(userId: String, commentId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        commentLikeStatusCache["$userId:$commentId"] = isLiked

        // Update comment like count in cached comment
        commentsCache[commentId]?.let { comment ->
            val updatedCount = if (isLiked) comment.likeCount + 1 else maxOf(0, comment.likeCount - 1)
            val updatedComment = comment.copy(likeCount = updatedCount)
            commentsCache[commentId] = updatedComment

            // Update in post comments cache
            val postComments = postCommentsCache[comment.postId]?.toMutableList()
            postComments?.let { comments ->
                val index = comments.indexOfFirst { it.commentId == commentId }
                if (index >= 0) {
                    comments[index] = updatedComment
                    postCommentsCache[comment.postId] = comments
                }
            }

            // Update in replies cache if it's a reply
            comment.parentCommentId?.let { parentId ->
                val replies = commentRepliesCache[parentId]?.toMutableList()
                replies?.let { replyList ->
                    val index = replyList.indexOfFirst { it.commentId == commentId }
                    if (index >= 0) {
                        replyList[index] = updatedComment
                        commentRepliesCache[parentId] = replyList
                    }
                }
            }
        }
    }

    // Multiple comments operation
    suspend fun getMultipleComments(commentIds: List<String>): List<Comment> = withContext(Dispatchers.IO) {
        return@withContext commentIds.mapNotNull { commentId -> commentsCache[commentId] }
    }

    // Cache management for post deletion
    suspend fun deletePostComments(postId: String) = withContext(Dispatchers.IO) {
        // Remove post comments and their individual cache entries
        val postComments = postCommentsCache[postId] ?: emptyList()
        postComments.forEach { comment ->
            commentsCache.remove(comment.commentId)
            commentLikesCache.remove(comment.commentId)
            commentRepliesCache.remove(comment.commentId)

            // Remove like status entries for this comment
            val keysToRemove = commentLikeStatusCache.keys.filter { it.endsWith(":${comment.commentId}") }
            keysToRemove.forEach { commentLikeStatusCache.remove(it) }
        }

        postCommentsCache.remove(postId)
    }

    // Cache management for user
    suspend fun clearUserCommentCache(userId: String) = withContext(Dispatchers.IO) {
        // Clear user-specific like status cache
        val keysToRemove = mutableListOf<String>()
        commentLikeStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { commentLikeStatusCache.remove(it) }
    }

    suspend fun clearAllCommentCache() = withContext(Dispatchers.IO) {
        commentsCache.clear()
        postCommentsCache.clear()
        commentRepliesCache.clear()
        commentLikesCache.clear()
        commentLikeStatusCache.clear()
    }

    // Validation methods
    suspend fun isCommentCached(commentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext commentsCache.containsKey(commentId)
    }

    suspend fun arePostCommentsCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postCommentsCache.containsKey(postId) &&
                postCommentsCache[postId]?.isNotEmpty() == true
    }

    // Cache size information
    suspend fun getCommentCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "comments" to commentsCache.size,
            "postComments" to postCommentsCache.values.sumOf { it.size },
            "commentReplies" to commentRepliesCache.values.sumOf { it.size },
            "commentLikes" to commentLikesCache.values.sumOf { it.size },
            "commentLikeStatus" to commentLikeStatusCache.size
        )
    }
}