
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class CommentLike(
    val likeId: String = "",
    val commentId: String = "",
    val userId: String = "",
    val likeType: LikeType = LikeType.LIKE,
    val createdAt: Timestamp = Timestamp.now()
)
