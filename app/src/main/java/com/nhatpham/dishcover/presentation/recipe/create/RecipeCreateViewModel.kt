// RecipeCreateViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.Ingredient
import com.nhatpham.dishcover.domain.model.Recipe
import com.nhatpham.dishcover.domain.model.RecipeIngredient
import com.nhatpham.dishcover.domain.usecase.recipe.CreateRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetCategoriesUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeCreateState())
    val state: StateFlow<RecipeCreateState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
        loadCategories()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                        }
                    }
                    else -> {} // Handle error if needed
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                getCategoriesUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { categories ->
                                _state.update { it.copy(availableCategories = categories) }
                            }
                        }
                        else -> {} // Handle error if needed
                    }
                }
            }
        }
    }

    fun createRecipe() {
        val userId = currentUserId ?: return
        val title = state.value.title
        val description = state.value.description
        val ingredients = state.value.ingredients
        val instructions = state.value.instructions
        val prepTime = state.value.prepTime
        val cookTime = state.value.cookTime
        val servings = state.value.servings
        val difficultyLevel = state.value.difficultyLevel
        val tags = state.value.selectedTags
        val isPublic = state.value.isPublic
        val coverImage = state.value.coverImageUri

        // Validate inputs
        val titleError = if (title.isBlank()) "Title is required" else null
        val instructionsError = if (instructions.isBlank()) "Instructions are required" else null
        val ingredientsError = if (ingredients.isEmpty()) "At least one ingredient is required" else null

        if (titleError != null || instructionsError != null || ingredientsError != null) {
            _state.update {
                it.copy(
                    titleError = titleError,
                    instructionsError = instructionsError,
                    ingredientsError = ingredientsError
                )
            }
            return
        }

        // Create recipe object
        val recipe = Recipe(
            recipeId = "",  // Will be set by Firebase
            userId = userId,
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            prepTime = prepTime.toIntOrNull() ?: 0,
            cookTime = cookTime.toIntOrNull() ?: 0,
            servings = servings.toIntOrNull() ?: 0,
            difficultyLevel = difficultyLevel,
            coverImage = coverImage,
            createdAt = Date(),
            updatedAt = Date(),
            isPublic = isPublic,
            viewCount = 0,
            likeCount = 0,
            isFeatured = false,
            tags = tags
        )

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            createRecipeUseCase(recipe).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                isCreated = true,
                                createdRecipeId = result.data?.recipeId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                error = result.message
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

    fun addIngredient(
        name: String,
        quantity: String,
        unit: String,
        notes: String? = null
    ) {
        if (name.isBlank() || quantity.isBlank()) {
            return  // Validate basic inputs
        }

        // Create a simplified version for this example
        // In a real app, we would need to handle ingredient creation/fetching
        val ingredientId = "temp_${name.replace(" ", "_").lowercase()}"
        val ingredient = Ingredient(
            ingredientId = ingredientId,
            name = name,
            description = null
        )

        val recipeIngredient = RecipeIngredient(
            recipeIngredientId = "temp_${System.currentTimeMillis()}",
            recipeId = "",  // Will be set later
            ingredientId = ingredientId,
            quantity = quantity,
            unit = unit,
            notes = notes,
            displayOrder = state.value.ingredients.size,
            ingredient = ingredient
        )

        _state.update {
            it.copy(
                ingredients = it.ingredients + recipeIngredient,
                ingredientsError = null
            )
        }
    }

    fun removeIngredient(index: Int) {
        val updatedList = state.value.ingredients.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            _state.update {
                it.copy(
                    ingredients = updatedList,
                    ingredientsError = if (updatedList.isEmpty()) "At least one ingredient is required" else null
                )
            }
        }
    }

    fun toggleTag(tag: String) {
        val currentTags = state.value.selectedTags.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _state.update { it.copy(selectedTags = currentTags) }
    }

    fun addCustomTag(tag: String) {
        if (tag.isBlank()) return

        val normalizedTag = tag.trim().lowercase()
        if (!state.value.selectedTags.contains(normalizedTag)) {
            _state.update {
                it.copy(
                    selectedTags = it.selectedTags + normalizedTag,
                    availableCategories = if (!it.availableCategories.contains(normalizedTag)) {
                        it.availableCategories + normalizedTag
                    } else {
                        it.availableCategories
                    }
                )
            }
        }
    }

    fun onEvent(event: RecipeCreateEvent) {
        when (event) {
            is RecipeCreateEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.title,
                        titleError = if (event.title.isNotBlank()) null else it.titleError
                    )
                }
            }
            is RecipeCreateEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is RecipeCreateEvent.InstructionsChanged -> {
                _state.update {
                    it.copy(
                        instructions = event.instructions,
                        instructionsError = if (event.instructions.isNotBlank()) null else it.instructionsError
                    )
                }
            }
            is RecipeCreateEvent.PrepTimeChanged -> {
                _state.update { it.copy(prepTime = event.prepTime) }
            }
            is RecipeCreateEvent.CookTimeChanged -> {
                _state.update { it.copy(cookTime = event.cookTime) }
            }
            is RecipeCreateEvent.ServingsChanged -> {
                _state.update { it.copy(servings = event.servings) }
            }
            is RecipeCreateEvent.DifficultyLevelChanged -> {
                _state.update { it.copy(difficultyLevel = event.difficultyLevel) }
            }
            is RecipeCreateEvent.PrivacyChanged -> {
                _state.update { it.copy(isPublic = event.isPublic) }
            }
            is RecipeCreateEvent.CoverImageChanged -> {
                _state.update { it.copy(coverImageUri = event.imageUri) }
            }
            is RecipeCreateEvent.AddIngredient -> {
                addIngredient(
                    name = event.name,
                    quantity = event.quantity,
                    unit = event.unit,
                    notes = event.notes
                )
            }
            is RecipeCreateEvent.RemoveIngredient -> {
                removeIngredient(event.index)
            }
            is RecipeCreateEvent.ToggleTag -> {
                toggleTag(event.tag)
            }
            is RecipeCreateEvent.AddCustomTag -> {
                addCustomTag(event.tag)
            }
            RecipeCreateEvent.Submit -> {
                createRecipe()
            }
        }
    }
}

data class RecipeCreateState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val instructions: String = "",
    val instructionsError: String? = null,
    val prepTime: String = "",
    val cookTime: String = "",
    val servings: String = "",
    val difficultyLevel: String = "Easy",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val ingredientsError: String? = null,
    val availableCategories: List<String> = emptyList(),
    val selectedTags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val coverImageUri: String? = null,
    val isSubmitting: Boolean = false,
    val isCreated: Boolean = false,
    val createdRecipeId: String? = null,
    val error: String? = null
)

sealed class RecipeCreateEvent {
    data class TitleChanged(val title: String) : RecipeCreateEvent()
    data class DescriptionChanged(val description: String) : RecipeCreateEvent()
    data class InstructionsChanged(val instructions: String) : RecipeCreateEvent()
    data class PrepTimeChanged(val prepTime: String) : RecipeCreateEvent()
    data class CookTimeChanged(val cookTime: String) : RecipeCreateEvent()
    data class ServingsChanged(val servings: String) : RecipeCreateEvent()
    data class DifficultyLevelChanged(val difficultyLevel: String) : RecipeCreateEvent()
    data class PrivacyChanged(val isPublic: Boolean) : RecipeCreateEvent()
    data class CoverImageChanged(val imageUri: String?) : RecipeCreateEvent()
    data class AddIngredient(val name: String, val quantity: String, val unit: String, val notes: String? = null) : RecipeCreateEvent()
    data class RemoveIngredient(val index: Int) : RecipeCreateEvent()
    data class ToggleTag(val tag: String) : RecipeCreateEvent()
    data class AddCustomTag(val tag: String) : RecipeCreateEvent()
    object Submit : RecipeCreateEvent()
}