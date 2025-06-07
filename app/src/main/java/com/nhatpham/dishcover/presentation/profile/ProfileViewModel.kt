package com.nhatpham.dishcover.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.user.FollowUserUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetUserFollowersUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetUserFollowingUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetUserProfileUseCase
import com.nhatpham.dishcover.domain.usecase.user.UnfollowUserUseCase
import com.nhatpham.dishcover.domain.usecase.user.UpdateUserProfileUseCase
import com.nhatpham.dishcover.domain.usecase.feed.GetUserPostsUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.user.IsFollowingUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getUserFollowersUseCase: GetUserFollowersUseCase,
    private val getUserFollowingUseCase: GetUserFollowingUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getUserRecipesUseCase: GetUserRecipesUseCase,
    private val isFollowingUserUseCase: IsFollowingUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        getCurrentUserIdOnly()
    }

    private fun getCurrentUserIdOnly() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                            // Don't automatically load profile here
                            // Wait for explicit LoadProfile event
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = result.message) }
                    }
                    is Resource.Loading -> {
                        // Don't set loading state here since we're not loading profile yet
                    }
                }
            }
        }
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                            loadUserProfile(user.userId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = result.message) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getUserProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _state.update {
                                it.copy(
                                    user = user,
                                    isCurrentUser = userId == currentUserId,
                                    isLoading = false
                                )
                            }

                            // Load related data
                            loadFollowersAndFollowing(userId)
                            loadUserPosts(userId)
                            loadUserRecipes(userId)

                            // Check follow status if not current user
                            if (userId != currentUserId) {
                                checkIfFollowing(userId)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadFollowersAndFollowing(userId: String) {
        // Load followers
        viewModelScope.launch {
            getUserFollowersUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { followers ->
                            _state.update { it.copy(followers = followers) }
                        }
                    }
                    is Resource.Error -> {
                        // Log error but don't update UI state
                    }
                    is Resource.Loading -> {
                        // Do nothing
                    }
                }
            }
        }

        // Load following
        viewModelScope.launch {
            getUserFollowingUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { following ->
                            _state.update { it.copy(following = following) }
                        }
                    }
                    is Resource.Error -> {
                        // Log error but don't update UI state
                    }
                    is Resource.Loading -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPosts = true) }

            getUserPostsUseCase(userId, limit = 50).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { posts ->
                            _state.update {
                                it.copy(
                                    userPosts = posts,
                                    isLoadingPosts = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                postsError = result.message,
                                isLoadingPosts = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingPosts = true) }
                    }
                }
            }
        }
    }

    private fun loadUserRecipes(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingRecipes = true) }

            getUserRecipesUseCase(userId, limit = 50).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipes ->
                            _state.update {
                                it.copy(
                                    userRecipes = recipes,
                                    isLoadingRecipes = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                recipesError = result.message,
                                isLoadingRecipes = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingRecipes = true) }
                    }
                }
            }
        }
    }

    private fun checkIfFollowing(userId: String) {
        currentUserId?.let { currentId ->
            viewModelScope.launch {
                _state.update { it.copy(isCheckingFollowStatus = true) }

                isFollowingUserUseCase(currentId, userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val isFollowing = result.data ?: false
                            _state.update {
                                it.copy(
                                    isFollowing = isFollowing,
                                    isCheckingFollowStatus = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    isCheckingFollowStatus = false,
                                    followStatusError = result.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _state.update { it.copy(isCheckingFollowStatus = true) }
                        }
                    }
                }
            }
        }
    }

    fun updateUserProfile(
        username: String,
        bio: String,
        profilePicture: String?
    ) {
        val currentUser = state.value.user ?: return

        val updatedUser = currentUser.copy(
            username = username,
            bio = bio,
            profilePicture = profilePicture
        )

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null) }

            updateUserProfileUseCase(updatedUser).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _state.update {
                                it.copy(
                                    user = user,
                                    isUpdating = false,
                                    isEditMode = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                updateError = result.message,
                                isUpdating = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already set above
                    }
                }
            }
        }
    }

    fun toggleFollowStatus() {
        val userId = state.value.user?.userId ?: return
        val isCurrentlyFollowing = state.value.isFollowing

        currentUserId?.let { currentId ->
            if (currentId == userId) return // Can't follow yourself

            viewModelScope.launch {
                _state.update { it.copy(isUpdatingFollowStatus = true) }

                if (isCurrentlyFollowing) {
                    unfollowUserUseCase(currentId, userId).collect { result ->
                        handleFollowStatusResult(result, false)
                    }
                } else {
                    followUserUseCase(currentId, userId).collect { result ->
                        handleFollowStatusResult(result, true)
                    }
                }
            }
        }
    }

    private fun handleFollowStatusResult(result: Resource<Unit>, isFollowing: Boolean) {
        when (result) {
            is Resource.Success -> {
                _state.update {
                    it.copy(
                        isFollowing = isFollowing,
                        isUpdatingFollowStatus = false
                    )
                }
                // Reload followers count
                state.value.user?.userId?.let { loadFollowersAndFollowing(it) }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        followStatusError = result.message,
                        isUpdatingFollowStatus = false
                    )
                }
            }
            is Resource.Loading -> {
                // Already set
            }
        }
    }

    fun setEditMode(isEditMode: Boolean) {
        _state.update { it.copy(isEditMode = isEditMode) }
    }

    fun refreshProfile() {
        state.value.user?.userId?.let { userId ->
            loadUserProfile(userId)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.ToggleEditMode -> {
                setEditMode(event.isEditMode)
            }
            is UserProfileEvent.UpdateProfile -> {
                updateUserProfile(event.username, event.bio, event.profilePicture)
            }
            is UserProfileEvent.ToggleFollowStatus -> {
                toggleFollowStatus()
            }
            is UserProfileEvent.LoadProfile -> {
                loadUserProfile(event.userId)
            }
        }
    }
}

data class UserProfileState(
    // User data
    val user: User? = null,
    val isCurrentUser: Boolean = false,

    // Loading states
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isLoadingPosts: Boolean = false,
    val isLoadingRecipes: Boolean = false,

    // Error states
    val error: String? = null,
    val updateError: String? = null,
    val postsError: String? = null,
    val recipesError: String? = null,

    // Edit mode
    val isEditMode: Boolean = false,

    // Social data
    val followers: List<User> = emptyList(),
    val following: List<User> = emptyList(),
    val isFollowing: Boolean = false,
    val isCheckingFollowStatus: Boolean = false,
    val isUpdatingFollowStatus: Boolean = false,
    val followStatusError: String? = null,

    // Content data
    val userPosts: List<PostListItem> = emptyList(),
    val userRecipes: List<RecipeListItem> = emptyList()
)

sealed class UserProfileEvent {
    data class ToggleEditMode(val isEditMode: Boolean) : UserProfileEvent()
    data class UpdateProfile(
        val username: String,
        val bio: String,
        val profilePicture: String?
    ) : UserProfileEvent()
    object ToggleFollowStatus : UserProfileEvent()
    data class LoadProfile(val userId: String) : UserProfileEvent()
}