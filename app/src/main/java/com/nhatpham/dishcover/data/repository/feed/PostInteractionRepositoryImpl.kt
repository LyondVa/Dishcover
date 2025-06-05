// PostInteractionRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.repository.feed.PostInteractionRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class PostInteractionRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : PostInteractionRepository {

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
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user saved posts")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun reportPost(userId: String, postId: String, reason: String, description: String?): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            emit(Resource.Success(true)) // Simple success for now
        } catch (e: Exception) {
            Timber.e(e, "Error reporting post")
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
}