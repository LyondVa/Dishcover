// FavoritesViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetFavoriteRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.MarkRecipeAsFavoriteUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavoriteRecipesUseCase: GetFavoriteRecipesUseCase,
    private val markRecipeAsFavoriteUseCase: MarkRecipeAsFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesViewState())
    val state: StateFlow<FavoritesViewState> = _state.asStateFlow()

    private var currentUserId: String = ""
    private var currentPage = 0
    private val pageSize = 20

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            currentUserId = user.userId
                            loadFavorites(isRefresh = true)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Failed to load user data"
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

    private fun loadFavorites(isRefresh: Boolean = false) {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            if (isRefresh) {
                currentPage = 0
                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            } else {
                _state.update { it.copy(isLoadingMore = true) }
            }

            val limit = if (isRefresh) pageSize else pageSize * (currentPage + 1)

            getFavoriteRecipesUseCase(currentUserId, limit).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            val newRecipes = if (isRefresh) {
                                recipes
                            } else {
                                // Only add new recipes that aren't already in the list
                                val existingIds = _state.value.favorites.map { it.recipeId }.toSet()
                                _state.value.favorites + recipes.filter { it.recipeId !in existingIds }
                            }

                            val hasMore = recipes.size == pageSize
                            if (!isRefresh) currentPage++

                            _state.update {
                                it.copy(
                                    favorites = newRecipes,
                                    isLoading = false,
                                    isLoadingMore = false,
                                    error = null,
                                    hasMore = hasMore
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = resource.message ?: "Failed to load favorites"
                            )
                        }
                        Timber.e("Error loading favorites: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Loading state already handled above
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.isLoadingMore || !_state.value.hasMore) {
            return
        }
        loadFavorites(isRefresh = false)
    }

    fun retry() {
        loadFavorites(isRefresh = true)
    }

    fun toggleFavorite(recipeId: String) {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            // Optimistically update UI
            val currentFavorites = _state.value.favorites.toMutableList()
            val recipeIndex = currentFavorites.indexOfFirst { it.recipeId == recipeId }

            if (recipeIndex >= 0) {
                // Remove from favorites
                currentFavorites.removeAt(recipeIndex)
                _state.update { it.copy(favorites = currentFavorites) }

                markRecipeAsFavoriteUseCase(currentUserId, recipeId, false).collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            // Revert on error
                            Timber.e("Error removing favorite: ${resource.message}")
                            loadFavorites(isRefresh = true) // Reload to get correct state
                        }
                        is Resource.Success -> {
                            Timber.d("Recipe $recipeId removed from favorites")
                        }
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                    }
                }
            } else {
                // This shouldn't happen in favorites screen, but handle gracefully
                Timber.w("Trying to toggle favorite for recipe not in favorites list: $recipeId")
            }
        }
    }

    fun refreshFavorites() {
        loadFavorites(isRefresh = true)
    }
}

data class FavoritesViewState(
    val favorites: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)