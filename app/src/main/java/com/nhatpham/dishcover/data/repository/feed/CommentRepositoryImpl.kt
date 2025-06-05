// CommentRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.feed.CommentRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : CommentRepository {

    override fun addComment(comment: Comment): Flow<Resource<Comment>> = flow {
        try {
            emit(Resource.Loading())

            val addedComment = feedRemoteDataSource.addComment(comment)
            if (addedComment != null) {
                // Cache the comment
                feedLocalDataSource.saveComment(addedComment)
                emit(Resource.Success(addedComment))
            } else {
                emit(Resource.Error("Failed to add comment"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateComment(comment: Comment): Flow<Resource<Comment>> = flow {
        try {
            emit(Resource.Loading())

            val updatedComment = feedRemoteDataSource.updateComment(comment)
            if (updatedComment != null) {
                feedLocalDataSource.saveComment(updatedComment)
                emit(Resource.Success(updatedComment))
            } else {
                emit(Resource.Error("Failed to update comment"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deleteComment(commentId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.deleteComment(commentId)
            if (success) {
                feedLocalDataSource.deleteComment(commentId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete comment"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostComments(postId: String, limit: Int, lastCommentId: String?): Flow<Resource<List<Comment>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first (only if no pagination)
            if (lastCommentId == null) {
                val cachedComments = feedLocalDataSource.getPostComments(postId, limit)
                if (cachedComments.isNotEmpty()) {
                    emit(Resource.Success(cachedComments))
                }
            }

            // Fetch from remote
            val remoteComments = feedRemoteDataSource.getPostComments(postId, limit, lastCommentId)
            if (remoteComments.isNotEmpty()) {
                if (lastCommentId == null) {
                    feedLocalDataSource.savePostComments(postId, remoteComments)
                }
                emit(Resource.Success(remoteComments))
            } else if (lastCommentId == null) {
                val cachedComments = feedLocalDataSource.getPostComments(postId, limit)
                emit(Resource.Success(cachedComments))
            } else {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post comments")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCommentReplies(commentId: String, limit: Int): Flow<Resource<List<Comment>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedReplies = feedLocalDataSource.getCommentReplies(commentId, limit)
            if (cachedReplies.isNotEmpty()) {
                emit(Resource.Success(cachedReplies))
            }

            // Fetch from remote
            val remoteReplies = feedRemoteDataSource.getCommentReplies(commentId, limit)
            if (remoteReplies.isNotEmpty()) {
                feedLocalDataSource.saveCommentReplies(commentId, remoteReplies)
                emit(Resource.Success(remoteReplies))
            } else if (cachedReplies.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment replies")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getComment(commentId: String): Flow<Resource<Comment>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedComment = feedLocalDataSource.getCommentById(commentId)
            if (cachedComment != null) {
                emit(Resource.Success(cachedComment))
            }

            // Fetch from remote
            val remoteComment = feedRemoteDataSource.getComment(commentId)
            if (remoteComment != null) {
                feedLocalDataSource.saveComment(remoteComment)
                emit(Resource.Success(remoteComment))
            } else if (cachedComment == null) {
                emit(Resource.Error("Comment not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun likeComment(userId: String, commentId: String, likeType: LikeType): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updateCommentLikeStatus(userId, commentId, true)

            val success = feedRemoteDataSource.likeComment(userId, commentId, likeType)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updateCommentLikeStatus(userId, commentId, false)
                emit(Resource.Error("Failed to like comment"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updateCommentLikeStatus(userId, commentId, false)
            Timber.e(e, "Error liking comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unlikeComment(userId: String, commentId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updateCommentLikeStatus(userId, commentId, false)

            val success = feedRemoteDataSource.unlikeComment(userId, commentId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updateCommentLikeStatus(userId, commentId, true)
                emit(Resource.Error("Failed to unlike comment"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updateCommentLikeStatus(userId, commentId, true)
            Timber.e(e, "Error unliking comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCommentLikes(commentId: String, limit: Int): Flow<Resource<List<CommentLike>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedLikes = feedLocalDataSource.getCommentLikes(commentId, limit)
            if (cachedLikes.isNotEmpty()) {
                emit(Resource.Success(cachedLikes))
            }

            // Fetch from remote
            val remoteLikes = feedRemoteDataSource.getCommentLikes(commentId, limit)
            if (remoteLikes.isNotEmpty()) {
                feedLocalDataSource.saveCommentLikes(commentId, remoteLikes)
                emit(Resource.Success(remoteLikes))
            } else if (cachedLikes.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment likes")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isCommentLikedByUser(userId: String, commentId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = feedLocalDataSource.isCommentLikedByUser(userId, commentId)
            emit(Resource.Success(cachedStatus))

            // Verify with remote (background sync)
            val remoteStatus = feedRemoteDataSource.isCommentLikedByUser(userId, commentId)
            if (remoteStatus != cachedStatus) {
                feedLocalDataSource.updateCommentLikeStatus(userId, commentId, remoteStatus)
                emit(Resource.Success(remoteStatus))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if comment is liked")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getMultipleComments(commentIds: List<String>): Flow<Resource<List<Comment>>> = flow {
        try {
            emit(Resource.Loading())

            val comments = mutableListOf<Comment>()

            // Try to get from cache first
            commentIds.forEach { commentId ->
                val cachedComment = feedLocalDataSource.getCommentById(commentId)
                cachedComment?.let { comments.add(it) }
            }

            emit(Resource.Success(comments))
        } catch (e: Exception) {
            Timber.e(e, "Error getting multiple comments")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun reportComment(userId: String, commentId: String, reason: String, description: String?): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error reporting comment")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}