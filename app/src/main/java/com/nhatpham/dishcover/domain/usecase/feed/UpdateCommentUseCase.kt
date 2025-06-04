
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.Comment
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class UpdateCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(comment: Comment) = feedRepository.updateComment(comment)
}
