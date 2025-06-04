
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class GetPostLikesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(postId: String, limit: Int = 50) = 
        feedRepository.getPostLikes(postId, limit)
}
