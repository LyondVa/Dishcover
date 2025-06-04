
package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class PostShareDto(
    val shareId: String? = null,
    val originalPostId: String? = null,
    val sharedByUserId: String? = null,
    val shareMessage: String? = null,
    val shareType: String? = null,
    val createdAt: Timestamp? = null
)
