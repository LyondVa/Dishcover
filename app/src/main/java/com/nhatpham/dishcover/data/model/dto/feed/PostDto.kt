package com.nhatpham.dishcover.data.model.dto.feed

import com.google.firebase.Timestamp

data class PostDto(
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null,
    val videoUrl: String? = null,
    val postType: String? = null,
    val taggedUsers: List<String>? = null,
    val hashtags: List<String>? = null,
    val location: String? = null,
    val public: Boolean? = null, // Firestore stores as 'public', not 'isPublic'
    val allowComments: Boolean? = null,
    val allowShares: Boolean? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val shareCount: Int? = null,
    val viewCount: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val edited: Boolean? = null, // Firestore stores as 'edited', not 'isEdited'
    val pinned: Boolean? = null, // Firestore stores as 'pinned', not 'isPinned'
    val archived: Boolean? = null // Firestore stores as 'archived', not 'isArchived'
)