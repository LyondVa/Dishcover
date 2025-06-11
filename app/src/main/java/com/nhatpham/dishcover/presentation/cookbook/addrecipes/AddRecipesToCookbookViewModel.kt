// AddRecipesToCookbookViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook.addrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.cookbook.Cookbook
import com.nhatpham.dishcover.domain.model.cookbook.CookbookRecipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.cookbook.AddRecipeToCookbookUseCase
import com.nhatpham.dishcover.domain.usecase.cookbook.GetCookbookRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.cookbook.GetCookbookUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddRecipesToCookbookViewModel @Inject constructor(
    private val getCookbookUseCase: GetCookbookUseCase,
    private val getUserRecipesUseCase: GetUserRecipesUseCase,
    private val getCookbookRecipesUseCase: GetCookbookRecipesUseCase,
    private val addRecipeToCookbookUseCase: AddRecipeToCookbookUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddRecipesToCookbookState())
    val state: StateFlow<AddRecipesToCookbookState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().first().data?.let { user ->
                currentUserId = user.userId
            }
        }
    }

    fun loadCookbook(cookbookId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cookbookId = cookbookId,
                isLoading = true,
                error = null
            )

            try {
                // Load cookbook details
                getCookbookUseCase(cookbookId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                cookbook = resource.data,
                                isLoading = false
                            )
                            // Load existing recipes in cookbook
                            loadExistingRecipes(cookbookId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = resource.message ?: "Failed to load cookbook"
                            )
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading cookbook")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load cookbook: ${e.message}"
                )
            }
        }
    }

    private fun loadExistingRecipes(cookbookId: String) {
        viewModelScope.launch {
            try {
                getCookbookRecipesUseCase(cookbookId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val existingRecipeIds = resource.data?.map { it.recipeId }?.toSet() ?: emptySet()
                            _state.value = _state.value.copy(
                                existingRecipeIds = existingRecipeIds
                            )
                        }
                        is Resource.Error -> {
                            Timber.e("Error loading existing recipes: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            // Already handled in main loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading existing recipes")
            }
        }
    }

    fun loadUserRecipes() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                try {
                    getUserRecipesUseCase(userId, limit = 100).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val recipes = resource.data ?: emptyList()
                                _state.value = _state.value.copy(
                                    availableRecipes = recipes,
                                    filteredRecipes = filterRecipes(recipes, _state.value.searchQuery)
                                )
                            }
                            is Resource.Error -> {
                                Timber.e("Error loading user recipes: ${resource.message}")
                            }
                            is Resource.Loading -> {
                                // Handled by main loading state
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading user recipes")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredRecipes = filterRecipes(_state.value.availableRecipes, query)
        )
    }

    private fun filterRecipes(recipes: List<RecipeListItem>, query: String): List<RecipeListItem> {
        if (query.isBlank()) return recipes

        return recipes.filter { recipe ->
            recipe.title.contains(query, ignoreCase = true) ||
                    recipe.description?.contains(query, ignoreCase = true) == true ||
                    recipe.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }

    fun toggleRecipeSelection(recipeId: String) {
        val currentSelected = _state.value.selectedRecipes.toMutableSet()

        if (currentSelected.contains(recipeId)) {
            currentSelected.remove(recipeId)
        } else {
            currentSelected.add(recipeId)
        }

        _state.value = _state.value.copy(selectedRecipes = currentSelected)
    }

    fun addSelectedRecipes() {
        val cookbook = _state.value.cookbook ?: return
        val selectedRecipeIds = _state.value.selectedRecipes
        val currentUser = currentUserId ?: return

        if (selectedRecipeIds.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isAdding = true, error = null)

            try {
                var successCount = 0
                var errorCount = 0

                selectedRecipeIds.forEach { recipeId ->
                    val cookbookRecipe = CookbookRecipe(
                        cookbookRecipeId = UUID.randomUUID().toString(),
                        cookbookId = cookbook.cookbookId,
                        recipeId = recipeId,
                        addedBy = currentUser,
                        notes = null,
                        displayOrder = 0, // Will be set by repository
                        addedAt = Timestamp.now()
                    )

                    addRecipeToCookbookUseCase(cookbookRecipe).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                successCount++
                            }
                            is Resource.Error -> {
                                errorCount++
                                Timber.e("Failed to add recipe $recipeId: ${resource.message}")
                            }
                            is Resource.Loading -> {
                                // Handle loading if needed
                            }
                        }
                    }
                }

                if (errorCount == 0) {
                    // All recipes added successfully
                    _state.value = _state.value.copy(
                        isAdding = false,
                        navigationEvent = AddRecipesToCookbookNavigationEvent.NavigateBack
                    )
                } else {
                    // Some recipes failed to add
                    _state.value = _state.value.copy(
                        isAdding = false,
                        error = "Failed to add $errorCount recipes. $successCount recipes were added successfully."
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error adding recipes to cookbook")
                _state.value = _state.value.copy(
                    isAdding = false,
                    error = "Failed to add recipes: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearNavigationEvent() {
        _state.value = _state.value.copy(navigationEvent = null)
    }
}

data class AddRecipesToCookbookState(
    val cookbookId: String = "",
    val cookbook: Cookbook? = null,
    val availableRecipes: List<RecipeListItem> = emptyList(),
    val filteredRecipes: List<RecipeListItem> = emptyList(),
    val existingRecipeIds: Set<String> = emptySet(),
    val selectedRecipes: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val error: String? = null,
    val navigationEvent: AddRecipesToCookbookNavigationEvent? = null
)

sealed class AddRecipesToCookbookNavigationEvent {
    object NavigateBack : AddRecipesToCookbookNavigationEvent()
}