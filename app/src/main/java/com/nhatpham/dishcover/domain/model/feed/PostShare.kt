
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class PostShare(
    val shareId: String = "",
    val originalPostId: String = "",
    val sharedByUserId: String = "",
    val shareMessage: String? = null,
    val shareType: ShareType = ShareType.REPOST,
    val createdAt: Timestamp = Timestamp.now()
)

enum class ShareType {
    REPOST, // Share to own feed
    STORY, // Share to story
    DIRECT_MESSAGE, // Share via DM
    EXTERNAL // Share outside app
}
