package com.nhatpham.dishcover.presentation.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.usecase.feed.*
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
    private val trackPostActivityUseCase: TrackPostActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FeedViewState())
    val state: StateFlow<FeedViewState> = _state.asStateFlow()

    init {
        loadUserData()
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

    fun onLikePost(postId: String, isCurrentlyLiked: Boolean) {
        val userId = _state.value.currentUserId
        if (userId.isEmpty()) return

        viewModelScope.launch {
            val flow = if (isCurrentlyLiked) {
                unlikePostUseCase(userId, postId)
            } else {
                likePostUseCase(userId, postId)
            }

            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Update the local state optimistically
                        _state.update { currentState ->
                            val updatedFeedItems = currentState.feedItems.map { feedItem ->
                                if (feedItem.post?.postId == postId) {
                                    val updatedPost = feedItem.post.copy(
                                        likeCount = if (isCurrentlyLiked) {
                                            maxOf(0, feedItem.post.likeCount - 1)
                                        } else {
                                            feedItem.post.likeCount + 1
                                        }
                                    )
                                    feedItem.copy(
                                        post = updatedPost,
                                        isLikedByCurrentUser = !isCurrentlyLiked
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
                                activityType = if (isCurrentlyLiked) PostActivityType.UNLIKE else PostActivityType.LIKE
                            )
                        )
                    }
                    is Resource.Error -> {
                        // Show error message
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Could show loading state on the specific post
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
    val selectedTab: FeedTab = FeedTab.FOLLOWING
)