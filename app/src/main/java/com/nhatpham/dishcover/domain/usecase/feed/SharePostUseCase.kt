
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.ShareType
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class SharePostUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(
        userId: String, 
        postId: String, 
        shareMessage: String? = null,
        shareType: ShareType = ShareType.REPOST
    ) = feedRepository.sharePost(userId, postId, shareMessage, shareType)
}
