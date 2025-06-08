// RecipeCreateViewModel.kt
package com.nhatpham.dishcover.presentation.recipe.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.recipe.Ingredient
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeIngredient
import com.nhatpham.dishcover.domain.usecase.recipe.CreateRecipeUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetCategoriesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetSystemIngredientsUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.SearchIngredientsUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.CreateIngredientUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.UploadRecipeImageUseCase
//import com.nhatpham.dishcover.domain.usecase.recipe.GetPopularTagsUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.ImageUtils
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSystemIngredientsUseCase: GetSystemIngredientsUseCase,
    private val searchIngredientsUseCase: SearchIngredientsUseCase,
    private val createIngredientUseCase: CreateIngredientUseCase,
    private val uploadRecipeImageUseCase: UploadRecipeImageUseCase
//    private val getPopularTagsUseCase: GetPopularTagsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeCreateState())
    val state: StateFlow<RecipeCreateState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                            loadUserCategories(user.userId)
                            loadSystemIngredients()
//                            loadPopularTags()
                        }
                    }

                    is Resource.Error -> {
                        _state.update { it.copy(error = result.message) }
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadUserCategories(userId: String) {
        viewModelScope.launch {
            getCategoriesUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { categories ->
                            _state.update {
                                it.copy(
                                    availableCategories = categories,
                                    isLoading = false
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        // Keep existing categories, just log error
                    }

                    is Resource.Loading -> {
                        // Already handled in main loading
                    }
                }
            }
        }
    }

    private fun loadSystemIngredients() {
        viewModelScope.launch {
            getSystemIngredientsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { ingredients ->
                            _state.update {
                                it.copy(systemIngredients = ingredients)
                            }
                        }
                    }

                    is Resource.Error -> {
                        // Just log, don't update UI state
                    }

                    is Resource.Loading -> {
                        // Already handled in main loading
                    }
                }
            }
        }
    }

//    private fun loadPopularTags() {
//        viewModelScope.launch {
//            getPopularTagsUseCase(20).collect { result ->
//                when (result) {
//                    is Resource.Success -> {
//                        result.data?.let { tags ->
//                            _state.update {
//                                it.copy(popularTags = tags)
//                            }
//                        }
//                    }
//                    is Resource.Error -> {
//                        // Just log, don't update UI state
//                    }
//                    is Resource.Loading -> {
//                        // Already handled in main loading
//                    }
//                }
//            }
//        }
//    }

    fun searchIngredients(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(ingredientSearchResults = emptyList()) }
            return
        }

        val userId = currentUserId ?: return

        viewModelScope.launch {
            searchIngredientsUseCase(query, userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { ingredients ->
                            _state.update {
                                it.copy(ingredientSearchResults = ingredients)
                            }
                        }
                    }

                    is Resource.Error -> {
                        // Show local system ingredients that match
                        val localMatches = state.value.systemIngredients.filter {
                            it.name.contains(query, ignoreCase = true)
                        }
                        _state.update {
                            it.copy(ingredientSearchResults = localMatches)
                        }
                    }

                    is Resource.Loading -> {
                        // Show searching state
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
        val instructionSteps = state.value.instructionSteps
        val prepTime = state.value.prepTime
        val cookTime = state.value.cookTime
        val servings = state.value.servings
        val difficultyLevel = state.value.difficultyLevel
        val tags = state.value.selectedTags
        val isPublic = state.value.isPublic
        val coverImage = state.value.coverImageUri

        // Convert instruction steps to single string
        val instructions = instructionSteps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n")

        // Validate inputs
        val titleError = if (title.isBlank()) "Title is required" else null
        val instructionsError =
            if (instructionSteps.none { it.isNotBlank() }) "At least one instruction step is required" else null
        val ingredientsError =
            if (ingredients.isEmpty()) "At least one ingredient is required" else null

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
            recipeId = "",
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
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
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
            return
        }

        val userId = currentUserId ?: return

        viewModelScope.launch {
            // First, try to find existing ingredient
            val existingIngredient = state.value.systemIngredients.find {
                it.name.equals(name, ignoreCase = true)
            }

            val ingredient = existingIngredient ?: run {
                // Create new custom ingredient
                val newIngredient = Ingredient(
                    ingredientId = UUID.randomUUID().toString(),
                    name = name,
                    description = null,
                    category = null,
                    isSystemIngredient = false,
                    createdBy = userId,
                    createdAt = Timestamp.now()
                )

                // Save to backend
                createIngredientUseCase(newIngredient).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Ingredient created successfully
                        }

                        is Resource.Error -> {
                            // Log error but continue with local ingredient
                        }

                        is Resource.Loading -> {
                            // Show loading if needed
                        }
                    }
                }

                newIngredient
            }

            val recipeIngredient = RecipeIngredient(
                recipeIngredientId = UUID.randomUUID().toString(),
                recipeId = "", // Will be set when recipe is created
                ingredientId = ingredient.ingredientId,
                quantity = quantity,
                unit = unit,
                notes = notes?.takeIf { it.isNotBlank() },
                displayOrder = state.value.ingredients.size,
                ingredient = ingredient
            )

            _state.update {
                it.copy(
                    ingredients = it.ingredients + recipeIngredient,
                    ingredientsError = null,
                    ingredientSearchResults = emptyList()
                )
            }
        }
    }

    fun removeIngredient(index: Int) {
        val updatedList = state.value.ingredients.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            // Reorder display order
            updatedList.forEachIndexed { i, ingredient ->
                updatedList[i] = ingredient.copy(displayOrder = i)
            }

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
            if (currentTags.size < 5) { // Limit to 5 tags as per business rules
                currentTags.add(tag)
            }
        }
        _state.update { it.copy(selectedTags = currentTags) }
    }

    fun addCustomTag(tag: String) {
        if (tag.isBlank()) return

        val normalizedTag = tag.trim().lowercase()
        val currentTags = state.value.selectedTags

        if (!currentTags.contains(normalizedTag) && currentTags.size < 5) {
            _state.update {
                it.copy(
                    selectedTags = currentTags + normalizedTag,
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
                                        imageUploadError = result.message
                                            ?: "Failed to upload image",
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

            is RecipeCreateEvent.InstructionStepChanged -> {
                val updatedSteps = state.value.instructionSteps.toMutableList()
                if (event.stepIndex < updatedSteps.size) {
                    updatedSteps[event.stepIndex] = event.instruction
                }
                _state.update {
                    it.copy(
                        instructionSteps = updatedSteps,
                        instructionsError = if (updatedSteps.any { step -> step.isNotBlank() }) null else it.instructionsError
                    )
                }
            }

            is RecipeCreateEvent.AddInstructionStep -> {
                val updatedSteps = state.value.instructionSteps.toMutableList()
                updatedSteps.add("")
                _state.update { it.copy(instructionSteps = updatedSteps) }
            }

            is RecipeCreateEvent.RemoveInstructionStep -> {
                val updatedSteps = state.value.instructionSteps.toMutableList()
                if (event.stepIndex < updatedSteps.size) {
                    updatedSteps.removeAt(event.stepIndex)
                    _state.update {
                        it.copy(
                            instructionSteps = updatedSteps,
                            instructionsError = if (updatedSteps.any { step -> step.isNotBlank() }) null else "At least one instruction step is required"
                        )
                    }
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

            is RecipeCreateEvent.UploadImage -> {
                uploadImage(event.context, event.uri)
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

//    private fun validateRecipeInput(state: RecipeCreateState): Map<String, String> {
//        val errors = mutableMapOf<String, String>()
//
//        if (state.title.isBlank()) {
//            errors["title"] = "Recipe title is required"
//        } else if (state.title.length > 100) {
//            errors["title"] = "Title must be less than 100 characters"
//        }
//
//        if (state.ingredients.isEmpty()) {
//            errors["ingredients"] = "At least one ingredient is required"
//        }
//
//        if (state.instructions.isBlank()) {
//            errors["instructions"] = "Instructions are required"
//        } else if (state.instructions.length > 4096) {
//            errors["instructions"] = "Instructions must be less than 4096 characters"
//        }
//
//        return errors
//    }
}

data class RecipeCreateState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val instructionSteps: List<String> = emptyList(),
    val instructionsError: String? = null,
    val prepTime: String = "",
    val cookTime: String = "",
    val servings: String = "",
    val difficultyLevel: String = "Easy",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val ingredientsError: String? = null,
    val availableCategories: List<String> = emptyList(),
    val selectedTags: List<String> = emptyList(),
    val popularTags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val coverImageUri: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCreated: Boolean = false,
    val createdRecipeId: String? = null,
    val error: String? = null,
    val systemIngredients: List<Ingredient> = emptyList(),
    val ingredientSearchResults: List<Ingredient> = emptyList(),
    val isUploadingImage: Boolean = false,
    val imageUploadError: String? = null
)

sealed class RecipeCreateEvent {
    data class TitleChanged(val title: String) : RecipeCreateEvent()
    data class DescriptionChanged(val description: String) : RecipeCreateEvent()
    data class InstructionStepChanged(val stepIndex: Int, val instruction: String) :
        RecipeCreateEvent()

    object AddInstructionStep : RecipeCreateEvent()
    data class RemoveInstructionStep(val stepIndex: Int) : RecipeCreateEvent()
    data class PrepTimeChanged(val prepTime: String) : RecipeCreateEvent()
    data class CookTimeChanged(val cookTime: String) : RecipeCreateEvent()
    data class ServingsChanged(val servings: String) : RecipeCreateEvent()
    data class DifficultyLevelChanged(val difficultyLevel: String) : RecipeCreateEvent()
    data class PrivacyChanged(val isPublic: Boolean) : RecipeCreateEvent()
    data class CoverImageChanged(val imageUri: String?) : RecipeCreateEvent()
    data class AddIngredient(
        val name: String,
        val quantity: String,
        val unit: String,
        val notes: String? = null
    ) : RecipeCreateEvent()

    data class RemoveIngredient(val index: Int) : RecipeCreateEvent()
    data class ToggleTag(val tag: String) : RecipeCreateEvent()
    data class AddCustomTag(val tag: String) : RecipeCreateEvent()
    object Submit : RecipeCreateEvent()
    data class UploadImage(val context: Context, val uri: Uri) : RecipeCreateEvent()
}