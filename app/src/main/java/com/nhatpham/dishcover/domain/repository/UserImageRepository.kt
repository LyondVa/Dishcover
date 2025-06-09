package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserImageRepository {
    fun uploadProfileImage(userId: String, imageData: ByteArray): Flow<Resource<String>>
    fun uploadBannerImage(userId: String, imageData: ByteArray): Flow<Resource<String>>
    fun deleteUserImage(imageUrl: String): Flow<Resource<Boolean>>
}