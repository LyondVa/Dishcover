package com.nhatpham.dishcover.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.domain.usecase.admin.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val moderateContentUseCase: ModerateContentUseCase,
    private val flagContentUseCase: FlagContentUseCase,
    private val featureRecipeUseCase: FeatureRecipeUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val moderateUserUseCase: ModerateUserUseCase,
    private val updateUserAdminStatusUseCase: UpdateUserAdminStatusUseCase,
    private val getFlaggedContentUseCase: GetFlaggedContentUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
        loadDashboardStats()
        loadContent()
        loadUsers()
        loadFlaggedContent()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUserId = result.data?.userId
                        _state.value = _state.value.copy(
                            currentUser = result.data?.username
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load current user: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getDashboardStatsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            dashboardStats = result.data,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to load dashboard stats",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            getContentItemsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            contentItems = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load content: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            getUsersUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            users = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load users: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    private fun loadFlaggedContent() {
        viewModelScope.launch {
            getFlaggedContentUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            flaggedContent = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load flagged content: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun moderateContent(
        contentId: String,
        contentType: AdminContentType,
        action: ContentModerationAction
    ) {
        viewModelScope.launch {
            val moderatorId = currentUserId ?: return@launch

            when (action) {
                is ContentModerationAction.UpdateStatus -> {
                    moderateContentUseCase(
                        contentId = contentId,
                        contentType = contentType,
                        status = action.status,
                        reason = action.reason,
                        moderatorId = moderatorId
                    ).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                // Refresh content lists
                                loadContent()
                                loadFlaggedContent()
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Failed to moderate content"
                                )
                            }
                            is Resource.Loading -> {
                                // Handle loading
                            }
                        }
                    }
                }
                is ContentModerationAction.Feature -> {
                    if (contentType == AdminContentType.RECIPE) {
                        featureRecipeUseCase(
                            recipeId = contentId,
                            featured = action.featured,
                            moderatorId = moderatorId
                        ).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    loadContent()
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        error = result.message ?: "Failed to feature recipe"
                                    )
                                }
                                is Resource.Loading -> {
                                    // Handle loading
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun moderateUser(userId: String, action: UserModerationAction) {
        viewModelScope.launch {
            val moderatorId = currentUserId ?: return@launch

            when (action) {
                is UserModerationAction.UpdateStatus -> {
                    moderateUserUseCase(
                        userId = userId,
                        status = action.status,
                        reason = action.reason,
                        moderatorId = moderatorId
                    ).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                loadUsers()
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Failed to moderate user"
                                )
                            }
                            is Resource.Loading -> {
                                // Handle loading
                            }
                        }
                    }
                }
                is UserModerationAction.UpdateAdminStatus -> {
                    updateUserAdminStatusUseCase(
                        userId = userId,
                        isAdmin = action.isAdmin,
                        moderatorId = moderatorId
                    ).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                loadUsers()
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Failed to update admin status"
                                )
                            }
                            is Resource.Loading -> {
                                // Handle loading
                            }
                        }
                    }
                }
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            getUsersUseCase(searchQuery = query).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            users = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to search users"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun loadMoreContent() {
        // Implement pagination logic
        val lastContentId = _state.value.contentItems.lastOrNull()?.contentId
        viewModelScope.launch {
            getContentItemsUseCase(lastContentId = lastContentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val currentItems = _state.value.contentItems
                        val newItems = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            contentItems = currentItems + newItems
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load more content: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun loadMoreUsers() {
        // Implement pagination logic
        val lastUserId = _state.value.users.lastOrNull()?.userId
        viewModelScope.launch {
            getUsersUseCase(lastUserId = lastUserId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val currentUsers = _state.value.users
                        val newUsers = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            users = currentUsers + newUsers
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load more users: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

data class AdminState(
    val currentUser: String? = null,
    val dashboardStats: AdminDashboardStats? = null,
    val contentItems: List<AdminContentItem> = emptyList(),
    val users: List<AdminUserItem> = emptyList(),
    val flaggedContent: List<AdminContentItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ContentModerationAction {
    data class UpdateStatus(
        val status: ContentStatus,
        val reason: String = ""
    ) : ContentModerationAction()

    data class Feature(
        val featured: Boolean
    ) : ContentModerationAction()
}

sealed class UserModerationAction {
    data class UpdateStatus(
        val status: UserStatus,
        val reason: String = ""
    ) : UserModerationAction()

    data class UpdateAdminStatus(
        val isAdmin: Boolean
    ) : UserModerationAction()
}