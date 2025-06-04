// RecipeEditViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeIngredient
import com.nhatpham.dishcover.domain.usecase.recipe.GetCategoriesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.UpdateRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.UploadRecipeImageUseCase
import com.nhatpham.dishcover.util.ImageUtils
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val uploadRecipeImageUseCase: UploadRecipeImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeEditState())
    val state: StateFlow<RecipeEditState> = _state.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getRecipeUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipe ->
                            _state.update {
                                it.copy(
                                    originalRecipe = recipe,
                                    title = recipe.title,
                                    description = recipe.description ?: "",
                                    instructions = recipe.instructions,
                                    prepTime = recipe.prepTime.toString(),
                                    cookTime = recipe.cookTime.toString(),
                                    servings = recipe.servings.toString(),
                                    difficultyLevel = recipe.difficultyLevel,
                                    ingredients = recipe.ingredients,
                                    selectedTags = recipe.tags,
                                    isPublic = recipe.isPublic,
                                    coverImageUri = recipe.coverImage,
                                    isLoading = false
                                )
                            }
                            loadCategories(recipe.userId)
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
                        // Already handled
                    }
                }
            }
        }
    }

    private fun loadCategories(userId: String) {
        viewModelScope.launch {
            getCategoriesUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { categories ->
                            _state.update {
                                it.copy(
                                    availableCategories = categories.toMutableList().apply {
                                        addAll(it.selectedTags.filter { tag -> !categories.contains(tag) })
                                    }
                                )
                            }
                        }
                    }
                    else -> {} // Handle error if needed
                }
            }
        }
    }

    fun updateRecipe() {
        val originalRecipe = state.value.originalRecipe ?: return
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

        // Create updated recipe object
        val updatedRecipe = originalRecipe.copy(
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            prepTime = prepTime.toIntOrNull() ?: 0,
            cookTime = cookTime.toIntOrNull() ?: 0,
            servings = servings.toIntOrNull() ?: 0,
            difficultyLevel = difficultyLevel,
            coverImage = coverImage,
            updatedAt = Timestamp.now(),
            isPublic = isPublic,
            tags = tags
        )

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            updateRecipeUseCase(updatedRecipe).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                isUpdated = true
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
        val ingredientId = "temp_${name.replace(" ", "_").lowercase()}"
        val ingredient = Ingredient(
            ingredientId = ingredientId,
            name = name,
            description = null
        )

        val recipeIngredient = RecipeIngredient(
            recipeIngredientId = "temp_${System.currentTimeMillis()}",
            recipeId = state.value.originalRecipe?.recipeId ?: "",
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

    private fun uploadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingImage = true, imageUploadError = null) }

            try {
                // Convert URI to ByteArray
                val imageData = ImageUtils.uriToByteArray(context, uri)

                if (imageData != null) {
                    // Create a temporary recipe ID for image upload
                    val tempRecipeId = "temp_${System.currentTimeMillis()}"

                    uploadRecipeImageUseCase(tempRecipeId, imageData).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { downloadUrl ->
                                    _state.update {
                                        it.copy(
                                            coverImageUri = downloadUrl,
                                            isUploadingImage = false
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                _state.update {
                                    it.copy(
                                        imageUploadError = result.message ?: "Failed to upload image",
                                        isUploadingImage = false
                                    )
                                }
                            }
                            is Resource.Loading -> {
                                // Already set above
                            }
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            imageUploadError = "Failed to process image",
                            isUploadingImage = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        imageUploadError = "Failed to upload image: ${e.message}",
                        isUploadingImage = false
                    )
                }
            }
        }
    }

    fun onEvent(event: RecipeEditEvent) {
        when (event) {
            is RecipeEditEvent.LoadRecipe -> {
                loadRecipe(event.recipeId)
            }
            is RecipeEditEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.title,
                        titleError = if (event.title.isNotBlank()) null else it.titleError
                    )
                }
            }
            is RecipeEditEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is RecipeEditEvent.InstructionsChanged -> {
                _state.update {
                    it.copy(
                        instructions = event.instructions,
                        instructionsError = if (event.instructions.isNotBlank()) null else it.instructionsError
                    )
                }
            }
            is RecipeEditEvent.PrepTimeChanged -> {
                _state.update { it.copy(prepTime = event.prepTime) }
            }
            is RecipeEditEvent.CookTimeChanged -> {
                _state.update { it.copy(cookTime = event.cookTime) }
            }
            is RecipeEditEvent.ServingsChanged -> {
                _state.update { it.copy(servings = event.servings) }
            }
            is RecipeEditEvent.DifficultyLevelChanged -> {
                _state.update { it.copy(difficultyLevel = event.difficultyLevel) }
            }
            is RecipeEditEvent.PrivacyChanged -> {
                _state.update { it.copy(isPublic = event.isPublic) }
            }
            is RecipeEditEvent.CoverImageChanged -> {
                _state.update { it.copy(coverImageUri = event.imageUri) }
            }
            is RecipeEditEvent.AddIngredient -> {
                addIngredient(
                    name = event.name,
                    quantity = event.quantity,
                    unit = event.unit,
                    notes = event.notes
                )
            }
            is RecipeEditEvent.UploadImage -> {
                uploadImage(event.context, event.uri)
            }
            is RecipeEditEvent.RemoveIngredient -> {
                removeIngredient(event.index)
            }
            is RecipeEditEvent.ToggleTag -> {
                toggleTag(event.tag)
            }
            is RecipeEditEvent.AddCustomTag -> {
                addCustomTag(event.tag)
            }
            RecipeEditEvent.Submit -> {
                updateRecipe()
            }
        }
    }
}

data class RecipeEditState(
    val originalRecipe: Recipe? = null,
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
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadError: String? = null
)

sealed class RecipeEditEvent {
    data class LoadRecipe(val recipeId: String) : RecipeEditEvent()
    data class TitleChanged(val title: String) : RecipeEditEvent()
    data class DescriptionChanged(val description: String) : RecipeEditEvent()
    data class InstructionsChanged(val instructions: String) : RecipeEditEvent()
    data class PrepTimeChanged(val prepTime: String) : RecipeEditEvent()
    data class CookTimeChanged(val cookTime: String) : RecipeEditEvent()
    data class ServingsChanged(val servings: String) : RecipeEditEvent()
    data class DifficultyLevelChanged(val difficultyLevel: String) : RecipeEditEvent()
    data class PrivacyChanged(val isPublic: Boolean) : RecipeEditEvent()
    data class CoverImageChanged(val imageUri: String?) : RecipeEditEvent()
    data class AddIngredient(val name: String, val quantity: String, val unit: String, val notes: String? = null) : RecipeEditEvent()
    data class RemoveIngredient(val index: Int) : RecipeEditEvent()
    data class ToggleTag(val tag: String) : RecipeEditEvent()
    data class AddCustomTag(val tag: String) : RecipeEditEvent()
    object Submit : RecipeEditEvent()
    data class UploadImage(val context: Context, val uri: Uri) : RecipeEditEvent()
}