
package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class CommentDto(
    val commentId: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val parentCommentId: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val taggedUsers: List<String>? = null,
    val likeCount: Int? = null,
    val replyCount: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val isEdited: Boolean? = null,
    val isDeleted: Boolean? = null
)
