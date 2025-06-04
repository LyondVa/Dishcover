
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.user.User

data class FeedItem(
    val feedItemId: String = "",
    val itemType: FeedItemType = FeedItemType.POST,
    val post: Post? = null,
    val sharedPost: PostShare? = null,
    val originalPost: Post? = null, // For shared posts
    val author: User? = null, // Post author
    val sharedBy: User? = null, // User who shared (if shared post)
    val isLikedByCurrentUser: Boolean = false,
    val isSharedByCurrentUser: Boolean = false,
    val isFollowingAuthor: Boolean = false,
    val displayPriority: Int = 0, // For feed algorithm
    val createdAt: Timestamp = Timestamp.now()
)

enum class FeedItemType {
    POST,
    SHARED_POST,
    SUGGESTED_POST,
    PROMOTED_POST,
    TRENDING_POST
}
