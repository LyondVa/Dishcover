
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class UnlikePostUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(userId: String, postId: String) = 
        feedRepository.unlikePost(userId, postId)
}
