package com.nhatpham.dishcover.data.model.dto.feed

import com.google.firebase.Timestamp

data class PostActivityDto(
    val activityId: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val activityType: String? = null,
    val metadata: Map<String, String>? = null,
    val createdAt: Timestamp? = null
)
