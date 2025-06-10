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
    // Dashboard
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,

    // Content management
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val getContentItemUseCase: GetContentItemUseCase,

    // Post actions
    private val hidePostUseCase: HidePostUseCase,
    private val unhidePostUseCase: UnhidePostUseCase,
    private val removePostUseCase: RemovePostUseCase,

    // Recipe actions
    private val hideRecipeUseCase: HideRecipeUseCase,
    private val unhideRecipeUseCase: UnhideRecipeUseCase,
    private val featureRecipeUseCase: FeatureRecipeUseCase,
    private val unfeatureRecipeUseCase: UnfeatureRecipeUseCase,
    private val removeRecipeUseCase: RemoveRecipeUseCase,

    // User management
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserUseCase: GetUserUseCase,

    // User actions
    private val suspendUserUseCase: SuspendUserUseCase,
    private val unsuspendUserUseCase: UnsuspendUserUseCase,
    private val makeAdminUseCase: MakeAdminUseCase,
    private val removeAdminUseCase: RemoveAdminUseCase,
    private val banUserUseCase: BanUserUseCase,

    // Reports and moderation
    private val getFlaggedContentUseCase: GetFlaggedContentUseCase,
    private val flagContentUseCase: FlagContentUseCase,
    private val getModerationHistoryUseCase: GetModerationHistoryUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
        loadDashboardStats()
        loadPosts()
        loadRecipes()
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

    // Dashboard Management
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

    // Content Loading
    private fun loadPosts() {
        viewModelScope.launch {
            val filters = AdminContentFilters(contentType = AdminContentType.POST)
            getContentItemsUseCase(filters).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            posts = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load posts: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            val filters = AdminContentFilters(contentType = AdminContentType.RECIPE)
            getContentItemsUseCase(filters).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            recipes = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load recipes: ${result.message}")
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

    // POST ACTIONS (3 actions) - according to admin flow plan
    fun hidePost(postId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            hidePostUseCase(postId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadPosts()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to hide post"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun unhidePost(postId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            unhidePostUseCase(postId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadPosts()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to unhide post"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun removePost(postId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            removePostUseCase(postId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadPosts()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to remove post"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // RECIPE ACTIONS (4 actions) - according to admin flow plan
    fun hideRecipe(recipeId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            hideRecipeUseCase(recipeId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecipes()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to hide recipe"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun unhideRecipe(recipeId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            unhideRecipeUseCase(recipeId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecipes()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to unhide recipe"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun featureRecipe(recipeId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            featureRecipeUseCase(recipeId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecipes()
                        loadDashboardStats()
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

    fun unfeatureRecipe(recipeId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            unfeatureRecipeUseCase(recipeId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecipes()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to unfeature recipe"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun removeRecipe(recipeId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            removeRecipeUseCase(recipeId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecipes()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to remove recipe"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // USER ACTIONS (4 actions) - according to admin flow plan
    fun suspendUser(userId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            suspendUserUseCase(userId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadUsers()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to suspend user"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun unsuspendUser(userId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            unsuspendUserUseCase(userId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadUsers()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to unsuspend user"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun makeAdmin(userId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            makeAdminUseCase(userId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadUsers()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to make user admin"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun removeAdmin(userId: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            removeAdminUseCase(userId, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadUsers()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to remove admin status"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun banUser(userId: String, reason: String) {
        val moderatorId = currentUserId ?: return
        viewModelScope.launch {
            banUserUseCase(userId, reason, moderatorId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadUsers()
                        loadDashboardStats()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Failed to ban user"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // Filtering and search
    fun filterContent(filters: AdminContentFilters) {
        viewModelScope.launch {
            getContentItemsUseCase(filters).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        if (filters.contentType == AdminContentType.POST) {
                            _state.value = _state.value.copy(
                                posts = result.data ?: emptyList()
                            )
                        } else {
                            _state.value = _state.value.copy(
                                recipes = result.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to filter content: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun filterUsers(filters: AdminUserFilters) {
        viewModelScope.launch {
            getUsersUseCase(filters).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            users = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to filter users: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    // Pagination
    fun loadMorePosts() {
        val lastPostId = _state.value.posts.lastOrNull()?.contentId
        viewModelScope.launch {
            val filters = AdminContentFilters(contentType = AdminContentType.POST)
            getContentItemsUseCase(filters, lastContentId = lastPostId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val currentPosts = _state.value.posts
                        val newPosts = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            posts = currentPosts + newPosts
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load more posts: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun loadMoreRecipes() {
        val lastRecipeId = _state.value.recipes.lastOrNull()?.contentId
        viewModelScope.launch {
            val filters = AdminContentFilters(contentType = AdminContentType.RECIPE)
            getContentItemsUseCase(filters, lastContentId = lastRecipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val currentRecipes = _state.value.recipes
                        val newRecipes = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            recipes = currentRecipes + newRecipes
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load more recipes: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun loadMoreUsers() {
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
    val posts: List<AdminContentItem> = emptyList(),
    val recipes: List<AdminContentItem> = emptyList(),
    val users: List<AdminUserItem> = emptyList(),
    val flaggedContent: List<AdminContentItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)