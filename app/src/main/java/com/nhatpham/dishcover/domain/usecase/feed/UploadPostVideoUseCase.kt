
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class UploadPostVideoUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(postId: String, videoData: ByteArray) = 
        feedRepository.uploadPostVideo(postId, videoData)
}
