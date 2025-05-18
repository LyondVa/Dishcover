package com.nhatpham.dishcover.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.recipe.GetAllRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetCategoriesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetFavoriteRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecentRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetRecipesByCategoryUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavoriteRecipesUseCase: GetFavoriteRecipesUseCase,
    private val getRecentRecipesUseCase: GetRecentRecipesUseCase,
    private val getRecipesByCategoryUseCase: GetRecipesByCategoryUseCase,
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    init {
        loadUserData()
        loadHomeData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update { it.copy(
                                userId = user.userId,
                                isLoading = false
                            ) }
                            loadRecipeData(user.userId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            error = resource.message,
                            isLoading = false
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(
                            isLoading = true
                        ) }
                    }
                }
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(
                favorites = listOf(
                    RecipeListItemUI(id = "1", name = "Tart", imageRes = TART_IMAGE),
                    RecipeListItemUI(id = "2", name = "Pancake", imageRes = PANCAKE_IMAGE),
                    RecipeListItemUI(id = "3", name = "Pasta", imageRes = PASTA_IMAGE)
                ),
                recentRecipes = listOf(
                    RecipeListItemUI(
                        id = "4",
                        name = "Pancake",
                        category = "Breakfast",
                        imageRes = PANCAKE_FEATURE_IMAGE,
                        isFeatured = true
                    )
                ),
                categories = listOf(
                    RecipeListItemUI(id = "5", name = "Cookie", imageRes = COOKIE_IMAGE),
                    RecipeListItemUI(id = "6", name = "Pancake", imageRes = PANCAKE_IMAGE),
                    RecipeListItemUI(id = "7", name = "Pasta", imageRes = PASTA_IMAGE),
                    RecipeListItemUI(id = "8", name = "None", imageRes = NONE_IMAGE)
                ),
                allRecipes = listOf(
                    RecipeListItemUI(id = "9", name = "Tart", imageRes = TART_IMAGE),
                    RecipeListItemUI(id = "10", name = "Pancake", imageRes = PANCAKE_IMAGE),
                    RecipeListItemUI(id = "11", name = "Pasta", imageRes = PASTA_IMAGE),
                    RecipeListItemUI(id = "12", name = "Cookie", imageRes = COOKIE_IMAGE)
                )
            ) }
        }
    }

    private fun loadRecipeData(userId: String) {
        viewModelScope.launch {
            getFavoriteRecipesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            _state.update { it.copy(
                                favorites = recipes.map { recipe -> mapToRecipeListItemUI(recipe) }
                            ) }
                        }
                    }
                    is Resource.Error -> {
                        // Keep existing data, just log the error
                    }
                    is Resource.Loading -> {
                        // Already showing placeholders, no need to update
                    }
                }
            }
        }

        viewModelScope.launch {
            getRecentRecipesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            if (recipes.isNotEmpty()) {
                                _state.update { it.copy(
                                    recentRecipes = recipes.map { recipe ->
                                        mapToRecipeListItemUI(recipe, isFeatured = true)
                                    }
                                ) }
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Keep existing data, just log the error
                    }
                    is Resource.Loading -> {
                        // Already showing placeholders, no need to update
                    }
                }
            }
        }

        viewModelScope.launch {
            getCategoriesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { categoryNames ->
                            val categories = mutableListOf<RecipeListItemUI>()

                            for (category in categoryNames) {
                                getRecipesByCategoryUseCase(userId, category, 1)
                                    .first()
                                    .data?.firstOrNull()?.let { recipe ->
                                        categories.add(mapToRecipeListItemUI(recipe, category = category))
                                    }
                            }

                            if (categories.isNotEmpty()) {
                                _state.update { it.copy(
                                    categories = categories
                                ) }
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Keep existing data, just log the error
                    }
                    is Resource.Loading -> {
                        // Already showing placeholders, no need to update
                    }
                }
            }
        }

        viewModelScope.launch {
            getAllRecipesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { recipes ->
                            _state.update { it.copy(
                                allRecipes = recipes.map { recipe -> mapToRecipeListItemUI(recipe) }
                            ) }
                        }
                    }
                    is Resource.Error -> {
                        // Keep existing data, just log the error
                    }
                    is Resource.Loading -> {
                        // Already showing placeholders, no need to update
                    }
                }
            }
        }
    }

    private fun mapToRecipeListItemUI(
        recipe: RecipeListItem,
        category: String? = null,
        isFeatured: Boolean = false
    ): RecipeListItemUI {
        val imageRes = when {
            recipe.title.contains("tart", ignoreCase = true) -> TART_IMAGE
            recipe.title.contains("pancake", ignoreCase = true) -> if (isFeatured) PANCAKE_FEATURE_IMAGE else PANCAKE_IMAGE
            recipe.title.contains("pasta", ignoreCase = true) -> PASTA_IMAGE
            recipe.title.contains("cookie", ignoreCase = true) -> COOKIE_IMAGE
            else -> IMG_PLACEHOLDER
        }

        return RecipeListItemUI(
            id = recipe.recipeId,
            name = recipe.title,
            category = category,
            imageUrl = recipe.coverImage,
            imageRes = imageRes,
            isFeatured = isFeatured
        )
    }

    companion object {
        // Placeholder image resources - using 'val' instead of 'const val' for resource IDs
        val IMG_PLACEHOLDER = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val TART_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val PANCAKE_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val PASTA_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val COOKIE_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val NONE_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
        val PANCAKE_FEATURE_IMAGE = com.nhatpham.dishcover.R.drawable.ic_launcher_foreground
    }
}

data class HomeViewState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val favorites: List<RecipeListItemUI> = emptyList(),
    val recentRecipes: List<RecipeListItemUI> = emptyList(),
    val categories: List<RecipeListItemUI> = emptyList(),
    val allRecipes: List<RecipeListItemUI> = emptyList()
)

data class RecipeListItemUI(
    val id: String,
    val name: String,
    val category: String? = null,
    val imageUrl: String? = null,
    val imageRes: Int = 0,
    val isFeatured: Boolean = false
)