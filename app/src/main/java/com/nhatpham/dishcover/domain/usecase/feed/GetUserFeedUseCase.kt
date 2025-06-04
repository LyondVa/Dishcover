
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class GetUserFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(userId: String, limit: Int = 20, lastPostId: String? = null) = 
        feedRepository.getUserFeed(userId, limit, lastPostId)
}
