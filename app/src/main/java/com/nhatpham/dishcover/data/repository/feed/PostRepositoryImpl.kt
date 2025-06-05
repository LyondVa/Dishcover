// PostRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.feed.PostRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : PostRepository {

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