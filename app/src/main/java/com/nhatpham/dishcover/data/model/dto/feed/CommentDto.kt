package com.nhatpham.dishcover.data.model.dto.feed

import com.google.firebase.Timestamp

data class CommentDto(
    val commentId: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val parentCommentId: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val taggedUsers: List<String>? = null,
    val likeCount: Int? = null,
    val replyCount: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val edited: Boolean? = null, // Firestore stores as 'edited', not 'isEdited'
    val deleted: Boolean? = null // Firestore stores as 'deleted', not 'isDeleted'
)