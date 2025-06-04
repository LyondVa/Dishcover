
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class PostListItem(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String? = null,
    val content: String = "",
    val firstImageUrl: String? = null,
    val postType: PostType = PostType.TEXT,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val isSharedByCurrentUser: Boolean = false,
    val hasRecipeReferences: Boolean = false,
    val hasCookbookReferences: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val isFollowingAuthor: Boolean = false
)
