package com.nhatpham.dishcover.data.model.dto.feed

import com.google.firebase.Timestamp

data class CommentLikeDto(
    val likeId: String? = null,
    val commentId: String? = null,
    val userId: String? = null,
    val likeType: String? = null,
    val createdAt: Timestamp? = null
)
