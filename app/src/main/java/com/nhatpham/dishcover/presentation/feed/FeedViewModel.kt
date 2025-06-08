package com.nhatpham.dishcover.presentation.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.model.realtime.FeedUpdateData
import com.nhatpham.dishcover.domain.model.realtime.FeedUpdateType
import com.nhatpham.dishcover.domain.model.realtime.LiveEngagementData
import com.nhatpham.dishcover.domain.model.realtime.LiveNotification
import com.nhatpham.dishcover.domain.model.realtime.RealTimeFeedUpdate
import com.nhatpham.dishcover.domain.usecase.feed.*
import com.nhatpham.dishcover.domain.usecase.realtime.GetUnreadNotificationCountUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.HandlePostInteractionUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.MarkNotificationAsReadUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.ObserveFeedEngagementsUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.ObserveFeedUpdatesUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.ObservePostEngagementUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.ObserveUserNotificationsUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.TrackPostViewUseCase
import com.nhatpham.dishcover.domain.usecase.realtime.UpdateUserOnlineStatusUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserFeedUseCase: GetUserFeedUseCase,
    private val getFollowingFeedUseCase: GetFollowingFeedUseCase,
    private val getTrendingPostsUseCase: GetTrendingPostsUseCase,
    private val getPopularPostsUseCase: GetPopularPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val sharePostUseCase: SharePostUseCase,
    private val unsharePostUseCase: UnsharePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val trackPostActivityUseCase: TrackPostActivityUseCase,
    private val observePostEngagementUseCase: ObservePostEngagementUseCase,
    private val observeFeedEngagementsUseCase: ObserveFeedEngagementsUseCase,
    private val handlePostInteractionUseCase: HandlePostInteractionUseCase,
    private val trackPostViewUseCase: TrackPostViewUseCase,
    private val observeFeedUpdatesUseCase: ObserveFeedUpdatesUseCase,
    private val updateUserOnlineStatusUseCase: UpdateUserOnlineStatusUseCase,
    private val observeUserNotificationsUseCase: ObserveUserNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FeedViewState())
    val state: StateFlow<FeedViewState> = _state.asStateFlow()

    init {
        loadUserData()
        initializeRealTimeFeatures()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update {
                                it.copy(
                                    currentUserId = user.userId,
                                    currentUsername = user.username,
                                    isLoading = false
                                )
                            }
                            loadFeedData(user.userId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = resource.message,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    private fun loadFeedData(userId: String) {
        when (_state.value.selectedTab) {
            FeedTab.FOLLOWING -> loadFollowingFeed(userId)
            FeedTab.POPULAR -> loadPopularPosts()
            FeedTab.RECENT -> loadTrendingPosts()
        }
    }

    fun onTabSelected(tab: FeedTab) {
        _state.update { it.copy(selectedTab = tab) }
        val userId = _state.value.currentUserId
        if (userId.isNotEmpty()) {
            loadFeedData(userId)
        }
    }

    private fun loadFollowingFeed(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingFeed = true) }

            getFollowingFeedUseCase(userId, limit = 20).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                feedItems = resource.data ?: emptyList(),
                                isLoadingFeed = false,
                                feedError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                feedError = resource.message,
                                isLoadingFeed = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingFeed = true) }
                    }
                }
            }
        }
    }

    private fun loadPopularPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingFeed = true) }

            getPopularPostsUseCase(limit = 20).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val feedItems = resource.data?.map { postListItem ->
                            // Convert PostListItem to FeedItem - now much simpler
                            FeedItem(
                                feedItemId = postListItem.postId,
                                itemType = FeedItemType.POST,
                                post = Post(
                                    postId = postListItem.postId,
                                    userId = postListItem.userId,
                                    username = postListItem.username, // Username already included
                                    content = postListItem.content,
                                    imageUrls = listOfNotNull(postListItem.firstImageUrl),
                                    postType = postListItem.postType,
                                    likeCount = postListItem.likeCount,
                                    commentCount = postListItem.commentCount,
                                    shareCount = postListItem.shareCount,
                                    createdAt = postListItem.createdAt
                                ),
                                isLikedByCurrentUser = postListItem.isLikedByCurrentUser,
                                isSharedByCurrentUser = postListItem.isSharedByCurrentUser,
                                isFollowingAuthor = postListItem.isFollowingAuthor,
                                createdAt = postListItem.createdAt
                            )
                        } ?: emptyList()

                        _state.update {
                            it.copy(
                                feedItems = feedItems,
                                isLoadingFeed = false,
                                feedError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                feedError = resource.message,
                                isLoadingFeed = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingFeed = true) }
                    }
                }
            }
        }
    }

    private fun loadTrendingPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingFeed = true) }
            Timber.tag("FeedViewModel").d("Loading trending posts...")

            getTrendingPostsUseCase(limit = 20, timeRange = "24h").collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val feedItems = resource.data?.map { postListItem ->
                            // Convert PostListItem to FeedItem - now much simpler
                            FeedItem(
                                feedItemId = postListItem.postId,
                                itemType = FeedItemType.POST,
                                post = Post(
                                    postId = postListItem.postId,
                                    userId = postListItem.userId,
                                    username = postListItem.username, // Username already included
                                    content = postListItem.content,
                                    imageUrls = listOfNotNull(postListItem.firstImageUrl),
                                    postType = postListItem.postType,
                                    likeCount = postListItem.likeCount,
                                    commentCount = postListItem.commentCount,
                                    shareCount = postListItem.shareCount,
                                    createdAt = postListItem.createdAt
                                ),
                                isLikedByCurrentUser = postListItem.isLikedByCurrentUser,
                                isSharedByCurrentUser = postListItem.isSharedByCurrentUser,
                                isFollowingAuthor = postListItem.isFollowingAuthor,
                                createdAt = postListItem.createdAt
                            )
                        } ?: emptyList()

                        _state.update {
                            it.copy(
                                feedItems = feedItems,
                                isLoadingFeed = false,
                                feedError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                feedError = resource.message,
                                isLoadingFeed = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingFeed = true) }
                    }
                }
            }
        }
    }

    // Enhanced like functionality with real-time updates
    fun onLikePost(postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val feedItem = _state.value.feedItems.find { it.feedItemId == postId }
            val authorId = feedItem?.post?.userId ?: return@launch
            val authorUsername = feedItem.post.username

            handlePostInteractionUseCase(
                postId = postId,
                userId = _state.value.currentUserId,
                authorId = authorId,
                isLiked = isLiked,
                authorUsername = authorUsername
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Real-time update will be handled by observeRealTimeEngagements
                        Timber.d("Post interaction handled successfully")
                    }
                    is Resource.Error -> {
                        Timber.e("Error handling post interaction: ${resource.message}")
                        // Optionally show error to user
                    }
                    is Resource.Loading -> {
                        // Optimistic update already handled in repository
                    }
                }
            }
        }
    }

    fun onSharePost(postId: String, shareMessage: String? = null) {
        val userId = _state.value.currentUserId
        if (userId.isEmpty()) return

        viewModelScope.launch {
            sharePostUseCase(userId, postId, shareMessage, ShareType.REPOST).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Update the local state
                        _state.update { currentState ->
                            val updatedFeedItems = currentState.feedItems.map { feedItem ->
                                if (feedItem.post?.postId == postId) {
                                    val updatedPost = feedItem.post.copy(
                                        shareCount = feedItem.post.shareCount + 1
                                    )
                                    feedItem.copy(
                                        post = updatedPost,
                                        isSharedByCurrentUser = true
                                    )
                                } else feedItem
                            }
                            currentState.copy(feedItems = updatedFeedItems)
                        }

                        // Track the activity
                        trackPostActivity(
                            PostActivity(
                                postId = postId,
                                userId = userId,
                                activityType = PostActivityType.SHARE
                            )
                        )
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Could show loading state
                    }
                }
            }
        }
    }

    // Simplified: Load comments for a specific post
    fun loadCommentsForPost(postId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingComments = true) }

            getPostCommentsUseCase(postId, limit = 50).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                currentPostComments = resource.data ?: emptyList(),
                                isLoadingComments = false,
                                selectedPostId = postId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = resource.message,
                                isLoadingComments = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingComments = true) }
                    }
                }
            }
        }
    }

    // Clear comments when dialog closes
    fun onCommentDialogClosed() {
        _state.update {
            it.copy(
                currentPostComments = emptyList(),
                selectedPostId = "",
                isLoadingComments = false
            )
        }
    }

    fun onAddComment(postId: String, content: String) {
        val userId = _state.value.currentUserId
        val username = _state.value.currentUsername
        if (userId.isEmpty() || content.isBlank()) return

        viewModelScope.launch {
            val comment = Comment(
                postId = postId,
                userId = userId,
                username = username, // Include username
                content = content.trim()
            )

            addCommentUseCase(comment).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Update the local state
                        _state.update { currentState ->
                            val updatedFeedItems = currentState.feedItems.map { feedItem ->
                                if (feedItem.post?.postId == postId) {
                                    val updatedPost = feedItem.post.copy(
                                        commentCount = feedItem.post.commentCount + 1
                                    )
                                    feedItem.copy(post = updatedPost)
                                } else feedItem
                            }

                            // Add the new comment to the current comments list
                            val updatedComments = if (currentState.selectedPostId == postId) {
                                listOf(resource.data!!) + currentState.currentPostComments
                            } else {
                                currentState.currentPostComments
                            }

                            currentState.copy(
                                feedItems = updatedFeedItems,
                                currentPostComments = updatedComments
                            )
                        }

                        // Track the activity
                        trackPostActivity(
                            PostActivity(
                                postId = postId,
                                userId = userId,
                                activityType = PostActivityType.COMMENT
                            )
                        )
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Could show loading state
                    }
                }
            }
        }
    }

    // Real-time engagement observation
    private fun observeRealTimeEngagements() {
        val postIds = _state.value.feedItems.map { it.feedItemId }
        if (postIds.isEmpty()) return

        viewModelScope.launch {
            observeFeedEngagementsUseCase(postIds).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update { currentState ->
                            currentState.copy(
                                realTimeEngagements = resource.data ?: emptyMap(),
                                // Update feed items with real-time engagement data
                                feedItems = currentState.feedItems.map { feedItem ->
                                    val engagement = resource.data?.get(feedItem.feedItemId)
                                    if (engagement != null && feedItem.post != null) {
                                        feedItem.copy(
                                            post = feedItem.post.copy(
                                                likeCount = engagement.likeCount,
                                                commentCount = engagement.commentCount,
                                                shareCount = engagement.shareCount
                                            )
                                        )
                                    } else {
                                        feedItem
                                    }
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error observing real-time engagements: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    // Real-time feed updates observation
    private fun observeRealTimeFeedUpdates() {
        viewModelScope.launch {
            observeFeedUpdatesUseCase(_state.value.currentUserId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val updates = resource.data ?: emptyList()
                        _state.update { currentState ->
                            currentState.copy(
                                feedUpdates = updates,
                                hasNewUpdates = updates.isNotEmpty()
                            )
                        }

                        // Process feed updates
                        processFeedUpdates(updates)
                    }
                    is Resource.Error -> {
                        Timber.e("Error observing feed updates: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // Real-time notifications observation
    private fun observeRealTimeNotifications() {
        viewModelScope.launch {
            observeUserNotificationsUseCase(_state.value.currentUserId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val notifications = resource.data ?: emptyList()
                        val previousNotifications = _state.value.notifications

                        _state.update { currentState ->
                            currentState.copy(notifications = notifications)
                        }

                        // Show floating notification for new notifications
                        val newNotifications = notifications.filter { notification ->
                            !notification.isRead &&
                                    previousNotifications.none { it.notificationId == notification.notificationId }
                        }

                        newNotifications.firstOrNull()?.let { newNotification ->
                            showNotificationFloater(newNotification)
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error observing notifications: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // Unread notification count observation
    private fun observeUnreadNotificationCount() {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase(_state.value.currentUserId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update { currentState ->
                            currentState.copy(unreadNotificationCount = resource.data ?: 0)
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error observing unread count: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    private fun processFeedUpdates(updates: List<RealTimeFeedUpdate>) {
        updates.forEach { update ->
            when (update.updateType) {
                FeedUpdateType.NEW_POST -> {
                    // Add new post to feed
                    when (val data = update.data) {
                        is FeedUpdateData.NewPost -> {
                            addNewPostToFeed(data)
                        }
                        else -> {}
                    }
                }
                FeedUpdateType.POST_UPDATED -> {
                    // Update existing post
                    when (val data = update.data) {
                        is FeedUpdateData.PostEngagement -> {
                            updatePostEngagement(data.postId, data.engagementData)
                        }
                        else -> {}
                    }
                }
                FeedUpdateType.POST_DELETED -> {
                    // Remove post from feed
                    when (val data = update.data) {
                        is FeedUpdateData.PostRemoved -> {
                            removePostFromFeed(data.postId)
                        }
                        else -> {}
                    }
                }
                else -> {
                    // Handle other update types
                }
            }
        }
    }

    private fun addNewPostToFeed(newPostData: FeedUpdateData.NewPost) {
        // Only add if it's from someone the user follows
        _state.update { currentState ->
            val newFeedItem = FeedItem(
                feedItemId = newPostData.postId,
                itemType = FeedItemType.POST,
                post = Post(
                    postId = newPostData.postId,
                    userId = newPostData.authorId,
                    username = newPostData.authorUsername,
                    content = newPostData.content,
                    imageUrls = listOfNotNull(newPostData.firstImageUrl),
                    postType = newPostData.postType,
                    likeCount = 0,
                    commentCount = 0,
                    shareCount = 0,
                    createdAt = com.google.firebase.Timestamp.now()
                ),
                isLikedByCurrentUser = false,
                isSharedByCurrentUser = false,
                isFollowingAuthor = true,
                createdAt = com.google.firebase.Timestamp.now()
            )

            currentState.copy(
                feedItems = listOf(newFeedItem) + currentState.feedItems
            )
        }
    }

    private fun updatePostEngagement(postId: String, engagement: LiveEngagementData) {
        _state.update { currentState ->
            currentState.copy(
                feedItems = currentState.feedItems.map { feedItem ->
                    if (feedItem.feedItemId == postId && feedItem.post != null) {
                        feedItem.copy(
                            post = feedItem.post.copy(
                                likeCount = engagement.likeCount,
                                commentCount = engagement.commentCount,
                                shareCount = engagement.shareCount
                            )
                        )
                    } else {
                        feedItem
                    }
                },
                realTimeEngagements = currentState.realTimeEngagements + (postId to engagement)
            )
        }
    }

    private fun removePostFromFeed(postId: String) {
        _state.update { currentState ->
            currentState.copy(
                feedItems = currentState.feedItems.filter { it.feedItemId != postId }
            )
        }
    }

    // Track when user views a post
    fun onPostViewed(postId: String) {
        viewModelScope.launch {
            trackPostViewUseCase(postId, _state.value.currentUserId).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        Timber.e("Error tracking post view: ${resource.message}")
                    }
                    else -> {
                        // Success or loading - no action needed
                    }
                }
            }
        }
    }

    // Set user online status
    fun setUserOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            updateUserOnlineStatusUseCase(_state.value.currentUserId, isOnline).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        Timber.e("Error updating online status: ${resource.message}")
                    }
                    else -> {
                        // Success or loading
                    }
                }
            }
        }
    }

    // Notification functions
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        Timber.e("Error marking notification as read: ${resource.message}")
                    }
                    else -> {
                        // Success - UI will update via real-time observation
                    }
                }
            }
        }
    }

    fun showNotificationFloater(notification: LiveNotification) {
        _state.update { it.copy(showNotificationFloater = notification) }

        // Auto-dismiss after 5 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            dismissNotificationFloater()
        }
    }

    fun dismissNotificationFloater() {
        _state.update { it.copy(showNotificationFloater = null) }
    }

    // Clear new updates indicator
    fun clearNewUpdates() {
        _state.update { it.copy(hasNewUpdates = false) }
    }

    // Initialize real-time features
    private fun initializeRealTimeFeatures() {
        observeRealTimeEngagements()
        observeRealTimeFeedUpdates()
        observeRealTimeNotifications()
        observeUnreadNotificationCount()
        setUserOnlineStatus(true)
    }

    private fun trackPostActivity(activity: PostActivity) {
        viewModelScope.launch {
            trackPostActivityUseCase(activity).collect { /* Handle result if needed */ }
        }
    }

    fun refreshFeed() {
        val userId = _state.value.currentUserId
        if (userId.isNotEmpty()) {
            loadFeedData(userId)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null, feedError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            setUserOnlineStatus(false)
        }
    }
}

enum class FeedTab {
    FOLLOWING, POPULAR, RECENT
}

data class FeedViewState(
    val currentUserId: String = "",
    val currentUsername: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    // Feed data
    val feedItems: List<FeedItem> = emptyList(),
    val isLoadingFeed: Boolean = false,
    val feedError: String? = null,

    // Comments data
    val currentPostComments: List<Comment> = emptyList(),
    val isLoadingComments: Boolean = false,
    val selectedPostId: String = "",

    // UI state
    val selectedTab: FeedTab = FeedTab.FOLLOWING,


    val realTimeEngagements: Map<String, LiveEngagementData> = emptyMap(),
    val feedUpdates: List<RealTimeFeedUpdate> = emptyList(),
    val hasNewUpdates: Boolean = false,
    val onlineUsers: Set<String> = emptySet(),
    val notifications: List<LiveNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val showNotificationFloater: LiveNotification? = null
)