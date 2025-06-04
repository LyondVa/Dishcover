
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class DeletePostMediaUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(mediaUrl: String) = 
        feedRepository.deletePostMedia(mediaUrl)
}
