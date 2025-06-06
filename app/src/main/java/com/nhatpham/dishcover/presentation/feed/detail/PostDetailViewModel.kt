package com.nhatpham.dishcover.presentation.feed.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.usecase.feed.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPostUseCase: GetPostUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val sharePostUseCase: SharePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val likeCommentUseCase: LikeCommentUseCase,
    private val unlikeCommentUseCase: UnlikeCommentUseCase,
    private val trackPostActivityUseCase: TrackPostActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailViewState())
    val state: StateFlow<PostDetailViewState> = _state.asStateFlow()

    private var currentPostId: String = ""
    private var lastCommentId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update {
                                it.copy(
                                    currentUserId = user.userId,
                                    currentUsername = user.username,
                                    currentUserProfilePicture = user.profilePicture
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun loadPost(postId: String) {
        currentPostId = postId
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            getPostUseCase(postId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { post ->
                            _state.update {
                                it.copy(
                                    post = post,
                                    isLoading = false,
                                    error = null
                                )
                            }

                            // Load comments
                            loadComments()

                            // Track view activity
                            trackPostActivity(
                                PostActivity(
                                    postId = postId,
                                    userId = _state.value.currentUserId,
                                    activityType = PostActivityType.VIEW
                                )
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadComments() {
        if (currentPostId.isBlank()) return

        _state.update { it.copy(isLoadingComments = true) }

        viewModelScope.launch {
            getPostCommentsUseCase(currentPostId, limit = 20, lastCommentId = null).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val comments = resource.data ?: emptyList()
                        _state.update {
                            it.copy(
                                comments = comments,
                                isLoadingComments = false,
                                hasMoreComments = comments.size >= 20
                            )
                        }

                        // Update last comment ID for pagination
                        lastCommentId = comments.lastOrNull()?.commentId
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoadingComments = false,
                                error = resource.message
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

    fun loadMoreComments() {
        if (currentPostId.isBlank() || _state.value.isLoadingComments) return

        _state.update { it.copy(isLoadingComments = true) }

        viewModelScope.launch {
            getPostCommentsUseCase(currentPostId, limit = 20, lastCommentId = lastCommentId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val newComments = resource.data ?: emptyList()
                        _state.update { currentState ->
                            currentState.copy(
                                comments = currentState.comments + newComments,
                                isLoadingComments = false,
                                hasMoreComments = newComments.size >= 20
                            )
                        }

                        // Update last comment ID for next pagination
                        lastCommentId = newComments.lastOrNull()?.commentId
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoadingComments = false,
                                error = resource.message
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

    fun toggleLike() {
        val post = _state.value.post ?: return
        val userId = _state.value.currentUserId
        if (userId.isEmpty()) return

        val isCurrentlyLiked = _state.value.isLikedByCurrentUser

        // Optimistic update
        _state.update { currentState ->
            val updatedPost = post.copy(
                likeCount = if (isCurrentlyLiked) {
                    maxOf(0, post.likeCount - 1)
                } else {
                    post.likeCount + 1
                }
            )
            currentState.copy(
                post = updatedPost,
                isLikedByCurrentUser = !isCurrentlyLiked
            )
        }

        viewModelScope.launch {
            val flow = if (isCurrentlyLiked) {
                unlikePostUseCase(userId, post.postId)
            } else {
                likePostUseCase(userId, post.postId)
            }

            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Track activity
                        trackPostActivity(
                            PostActivity(
                                postId = post.postId,
                                userId = userId,
                                activityType = if (isCurrentlyLiked) PostActivityType.UNLIKE else PostActivityType.LIKE
                            )
                        )
                    }
                    is Resource.Error -> {
                        // Revert optimistic update
                        _state.update { currentState ->
                            val revertedPost = currentState.post?.copy(
                                likeCount = if (isCurrentlyLiked) {
                                    post.likeCount
                                } else {
                                    maxOf(0, post.likeCount)
                                }
                            )
                            currentState.copy(
                                post = revertedPost,
                                isLikedByCurrentUser = isCurrentlyLiked,
                                error = resource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled optimistically
                    }
                }
            }
        }
    }

    fun sharePost() {
        val post = _state.value.post ?: return
        val userId = _state.value.currentUserId
        if (userId.isEmpty()) return

        viewModelScope.launch {
            sharePostUseCase(userId, post.postId, null, ShareType.REPOST).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Update share count optimistically
                        _state.update { currentState ->
                            val updatedPost = currentState.post?.copy(
                                shareCount = currentState.post.shareCount + 1
                            )
                            currentState.copy(
                                post = updatedPost,
                                isSharedByCurrentUser = true
                            )
                        }

                        // Track activity
                        trackPostActivity(
                            PostActivity(
                                postId = post.postId,
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
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun addComment(content: String) {
        if (content.isBlank() || currentPostId.isBlank()) return

        val userId = _state.value.currentUserId
        val username = _state.value.currentUsername
        if (userId.isEmpty()) return

        val comment = Comment(
            postId = currentPostId,
            userId = userId,
            username = username,
            content = content.trim()
        )

        viewModelScope.launch {
            addCommentUseCase(comment).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { newComment ->
                            // Add comment to the list
                            _state.update { currentState ->
                                val updatedPost = currentState.post?.copy(
                                    commentCount = currentState.post.commentCount + 1
                                )
                                currentState.copy(
                                    post = updatedPost,
                                    comments = listOf(newComment) + currentState.comments
                                )
                            }

                            // Track activity
                            trackPostActivity(
                                PostActivity(
                                    postId = currentPostId,
                                    userId = userId,
                                    activityType = PostActivityType.COMMENT
                                )
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        val userId = _state.value.currentUserId
        if (userId.isEmpty()) return

        val comment = _state.value.comments.find { it.commentId == commentId } ?: return
        val isCurrentlyLiked = false // We'd need to track this state separately

        viewModelScope.launch {
            val flow = if (isCurrentlyLiked) {
                unlikeCommentUseCase(userId, commentId)
            } else {
                likeCommentUseCase(userId, commentId)
            }

            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Update comment like count
                        _state.update { currentState ->
                            val updatedComments = currentState.comments.map { c ->
                                if (c.commentId == commentId) {
                                    c.copy(
                                        likeCount = if (isCurrentlyLiked) {
                                            maxOf(0, c.likeCount - 1)
                                        } else {
                                            c.likeCount + 1
                                        }
                                    )
                                } else c
                            }
                            currentState.copy(comments = updatedComments)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun replyToComment(commentId: String) {
        // TODO: Implement reply functionality
        // This would involve setting a reply target and updating the comment input
    }

    fun loadPostLikes() {
        // TODO: Implement showing who liked the post
        // This would open a dialog or bottom sheet with the list of users who liked
    }

    private fun trackPostActivity(activity: PostActivity) {
        viewModelScope.launch {
            trackPostActivityUseCase(activity).collect { /* Handle result if needed */ }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class PostDetailViewState(
    val currentUserId: String = "",
    val currentUsername: String = "",
    val currentUserProfilePicture: String? = null,
    val post: Post? = null,
    val author: User? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val hasMoreComments: Boolean = false,
    val isLikedByCurrentUser: Boolean = false,
    val isSharedByCurrentUser: Boolean = false,
    val error: String? = null
)