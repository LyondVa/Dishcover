
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class GetPopularPostsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(limit: Int = 20) = feedRepository.getPopularPosts(limit)
}
