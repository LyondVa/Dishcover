package com.nhatpham.dishcover.presentation.recipe.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.usecase.recipe.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import com.nhatpham.dishcover.util.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val markRecipeAsFavoriteUseCase: MarkRecipeAsFavoriteUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val incrementViewCountUseCase: IncrementViewCountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailViewState())
    val state: StateFlow<RecipeDetailViewState> = _state.asStateFlow()

    private var currentUserId: String? = null
    private var hasIncrementedView = false

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                            checkFavoriteStatus()
                        }
                    }
                    else -> {} // Handle error if needed
                }
            }
        }
    }

    fun loadRecipe(recipeId: String, isSharedView: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSharedView = isSharedView) }

            getRecipeUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipe ->
                            val canView = recipe.isPublic || recipe.userId == currentUserId

                            _state.update {
                                it.copy(
                                    recipe = recipe,
                                    isLoading = false,
                                    isCurrentUserOwner = recipe.userId == currentUserId,
                                    canViewRecipe = canView
                                )
                            }

                            // Increment view count for public recipes viewed by others
                            if (canView && recipe.userId != currentUserId && !hasIncrementedView) {
                                incrementViewCount(recipeId)
                                hasIncrementedView = true
                            }

                            checkFavoriteStatus()
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message,
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

    private fun incrementViewCount(recipeId: String) {
        viewModelScope.launch {
            incrementViewCountUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Optionally update the view count in the current state
                        _state.update { currentState ->
                            currentState.recipe?.let { recipe ->
                                currentState.copy(
                                    recipe = recipe.copy(viewCount = recipe.viewCount + 1)
                                )
                            } ?: currentState
                        }
                    }
                    is Resource.Error -> {
                        // Silently handle error - view count increment is not critical
                    }
                    is Resource.Loading -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun checkFavoriteStatus() {
        val recipeId = state.value.recipe?.recipeId ?: return
        val userId = currentUserId ?: return

        // In a real app, we would check the favorite status from the repository
        // For now, we'll just assume it's not a favorite initially
        _state.update { it.copy(isFavorite = false) }
    }

    fun toggleFavorite() {
        val recipe = state.value.recipe ?: return
        val userId = currentUserId ?: return
        val newFavoriteStatus = !state.value.isFavorite

        viewModelScope.launch {
            _state.update { it.copy(isUpdatingFavorite = true) }

            markRecipeAsFavoriteUseCase(userId, recipe.recipeId, newFavoriteStatus).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                isUpdatingFavorite = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message,
                                isUpdatingFavorite = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled
                    }
                }
            }
        }
    }

    fun deleteRecipe() {
        val recipe = state.value.recipe ?: return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }

            deleteRecipeUseCase(recipe.recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isDeleted = true,
                                isDeleting = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message,
                                isDeleting = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled
                    }
                }
            }
        }
    }

    fun shareRecipe(context: Context) {
        val recipe = state.value.recipe ?: return

        if (!recipe.isPublic) {
            _state.update {
                it.copy(error = "This recipe is private and cannot be shared")
            }
            return
        }

        try {
            val shareLink = ShareUtils.generateWebShareLink(recipe.recipeId)
            val shareText = ShareUtils.buildShareText(
                title = recipe.title,
                description = recipe.description,
                prepTime = recipe.prepTime,
                cookTime = recipe.cookTime,
                servings = recipe.servings,
                difficulty = recipe.difficultyLevel,
                shareLink = shareLink
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe: ${recipe.title}")
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Recipe")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            // Show success message
            _state.update {
                it.copy(shareSuccess = "Recipe shared successfully!")
            }

        } catch (e: Exception) {
            _state.update {
                it.copy(error = "Failed to share recipe: ${e.message}")
            }
        }
    }

    fun onEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.LoadRecipe -> loadRecipe(event.recipeId, event.isSharedView)
            RecipeDetailEvent.ToggleFavorite -> toggleFavorite()
            RecipeDetailEvent.DeleteRecipe -> deleteRecipe()
            is RecipeDetailEvent.ShareRecipe -> shareRecipe(event.context)
            RecipeDetailEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            RecipeDetailEvent.ClearShareSuccess -> {
                _state.update { it.copy(shareSuccess = null) }
            }
        }
    }
}

data class RecipeDetailViewState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val isCurrentUserOwner: Boolean = false,
    val canViewRecipe: Boolean = false,
    val isSharedView: Boolean = false,
    val isFavorite: Boolean = false,
    val isUpdatingFavorite: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
    val shareSuccess: String? = null
)

sealed class RecipeDetailEvent {
    data class LoadRecipe(val recipeId: String, val isSharedView: Boolean = false) : RecipeDetailEvent()
    object ToggleFavorite : RecipeDetailEvent()
    object DeleteRecipe : RecipeDetailEvent()
    data class ShareRecipe(val context: Context) : RecipeDetailEvent()
    object ClearError : RecipeDetailEvent()
    object ClearShareSuccess : RecipeDetailEvent()
}