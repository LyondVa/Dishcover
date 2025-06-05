package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val postType: PostType = PostType.TEXT,
    val recipeReferences: List<PostRecipeReference> = emptyList(),
    val cookbookReferences: List<PostCookbookReference> = emptyList(),
    val taggedUsers: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val location: String? = null,
    val isPublic: Boolean = true,
    val allowComments: Boolean = true,
    val allowShares: Boolean = true,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)

enum class PostType {
    TEXT,
    IMAGE,
    VIDEO,
    RECIPE_SHARE,
    COOKBOOK_SHARE,
    COOKING_PROGRESS,
    REVIEW
}