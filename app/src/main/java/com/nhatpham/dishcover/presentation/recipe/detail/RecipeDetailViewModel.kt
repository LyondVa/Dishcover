// RecipeDetailViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.usecase.recipe.DeleteRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.MarkRecipeAsFavoriteUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
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
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailViewState())
    val state: StateFlow<RecipeDetailViewState> = _state.asStateFlow()

    private var currentUserId: String? = null

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

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getRecipeUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipe ->
                            _state.update {
                                it.copy(
                                    recipe = recipe,
                                    isLoading = false,
                                    isCurrentUserOwner = recipe.userId == currentUserId
                                )
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

    fun shareRecipe() {
        // Implement share functionality
        // This would typically integrate with platform sharing capabilities
    }

    fun onEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.LoadRecipe -> loadRecipe(event.recipeId)
            RecipeDetailEvent.ToggleFavorite -> toggleFavorite()
            RecipeDetailEvent.DeleteRecipe -> deleteRecipe()
            RecipeDetailEvent.ShareRecipe -> shareRecipe()
        }
    }
}

data class RecipeDetailViewState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val isCurrentUserOwner: Boolean = false,
    val isFavorite: Boolean = false,
    val isUpdatingFavorite: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

sealed class RecipeDetailEvent {
    data class LoadRecipe(val recipeId: String) : RecipeDetailEvent()
    object ToggleFavorite : RecipeDetailEvent()
    object DeleteRecipe : RecipeDetailEvent()
    object ShareRecipe : RecipeDetailEvent()
}