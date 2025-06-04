
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class GetCommentRepliesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(commentId: String, limit: Int = 20) = 
        feedRepository.getCommentReplies(commentId, limit)
}
