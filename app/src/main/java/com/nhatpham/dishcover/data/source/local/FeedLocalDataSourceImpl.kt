// FeedLocalDataSourceImpl.kt - Facade Implementation
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.data.source.local.feed.*
import com.nhatpham.dishcover.domain.model.feed.*
import javax.inject.Inject

/**
 * Facade that implements the legacy FeedLocalDataSource interface by delegating
 * to the new domain-specific local data sources. This maintains backward compatibility
 * while using the new modular architecture internally.
 */
class FeedLocalDataSourceImpl @Inject constructor(
    private val postLocalDataSource: PostLocalDataSource,
    private val postInteractionLocalDataSource: PostInteractionLocalDataSource,
    private val commentLocalDataSource: CommentLocalDataSource,
    private val feedAggregationLocalDataSource: FeedAggregationLocalDataSource,
    private val postAnalyticsLocalDataSource: PostAnalyticsLocalDataSource,
    private val postReferenceLocalDataSource: PostReferenceLocalDataSource
) : FeedLocalDataSource {

    // Post operations - Delegate to PostLocalDataSource
    override suspend fun savePost(post: Post) {
        postLocalDataSource.savePost(post)
    }

    override suspend fun getPostById(postId: String): Post? {
        return postLocalDataSource.getPostById(postId)
    }

    override suspend fun deletePost(postId: String) {
        postLocalDataSource.deletePost(postId)
        postInteractionLocalDataSource.deletePostInteractions(postId)
        commentLocalDataSource.deletePostComments(postId)
        feedAggregationLocalDataSource.removePostFromFeeds(postId)
        postAnalyticsLocalDataSource.deletePostAnalytics(postId)
        postReferenceLocalDataSource.deletePostReferences(postId)
    }

    override suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem> {
        return postLocalDataSource.getUserPosts(userId, limit)
    }

    override suspend fun saveUserPosts(userId: String, posts: List<PostListItem>) {
        postLocalDataSource.saveUserPosts(userId, posts)
    }

    override suspend fun addPostToUserLists(userId: String, post: PostListItem) {
        postLocalDataSource.addPostToUserLists(userId, post)
    }

    override suspend fun updatePostInUserLists(userId: String, updatedPost: PostListItem) {
        postLocalDataSource.updatePostInUserLists(userId, updatedPost)
        feedAggregationLocalDataSource.updatePostInTrendingAndPopular(updatedPost)
    }

    override suspend fun removePostFromUserLists(userId: String, postId: String) {
        postLocalDataSource.removePostFromUserLists(userId, postId)
    }

    override suspend fun getSearchResults(query: String, limit: Int): List<PostListItem> {
        return postLocalDataSource.getSearchResults(query, limit)
    }

    override suspend fun saveSearchResults(query: String, posts: List<PostListItem>) {
        postLocalDataSource.saveSearchResults(query, posts)
    }

    override suspend fun getMultiplePosts(postIds: List<String>): List<Post> {
        return postLocalDataSource.getMultiplePosts(postIds)
    }

    override suspend fun isPostCached(postId: String): Boolean {
        return postLocalDataSource.isPostCached(postId)
    }

    // Comment operations - Delegate to CommentLocalDataSource
    override suspend fun saveComment(comment: Comment) {
        commentLocalDataSource.saveComment(comment)
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        return commentLocalDataSource.getCommentById(commentId)
    }

    override suspend fun deleteComment(commentId: String) {
        commentLocalDataSource.deleteComment(commentId)
    }

    override suspend fun getPostComments(postId: String, limit: Int): List<Comment> {
        return commentLocalDataSource.getPostComments(postId, limit)
    }

    override suspend fun savePostComments(postId: String, comments: List<Comment>) {
        commentLocalDataSource.savePostComments(postId, comments)
    }

    override suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> {
        return commentLocalDataSource.getCommentReplies(commentId, limit)
    }

    override suspend fun saveCommentReplies(commentId: String, replies: List<Comment>) {
        commentLocalDataSource.saveCommentReplies(commentId, replies)
    }

    override suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> {
        return commentLocalDataSource.getCommentLikes(commentId, limit)
    }

    override suspend fun saveCommentLikes(commentId: String, likes: List<CommentLike>) {
        commentLocalDataSource.saveCommentLikes(commentId, likes)
    }

    override suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean {
        return commentLocalDataSource.isCommentLikedByUser(userId, commentId)
    }

    override suspend fun updateCommentLikeStatus(userId: String, commentId: String, isLiked: Boolean) {
        commentLocalDataSource.updateCommentLikeStatus(userId, commentId, isLiked)
    }

    override suspend fun getMultipleComments(commentIds: List<String>): List<Comment> {
        return commentLocalDataSource.getMultipleComments(commentIds)
    }

    override suspend fun isCommentCached(commentId: String): Boolean {
        return commentLocalDataSource.isCommentCached(commentId)
    }

    // Feed operations - Delegate to FeedAggregationLocalDataSource
    override suspend fun getUserFeed(userId: String, limit: Int): List<FeedItem> {
        return feedAggregationLocalDataSource.getUserFeed(userId, limit)
    }

    override suspend fun saveUserFeed(userId: String, feedItems: List<FeedItem>) {
        feedAggregationLocalDataSource.saveUserFeed(userId, feedItems)
    }

    override suspend fun getFollowingFeed(userId: String, limit: Int): List<FeedItem> {
        return feedAggregationLocalDataSource.getFollowingFeed(userId, limit)
    }

    override suspend fun saveFollowingFeed(userId: String, feedItems: List<FeedItem>) {
        feedAggregationLocalDataSource.saveFollowingFeed(userId, feedItems)
    }

    override suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> {
        return feedAggregationLocalDataSource.getDiscoverFeed(userId, limit)
    }

    override suspend fun saveDiscoverFeed(userId: String, feedItems: List<FeedItem>) {
        feedAggregationLocalDataSource.saveDiscoverFeed(userId, feedItems)
    }

    override suspend fun getTrendingPosts(limit: Int): List<PostListItem> {
        return feedAggregationLocalDataSource.getTrendingPosts(limit)
    }

    override suspend fun saveTrendingPosts(posts: List<PostListItem>) {
        feedAggregationLocalDataSource.saveTrendingPosts(posts)
    }

    override suspend fun getPopularPosts(limit: Int): List<PostListItem> {
        return feedAggregationLocalDataSource.getPopularPosts(limit)
    }

    override suspend fun savePopularPosts(posts: List<PostListItem>) {
        feedAggregationLocalDataSource.savePopularPosts(posts)
    }

    override suspend fun isUserFeedCached(userId: String): Boolean {
        return feedAggregationLocalDataSource.isUserFeedCached(userId)
    }

    // Post interaction operations - Delegate to PostInteractionLocalDataSource
    override suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> {
        return postInteractionLocalDataSource.getPostLikes(postId, limit)
    }

    override suspend fun savePostLikes(postId: String, likes: List<PostLike>) {
        postInteractionLocalDataSource.savePostLikes(postId, likes)
    }

    override suspend fun getPostShares(postId: String, limit: Int): List<PostShare> {
        return postInteractionLocalDataSource.getPostShares(postId, limit)
    }

    override suspend fun savePostShares(postId: String, shares: List<PostShare>) {
        postInteractionLocalDataSource.savePostShares(postId, shares)
    }

    override suspend fun isPostLikedByUser(userId: String, postId: String): Boolean {
        return postInteractionLocalDataSource.isPostLikedByUser(userId, postId)
    }

    override suspend fun updatePostLikeStatus(userId: String, postId: String, isLiked: Boolean) {
        postInteractionLocalDataSource.updatePostLikeStatus(userId, postId, isLiked)

        // Update feeds with like status change
        feedAggregationLocalDataSource.updatePostInFeeds(postId) { feedItem ->
            feedItem.copy(isLikedByCurrentUser = isLiked)
        }
    }

    override suspend fun isPostSharedByUser(userId: String, postId: String): Boolean {
        return postInteractionLocalDataSource.isPostSharedByUser(userId, postId)
    }

    override suspend fun updatePostShareStatus(userId: String, postId: String, isShared: Boolean) {
        postInteractionLocalDataSource.updatePostShareStatus(userId, postId, isShared)

        // Update feeds with share status change
        feedAggregationLocalDataSource.updatePostInFeeds(postId) { feedItem ->
            feedItem.copy(isSharedByCurrentUser = isShared)
        }
    }

    override suspend fun isPostSavedByUser(userId: String, postId: String): Boolean {
        return postInteractionLocalDataSource.isPostSavedByUser(userId, postId)
    }

    override suspend fun updatePostSaveStatus(userId: String, postId: String, isSaved: Boolean) {
        postInteractionLocalDataSource.updatePostSaveStatus(userId, postId, isSaved)
    }

    override suspend fun getUserLikedPosts(userId: String, limit: Int): List<PostListItem> {
        return postInteractionLocalDataSource.getUserLikedPosts(userId, limit)
    }

    override suspend fun getUserSharedPosts(userId: String, limit: Int): List<PostListItem> {
        return postInteractionLocalDataSource.getUserSharedPosts(userId, limit)
    }

    override suspend fun getUserSavedPosts(userId: String, limit: Int): List<PostListItem> {
        return postInteractionLocalDataSource.getUserSavedPosts(userId, limit)
    }

    // Reference operations - Delegate to PostReferenceLocalDataSource
    override suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> {
        return postReferenceLocalDataSource.getPostRecipeReferences(postId)
    }

    override suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>) {
        postReferenceLocalDataSource.savePostRecipeReferences(postId, references)
    }

    override suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> {
        return postReferenceLocalDataSource.getPostCookbookReferences(postId)
    }

    override suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>) {
        postReferenceLocalDataSource.savePostCookbookReferences(postId, references)
    }

    // Analytics operations - Delegate to PostAnalyticsLocalDataSource
    override suspend fun getPostAnalytics(postId: String): Map<String, Any>? {
        return postAnalyticsLocalDataSource.getPostAnalytics(postId)
    }

    override suspend fun savePostAnalytics(postId: String, analytics: Map<String, Any>) {
        postAnalyticsLocalDataSource.savePostAnalytics(postId, analytics)
    }

    override suspend fun getUserPostAnalytics(userId: String): Map<String, Any>? {
        return postAnalyticsLocalDataSource.getUserPostAnalytics(userId)
    }

    override suspend fun saveUserPostAnalytics(userId: String, analytics: Map<String, Any>) {
        postAnalyticsLocalDataSource.saveUserPostAnalytics(userId, analytics)
    }

    // Hashtag operations - Delegate to FeedAggregationLocalDataSource
    override suspend fun getTrendingHashtags(limit: Int): List<String> {
        return feedAggregationLocalDataSource.getTrendingHashtags(limit)
    }

    override suspend fun saveTrendingHashtags(hashtags: List<String>) {
        feedAggregationLocalDataSource.saveTrendingHashtags(hashtags)
    }

    // User following operations - Delegate to FeedAggregationLocalDataSource
    override suspend fun getUserFollowing(userId: String): List<String> {
        return feedAggregationLocalDataSource.getUserFollowing(userId)
    }

    override suspend fun saveUserFollowing(userId: String, followingIds: List<String>) {
        feedAggregationLocalDataSource.saveUserFollowing(userId, followingIds)
    }

    // Cache management operations - Coordinate across all data sources
    override suspend fun clearUserCache(userId: String) {
        postLocalDataSource.clearUserPostsCache(userId)
        postInteractionLocalDataSource.clearUserInteractionCache(userId)
        commentLocalDataSource.clearUserCommentCache(userId)
        feedAggregationLocalDataSource.clearUserFeedCache(userId)
        postAnalyticsLocalDataSource.clearUserAnalyticsCache(userId)
    }

    override suspend fun clearAllCache() {
        postLocalDataSource.clearAllPostsCache()
        postInteractionLocalDataSource.clearAllInteractionCache()
        commentLocalDataSource.clearAllCommentCache()
        feedAggregationLocalDataSource.clearAllFeedCache()
        postAnalyticsLocalDataSource.clearAllAnalyticsCache()
        postReferenceLocalDataSource.clearAllReferencesCache()
    }

    override suspend fun getCacheSize(): Map<String, Int> {
        val postsCacheSize = postLocalDataSource.getPostsCacheSize()
        val interactionCacheSize = postInteractionLocalDataSource.getInteractionCacheSize()
        val commentCacheSize = commentLocalDataSource.getCommentCacheSize()
        val feedCacheSize = feedAggregationLocalDataSource.getFeedCacheSize()
        val analyticsCacheSize = postAnalyticsLocalDataSource.getAnalyticsCacheSize()
        val referencesCacheSize = postReferenceLocalDataSource.getReferencesCacheSize()

        return buildMap {
            putAll(postsCacheSize)
            putAll(interactionCacheSize)
            putAll(commentCacheSize)
            putAll(feedCacheSize)
            putAll(analyticsCacheSize)
            putAll(referencesCacheSize)
        }
    }

    override suspend fun getLastCacheUpdate(): Long {
        // Return the most recent cache update time across all data sources
        return maxOf(
            postLocalDataSource.getLastCacheUpdate(),
            feedAggregationLocalDataSource.getLastCacheUpdate(),
            postAnalyticsLocalDataSource.getLastCacheUpdate(),
            postReferenceLocalDataSource.getLastCacheUpdate()
        )
    }
}