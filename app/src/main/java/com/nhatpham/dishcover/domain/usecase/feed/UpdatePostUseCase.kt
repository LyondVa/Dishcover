
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class UpdatePostUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(post: Post) = feedRepository.updatePost(post)
}
