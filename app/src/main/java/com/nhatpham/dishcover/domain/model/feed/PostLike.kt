
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class PostLike(
    val likeId: String = "",
    val postId: String = "",
    val userId: String = "",
    val likeType: LikeType = LikeType.LIKE,
    val createdAt: Timestamp = Timestamp.now()
)

enum class LikeType {
    LIKE,
    LOVE,
    LAUGH,
    WOW,
    SAD,
    ANGRY
}
