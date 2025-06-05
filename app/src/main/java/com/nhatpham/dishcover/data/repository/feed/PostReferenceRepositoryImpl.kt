// PostReferenceRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.domain.repository.feed.PostReferenceRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class PostReferenceRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource,
    private val feedLocalDataSource: FeedLocalDataSource
) : PostReferenceRepository {

    override fun addRecipeReference(reference: PostRecipeReference): Flow<Resource<PostRecipeReference>> = flow {
        try {
            emit(Resource.Loading())

            val addedReference = feedRemoteDataSource.addRecipeReference(reference)
            if (addedReference != null) {
                // Update cache
                val currentRefs = feedLocalDataSource.getPostRecipeReferences(reference.postId).toMutableList()
                currentRefs.add(addedReference)
                feedLocalDataSource.savePostRecipeReferences(reference.postId, currentRefs)

                emit(Resource.Success(addedReference))
            } else {
                emit(Resource.Error("Failed to add recipe reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun addCookbookReference(reference: PostCookbookReference): Flow<Resource<PostCookbookReference>> = flow {
        try {
            emit(Resource.Loading())

            val addedReference = feedRemoteDataSource.addCookbookReference(reference)
            if (addedReference != null) {
                // Update cache
                val currentRefs = feedLocalDataSource.getPostCookbookReferences(reference.postId).toMutableList()
                currentRefs.add(addedReference)
                feedLocalDataSource.savePostCookbookReferences(reference.postId, currentRefs)

                emit(Resource.Success(addedReference))
            } else {
                emit(Resource.Error("Failed to add cookbook reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding cookbook reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeRecipeReference(referenceId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.removeRecipeReference(referenceId)
            if (success) {
                // Note: We'd need the postId to update cache properly
                // In a real implementation, you might want to pass postId as well
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove recipe reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun removeCookbookReference(referenceId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.removeCookbookReference(referenceId)
            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to remove cookbook reference"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error removing cookbook reference")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostRecipeReferences(postId: String): Flow<Resource<List<PostRecipeReference>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedRefs = feedLocalDataSource.getPostRecipeReferences(postId)
            if (cachedRefs.isNotEmpty()) {
                emit(Resource.Success(cachedRefs))
            }

            // Fetch from remote
            val remoteRefs = feedRemoteDataSource.getPostRecipeReferences(postId)
            if (remoteRefs.isNotEmpty()) {
                feedLocalDataSource.savePostRecipeReferences(postId, remoteRefs)
                emit(Resource.Success(remoteRefs))
            } else if (cachedRefs.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post recipe references")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getPostCookbookReferences(postId: String): Flow<Resource<List<PostCookbookReference>>> = flow {
        try {
            emit(Resource.Loading())

            // Try cache first
            val cachedRefs = feedLocalDataSource.getPostCookbookReferences(postId)
            if (cachedRefs.isNotEmpty()) {
                emit(Resource.Success(cachedRefs))
            }

            // Fetch from remote
            val remoteRefs = feedRemoteDataSource.getPostCookbookReferences(postId)
            if (remoteRefs.isNotEmpty()) {
                feedLocalDataSource.savePostCookbookReferences(postId, remoteRefs)
                emit(Resource.Success(remoteRefs))
            } else if (cachedRefs.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post cookbook references")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}