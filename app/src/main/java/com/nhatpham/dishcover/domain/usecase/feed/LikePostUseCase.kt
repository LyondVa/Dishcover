
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.LikeType
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(
        userId: String, 
        postId: String, 
        likeType: LikeType = LikeType.LIKE
    ) = feedRepository.likePost(userId, postId, likeType)
}
