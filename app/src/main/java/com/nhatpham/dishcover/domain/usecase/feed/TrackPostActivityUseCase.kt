
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.PostActivity
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class TrackPostActivityUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(activity: PostActivity) = 
        feedRepository.trackPostActivity(activity)
}
