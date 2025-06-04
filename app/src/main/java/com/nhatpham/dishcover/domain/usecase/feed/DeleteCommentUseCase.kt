
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(commentId: String) = feedRepository.deleteComment(commentId)
}
