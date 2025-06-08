package com.nhatpham.dishcover.domain.usecase.realtime

import com.nhatpham.dishcover.domain.model.realtime.*
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeCommentRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeEngagementRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeFeedRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeNotificationRepository
import com.nhatpham.dishcover.domain.repository.realtime.RealTimeUserActivityRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePostEngagementUseCase @Inject constructor(
    private val repository: RealTimeEngagementRepository
) {
    operator fun invoke(postId: String): Flow<Resource<LiveEngagementData>> {
        return repository.observePostEngagement(postId)
    }
}

class ObserveFeedEngagementsUseCase @Inject constructor(
    private val repository: RealTimeEngagementRepository
) {
    operator fun invoke(postIds: List<String>): Flow<Resource<Map<String, LiveEngagementData>>> {
        return repository.observeMultiplePostEngagements(postIds)
    }
}

class ReactToPostUseCase @Inject constructor(
    private val repository: RealTimeEngagementRepository
) {
    suspend operator fun invoke(
        postId: String,
        userId: String,
        reactionType: ReactionType
    ): Flow<Resource<Unit>> {
        return repository.addReaction(postId, userId, reactionType)
    }
}

class RemoveReactionUseCase @Inject constructor(
    private val repository: RealTimeEngagementRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Flow<Resource<Unit>> {
        return repository.removeReaction(postId, userId)
    }
}

class TrackPostViewUseCase @Inject constructor(
    private val engagementRepository: RealTimeEngagementRepository,
    private val userActivityRepository: RealTimeUserActivityRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Flow<Resource<Unit>> {
        // Increment view count
        engagementRepository.incrementViewCount(postId, userId)

        // Track user activity
        return userActivityRepository.markUserAsViewingPost(userId, postId)
    }
}

class ObserveFeedUpdatesUseCase @Inject constructor(
    private val repository: RealTimeFeedRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<RealTimeFeedUpdate>>> {
        return repository.observeFeedUpdates(userId)
    }
}

class PublishFeedUpdateUseCase @Inject constructor(
    private val repository: RealTimeFeedRepository
) {
    suspend operator fun invoke(update: RealTimeFeedUpdate): Flow<Resource<Unit>> {
        return repository.publishFeedUpdate(update)
    }
}

class ObservePostCommentsUseCase @Inject constructor(
    private val repository: RealTimeCommentRepository
) {
    operator fun invoke(postId: String): Flow<Resource<List<RealTimeComment>>> {
        return repository.observePostComments(postId)
    }
}

class AddCommentUseCase @Inject constructor(
    private val commentRepository: RealTimeCommentRepository,
    private val engagementRepository: RealTimeEngagementRepository,
    private val userActivityRepository: RealTimeUserActivityRepository
) {
    suspend operator fun invoke(
        postId: String,
        userId: String,
        username: String,
        profilePicture: String?,
        content: String
    ): Flow<Resource<RealTimeComment>> {
        val comment = RealTimeComment(
            commentId = java.util.UUID.randomUUID().toString(),
            postId = postId,
            userId = userId,
            username = username,
            profilePicture = profilePicture,
            content = content
        )

        // Stop typing indicator
        userActivityRepository.clearUserActivity(userId, UserActivityType.TYPING_COMMENT)

        return commentRepository.addComment(comment)
    }
}

class StartTypingCommentUseCase @Inject constructor(
    private val repository: RealTimeUserActivityRepository
) {
    suspend operator fun invoke(userId: String, postId: String): Flow<Resource<Unit>> {
        return repository.markUserAsTypingComment(userId, postId)
    }
}

class StopTypingCommentUseCase @Inject constructor(
    private val repository: RealTimeUserActivityRepository
) {
    suspend operator fun invoke(userId: String): Flow<Resource<Unit>> {
        return repository.clearUserActivity(userId, UserActivityType.TYPING_COMMENT)
    }
}

class ObservePostViewersUseCase @Inject constructor(
    private val repository: RealTimeUserActivityRepository
) {
    operator fun invoke(postId: String): Flow<Resource<List<LiveUserActivity>>> {
        return repository.observePostViewers(postId)
    }
}

class UpdateUserOnlineStatusUseCase @Inject constructor(
    private val repository: RealTimeUserActivityRepository
) {
    suspend operator fun invoke(userId: String, isOnline: Boolean): Flow<Resource<Unit>> {
        return repository.setUserOnlineStatus(userId, isOnline)
    }
}

class ObserveUserNotificationsUseCase @Inject constructor(
    private val repository: RealTimeNotificationRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<LiveNotification>>> {
        return repository.observeUserNotifications(userId)
    }
}

class SendNotificationUseCase @Inject constructor(
    private val repository: RealTimeNotificationRepository
) {
    suspend operator fun invoke(
        targetUserId: String,
        type: NotificationType,
        title: String,
        content: String,
        data: Map<String, String> = emptyMap()
    ): Flow<Resource<Unit>> {
        val notification = LiveNotification(
            userId = targetUserId,
            type = type,
            title = title,
            content = content,
            data = data
        )
        return repository.sendNotification(notification)
    }
}

class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: RealTimeNotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Flow<Resource<Unit>> {
        return repository.markNotificationAsRead(notificationId)
    }
}

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val repository: RealTimeNotificationRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Int>> {
        return repository.getUnreadNotificationCount(userId)
    }
}

// Composite Use Cases for complex real-time operations

class HandlePostInteractionUseCase @Inject constructor(
    private val reactToPostUseCase: ReactToPostUseCase,
    private val removeReactionUseCase: RemoveReactionUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(
        postId: String,
        userId: String,
        authorId: String,
        isLiked: Boolean,
        authorUsername: String
    ): Flow<Resource<Unit>> {
        return if (isLiked) {
            // Add like and send notification
            reactToPostUseCase(postId, userId, ReactionType.LIKE).also {
                if (userId != authorId) {
                    sendNotificationUseCase(
                        targetUserId = authorId,
                        type = NotificationType.LIKE,
                        title = "New like on your post",
                        content = "Someone liked your post",
                        data = mapOf("postId" to postId, "userId" to userId)
                    )
                }
            }
        } else {
            removeReactionUseCase(postId, userId)
        }
    }
}

class HandleCommentAddedUseCase @Inject constructor(
    private val addCommentUseCase: AddCommentUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(
        postId: String,
        userId: String,
        username: String,
        profilePicture: String?,
        content: String,
        authorId: String
    ): Flow<Resource<RealTimeComment>> {
        return addCommentUseCase(postId, userId, username, profilePicture, content).also {
            if (userId != authorId) {
                sendNotificationUseCase(
                    targetUserId = authorId,
                    type = NotificationType.COMMENT,
                    title = "New comment on your post",
                    content = "$username commented: ${content.take(50)}${if (content.length > 50) "..." else ""}",
                    data = mapOf("postId" to postId, "userId" to userId, "commentId" to "")
                )
            }
        }
    }
}