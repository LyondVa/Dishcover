
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.LikeType
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class LikeCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(
        userId: String, 
        commentId: String, 
        likeType: LikeType = LikeType.LIKE
    ) = feedRepository.likeComment(userId, commentId, likeType)
}
