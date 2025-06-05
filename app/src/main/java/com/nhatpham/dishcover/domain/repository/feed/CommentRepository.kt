package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    // Comment CRUD Operations
    fun addComment(comment: Comment): Flow<Resource<Comment>>
    fun updateComment(comment: Comment): Flow<Resource<Comment>>
    fun deleteComment(commentId: String): Flow<Resource<Boolean>>
    fun getComment(commentId: String): Flow<Resource<Comment>>

    // Comment Queries
    fun getPostComments(postId: String, limit: Int = 50, lastCommentId: String? = null): Flow<Resource<List<Comment>>>
    fun getCommentReplies(commentId: String, limit: Int = 20): Flow<Resource<List<Comment>>>
    fun getMultipleComments(commentIds: List<String>): Flow<Resource<List<Comment>>>

    // Comment Interaction Operations
    fun likeComment(userId: String, commentId: String, likeType: LikeType = LikeType.LIKE): Flow<Resource<Boolean>>
    fun unlikeComment(userId: String, commentId: String): Flow<Resource<Boolean>>
    fun getCommentLikes(commentId: String, limit: Int = 20): Flow<Resource<List<CommentLike>>>
    fun isCommentLikedByUser(userId: String, commentId: String): Flow<Resource<Boolean>>

    // Moderation Operations
    fun reportComment(userId: String, commentId: String, reason: String, description: String? = null): Flow<Resource<Boolean>>
}