// FeedRepositoryFacade.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.domain.repository.feed.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Facade that implements the legacy FeedRepository interface by delegating
 * to the new domain-specific repositories. This maintains backward compatibility
 * while using the new modular architecture internally.
 */
class FeedRepositoryFacade @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionRepository: PostInteractionRepository,
    private val commentRepository: CommentRepository,
    private val feedAggregationRepository: FeedAggregationRepository,
    private val postMediaRepository: PostMediaRepository,
    private val postAnalyticsRepository: PostAnalyticsRepository,
    private val postReferenceRepository: PostReferenceRepository
) : FeedRepository {

    // Post CRUD Operations - Delegate to PostRepository
    override fun createPost(post: Post): Flow<Resource<Post>> =
        postRepository.createPost(post)

    override fun updatePost(post: Post): Flow<Resource<Post>> =
        postRepository.updatePost(post)

    override fun deletePost(postId: String): Flow<Resource<Boolean>> =
        postRepository.deletePost(postId)

    override fun getPost(postId: String): Flow<Resource<Post>> =
        postRepository.getPost(postId)

    override fun getUserPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> =
        postRepository.getUserPosts(userId, limit)

    // Feed Operations - Delegate to FeedAggregationRepository
    override fun getUserFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> =
        feedAggregationRepository.getUserFeed(userId, limit, lastPostId)

    override fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): Flow<Resource<List<FeedItem>>> =
        feedAggregationRepository.getFollowingFeed(userId, limit, lastPostId)

    override fun getTrendingPosts(limit: Int, timeRange: String): Flow<Resource<List<PostListItem>>> =
        feedAggregationRepository.getTrendingPosts(limit, timeRange)

    override fun getPopularPosts(limit: Int): Flow<Resource<List<PostListItem>>> =
        feedAggregationRepository.getPopularPosts(limit)

    override fun getDiscoverFeed(userId: String, limit: Int): Flow<Resource<List<FeedItem>>> =
        feedAggregationRepository.getDiscoverFeed(userId, limit)

    override fun searchPosts(query: String, userId: String?, limit: Int): Flow<Resource<List<PostListItem>>> =
        postRepository.searchPosts(query, userId, limit)

    // Post Interaction Operations - Delegate to PostInteractionRepository
    override fun likePost(userId: String, postId: String, likeType: LikeType): Flow<Resource<Boolean>> =
        postInteractionRepository.likePost(userId, postId, likeType)

    override fun unlikePost(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.unlikePost(userId, postId)

    override fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): Flow<Resource<PostShare>> =
        postInteractionRepository.sharePost(userId, postId, shareMessage, shareType)

    override fun unsharePost(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.unsharePost(userId, postId)

    override fun getPostLikes(postId: String, limit: Int): Flow<Resource<List<PostLike>>> =
        postInteractionRepository.getPostLikes(postId, limit)

    override fun getPostShares(postId: String, limit: Int): Flow<Resource<List<PostShare>>> =
        postInteractionRepository.getPostShares(postId, limit)

    override fun isPostLikedByUser(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.isPostLikedByUser(userId, postId)

    override fun isPostSharedByUser(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.isPostSharedByUser(userId, postId)

    // Comment Operations - Delegate to CommentRepository
    override fun addComment(comment: Comment): Flow<Resource<Comment>> =
        commentRepository.addComment(comment)

    override fun updateComment(comment: Comment): Flow<Resource<Comment>> =
        commentRepository.updateComment(comment)

    override fun deleteComment(commentId: String): Flow<Resource<Boolean>> =
        commentRepository.deleteComment(commentId)

    override fun getPostComments(postId: String, limit: Int, lastCommentId: String?): Flow<Resource<List<Comment>>> =
        commentRepository.getPostComments(postId, limit, lastCommentId)

    override fun getCommentReplies(commentId: String, limit: Int): Flow<Resource<List<Comment>>> =
        commentRepository.getCommentReplies(commentId, limit)

    override fun getComment(commentId: String): Flow<Resource<Comment>> =
        commentRepository.getComment(commentId)

    // Comment Interaction Operations - Delegate to CommentRepository
    override fun likeComment(userId: String, commentId: String, likeType: LikeType): Flow<Resource<Boolean>> =
        commentRepository.likeComment(userId, commentId, likeType)

    override fun unlikeComment(userId: String, commentId: String): Flow<Resource<Boolean>> =
        commentRepository.unlikeComment(userId, commentId)

    override fun getCommentLikes(commentId: String, limit: Int): Flow<Resource<List<CommentLike>>> =
        commentRepository.getCommentLikes(commentId, limit)

    override fun isCommentLikedByUser(userId: String, commentId: String): Flow<Resource<Boolean>> =
        commentRepository.isCommentLikedByUser(userId, commentId)

    // Recipe and Cookbook Reference Operations - Delegate to PostReferenceRepository
    override fun addRecipeReference(reference: PostRecipeReference): Flow<Resource<PostRecipeReference>> =
        postReferenceRepository.addRecipeReference(reference)

    override fun addCookbookReference(reference: PostCookbookReference): Flow<Resource<PostCookbookReference>> =
        postReferenceRepository.addCookbookReference(reference)

    override fun removeRecipeReference(referenceId: String): Flow<Resource<Boolean>> =
        postReferenceRepository.removeRecipeReference(referenceId)

    override fun removeCookbookReference(referenceId: String): Flow<Resource<Boolean>> =
        postReferenceRepository.removeCookbookReference(referenceId)

    override fun getPostRecipeReferences(postId: String): Flow<Resource<List<PostRecipeReference>>> =
        postReferenceRepository.getPostRecipeReferences(postId)

    override fun getPostCookbookReferences(postId: String): Flow<Resource<List<PostCookbookReference>>> =
        postReferenceRepository.getPostCookbookReferences(postId)

    // Media Operations - Delegate to PostMediaRepository
    override fun uploadPostImage(postId: String, imageData: ByteArray): Flow<Resource<String>> =
        postMediaRepository.uploadPostImage(postId, imageData)

    override fun uploadPostVideo(postId: String, videoData: ByteArray): Flow<Resource<String>> =
        postMediaRepository.uploadPostVideo(postId, videoData)

    override fun deletePostMedia(mediaUrl: String): Flow<Resource<Boolean>> =
        postMediaRepository.deletePostMedia(mediaUrl)

    // Activity Tracking and Analytics - Delegate to PostAnalyticsRepository
    override fun trackPostActivity(activity: PostActivity): Flow<Resource<Boolean>> =
        postAnalyticsRepository.trackPostActivity(activity)

    override fun getPostAnalytics(postId: String): Flow<Resource<Map<String, Any>>> =
        postAnalyticsRepository.getPostAnalytics(postId)

    override fun getUserPostAnalytics(userId: String, dateRange: String): Flow<Resource<Map<String, Any>>> =
        postAnalyticsRepository.getUserPostAnalytics(userId, dateRange)

    // Hashtag Operations - Delegate to FeedAggregationRepository
    override fun getPostsByHashtag(hashtag: String, limit: Int): Flow<Resource<List<PostListItem>>> =
        feedAggregationRepository.getPostsByHashtag(hashtag, limit)

    override fun getTrendingHashtags(limit: Int): Flow<Resource<List<String>>> =
        feedAggregationRepository.getTrendingHashtags(limit)

    override fun searchHashtags(query: String, limit: Int): Flow<Resource<List<String>>> =
        feedAggregationRepository.searchHashtags(query, limit)

    // User-specific Operations - Delegate to PostInteractionRepository
    override fun getUserLikedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> =
        postInteractionRepository.getUserLikedPosts(userId, limit)

    override fun getUserSharedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> =
        postInteractionRepository.getUserSharedPosts(userId, limit)

    override fun getUserSavedPosts(userId: String, limit: Int): Flow<Resource<List<PostListItem>>> =
        postInteractionRepository.getUserSavedPosts(userId, limit)

    override fun savePost(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.savePost(userId, postId)

    override fun unsavePost(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.unsavePost(userId, postId)

    override fun isPostSavedByUser(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.isPostSavedByUser(userId, postId)

    // Moderation Operations - Delegate to appropriate repositories
    override fun reportPost(userId: String, postId: String, reason: String, description: String?): Flow<Resource<Boolean>> =
        postInteractionRepository.reportPost(userId, postId, reason, description)

    override fun reportComment(userId: String, commentId: String, reason: String, description: String?): Flow<Resource<Boolean>> =
        commentRepository.reportComment(userId, commentId, reason, description)

    override fun blockUserFromPost(postId: String, blockedUserId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.blockUserFromPost(postId, blockedUserId)

    override fun hidePost(userId: String, postId: String): Flow<Resource<Boolean>> =
        postInteractionRepository.hidePost(userId, postId)

    // Post Settings and Visibility - Delegate to PostRepository
    override fun updatePostVisibility(postId: String, isPublic: Boolean): Flow<Resource<Boolean>> =
        postRepository.updatePostVisibility(postId, isPublic)

    override fun updateCommentSettings(postId: String, allowComments: Boolean): Flow<Resource<Boolean>> =
        postRepository.updateCommentSettings(postId, allowComments)

    override fun updateShareSettings(postId: String, allowShares: Boolean): Flow<Resource<Boolean>> =
        postRepository.updateShareSettings(postId, allowShares)

    override fun archivePost(postId: String): Flow<Resource<Boolean>> =
        postRepository.archivePost(postId)

    override fun unarchivePost(postId: String): Flow<Resource<Boolean>> =
        postRepository.unarchivePost(postId)

    override fun pinPost(postId: String): Flow<Resource<Boolean>> =
        postRepository.pinPost(postId)

    override fun unpinPost(postId: String): Flow<Resource<Boolean>> =
        postRepository.unpinPost(postId)

    // Feed Algorithm and Personalization - Delegate to FeedAggregationRepository
    override fun updateFeedPreferences(userId: String, preferences: Map<String, Any>): Flow<Resource<Boolean>> =
        feedAggregationRepository.updateFeedPreferences(userId, preferences)

    override fun getFeedPreferences(userId: String): Flow<Resource<Map<String, Any>>> =
        feedAggregationRepository.getFeedPreferences(userId)

    // Batch Operations - Delegate to PostRepository and CommentRepository
    override fun getMultiplePosts(postIds: List<String>): Flow<Resource<List<Post>>> =
        postRepository.getMultiplePosts(postIds)

    override fun getMultipleComments(commentIds: List<String>): Flow<Resource<List<Comment>>> =
        commentRepository.getMultipleComments(commentIds)

    override fun markPostsAsViewed(userId: String, postIds: List<String>): Flow<Resource<Boolean>> =
        feedAggregationRepository.markPostsAsViewed(userId, postIds)

    override fun getUnreadPostCount(userId: String): Flow<Resource<Int>> =
        feedAggregationRepository.getUnreadPostCount(userId)

    // Cache Management - Delegate to FeedAggregationRepository
    override fun refreshUserFeed(userId: String): Flow<Resource<Boolean>> =
        feedAggregationRepository.refreshUserFeed(userId)

    override fun clearFeedCache(userId: String): Flow<Resource<Boolean>> =
        feedAggregationRepository.clearFeedCache(userId)
}