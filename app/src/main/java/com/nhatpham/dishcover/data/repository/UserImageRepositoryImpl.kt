package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.remote.UserImageRemoteDataSource
import com.nhatpham.dishcover.domain.repository.UserImageRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class UserImageRepositoryImpl @Inject constructor(
    private val userImageRemoteDataSource: UserImageRemoteDataSource
) : UserImageRepository {

    override fun uploadProfileImage(userId: String, imageData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val imageUrl = userImageRemoteDataSource.uploadProfileImage(userId, imageData)
            if (imageUrl != null) {
                emit(Resource.Success(imageUrl))
            } else {
                emit(Resource.Error("Failed to upload profile image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading profile image for user: $userId")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun uploadBannerImage(userId: String, imageData: ByteArray): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val imageUrl = userImageRemoteDataSource.uploadBannerImage(userId, imageData)
            if (imageUrl != null) {
                emit(Resource.Success(imageUrl))
            } else {
                emit(Resource.Error("Failed to upload banner image"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading banner image for user: $userId")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun deleteUserImage(imageUrl: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val success = userImageRemoteDataSource.deleteUserImage(imageUrl)
            emit(Resource.Success(success))
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user image: $imageUrl")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}