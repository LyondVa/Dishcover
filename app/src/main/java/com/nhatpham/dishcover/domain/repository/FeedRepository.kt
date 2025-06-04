// FeedRepository.kt
package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.*
import com.nhatpham.dishcover.domain.model.feed.Comment
import com.nhatpham.dishcover.domain.model.feed.CommentLike
import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.LikeType
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostActivity
import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.model.feed.PostLike
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.domain.model.feed.PostShare
import com.nhatpham.dishcover.domain.model.feed.ShareType
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    // Post CRUD Operations
    fun createPost(post: Post): Flow<Resource<Post>>
    fun updatePost(post: Post): Flow<Resource<Post>>
    fun deletePost(postId: String): Flow<Resource<Boolean>>
    fun getPost(postId: String): Flow<Resource<Post>>
    fun getUserPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>

    // Feed Operations
    fun getUserFeed(
        userId: String,
        limit: Int = 20,
        lastPostId: String? = null
    ): Flow<Resource<List<FeedItem>>>

    fun getFollowingFeed(
        userId: String,
        limit: Int = 20,
        lastPostId: String? = null
    ): Flow<Resource<List<FeedItem>>>

    fun getTrendingPosts(
        limit: Int = 20,
        timeRange: String = "24h"
    ): Flow<Resource<List<PostListItem>>>

    fun getPopularPosts(limit: Int = 20): Flow<Resource<List<PostListItem>>>

    fun getDiscoverFeed(
        userId: String,
        limit: Int = 20
    ): Flow<Resource<List<FeedItem>>>

    fun searchPosts(
        query: String,
        userId: String? = null,
        limit: Int = 20
    ): Flow<Resource<List<PostListItem>>>

    // Post Interaction Operations
    fun likePost(
        userId: String,
        postId: String,
        likeType: LikeType = LikeType.LIKE
    ): Flow<Resource<Boolean>>

    fun unlikePost(userId: String, postId: String): Flow<Resource<Boolean>>

    fun sharePost(
        userId: String,
        postId: String,
        shareMessage: String? = null,
        shareType: ShareType = ShareType.REPOST
    ): Flow<Resource<PostShare>>

    fun unsharePost(userId: String, postId: String): Flow<Resource<Boolean>>

    fun getPostLikes(postId: String, limit: Int = 50): Flow<Resource<List<PostLike>>>
    fun getPostShares(postId: String, limit: Int = 50): Flow<Resource<List<PostShare>>>

    fun isPostLikedByUser(userId: String, postId: String): Flow<Resource<Boolean>>
    fun isPostSharedByUser(userId: String, postId: String): Flow<Resource<Boolean>>

    // Comment Operations
    fun addComment(comment: Comment): Flow<Resource<Comment>>
    fun updateComment(comment: Comment): Flow<Resource<Comment>>
    fun deleteComment(commentId: String): Flow<Resource<Boolean>>

    fun getPostComments(
        postId: String,
        limit: Int = 50,
        lastCommentId: String? = null
    ): Flow<Resource<List<Comment>>>

    fun getCommentReplies(
        commentId: String,
        limit: Int = 20
    ): Flow<Resource<List<Comment>>>

    fun getComment(commentId: String): Flow<Resource<Comment>>

    // Comment Interaction Operations
    fun likeComment(
        userId: String,
        commentId: String,
        likeType: LikeType = LikeType.LIKE
    ): Flow<Resource<Boolean>>

    fun unlikeComment(userId: String, commentId: String): Flow<Resource<Boolean>>

    fun getCommentLikes(commentId: String, limit: Int = 20): Flow<Resource<List<CommentLike>>>
    fun isCommentLikedByUser(userId: String, commentId: String): Flow<Resource<Boolean>>

    // Recipe and Cookbook Reference Operations
    fun addRecipeReference(reference: PostRecipeReference): Flow<Resource<PostRecipeReference>>
    fun addCookbookReference(reference: PostCookbookReference): Flow<Resource<PostCookbookReference>>
    fun removeRecipeReference(referenceId: String): Flow<Resource<Boolean>>
    fun removeCookbookReference(referenceId: String): Flow<Resource<Boolean>>

    fun getPostRecipeReferences(postId: String): Flow<Resource<List<PostRecipeReference>>>
    fun getPostCookbookReferences(postId: String): Flow<Resource<List<PostCookbookReference>>>

    // Media Operations
    fun uploadPostImage(postId: String, imageData: ByteArray): Flow<Resource<String>>
    fun uploadPostVideo(postId: String, videoData: ByteArray): Flow<Resource<String>>
    fun deletePostMedia(mediaUrl: String): Flow<Resource<Boolean>>

    // Activity Tracking and Analytics
    fun trackPostActivity(activity: PostActivity): Flow<Resource<Boolean>>
    fun getPostAnalytics(postId: String): Flow<Resource<Map<String, Any>>>
    fun getUserPostAnalytics(userId: String, dateRange: String): Flow<Resource<Map<String, Any>>>

    // Hashtag Operations
    fun getPostsByHashtag(hashtag: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getTrendingHashtags(limit: Int = 10): Flow<Resource<List<String>>>
    fun searchHashtags(query: String, limit: Int = 10): Flow<Resource<List<String>>>

    // User-specific Operations
    fun getUserLikedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getUserSharedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getUserSavedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>

    fun savePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun unsavePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun isPostSavedByUser(userId: String, postId: String): Flow<Resource<Boolean>>

    // Moderation Operations
    fun reportPost(
        userId: String,
        postId: String,
        reason: String,
        description: String? = null
    ): Flow<Resource<Boolean>>

    fun reportComment(
        userId: String,
        commentId: String,
        reason: String,
        description: String? = null
    ): Flow<Resource<Boolean>>

    fun blockUserFromPost(postId: String, blockedUserId: String): Flow<Resource<Boolean>>
    fun hidePost(userId: String, postId: String): Flow<Resource<Boolean>>

    // Post Settings and Visibility
    fun updatePostVisibility(postId: String, isPublic: Boolean): Flow<Resource<Boolean>>
    fun updateCommentSettings(postId: String, allowComments: Boolean): Flow<Resource<Boolean>>
    fun updateShareSettings(postId: String, allowShares: Boolean): Flow<Resource<Boolean>>

    fun archivePost(postId: String): Flow<Resource<Boolean>>
    fun unarchivePost(postId: String): Flow<Resource<Boolean>>
    fun pinPost(postId: String): Flow<Resource<Boolean>>
    fun unpinPost(postId: String): Flow<Resource<Boolean>>

    // Feed Algorithm and Personalization
    fun updateFeedPreferences(
        userId: String,
        preferences: Map<String, Any>
    ): Flow<Resource<Boolean>>

    fun getFeedPreferences(userId: String): Flow<Resource<Map<String, Any>>>

    // Batch Operations (for efficiency)
    fun getMultiplePosts(postIds: List<String>): Flow<Resource<List<Post>>>
    fun getMultipleComments(commentIds: List<String>): Flow<Resource<List<Comment>>>

    fun markPostsAsViewed(userId: String, postIds: List<String>): Flow<Resource<Boolean>>
    fun getUnreadPostCount(userId: String): Flow<Resource<Int>>

    // Cache Management
    fun refreshUserFeed(userId: String): Flow<Resource<Boolean>>
    fun clearFeedCache(userId: String): Flow<Resource<Boolean>>
}