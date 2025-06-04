
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class UploadPostImageUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(postId: String, imageData: ByteArray) = 
        feedRepository.uploadPostImage(postId, imageData)
}
