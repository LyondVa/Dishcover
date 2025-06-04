
package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class PostLikeDto(
    val likeId: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val likeType: String? = null,
    val createdAt: Timestamp? = null
)
