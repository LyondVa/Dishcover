// FeedRemoteDataSource.kt - Now an interface
package com.nhatpham.dishcover.data.source.remote

import com.nhatpham.dishcover.domain.model.feed.*

interface FeedRemoteDataSource {
    // Post CRUD Operations
    suspend fun createPost(post: Post): Post?
    suspend fun updatePost(post: Post): Post?
    suspend fun deletePost(postId: String): Boolean
    suspend fun getPostById(postId: String): Post?
    suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem>

    // Feed Operations
    suspend fun getUserFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem>
    suspend fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem>
    suspend fun getTrendingPosts(limit: Int, timeRange: String): List<PostListItem>
    suspend fun getPopularPosts(limit: Int): List<PostListItem>
    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem>
    suspend fun searchPosts(query: String, userId: String?, limit: Int): List<PostListItem>

    // Post Interaction Operations
    suspend fun likePost(userId: String, postId: String, likeType: LikeType): Boolean
    suspend fun unlikePost(userId: String, postId: String): Boolean
    suspend fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): PostShare?
    suspend fun unsharePost(userId: String, postId: String): Boolean
    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike>
    suspend fun getPostShares(postId: String, limit: Int): List<PostShare>
    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean
    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean

    // Comment Operations
    suspend fun addComment(comment: Comment): Comment?
    suspend fun updateComment(comment: Comment): Comment?
    suspend fun deleteComment(commentId: String): Boolean
    suspend fun getPostComments(postId: String, limit: Int, lastCommentId: String?): List<Comment>
    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment>
    suspend fun getComment(commentId: String): Comment?

    // Comment Interaction Operations
    suspend fun likeComment(userId: String, commentId: String, likeType: LikeType): Boolean
    suspend fun unlikeComment(userId: String, commentId: String): Boolean
    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike>
    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean

    // Recipe and Cookbook Reference Operations
    suspend fun addRecipeReference(reference: PostRecipeReference): PostRecipeReference?
    suspend fun addCookbookReference(reference: PostCookbookReference): PostCookbookReference?
    suspend fun removeRecipeReference(referenceId: String): Boolean
    suspend fun removeCookbookReference(referenceId: String): Boolean
    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference>
    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference>

    // Media Operations
    suspend fun uploadPostImage(postId: String, imageData: ByteArray): String?
    suspend fun uploadPostVideo(postId: String, videoData: ByteArray): String?
    suspend fun deletePostMedia(mediaUrl: String): Boolean

    // Activity Tracking and Analytics
    suspend fun trackPostActivity(activity: PostActivity): Boolean
    suspend fun getPostAnalytics(postId: String): Map<String, Any>
    suspend fun getUserPostAnalytics(userId: String, dateRange: String): Map<String, Any>

    // Hashtag Operations
    suspend fun getTrendingHashtags(limit: Int): List<String>

    // User Operations (Simple)
    suspend fun savePost(userId: String, postId: String): Boolean
    suspend fun unsavePost(userId: String, postId: String): Boolean
    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean
}