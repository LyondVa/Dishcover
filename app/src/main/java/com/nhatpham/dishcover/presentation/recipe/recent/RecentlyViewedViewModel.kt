// RecentlyViewedViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecentRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.MarkRecipeAsFavoriteUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getRecentRecipesUseCase: GetRecentRecipesUseCase,
    private val markRecipeAsFavoriteUseCase: MarkRecipeAsFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecentlyViewedViewState())
    val state: StateFlow<RecentlyViewedViewState> = _state.asStateFlow()

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
                            loadRecentRecipes(isRefresh = true)
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

    private fun loadRecentRecipes(isRefresh: Boolean = false) {
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

            getRecentRecipesUseCase(currentUserId, limit).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            val newRecipes = if (isRefresh) {
                                recipes
                            } else {
                                // Only add new recipes that aren't already in the list
                                val existingIds = _state.value.recentRecipes.map { it.recipeId }.toSet()
                                _state.value.recentRecipes + recipes.filter { it.recipeId !in existingIds }
                            }

                            val hasMore = recipes.size == pageSize
                            if (!isRefresh) currentPage++

                            _state.update {
                                it.copy(
                                    recentRecipes = newRecipes,
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
                                error = resource.message ?: "Failed to load recent recipes"
                            )
                        }
                        Timber.e("Error loading recent recipes: ${resource.message}")
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
        loadRecentRecipes(isRefresh = false)
    }

    fun retry() {
        loadRecentRecipes(isRefresh = true)
    }

    fun toggleFavorite(recipeId: String) {
//        if (currentUserId.isEmpty()) return
//
//        viewModelScope.launch {
//            // Find the recipe to check current favorite status
//            val recipe = _state.value.recentRecipes.find { it.recipeId == recipeId }
//            val newFavoriteStatus = !(recipe?.isFavorite ?: false)
//
//            // Optimistically update UI
//            val updatedRecipes = _state.value.recentRecipes.map { currentRecipe ->
//                if (currentRecipe.recipeId == recipeId) {
//                    currentRecipe.copy(isFavorite = newFavoriteStatus)
//                } else {
//                    currentRecipe
//                }
//            }
//            _state.update { it.copy(recentRecipes = updatedRecipes) }
//
//            markRecipeAsFavoriteUseCase(currentUserId, recipeId, newFavoriteStatus).collect { resource ->
//                when (resource) {
//                    is Resource.Error -> {
//                        // Revert on error
//                        Timber.e("Error toggling favorite: ${resource.message}")
//                        val revertedRecipes = _state.value.recentRecipes.map { currentRecipe ->
//                            if (currentRecipe.recipeId == recipeId) {
//                                currentRecipe.copy(isFavorite = !newFavoriteStatus)
//                            } else {
//                                currentRecipe
//                            }
//                        }
//                        _state.update { it.copy(recentRecipes = revertedRecipes) }
//                    }
//                    is Resource.Success -> {
//                        Timber.d("Recipe $recipeId favorite status updated to $newFavoriteStatus")
//                    }
//                    is Resource.Loading -> {
//                        // Handle loading if needed
//                    }
//                }
//            }
//        }
    }

    fun refreshRecentRecipes() {
        loadRecentRecipes(isRefresh = true)
    }
}

data class RecentlyViewedViewState(
    val recentRecipes: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)