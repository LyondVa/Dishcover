package com.nhatpham.dishcover.presentation.profile.following

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.usecase.user.*
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserFollowingUseCase: GetUserFollowingUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val isFollowingUserUseCase: IsFollowingUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FollowingViewState())
    val state: StateFlow<FollowingViewState> = _state.asStateFlow()

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
                                it.copy(currentUserId = user.userId)
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

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isCurrentUser = userId == _state.value.currentUserId
                )
            }

            // Load target user info
            getUserProfileUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update { it.copy(targetUser = user) }
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = resource.message) }
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }

            // Load following
            getUserFollowingUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { following ->
                            _state.update {
                                it.copy(
                                    following = following,
                                    isLoading = false
                                )
                            }
                            // Load follow status for each user being followed
                            loadFollowStatusForUsers(following)
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
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadFollowStatusForUsers(users: List<User>) {
        val currentUserId = _state.value.currentUserId
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            users.forEach { user ->
                if (user.userId != currentUserId) {
                    isFollowingUserUseCase(currentUserId, user.userId).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val isFollowing = resource.data ?: false
                                _state.update { currentState ->
                                    currentState.copy(
                                        followingStatus = currentState.followingStatus.toMutableMap().apply {
                                            put(user.userId, isFollowing)
                                        }
                                    )
                                }
                            }
                            is Resource.Error -> {
                                // Log error but don't update UI
                            }
                            is Resource.Loading -> {
                                // Handle loading if needed
                            }
                        }
                    }
                }
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        val currentUserId = _state.value.currentUserId
        if (currentUserId.isEmpty() || targetUserId == currentUserId) return

        val isCurrentlyFollowing = _state.value.followingStatus[targetUserId] ?: false

        // Add to updating set
        _state.update { currentState ->
            currentState.copy(
                updatingFollowStatus = currentState.updatingFollowStatus + targetUserId
            )
        }

        viewModelScope.launch {
            val useCase = if (isCurrentlyFollowing) {
                unfollowUserUseCase(currentUserId, targetUserId)
            } else {
                followUserUseCase(currentUserId, targetUserId)
            }

            useCase.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update { currentState ->
                            currentState.copy(
                                followingStatus = currentState.followingStatus.toMutableMap().apply {
                                    put(targetUserId, !isCurrentlyFollowing)
                                },
                                updatingFollowStatus = currentState.updatingFollowStatus - targetUserId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update { currentState ->
                            currentState.copy(
                                error = resource.message,
                                updatingFollowStatus = currentState.updatingFollowStatus - targetUserId
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled above
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class FollowingViewState(
    val currentUserId: String = "",
    val targetUser: User? = null,
    val isCurrentUser: Boolean = false,
    val following: List<User> = emptyList(),
    val followingStatus: Map<String, Boolean> = emptyMap(),
    val updatingFollowStatus: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)