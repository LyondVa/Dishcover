// FeedLocalDataSource.kt - Interface
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.feed.*

interface FeedLocalDataSource {
    // Post operations
    suspend fun savePost(post: Post)
    suspend fun getPostById(postId: String): Post?
    suspend fun deletePost(postId: String)
    suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem>
    suspend fun saveUserPosts(userId: String, posts: List<PostListItem>)
    suspend fun addPostToUserLists(userId: String, post: PostListItem)
    suspend fun updatePostInUserLists(userId: String, updatedPost: PostListItem)
    suspend fun removePostFromUserLists(userId: String, postId: String)
    suspend fun getSearchResults(query: String, limit: Int): List<PostListItem>
    suspend fun saveSearchResults(query: String, posts: List<PostListItem>)
    suspend fun getMultiplePosts(postIds: List<String>): List<Post>
    suspend fun isPostCached(postId: String): Boolean

    // Comment operations
    suspend fun saveComment(comment: Comment)
    suspend fun getCommentById(commentId: String): Comment?
    suspend fun deleteComment(commentId: String)
    suspend fun getPostComments(postId: String, limit: Int): List<Comment>
    suspend fun savePostComments(postId: String, comments: List<Comment>)
    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment>
    suspend fun saveCommentReplies(commentId: String, replies: List<Comment>)
    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike>
    suspend fun saveCommentLikes(commentId: String, likes: List<CommentLike>)
    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean
    suspend fun updateCommentLikeStatus(userId: String, commentId: String, isLiked: Boolean)
    suspend fun getMultipleComments(commentIds: List<String>): List<Comment>
    suspend fun isCommentCached(commentId: String): Boolean

    // Feed operations
    suspend fun getUserFeed(userId: String, limit: Int): List<FeedItem>
    suspend fun saveUserFeed(userId: String, feedItems: List<FeedItem>)
    suspend fun getFollowingFeed(userId: String, limit: Int): List<FeedItem>
    suspend fun saveFollowingFeed(userId: String, feedItems: List<FeedItem>)
    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem>
    suspend fun saveDiscoverFeed(userId: String, feedItems: List<FeedItem>)
    suspend fun getTrendingPosts(limit: Int): List<PostListItem>
    suspend fun saveTrendingPosts(posts: List<PostListItem>)
    suspend fun getPopularPosts(limit: Int): List<PostListItem>
    suspend fun savePopularPosts(posts: List<PostListItem>)
    suspend fun isUserFeedCached(userId: String): Boolean

    // Post interaction operations
    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike>
    suspend fun savePostLikes(postId: String, likes: List<PostLike>)
    suspend fun getPostShares(postId: String, limit: Int): List<PostShare>
    suspend fun savePostShares(postId: String, shares: List<PostShare>)
    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean
    suspend fun updatePostLikeStatus(userId: String, postId: String, isLiked: Boolean)
    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean
    suspend fun updatePostShareStatus(userId: String, postId: String, isShared: Boolean)
    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean
    suspend fun updatePostSaveStatus(userId: String, postId: String, isSaved: Boolean)
    suspend fun getUserLikedPosts(userId: String, limit: Int): List<PostListItem>
    suspend fun getUserSharedPosts(userId: String, limit: Int): List<PostListItem>
    suspend fun getUserSavedPosts(userId: String, limit: Int): List<PostListItem>

    // Reference operations
    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference>
    suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>)
    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference>
    suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>)

    // Analytics operations
    suspend fun getPostAnalytics(postId: String): Map<String, Any>?
    suspend fun savePostAnalytics(postId: String, analytics: Map<String, Any>)
    suspend fun getUserPostAnalytics(userId: String): Map<String, Any>?
    suspend fun saveUserPostAnalytics(userId: String, analytics: Map<String, Any>)

    // Hashtag operations
    suspend fun getTrendingHashtags(limit: Int): List<String>
    suspend fun saveTrendingHashtags(hashtags: List<String>)

    // User following operations
    suspend fun getUserFollowing(userId: String): List<String>
    suspend fun saveUserFollowing(userId: String, followingIds: List<String>)

    // Cache management operations
    suspend fun clearUserCache(userId: String)
    suspend fun clearAllCache()
    suspend fun getCacheSize(): Map<String, Int>
    suspend fun getLastCacheUpdate(): Long
}