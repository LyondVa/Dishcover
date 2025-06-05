package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostInteractionRepository {
    // Like Operations
    fun likePost(userId: String, postId: String, likeType: LikeType = LikeType.LIKE): Flow<Resource<Boolean>>
    fun unlikePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun getPostLikes(postId: String, limit: Int = 50): Flow<Resource<List<PostLike>>>
    fun isPostLikedByUser(userId: String, postId: String): Flow<Resource<Boolean>>

    // Share Operations
    fun sharePost(userId: String, postId: String, shareMessage: String? = null, shareType: ShareType = ShareType.REPOST): Flow<Resource<PostShare>>
    fun unsharePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun getPostShares(postId: String, limit: Int = 50): Flow<Resource<List<PostShare>>>
    fun isPostSharedByUser(userId: String, postId: String): Flow<Resource<Boolean>>

    // Save Operations
    fun savePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun unsavePost(userId: String, postId: String): Flow<Resource<Boolean>>
    fun isPostSavedByUser(userId: String, postId: String): Flow<Resource<Boolean>>

    // User-specific Operations
    fun getUserLikedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getUserSharedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getUserSavedPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>

    // Moderation Operations
    fun reportPost(userId: String, postId: String, reason: String, description: String? = null): Flow<Resource<Boolean>>
    fun blockUserFromPost(postId: String, blockedUserId: String): Flow<Resource<Boolean>>
    fun hidePost(userId: String, postId: String): Flow<Resource<Boolean>>
}