package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostMediaRepository {
    fun uploadPostImage(postId: String, imageData: ByteArray): Flow<Resource<String>>
    fun uploadPostVideo(postId: String, videoData: ByteArray): Flow<Resource<String>>
    fun deletePostMedia(mediaUrl: String): Flow<Resource<Boolean>>
}