package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.repository.UserImageRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadProfileImageUseCase @Inject constructor(
    private val userImageRepository: UserImageRepository
) {
    operator fun invoke(userId: String, imageData: ByteArray): Flow<Resource<String>> {
        return userImageRepository.uploadProfileImage(userId, imageData)
    }
}

class UploadBannerImageUseCase @Inject constructor(
    private val userImageRepository: UserImageRepository
) {
    operator fun invoke(userId: String, imageData: ByteArray): Flow<Resource<String>> {
        return userImageRepository.uploadBannerImage(userId, imageData)
    }
}

class DeleteUserImageUseCase @Inject constructor(
    private val userImageRepository: UserImageRepository
) {
    operator fun invoke(imageUrl: String): Flow<Resource<Boolean>> {
        return userImageRepository.deleteUserImage(imageUrl)
    }
}