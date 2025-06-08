package com.nhatpham.dishcover.domain.model.realtime

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.feed.PostType
import java.util.*

data class LiveEngagementData(
    val postId: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val activeViewers: Int = 0,
    val recentReactions: List<RecentReaction> = emptyList(),
    val lastUpdated: Timestamp = Timestamp.now()
)

data class RecentReaction(
    val userId: String,
    val username: String,
    val profilePicture: String?,
    val reactionType: ReactionType,
    val timestamp: Timestamp
)

enum class ReactionType {
    LIKE, LOVE, LAUGH, WOW, ANGRY, SAD
}

data class RealTimePostUpdate(
    val postId: String,
    val updateType: PostUpdateType,
    val userId: String,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Timestamp = Timestamp.now()
)

enum class PostUpdateType {
    CONTENT_UPDATED, MEDIA_ADDED, MEDIA_REMOVED, VISIBILITY_CHANGED, DELETED
}

data class LiveUserActivity(
    val userId: String,
    val activityType: UserActivityType,
    val targetId: String, // postId, userId, etc.
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Timestamp = Timestamp.now()
)

enum class UserActivityType {
    VIEWING_POST, TYPING_COMMENT, ONLINE, OFFLINE, INTERACTING_WITH_POST
}

data class RealTimeFeedUpdate(
    val feedUpdateId: String = UUID.randomUUID().toString(),
    val userId: String, // Feed owner
    val updateType: FeedUpdateType,
    val data: FeedUpdateData,
    val timestamp: Timestamp = Timestamp.now(),
    val priority: Int = 0 // Higher priority updates appear first
)

enum class FeedUpdateType {
    NEW_POST, POST_UPDATED, POST_DELETED, NEW_FOLLOWER_POST, RECOMMENDED_POST
}

sealed class FeedUpdateData {
    data class NewPost(
        val postId: String,
        val authorId: String,
        val authorUsername: String,
        val authorProfilePicture: String?,
        val postType: PostType,
        val content: String,
        val firstImageUrl: String?
    ) : FeedUpdateData()

    data class PostEngagement(
        val postId: String,
        val engagementData: LiveEngagementData
    ) : FeedUpdateData()

    data class PostRemoved(
        val postId: String,
        val reason: String
    ) : FeedUpdateData()
}

data class RealTimeComment(
    val commentId: String,
    val postId: String,
    val userId: String,
    val username: String,
    val profilePicture: String?,
    val content: String,
    val likeCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val status: CommentStatus = CommentStatus.ACTIVE
)

enum class CommentStatus {
    ACTIVE, DELETED, HIDDEN, PENDING_MODERATION
}

data class LiveNotification(
    val notificationId: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, MENTION, POST_FEATURED, RECIPE_SHARED
}