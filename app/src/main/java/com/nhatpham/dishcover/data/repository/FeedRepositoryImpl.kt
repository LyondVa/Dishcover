// FeedRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : FeedRepository {

    // Post CRUD Operations
    override fun createPost(post: Post): Flow<Resource<Post>> = flow {
        try {
            emit(Resource.Loading())

            val createdPost = feedRemoteDataSource.createPost(post)
            if (createdPost != null) {
                // Cache the created post
                feedLocalDataSource.savePost(createdPost)

                // Add to user's post list
                val postListItem = createdPost.toListItem()
                feedLocalDataSource.addPostToUserLists(createdPost.userId, postListItem)

                emit(Resource.Success(createdPost))
            } else {
                emit(Resource.Error("Failed to create post"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updatePost(post: Post): Flow<Resource<Post>> = flow {
        try {
            emit(Resource.Loading())

            val updatedPost = feedRemoteDataSource.updatePost(post)
            if (updatedPost != null) {
                // Update cache
                feedLocalDataSource.savePost(updatedPost)

                // Update in user lists
                val postListItem = updatedPost.toListItem()
                feedLocalDataSource.updatePostInUserLists(updatedPost.userId, postListItem)

                emit(Resource.Success(updatedPost))
            } else {
                emit(Resource.Error("Failed to update post"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deletePost(postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.deletePost(postId)
            if (success) {
                // Remove from cache
                feedLocalDataSource.deletePost(postId)
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to delete post"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPost(postId: String): Flow<Resource<Post>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPost = feedLocalDataSource.getPostById(postId)
            if (cachedPost != null) {
                emit(Resource.Success(cachedPost))
            }

            // Fetch from remote
            val remotePost = feedRemoteDataSource.getPostById(postId)
            if (remotePost != null) {
                feedLocalDataSource.savePost(remotePost)
                emit(Resource.Success(remotePost))
            } else if (cachedPost == null) {
                emit(Resource.Error("Post not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPosts = feedLocalDataSource.getUserPosts(userId, limit)
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }

            // Fetch from remote
            val remotePosts = feedRemoteDataSource.getUserPosts(userId, limit)
            if (remotePosts.isNotEmpty()) {
                feedLocalDataSource.saveUserPosts(userId, remotePosts)
                emit(Resource.Success(remotePosts))
            } else if (cachedPosts.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Feed Operations
    override fun getUserFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first (only if no pagination)
            if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getUserFeed(userId, limit)
                if (cachedFeed.isNotEmpty()) {
                    emit(Resource.Success(cachedFeed))
                }
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getUserFeed(userId, limit, lastPostId)
            if (remoteFeed.isNotEmpty()) {
                if (lastPostId == null) {
                    // Fresh feed, replace cache
                    feedLocalDataSource.saveUserFeed(userId, remoteFeed)
                }
                emit(Resource.Success(remoteFeed))
            } else if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getUserFeed(userId, limit)
                emit(Resource.Success(cachedFeed))
            } else {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first (only if no pagination)
            if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getFollowingFeed(userId, limit)
                if (cachedFeed.isNotEmpty()) {
                    emit(Resource.Success(cachedFeed))
                }
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getFollowingFeed(userId, limit, lastPostId)
            if (remoteFeed.isNotEmpty()) {
                if (lastPostId == null) {
                    feedLocalDataSource.saveFollowingFeed(userId, remoteFeed)
                }
                emit(Resource.Success(remoteFeed))
            } else if (lastPostId == null) {
                val cachedFeed = feedLocalDataSource.getFollowingFeed(userId, limit)
                emit(Resource.Success(cachedFeed))
            } else {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting following feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getTrendingPosts(limit: Int, timeRange: String): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPosts = feedLocalDataSource.getTrendingPosts(limit)
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }

            // Fetch from remote
            val remotePosts = feedRemoteDataSource.getTrendingPosts(limit, timeRange)
            if (remotePosts.isNotEmpty()) {
                feedLocalDataSource.saveTrendingPosts(remotePosts)
                emit(Resource.Success(remotePosts))
            } else if (cachedPosts.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPopularPosts(limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedPosts = feedLocalDataSource.getPopularPosts(limit)
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }

            // Fetch from remote
            val remotePosts = feedRemoteDataSource.getPopularPosts(limit)
            if (remotePosts.isNotEmpty()) {
                feedLocalDataSource.savePopularPosts(remotePosts)
                emit(Resource.Success(remotePosts))
            } else if (cachedPosts.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getDiscoverFeed(userId: String, limit: Int): Flow<Resource<List<FeedItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedFeed = feedLocalDataSource.getDiscoverFeed(userId, limit)
            if (cachedFeed.isNotEmpty()) {
                emit(Resource.Success(cachedFeed))
            }

            // Fetch from remote
            val remoteFeed = feedRemoteDataSource.getDiscoverFeed(userId, limit)
            if (remoteFeed.isNotEmpty()) {
                feedLocalDataSource.saveDiscoverFeed(userId, remoteFeed)
                emit(Resource.Success(remoteFeed))
            } else if (cachedFeed.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting discover feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun searchPosts(query: String, userId: String?, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedResults = feedLocalDataSource.getSearchResults(query, limit)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Search remote
            val remoteResults = feedRemoteDataSource.searchPosts(query, userId, limit)
            if (remoteResults.isNotEmpty()) {
                feedLocalDataSource.saveSearchResults(query, remoteResults)
                emit(Resource.Success(remoteResults))
            } else if (cachedResults.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Post Interaction Operations
    override fun likePost(userId: String, postId: String, likeType: LikeType): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostLikeStatus(userId, postId, true)

            val success = feedRemoteDataSource.likePost(userId, postId, likeType)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostLikeStatus(userId, postId, false)
                emit(Resource.Error("Failed to like post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostLikeStatus(userId, postId, false)
            Timber.e(e, "Error liking post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unlikePost(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostLikeStatus(userId, postId, false)

            val success = feedRemoteDataSource.unlikePost(userId, postId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostLikeStatus(userId, postId, true)
                emit(Resource.Error("Failed to unlike post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostLikeStatus(userId, postId, true)
            Timber.e(e, "Error unliking post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): Flow<Resource<PostShare>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostShareStatus(userId, postId, true)

            val postShare = feedRemoteDataSource.sharePost(userId, postId, shareMessage, shareType)
            if (postShare != null) {
                emit(Resource.Success(postShare))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostShareStatus(userId, postId, false)
                emit(Resource.Error("Failed to share post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostShareStatus(userId, postId, false)
            Timber.e(e, "Error sharing post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unsharePost(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostShareStatus(userId, postId, false)

            val success = feedRemoteDataSource.unsharePost(userId, postId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostShareStatus(userId, postId, true)
                emit(Resource.Error("Failed to unshare post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostShareStatus(userId, postId, true)
            Timber.e(e, "Error unsharing post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostLikes(postId: String, limit: Int): Flow<Resource<List<PostLike>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedLikes = feedLocalDataSource.getPostLikes(postId, limit)
            if (cachedLikes.isNotEmpty()) {
                emit(Resource.Success(cachedLikes))
            }

            // Fetch from remote
            val remoteLikes = feedRemoteDataSource.getPostLikes(postId, limit)
            if (remoteLikes.isNotEmpty()) {
                feedLocalDataSource.savePostLikes(postId, remoteLikes)
                emit(Resource.Success(remoteLikes))
            } else if (cachedLikes.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post likes")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostShares(postId: String, limit: Int): Flow<Resource<List<PostShare>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedShares = feedLocalDataSource.getPostShares(postId, limit)
            if (cachedShares.isNotEmpty()) {
                emit(Resource.Success(cachedShares))
            }

            // Fetch from remote
            val remoteShares = feedRemoteDataSource.getPostShares(postId, limit)
            if (remoteShares.isNotEmpty()) {
                feedLocalDataSource.savePostShares(postId, remoteShares)
                emit(Resource.Success(remoteShares))
            } else if (cachedShares.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post shares")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isPostLikedByUser(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = feedLocalDataSource.isPostLikedByUser(userId, postId)
            emit(Resource.Success(cachedStatus))

            // Verify with remote (background sync)
            val remoteStatus = feedRemoteDataSource.isPostLikedByUser(userId, postId)
            if (remoteStatus != cachedStatus) {
                feedLocalDataSource.updatePostLikeStatus(userId, postId, remoteStatus)
                emit(Resource.Success(remoteStatus))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is liked")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isPostSharedByUser(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = feedLocalDataSource.isPostSharedByUser(userId, postId)
            emit(Resource.Success(cachedStatus))

            // Verify with remote (background sync)
            val remoteStatus = feedRemoteDataSource.isPostSharedByUser(userId, postId)
            if (remoteStatus != cachedStatus) {
                feedLocalDataSource.updatePostShareStatus(userId, postId, remoteStatus)
                emit(Resource.Success(remoteStatus))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is shared")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Comment Operations
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

    // Comment Interaction Operations
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

    // Recipe and Cookbook Reference Operations
    override fun addRecipeReference(reference: PostRecipeReference): Flow<Resource<PostRecipeReference>> = flow {
        try {
            emit(Resource.Loading())

            val addedReference = feedRemoteDataSource.addRecipeReference(reference)
            if (addedReference != null) {
                // Update cache
                val currentRefs = feedLocalDataSource.getPostRecipeReferences(reference.postId).toMutableList()
                currentRefs.add(addedReference)
                feedLocalDataSource.savePostRecipeReferences(reference.postId, currentRefs)

                emit(Resource.Success(addedReference))
            } else {
                emit(Resource.Error("Failed to add recipe reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun addCookbookReference(reference: PostCookbookReference): Flow<Resource<PostCookbookReference>> = flow {
        try {
            emit(Resource.Loading())

            val addedReference = feedRemoteDataSource.addCookbookReference(reference)
            if (addedReference != null) {
                // Update cache
                val currentRefs = feedLocalDataSource.getPostCookbookReferences(reference.postId).toMutableList()
                currentRefs.add(addedReference)
                feedLocalDataSource.savePostCookbookReferences(reference.postId, currentRefs)

                emit(Resource.Success(addedReference))
            } else {
                emit(Resource.Error("Failed to add cookbook reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding cookbook reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeRecipeReference(referenceId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.removeRecipeReference(referenceId)
            if (success) {
                // Note: We'd need the postId to update cache properly
                // In a real implementation, you might want to pass postId as well
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove recipe reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeCookbookReference(referenceId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.removeCookbookReference(referenceId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove cookbook reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing cookbook reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostRecipeReferences(postId: String): Flow<Resource<List<PostRecipeReference>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedRefs = feedLocalDataSource.getPostRecipeReferences(postId)
            if (cachedRefs.isNotEmpty()) {
                emit(Resource.Success(cachedRefs))
            }

            // Fetch from remote
            val remoteRefs = feedRemoteDataSource.getPostRecipeReferences(postId)
            if (remoteRefs.isNotEmpty()) {
                feedLocalDataSource.savePostRecipeReferences(postId, remoteRefs)
                emit(Resource.Success(remoteRefs))
            } else if (cachedRefs.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post recipe references")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostCookbookReferences(postId: String): Flow<Resource<List<PostCookbookReference>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedRefs = feedLocalDataSource.getPostCookbookReferences(postId)
            if (cachedRefs.isNotEmpty()) {
                emit(Resource.Success(cachedRefs))
            }

            // Fetch from remote
            val remoteRefs = feedRemoteDataSource.getPostCookbookReferences(postId)
            if (remoteRefs.isNotEmpty()) {
                feedLocalDataSource.savePostCookbookReferences(postId, remoteRefs)
                emit(Resource.Success(remoteRefs))
            } else if (cachedRefs.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post cookbook references")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Media Operations
    override fun uploadPostImage(postId: String, imageData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val imageUrl = feedRemoteDataSource.uploadPostImage(postId, imageData)
            if (imageUrl != null) {
                emit(Resource.Success(imageUrl))
            } else {
                emit(Resource.Error("Failed to upload image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post image")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun uploadPostVideo(postId: String, videoData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val videoUrl = feedRemoteDataSource.uploadPostVideo(postId, videoData)
            if (videoUrl != null) {
                emit(Resource.Success(videoUrl))
            } else {
                emit(Resource.Error("Failed to upload video"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post video")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deletePostMedia(mediaUrl: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.deletePostMedia(mediaUrl)
            emit(Resource.Success(success))
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post media")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Activity Tracking and Analytics
    override fun trackPostActivity(activity: PostActivity): Flow<Resource<Boolean>> = flow {
        try {
            val success = feedRemoteDataSource.trackPostActivity(activity)
            emit(Resource.Success(success))
        } catch (e: Exception) {
            Timber.e(e, "Error tracking post activity")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostAnalytics(postId: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedAnalytics = feedLocalDataSource.getPostAnalytics(postId)
            if (cachedAnalytics != null) {
                emit(Resource.Success(cachedAnalytics))
            }

            // Fetch from remote
            val remoteAnalytics = feedRemoteDataSource.getPostAnalytics(postId)
            if (remoteAnalytics.isNotEmpty()) {
                feedLocalDataSource.savePostAnalytics(postId, remoteAnalytics)
                emit(Resource.Success(remoteAnalytics))
            } else if (cachedAnalytics == null) {
                emit(Resource.Success(emptyMap()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post analytics")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserPostAnalytics(userId: String, dateRange: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedAnalytics = feedLocalDataSource.getUserPostAnalytics(userId)
            if (cachedAnalytics != null) {
                emit(Resource.Success(cachedAnalytics))
            }

            // Fetch from remote
            val remoteAnalytics = feedRemoteDataSource.getUserPostAnalytics(userId, dateRange)
            if (remoteAnalytics.isNotEmpty()) {
                feedLocalDataSource.saveUserPostAnalytics(userId, remoteAnalytics)
                emit(Resource.Success(remoteAnalytics))
            } else if (cachedAnalytics == null) {
                emit(Resource.Success(emptyMap()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user post analytics")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Hashtag Operations (simple implementations)
    override fun getPostsByHashtag(hashtag: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // For hashtag search, go directly to remote (no cache for now)
            val posts = feedRemoteDataSource.searchPosts("#$hashtag", null, limit)
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            Timber.e(e, "Error getting posts by hashtag")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getTrendingHashtags(limit: Int): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedHashtags = feedLocalDataSource.getTrendingHashtags(limit)
            if (cachedHashtags.isNotEmpty()) {
                emit(Resource.Success(cachedHashtags))
            }

            // Fetch from remote
            val remoteHashtags = feedRemoteDataSource.getTrendingHashtags(limit)
            if (remoteHashtags.isNotEmpty()) {
                feedLocalDataSource.saveTrendingHashtags(remoteHashtags)
                emit(Resource.Success(remoteHashtags))
            } else if (cachedHashtags.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending hashtags")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun searchHashtags(query: String, limit: Int): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            // For hashtag search, this would typically be a remote operation
            // For now, return empty list (can be implemented later)
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error searching hashtags")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // User-specific Operations (simplified implementations)
    override fun getUserLikedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // This would require additional remote implementation
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user liked posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserSharedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // This would require additional remote implementation
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user shared posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserSavedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> = flow {
        try {
            emit(Resource.Loading())

            // For saved posts, we mainly rely on local cache for now
            // This would be enhanced with remote sync later
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user saved posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun savePost(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostSaveStatus(userId, postId, true)

            val success = feedRemoteDataSource.savePost(userId, postId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostSaveStatus(userId, postId, false)
                emit(Resource.Error("Failed to save post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostSaveStatus(userId, postId, false)
            Timber.e(e, "Error saving post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unsavePost(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Update local cache optimistically
            feedLocalDataSource.updatePostSaveStatus(userId, postId, false)

            val success = feedRemoteDataSource.unsavePost(userId, postId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                // Revert local change on failure
                feedLocalDataSource.updatePostSaveStatus(userId, postId, true)
                emit(Resource.Error("Failed to unsave post"))
            }
        } catch (e: Exception) {
            // Revert local change on error
            feedLocalDataSource.updatePostSaveStatus(userId, postId, true)
            Timber.e(e, "Error unsaving post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isPostSavedByUser(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Check cache first
            val cachedStatus = feedLocalDataSource.isPostSavedByUser(userId, postId)
            emit(Resource.Success(cachedStatus))

            // Verify with remote (background sync)
            val remoteStatus = feedRemoteDataSource.isPostSavedByUser(userId, postId)
            if (remoteStatus != cachedStatus) {
                feedLocalDataSource.updatePostSaveStatus(userId, postId, remoteStatus)
                emit(Resource.Success(remoteStatus))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is saved")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Simplified implementations for features without domain models
    override fun reportPost(userId: String, postId: String, reason: String, description: String?): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error reporting post")
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

    override fun blockUserFromPost(postId: String, blockedUserId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error blocking user from post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun hidePost(userId: String, postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error hiding post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updatePostVisibility(postId: String, isPublic: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error updating post visibility")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateCommentSettings(postId: String, allowComments: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error updating comment settings")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateShareSettings(postId: String, allowShares: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error updating share settings")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun archivePost(postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error archiving post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unarchivePost(postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error unarchiving post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun pinPost(postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error pinning post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unpinPost(postId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error unpinning post")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateFeedPreferences(userId: String, preferences: Map<String, Any>): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error updating feed preferences")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getFeedPreferences(userId: String): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(emptyMap())) // Return empty map for now
        } catch (e: Exception) {
            Timber.e(e, "Error getting feed preferences")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getMultiplePosts(postIds: List<String>): Flow<Resource<List<Post>>> = flow {
        try {
            emit(Resource.Loading())

            val posts = mutableListOf<Post>()

            // Try to get from cache first
            postIds.forEach { postId ->
                val cachedPost = feedLocalDataSource.getPostById(postId)
                cachedPost?.let { posts.add(it) }
            }

            emit(Resource.Success(posts))
        } catch (e: Exception) {
            Timber.e(e, "Error getting multiple posts")
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

    override fun markPostsAsViewed(userId: String, postIds: List<String>): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error marking posts as viewed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUnreadPostCount(userId: String): Flow<Resource<Int>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(0)) // Return 0 for now
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread post count")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun refreshUserFeed(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            // Clear cache and fetch fresh data
            feedLocalDataSource.clearUserCache(userId)

            // Fetch fresh feed
            val freshFeed = feedRemoteDataSource.getUserFeed(userId, 20, null)
            if (freshFeed.isNotEmpty()) {
                feedLocalDataSource.saveUserFeed(userId, freshFeed)
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing user feed")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun clearFeedCache(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            feedLocalDataSource.clearUserCache(userId)
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error clearing feed cache")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Helper extension function for Post to PostListItem conversion
    private fun Post.toListItem(
        username: String = "",
        userProfilePicture: String? = null,
        isLikedByCurrentUser: Boolean = false,
        isSharedByCurrentUser: Boolean = false,
        isFollowingAuthor: Boolean = false
    ): PostListItem {
        return PostListItem(
            postId = this.postId,
            userId = this.userId,
            username = username,
            userProfilePicture = userProfilePicture,
            content = this.content,
            firstImageUrl = this.imageUrls.firstOrNull(),
            postType = this.postType,
            likeCount = this.likeCount,
            commentCount = this.commentCount,
            shareCount = this.shareCount,
            isLikedByCurrentUser = isLikedByCurrentUser,
            isSharedByCurrentUser = isSharedByCurrentUser,
            hasRecipeReferences = this.recipeReferences.isNotEmpty(),
            hasCookbookReferences = this.cookbookReferences.isNotEmpty(),
            createdAt = this.createdAt,
            isFollowingAuthor = isFollowingAuthor
        )
    }
}