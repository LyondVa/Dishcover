
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class GetTrendingPostsUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(limit: Int = 20, timeRange: String = "24h") = 
        feedRepository.getTrendingPosts(limit, timeRange)
}
