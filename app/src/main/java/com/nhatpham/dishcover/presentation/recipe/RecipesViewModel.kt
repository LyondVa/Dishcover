package com.nhatpham.dishcover.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetAllRecipesUseCase
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
class RecipesViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllRecipesUseCase: GetAllRecipesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipesViewState())
    val state: StateFlow<RecipesViewState> = _state.asStateFlow()

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
                            loadRecipes()
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

    fun loadRecipes() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getAllRecipesUseCase(userId, limit = 50).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipes ->
                            _state.update {
                                it.copy(
                                    recipes = recipes,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Failed to load recipes",
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

    fun refreshRecipes() {
        loadRecipes()
    }

    fun selectTab(tabIndex: Int) {
        _state.update { it.copy(selectedTab = tabIndex) }
    }
}

data class RecipesViewState(
    val recipes: List<RecipeListItem> = emptyList(),
    val selectedTab: Int = 0, // 0 = Recipes, 1 = Cookbooks
    val isLoading: Boolean = false,
    val error: String? = null
)