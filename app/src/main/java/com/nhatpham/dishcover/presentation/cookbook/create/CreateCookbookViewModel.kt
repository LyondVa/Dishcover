// CreateCookbookViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.cookbook.Cookbook
import com.nhatpham.dishcover.domain.model.cookbook.CookbookRecipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.cookbook.AddRecipeToCookbookUseCase
import com.nhatpham.dishcover.domain.usecase.cookbook.CreateCookbookUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateCookbookViewModel @Inject constructor(
    private val createCookbookUseCase: CreateCookbookUseCase,
    private val getUserRecipesUseCase: GetUserRecipesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addRecipeToCookbookUseCase: AddRecipeToCookbookUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateCookbookState())
    val state: StateFlow<CreateCookbookState> = _state.asStateFlow()

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
                        loadUserRecipes()
                    }
                    is Resource.Error -> {
                        Timber.e("Error getting current user: ${result.message}")
                        _state.value = _state.value.copy(
                            error = "Authentication error. Please try again."
                        )
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    private fun loadUserRecipes() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingRecipes = true)

            getUserRecipesUseCase(currentUserId, limit = 100).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            availableRecipes = result.data ?: emptyList(),
                            isLoadingRecipes = false
                        )
                    }
                    is Resource.Error -> {
                        Timber.e("Error loading user recipes: ${result.message}")
                        _state.value = _state.value.copy(isLoadingRecipes = false)
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoadingRecipes = true)
                    }
                }
            }
        }
    }


    fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            error = null
        )
        updateCanCreate()
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(
            description = description,
            error = null
        )
    }

    fun updateCoverImage(imageUrl: String?) {
        _state.value = _state.value.copy(
            coverImageUrl = imageUrl,
            error = null
        )
    }

    fun updateIsPublic(isPublic: Boolean) {
        val newState = _state.value.copy(
            isPublic = isPublic,
            error = null
        )

        // If making private, disable collaboration
        if (!isPublic && newState.isCollaborative) {
            _state.value = newState.copy(isCollaborative = false)
        } else {
            _state.value = newState
        }
    }

    fun updateIsCollaborative(isCollaborative: Boolean) {
        _state.value = _state.value.copy(
            isCollaborative = isCollaborative,
            error = null
        )
    }

    fun updateTags(tags: List<String>) {
        _state.value = _state.value.copy(
            tags = tags,
            error = null
        )
    }

    fun addSelectedRecipe(recipe: RecipeListItem) {
        val currentRecipes = _state.value.selectedRecipes.toMutableList()
        if (!currentRecipes.any { it.recipeId == recipe.recipeId }) {
            currentRecipes.add(recipe)
            _state.value = _state.value.copy(selectedRecipes = currentRecipes)
        }
    }

    fun removeSelectedRecipe(recipeId: String) {
        val currentRecipes = _state.value.selectedRecipes.toMutableList()
        currentRecipes.removeAll { it.recipeId == recipeId }
        _state.value = _state.value.copy(selectedRecipes = currentRecipes)
    }

    fun toggleRecipeSelection(recipe: RecipeListItem) {
        val currentRecipes = _state.value.selectedRecipes
        if (currentRecipes.any { it.recipeId == recipe.recipeId }) {
            removeSelectedRecipe(recipe.recipeId)
        } else {
            addSelectedRecipe(recipe)
        }
    }

    private fun updateCanCreate() {
        val currentState = _state.value
        val canCreate = currentState.title.isNotBlank() &&
                currentUserId.isNotEmpty() &&
                !currentState.isLoading

        _state.value = currentState.copy(canCreate = canCreate)
    }

    fun createCookbook() {
        val currentState = _state.value

        if (!currentState.canCreate) {
            _state.value = currentState.copy(
                error = "Please fill in all required fields."
            )
            return
        }

        if (currentUserId.isEmpty()) {
            _state.value = currentState.copy(
                error = "Authentication error. Please try again."
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                error = null
            )

            val cookbook = Cookbook(
                cookbookId = "", // Will be generated by repository
                userId = currentUserId,
                title = currentState.title.trim(),
                description = currentState.description.trim().takeIf { it.isNotBlank() },
                coverImage = currentState.coverImageUrl,
                isPublic = currentState.isPublic,
                isCollaborative = currentState.isCollaborative,
                tags = currentState.tags.filter { it.isNotBlank() },
                recipeCount = currentState.selectedRecipes.size,
                followerCount = 0,
                likeCount = 0,
                viewCount = 0,
                isFeatured = false,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            createCookbookUseCase(cookbook).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already set loading state above
                    }
                    is Resource.Success -> {
                        val createdCookbook = result.data
                        if (createdCookbook != null) {
                            // Add selected recipes to the cookbook
                            addRecipesToCookbook(createdCookbook.cookbookId, currentState.selectedRecipes)
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Failed to create cookbook"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to create cookbook"
                        )
                        Timber.e("Error creating cookbook: ${result.message}")
                    }
                }
            }
        }
    }

    private suspend fun addRecipesToCookbook(cookbookId: String, recipes: List<RecipeListItem>) {
        if (recipes.isEmpty()) {
            // No recipes to add, cookbook creation successful
            _state.value = _state.value.copy(
                isLoading = false,
                isSuccess = true,
                createdCookbookId = cookbookId,
                error = null
            )
            return
        }

        try {
            var successCount = 0
            var failureCount = 0

            recipes.forEachIndexed { index, recipe ->
                val cookbookRecipe = CookbookRecipe(
                    cookbookRecipeId = UUID.randomUUID().toString(),
                    cookbookId = cookbookId,
                    recipeId = recipe.recipeId,
                    addedBy = currentUserId,
                    notes = null,
                    displayOrder = index,
                    addedAt = Timestamp.now()
                )

                addRecipeToCookbookUseCase(cookbookRecipe).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            successCount++
                        }
                        is Resource.Error -> {
                            failureCount++
                            Timber.e("Failed to add recipe ${recipe.recipeId}: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Handle loading
                        }
                    }
                }
            }

            // Update state based on results
            if (failureCount == 0) {
                // All recipes added successfully
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdCookbookId = cookbookId,
                    error = null
                )
            } else {
                // Some recipes failed
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdCookbookId = cookbookId,
                    error = "Cookbook created but ${failureCount} recipes could not be added"
                )
            }

        } catch (e: Exception) {
            Timber.e(e, "Error adding recipes to cookbook")
            _state.value = _state.value.copy(
                isLoading = false,
                isSuccess = true,
                createdCookbookId = cookbookId,
                error = "Cookbook created but recipes could not be added: ${e.message}"
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = CreateCookbookState()
        loadUserRecipes()
    }
}

// CreateCookbookState.kt
data class CreateCookbookState(
    val title: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true,
    val isCollaborative: Boolean = false,
    val tags: List<String> = emptyList(),

    // Recipe selection
    val selectedRecipes: List<RecipeListItem> = emptyList(),
    val availableRecipes: List<RecipeListItem> = emptyList(),
    val isLoadingRecipes: Boolean = false,

    // Creation state
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdCookbookId: String? = null,
    val canCreate: Boolean = false,
    val error: String? = null
)