
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val parentCommentId: String? = null, // For threaded comments
    val content: String = "",
    val imageUrl: String? = null,
    val taggedUsers: List<String> = emptyList(),
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
)
