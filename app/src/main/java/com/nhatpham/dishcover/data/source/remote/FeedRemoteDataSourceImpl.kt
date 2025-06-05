// FeedRemoteDataSourceImpl.kt - Facade Implementation
package com.nhatpham.dishcover.data.source.remote

import com.nhatpham.dishcover.data.source.remote.feed.*
import com.nhatpham.dishcover.domain.model.feed.*
import javax.inject.Inject

class FeedRemoteDataSourceImpl @Inject constructor(
    private val postRemoteDataSource: PostRemoteDataSource,
    private val postInteractionRemoteDataSource: PostInteractionRemoteDataSource,
    private val commentRemoteDataSource: CommentRemoteDataSource,
    private val postMediaRemoteDataSource: PostMediaRemoteDataSource,
    private val postAnalyticsRemoteDataSource: PostAnalyticsRemoteDataSource,
    private val postReferenceRemoteDataSource: PostReferenceRemoteDataSource,
    private val feedAggregationRemoteDataSource: FeedAggregationRemoteDataSource
) : FeedRemoteDataSource {

    // Post CRUD Operations - Delegate to PostRemoteDataSource
    override suspend fun createPost(post: Post): Post? {
        val createdPost = postRemoteDataSource.createPost(post)

        // Save references and update hashtag counts
        createdPost?.let {
            postReferenceRemoteDataSource.savePostRecipeReferences(it.postId, it.recipeReferences)
            postReferenceRemoteDataSource.savePostCookbookReferences(it.postId, it.cookbookReferences)
            feedAggregationRemoteDataSource.updateHashtagCounts(it.hashtags, increment = true)
        }

        return createdPost
    }

    override suspend fun updatePost(post: Post): Post? {
        // Get old post to update hashtag counts
        val oldPost = postRemoteDataSource.getPostById(post.postId)

        val updatedPost = postRemoteDataSource.updatePost(post)

        updatedPost?.let {
            // Update references
            postReferenceRemoteDataSource.deletePostRecipeReferences(it.postId)
            postReferenceRemoteDataSource.savePostRecipeReferences(it.postId, it.recipeReferences)

            postReferenceRemoteDataSource.deletePostCookbookReferences(it.postId)
            postReferenceRemoteDataSource.savePostCookbookReferences(it.postId, it.cookbookReferences)

            // Update hashtag counts
            oldPost?.let { old -> feedAggregationRemoteDataSource.updateHashtagCounts(old.hashtags, increment = false) }
            feedAggregationRemoteDataSource.updateHashtagCounts(it.hashtags, increment = true)
        }

        return updatedPost
    }

    override suspend fun deletePost(postId: String): Boolean {
        // Get post to update hashtag counts
        val post = postRemoteDataSource.getPostById(postId)

        val success = postRemoteDataSource.deletePost(postId)

        if (success) {
            // Clean up associated data
            commentRemoteDataSource.deletePostComments(postId)
            postInteractionRemoteDataSource.deletePostLikes(postId)
            postInteractionRemoteDataSource.deletePostShares(postId)
            postInteractionRemoteDataSource.deleteSavedPosts(postId)
            postAnalyticsRemoteDataSource.deletePostActivity(postId)
            postAnalyticsRemoteDataSource.deletePostViews(postId)
            postReferenceRemoteDataSource.deletePostRecipeReferences(postId)
            postReferenceRemoteDataSource.deletePostCookbookReferences(postId)

            // Update hashtag counts
            post?.let { feedAggregationRemoteDataSource.updateHashtagCounts(it.hashtags, increment = false) }
        }

        return success
    }

    override suspend fun getPostById(postId: String): Post? {
        val post = postRemoteDataSource.getPostById(postId)

        return post?.let {
            val recipeReferences = postReferenceRemoteDataSource.getPostRecipeReferences(postId)
            val cookbookReferences = postReferenceRemoteDataSource.getPostCookbookReferences(postId)

            it.copy(
                recipeReferences = recipeReferences,
                cookbookReferences = cookbookReferences
            )
        }
    }

    override suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem> =
        postRemoteDataSource.getUserPosts(userId, limit)

    // Feed Operations - Delegate to FeedAggregationRemoteDataSource
    override suspend fun getUserFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> =
        feedAggregationRemoteDataSource.getUserFeed(userId, limit, lastPostId)

    override suspend fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> =
        feedAggregationRemoteDataSource.getFollowingFeed(userId, limit, lastPostId)

    override suspend fun getTrendingPosts(limit: Int, timeRange: String): List<PostListItem> =
        feedAggregationRemoteDataSource.getTrendingPosts(limit, timeRange)

    override suspend fun getPopularPosts(limit: Int): List<PostListItem> =
        feedAggregationRemoteDataSource.getPopularPosts(limit)

    override suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> =
        feedAggregationRemoteDataSource.getDiscoverFeed(userId, limit)

    override suspend fun searchPosts(query: String, userId: String?, limit: Int): List<PostListItem> =
        postRemoteDataSource.searchPosts(query, userId, limit)

    // Post Interaction Operations - Delegate to PostInteractionRemoteDataSource
    override suspend fun likePost(userId: String, postId: String, likeType: LikeType): Boolean {
        val success = postInteractionRemoteDataSource.likePost(userId, postId, likeType)

        if (success) {
            // Track activity
            postAnalyticsRemoteDataSource.trackPostActivity(
                PostActivity(
                    postId = postId,
                    userId = userId,
                    activityType = PostActivityType.LIKE
                )
            )
        }

        return success
    }

    override suspend fun unlikePost(userId: String, postId: String): Boolean {
        val success = postInteractionRemoteDataSource.unlikePost(userId, postId)

        if (success) {
            // Track activity
            postAnalyticsRemoteDataSource.trackPostActivity(
                PostActivity(
                    postId = postId,
                    userId = userId,
                    activityType = PostActivityType.UNLIKE
                )
            )
        }

        return success
    }

    override suspend fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): PostShare? {
        val postShare = postInteractionRemoteDataSource.sharePost(userId, postId, shareMessage, shareType)

        if (postShare != null) {
            // Track activity
            postAnalyticsRemoteDataSource.trackPostActivity(
                PostActivity(
                    postId = postId,
                    userId = userId,
                    activityType = PostActivityType.SHARE
                )
            )
        }

        return postShare
    }

    override suspend fun unsharePost(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.unsharePost(userId, postId)

    override suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> =
        postInteractionRemoteDataSource.getPostLikes(postId, limit)

    override suspend fun getPostShares(postId: String, limit: Int): List<PostShare> =
        postInteractionRemoteDataSource.getPostShares(postId, limit)

    override suspend fun isPostLikedByUser(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.isPostLikedByUser(userId, postId)

    override suspend fun isPostSharedByUser(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.isPostSharedByUser(userId, postId)

    // Comment Operations - Delegate to CommentRemoteDataSource
    override suspend fun addComment(comment: Comment): Comment? {
        val addedComment = commentRemoteDataSource.addComment(comment)

        if (addedComment != null) {
            // Track activity
            postAnalyticsRemoteDataSource.trackPostActivity(
                PostActivity(
                    postId = comment.postId,
                    userId = comment.userId,
                    activityType = PostActivityType.COMMENT
                )
            )
        }

        return addedComment
    }

    override suspend fun updateComment(comment: Comment): Comment? =
        commentRemoteDataSource.updateComment(comment)

    override suspend fun deleteComment(commentId: String): Boolean =
        commentRemoteDataSource.deleteComment(commentId)

    override suspend fun getPostComments(postId: String, limit: Int, lastCommentId: String?): List<Comment> =
        commentRemoteDataSource.getPostComments(postId, limit, lastCommentId)

    override suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> =
        commentRemoteDataSource.getCommentReplies(commentId, limit)

    override suspend fun getComment(commentId: String): Comment? =
        commentRemoteDataSource.getComment(commentId)

    // Comment Interaction Operations - Delegate to CommentRemoteDataSource
    override suspend fun likeComment(userId: String, commentId: String, likeType: LikeType): Boolean =
        commentRemoteDataSource.likeComment(userId, commentId, likeType)

    override suspend fun unlikeComment(userId: String, commentId: String): Boolean =
        commentRemoteDataSource.unlikeComment(userId, commentId)

    override suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> =
        commentRemoteDataSource.getCommentLikes(commentId, limit)

    override suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean =
        commentRemoteDataSource.isCommentLikedByUser(userId, commentId)

    // Recipe and Cookbook Reference Operations - Delegate to PostReferenceRemoteDataSource
    override suspend fun addRecipeReference(reference: PostRecipeReference): PostRecipeReference? =
        postReferenceRemoteDataSource.addRecipeReference(reference)

    override suspend fun addCookbookReference(reference: PostCookbookReference): PostCookbookReference? =
        postReferenceRemoteDataSource.addCookbookReference(reference)

    override suspend fun removeRecipeReference(referenceId: String): Boolean =
        postReferenceRemoteDataSource.removeRecipeReference(referenceId)

    override suspend fun removeCookbookReference(referenceId: String): Boolean =
        postReferenceRemoteDataSource.removeCookbookReference(referenceId)

    override suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> =
        postReferenceRemoteDataSource.getPostRecipeReferences(postId)

    override suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> =
        postReferenceRemoteDataSource.getPostCookbookReferences(postId)

    // Media Operations - Delegate to PostMediaRemoteDataSource
    override suspend fun uploadPostImage(postId: String, imageData: ByteArray): String? =
        postMediaRemoteDataSource.uploadPostImage(postId, imageData)

    override suspend fun uploadPostVideo(postId: String, videoData: ByteArray): String? =
        postMediaRemoteDataSource.uploadPostVideo(postId, videoData)

    override suspend fun deletePostMedia(mediaUrl: String): Boolean =
        postMediaRemoteDataSource.deletePostMedia(mediaUrl)

    // Activity Tracking and Analytics - Delegate to PostAnalyticsRemoteDataSource
    override suspend fun trackPostActivity(activity: PostActivity): Boolean =
        postAnalyticsRemoteDataSource.trackPostActivity(activity)

    override suspend fun getPostAnalytics(postId: String): Map<String, Any> =
        postAnalyticsRemoteDataSource.getPostAnalytics(postId)

    override suspend fun getUserPostAnalytics(userId: String, dateRange: String): Map<String, Any> =
        postAnalyticsRemoteDataSource.getUserPostAnalytics(userId, dateRange)

    // Hashtag Operations - Delegate to FeedAggregationRemoteDataSource
    override suspend fun getTrendingHashtags(limit: Int): List<String> =
        feedAggregationRemoteDataSource.getTrendingHashtags(limit)

    // User Operations (Simple) - Delegate to PostInteractionRemoteDataSource
    override suspend fun savePost(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.savePost(userId, postId)

    override suspend fun unsavePost(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.unsavePost(userId, postId)

    override suspend fun isPostSavedByUser(userId: String, postId: String): Boolean =
        postInteractionRemoteDataSource.isPostSavedByUser(userId, postId)
}