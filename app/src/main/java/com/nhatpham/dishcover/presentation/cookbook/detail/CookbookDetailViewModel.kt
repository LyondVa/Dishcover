// CookbookDetailViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.cookbook.Cookbook
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.cookbook.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CookbookDetailViewModel @Inject constructor(
    private val getCookbookUseCase: GetCookbookUseCase,
    private val getCookbookRecipesUseCase: GetCookbookRecipesUseCase,
    private val followCookbookUseCase: FollowCookbookUseCase,
    private val unfollowCookbookUseCase: UnfollowCookbookUseCase,
    private val likeCookbookUseCase: LikeCookbookUseCase,
    private val unlikeCookbookUseCase: UnlikeCookbookUseCase,
    private val isCookbookFollowedByUserUseCase: IsCookbookFollowedByUserUseCase,
    private val isCookbookLikedByUserUseCase: IsCookbookLikedByUserUseCase,
    private val incrementCookbookViewUseCase: IncrementCookbookViewUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CookbookDetailState())
    val state: StateFlow<CookbookDetailState> = _state.asStateFlow()

    private var currentUserId: String = ""

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUserId = result.data?.userId ?: ""
                    }
                    is Resource.Error -> {
                        Timber.e("Error getting current user: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun onEvent(event: CookbookDetailEvent) {
        when (event) {
            is CookbookDetailEvent.LoadCookbook -> {
                loadCookbook(event.cookbookId)
            }
            is CookbookDetailEvent.ToggleFollow -> {
                toggleFollow()
            }
            is CookbookDetailEvent.ToggleLike -> {
                toggleLike()
            }
            is CookbookDetailEvent.ShareCookbook -> {
                shareCookbook(event.context)
            }
            is CookbookDetailEvent.NavigateToRecipe -> {
                _state.value = _state.value.copy(navigateToRecipe = event.recipeId)
            }
            is CookbookDetailEvent.ClearNavigation -> {
                _state.value = _state.value.copy(navigateToRecipe = null)
            }
            is CookbookDetailEvent.ClearShareSuccess -> {
                _state.value = _state.value.copy(shareSuccess = null)
            }
        }
    }

    private fun loadCookbook(cookbookId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            // Load cookbook data
            getCookbookUseCase(cookbookId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val cookbook = result.data
                        if (cookbook != null) {
                            _state.value = _state.value.copy(
                                cookbook = cookbook,
                                isOwner = cookbook.userId == currentUserId,
                                canAddRecipes = cookbook.userId == currentUserId ||
                                        (cookbook.isCollaborative && currentUserId.isNotEmpty()),
                                isLoading = false,
                                error = null
                            )

                            // Load additional data
                            loadCookbookRecipes(cookbookId)
                            if (currentUserId.isNotEmpty() && cookbook.userId != currentUserId) {
                                loadFollowStatus(cookbookId)
                                loadLikeStatus(cookbookId)
                            }

                            // Increment view count
                            incrementCookbookView(cookbookId)
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Cookbook not found"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load cookbook"
                        )
                        Timber.e("Error loading cookbook: ${result.message}")
                    }
                }
            }
        }
    }

    private fun loadCookbookRecipes(cookbookId: String) {
        viewModelScope.launch {
            getCookbookRecipesUseCase(cookbookId, limit = 50).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            recipes = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Error loading cookbook recipes: ${result.message}")
                        // Don't update error state here, just log it
                    }
                    is Resource.Loading -> {
                        // Already handled by main cookbook loading
                    }
                }
            }
        }
    }

    private fun loadFollowStatus(cookbookId: String) {
        viewModelScope.launch {
            isCookbookFollowedByUserUseCase(currentUserId, cookbookId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isFollowing = result.data ?: false
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Error checking follow status: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    private fun loadLikeStatus(cookbookId: String) {
        viewModelScope.launch {
            isCookbookLikedByUserUseCase(currentUserId, cookbookId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLiked = result.data ?: false
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Error checking like status: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    private fun toggleFollow() {
        val cookbook = _state.value.cookbook ?: return
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            val isCurrentlyFollowing = _state.value.isFollowing

            // Optimistic update
            _state.value = _state.value.copy(isFollowing = !isCurrentlyFollowing)

            val useCase = if (isCurrentlyFollowing) {
                unfollowCookbookUseCase(currentUserId, cookbook.cookbookId)
            } else {
                followCookbookUseCase(currentUserId, cookbook.cookbookId)
            }

            useCase.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update the cookbook's follower count locally
                        val updatedCookbook = cookbook.copy(
                            followerCount = if (isCurrentlyFollowing) {
                                (cookbook.followerCount - 1).coerceAtLeast(0)
                            } else {
                                cookbook.followerCount + 1
                            }
                        )
                        _state.value = _state.value.copy(cookbook = updatedCookbook)
                    }
                    is Resource.Error -> {
                        // Revert optimistic update
                        _state.value = _state.value.copy(isFollowing = isCurrentlyFollowing)
                        Timber.e("Error toggling follow: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Already handled by optimistic update
                    }
                }
            }
        }
    }

    private fun toggleLike() {
        val cookbook = _state.value.cookbook ?: return
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            val isCurrentlyLiked = _state.value.isLiked

            // Optimistic update
            _state.value = _state.value.copy(isLiked = !isCurrentlyLiked)

            val useCase = if (isCurrentlyLiked) {
                unlikeCookbookUseCase(currentUserId, cookbook.cookbookId)
            } else {
                likeCookbookUseCase(currentUserId, cookbook.cookbookId)
            }

            useCase.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update the cookbook's like count locally
                        val updatedCookbook = cookbook.copy(
                            likeCount = if (isCurrentlyLiked) {
                                (cookbook.likeCount - 1).coerceAtLeast(0)
                            } else {
                                cookbook.likeCount + 1
                            }
                        )
                        _state.value = _state.value.copy(cookbook = updatedCookbook)
                    }
                    is Resource.Error -> {
                        // Revert optimistic update
                        _state.value = _state.value.copy(isLiked = isCurrentlyLiked)
                        Timber.e("Error toggling like: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Already handled by optimistic update
                    }
                }
            }
        }
    }

    private fun shareCookbook(context: Context) {
        val cookbook = _state.value.cookbook ?: return

        try {
            val shareText = buildString {
                append("Check out this cookbook: \"${cookbook.title}\"")
                if (cookbook.description != null) {
                    append("\n\n${cookbook.description}")
                }
                append("\n\n📚 ${cookbook.recipeCount} recipes")
                if (cookbook.tags.isNotEmpty()) {
                    append("\n🏷️ ${cookbook.tags.joinToString(", ") { "#$it" }}")
                }
                append("\n\nShared from DishCover")
            }

            val intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this cookbook: ${cookbook.title}")
            }

            val chooser = android.content.Intent.createChooser(intent, "Share cookbook")
            context.startActivity(chooser)

            _state.value = _state.value.copy(shareSuccess = "Cookbook shared!")
        } catch (e: Exception) {
            Timber.e(e, "Error sharing cookbook")
            _state.value = _state.value.copy(shareSuccess = "Failed to share cookbook")
        }
    }

    private fun incrementCookbookView(cookbookId: String) {
        viewModelScope.launch {
            incrementCookbookViewUseCase(cookbookId, currentUserId.takeIf { it.isNotEmpty() })
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Optionally update view count in local state
                            _state.value.cookbook?.let { cookbook ->
                                _state.value = _state.value.copy(
                                    cookbook = cookbook.copy(viewCount = cookbook.viewCount + 1)
                                )
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Error incrementing view count: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                    }
                }
        }
    }
}

// CookbookDetailState.kt
data class CookbookDetailState(
    val cookbook: Cookbook? = null,
    val recipes: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFollowing: Boolean = false,
    val isLiked: Boolean = false,
    val isOwner: Boolean = false,
    val canAddRecipes: Boolean = false,
    val navigateToRecipe: String? = null,
    val shareSuccess: String? = null
)

// CookbookDetailEvent.kt
sealed class CookbookDetailEvent {
    data class LoadCookbook(val cookbookId: String) : CookbookDetailEvent()
    object ToggleFollow : CookbookDetailEvent()
    object ToggleLike : CookbookDetailEvent()
    data class ShareCookbook(val context: Context) : CookbookDetailEvent()
    data class NavigateToRecipe(val recipeId: String) : CookbookDetailEvent()
    object ClearNavigation : CookbookDetailEvent()
    object ClearShareSuccess : CookbookDetailEvent()
}