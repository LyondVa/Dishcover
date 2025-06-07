// RecipeSelectionViewModel.kt
package com.nhatpham.dishcover.presentation.feed.create.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesForSelectionUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeSelectionViewModel @Inject constructor(
    private val getUserRecipesUseCase: GetUserRecipesForSelectionUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    data class RecipeSelectionState(
        val recipes: List<RecipeListItem> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(RecipeSelectionState())
    val state: StateFlow<RecipeSelectionState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var currentUserId: String? = null

    init {
        loadCurrentUser()
        observeSearchQuery()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                            // Load initial recipes once we have user
                            loadUserRecipes()
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Failed to load user: ${result.message}",
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

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Debounce search queries
                .distinctUntilChanged()
                .collect { query ->
                    _state.value = _state.value.copy(searchQuery = query)
                    searchRecipes(query)
                }
        }
    }

    fun loadUserRecipes() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            getUserRecipesUseCase(userId, "", 50).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            recipes = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun onSearchChanged(query: String) {
        _searchQuery.value = query
    }

    private fun searchRecipes(query: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            getUserRecipesUseCase(userId, query, 50).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            recipes = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }
}