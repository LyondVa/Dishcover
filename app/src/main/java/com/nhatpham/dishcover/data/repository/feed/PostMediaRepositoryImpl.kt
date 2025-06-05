// PostMediaRepositoryImpl.kt
package com.nhatpham.dishcover.data.repository.feed

import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.repository.feed.PostMediaRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class PostMediaRepositoryImpl @Inject constructor(
    private val feedRemoteDataSource: FeedRemoteDataSource
) : PostMediaRepository {

    override fun uploadPostImage(postId: String, imageData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val imageUrl = feedRemoteDataSource.uploadPostImage(postId, imageData)
            if (imageUrl != null) {
                emit(Resource.Success(imageUrl))
            } else {
                emit(Resource.Error("Failed to upload image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post image")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun uploadPostVideo(postId: String, videoData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val videoUrl = feedRemoteDataSource.uploadPostVideo(postId, videoData)
            if (videoUrl != null) {
                emit(Resource.Success(videoUrl))
            } else {
                emit(Resource.Error("Failed to upload video"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post video")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deletePostMedia(mediaUrl: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = feedRemoteDataSource.deletePostMedia(mediaUrl)
            emit(Resource.Success(success))
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post media")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}