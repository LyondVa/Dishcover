package com.nhatpham.dishcover.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetCategoriesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetFavoriteRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecentRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavoriteRecipesUseCase: GetFavoriteRecipesUseCase,
    private val getRecentRecipesUseCase: GetRecentRecipesUseCase,
    private val getUserRecipesUseCase: GetUserRecipesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update {
                                it.copy(
                                    username = user.username,
                                    userId = user.userId,
                                    isLoading = false
                                )
                            }
                            loadRecipeData(user.userId)
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = resource.message, isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadRecipeData(userId: String) {
        loadFavoriteRecipes(userId)
        loadRecentRecipes(userId)
        loadAllRecipes(userId)
        loadCategories(userId)
    }

    private fun loadFavoriteRecipes(userId: String) {
        viewModelScope.launch {
            getFavoriteRecipesUseCase(userId, limit = 10).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            _state.update {
                                it.copy(isFavoritesLoading = false)
                            }
                            _state.update {
                                it.copy(favorites = recipes)
                            }
                        }
                    }

                    is Resource.Error -> {
                        // Keep existing data, just log the error
                        _state.update {
                            it.copy(
                                favoriteError = resource.message
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isFavoritesLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadRecentRecipes(userId: String) {
        viewModelScope.launch {
            getRecentRecipesUseCase(userId, limit = 10).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            _state.update {
                                it.copy(
                                    recentRecipes = recipes, isRecentLoading = false
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                recentError = resource.message, isRecentLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isRecentLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadAllRecipes(userId: String) {
        viewModelScope.launch {
            getUserRecipesUseCase(userId, limit = 20).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            _state.update {
                                it.copy(
                                    allRecipes = recipes, isAllRecipesLoading = false
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                allRecipesError = resource.message, isAllRecipesLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isAllRecipesLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadCategories(userId: String) {
        viewModelScope.launch {
            getCategoriesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { categoryNames ->
                            _state.update {
                                it.copy(
                                    availableCategories = categoryNames, isCategoriesLoading = false
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                categoriesError = resource.message, isCategoriesLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isCategoriesLoading = true) }
                    }
                }
            }
        }
    }

    fun refreshData() {
        val userId = state.value.userId
        if (userId.isNotEmpty()) {
            loadRecipeData(userId)
        }
    }

    fun clearError(errorType: String) {
        _state.update { currentState ->
            when (errorType) {
                "favorites" -> currentState.copy(favoriteError = null)
                "recent" -> currentState.copy(recentError = null)
                "all" -> currentState.copy(allRecipesError = null)
                "categories" -> currentState.copy(categoriesError = null)
                "general" -> currentState.copy(error = null)
                else -> currentState
            }
        }
    }
}

data class HomeViewState(
    val username: String = "User",
    val userId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    // Favorites
    val favorites: List<RecipeListItem> = emptyList(),
    val isFavoritesLoading: Boolean = false,
    val favoriteError: String? = null,

    // Recent Recipes
    val recentRecipes: List<RecipeListItem> = emptyList(),
    val isRecentLoading: Boolean = false,
    val recentError: String? = null,

    // All Recipes
    val allRecipes: List<RecipeListItem> = emptyList(),
    val isAllRecipesLoading: Boolean = false,
    val allRecipesError: String? = null,

    // Categories
    val availableCategories: List<String> = emptyList(),
    val isCategoriesLoading: Boolean = false,
    val categoriesError: String? = null
)