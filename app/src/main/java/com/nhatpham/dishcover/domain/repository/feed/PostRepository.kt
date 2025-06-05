package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun createPost(post: Post): Flow<Resource<Post>>
    fun updatePost(post: Post): Flow<Resource<Post>>
    fun deletePost(postId: String): Flow<Resource<Boolean>>
    fun getPost(postId: String): Flow<Resource<Post>>
    fun getUserPosts(userId: String, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun searchPosts(query: String, userId: String? = null, limit: Int = 20): Flow<Resource<List<PostListItem>>>
    fun getMultiplePosts(postIds: List<String>): Flow<Resource<List<Post>>>

    // Post Settings
    fun updatePostVisibility(postId: String, isPublic: Boolean): Flow<Resource<Boolean>>
    fun updateCommentSettings(postId: String, allowComments: Boolean): Flow<Resource<Boolean>>
    fun updateShareSettings(postId: String, allowShares: Boolean): Flow<Resource<Boolean>>
    fun archivePost(postId: String): Flow<Resource<Boolean>>
    fun unarchivePost(postId: String): Flow<Resource<Boolean>>
    fun pinPost(postId: String): Flow<Resource<Boolean>>
    fun unpinPost(postId: String): Flow<Resource<Boolean>>
}
