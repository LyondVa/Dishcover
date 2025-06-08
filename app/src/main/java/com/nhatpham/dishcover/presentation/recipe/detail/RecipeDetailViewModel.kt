// RecipeDetailViewModel.kt - Fixed getCurrentUserUseCase issue
package com.nhatpham.dishcover.presentation.recipe.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.domain.usecase.recipe.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val getRecipeRatingsUseCase: GetRecipeRatingsUseCase,
    private val addRecipeRatingUseCase: AddRecipeRatingUseCase,
    private val getRecipeReviewsUseCase: GetRecipeReviewsUseCase,
    private val addRecipeReviewUseCase: AddRecipeReviewUseCase,
    private val markReviewHelpfulUseCase: MarkReviewHelpfulUseCase,
    private val scaleRecipeIngredientsUseCase: ScaleRecipeIngredientsUseCase,
    private val getNutritionalInfoUseCase: GetNutritionalInfoUseCase,
    private val calculateNutritionalInfoUseCase: CalculateNutritionalInfoUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailState())
    val state: StateFlow<RecipeDetailState> = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        // Get current user ID on initialization
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                currentUserId = when (resource) {
                    is Resource.Success -> {
                        resource.data?.userId
                    }

                    else -> {
                        null
                    }
                }
            }
        }
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load recipe details
            getRecipeUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { recipe ->
                            _state.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    recipe = recipe,
                                    scaledRecipe = recipe,
                                    currentServings = recipe.servings,
                                    originalServings = recipe.servings,
                                    error = null
                                )
                            }

                            // Load additional data
                            loadRatings(recipeId)
                            loadReviews(recipeId)
                            loadNutritionalInfo(recipeId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load recipe"
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

    private fun loadRatings(recipeId: String) {
        viewModelScope.launch {
            getRecipeRatingsUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(ratingAggregate = result.data)
                        }
                    }
                    is Resource.Error -> {
                        // Handle silently for ratings
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(ratingsLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadReviews(recipeId: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            val offset = if (loadMore) _state.value.reviews.size else 0

            getRecipeReviewsUseCase(recipeId, limit = 20, offset = offset).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { newReviews ->
                            _state.update { currentState ->
                                val updatedReviews = if (loadMore) {
                                    currentState.reviews + newReviews
                                } else {
                                    newReviews
                                }
                                currentState.copy(
                                    reviews = updatedReviews,
                                    reviewsLoading = false,
                                    hasMoreReviews = newReviews.size >= 20
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(reviewsLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(reviewsLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadNutritionalInfo(recipeId: String) {
        viewModelScope.launch {
            getNutritionalInfoUseCase(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                nutritionalInfo = result.data,
                                nutritionLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(nutritionLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(nutritionLoading = true) }
                    }
                }
            }
        }
    }

    fun onServingsChanged(newServings: Int) {
        val originalRecipe = _state.value.recipe ?: return

        if (newServings <= 0) return

        val scaledRecipe = scaleRecipeIngredientsUseCase(originalRecipe, newServings)

        _state.update {
            it.copy(
                scaledRecipe = scaledRecipe,
                currentServings = newServings
            )
        }
    }

    fun onRatingSubmitted(rating: Int) {
        val recipeId = _state.value.recipe?.recipeId ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch {
            addRecipeRatingUseCase(recipeId, userId, rating).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Reload ratings to get updated aggregate
                        loadRatings(recipeId)
                        _state.update {
                            it.copy(userRating = rating)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = result.message ?: "Failed to submit rating")
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    fun onReviewSubmitted(
        rating: Int,
        comment: String,
        images: List<String> = emptyList(),
        verified: Boolean = false
    ) {
        val recipeId = _state.value.recipe?.recipeId ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch {
            // Get current user info for the review
            getCurrentUserUseCase().firstOrNull()?.let { userResource ->
                when (userResource) {
                    is Resource.Success -> {
                        userResource.data?.let { currentUser ->
                            addRecipeReviewUseCase(
                                recipeId = recipeId,
                                userId = userId,
                                userName = currentUser.username,
                                rating = rating,
                                comment = comment,
                                images = images,
                                verified = verified
                            ).collect { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        // Reload reviews and ratings
                                        loadReviews(recipeId)
                                        loadRatings(recipeId)
                                        _state.update {
                                            it.copy(showReviewDialog = false)
                                        }
                                    }
                                    is Resource.Error -> {
                                        _state.update {
                                            it.copy(error = result.message ?: "Failed to submit review")
                                        }
                                    }
                                    is Resource.Loading -> {
                                        // Handle loading state if needed
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        _state.update {
                            it.copy(error = "User not found")
                        }
                    }
                }
            }
        }
    }

    fun onReviewHelpful(reviewId: String, helpful: Boolean) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            markReviewHelpfulUseCase(reviewId, userId, helpful).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update local state optimistically
                        _state.update { currentState ->
                            val updatedReviews = currentState.reviews.map { review ->
                                if (review.reviewId == reviewId) {
                                    review.copy(
                                        helpful = if (helpful) review.helpful + 1 else maxOf(0, review.helpful - 1)
                                    )
                                } else {
                                    review
                                }
                            }
                            currentState.copy(reviews = updatedReviews)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = result.message ?: "Failed to mark review as helpful")
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    fun onCalculateNutrition() {
        val recipe = _state.value.scaledRecipe ?: return

        viewModelScope.launch {
            _state.update { it.copy(nutritionLoading = true) }

            calculateNutritionalInfoUseCase(recipe).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                nutritionalInfo = result.data,
                                nutritionLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                nutritionLoading = false,
                                error = result.message ?: "Failed to calculate nutrition"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(nutritionLoading = true) }
                    }
                }
            }
        }
    }

    fun onLoadMoreReviews() {
        val recipeId = _state.value.recipe?.recipeId ?: return
        loadReviews(recipeId, loadMore = true)
    }

    fun onShowReviewDialog() {
        _state.update { it.copy(showReviewDialog = true) }
    }

    fun onHideReviewDialog() {
        _state.update { it.copy(showReviewDialog = false) }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    fun onEvent(){

    }
}

data class RecipeDetailState(
    val isLoading: Boolean = false,
    val recipe: Recipe? = null,
    val scaledRecipe: Recipe? = null,
    val currentServings: Int = 1,
    val originalServings: Int = 1,
    val ratingAggregate: RecipeRatingAggregate? = null,
    val reviews: List<RecipeReview> = emptyList(),
    val nutritionalInfo: NutritionalInfo? = null,
    val userRating: Int = 0,
    val ratingsLoading: Boolean = false,
    val reviewsLoading: Boolean = false,
    val nutritionLoading: Boolean = false,
    val hasMoreReviews: Boolean = false,
    val showReviewDialog: Boolean = false,
    val error: String? = null
)

sealed class RecipeDetailEvent {
    data class LoadRecipe(val recipeId: String) : RecipeDetailEvent()
    data class ServingsChanged(val servings: Int) : RecipeDetailEvent()
    data class RatingSubmitted(val rating: Int) : RecipeDetailEvent()
    data class ReviewSubmitted(
        val rating: Int,
        val comment: String,
        val images: List<String> = emptyList(),
        val verified: Boolean = false
    ) : RecipeDetailEvent()
    data class ReviewHelpful(val reviewId: String, val helpful: Boolean) : RecipeDetailEvent()
    object CalculateNutrition : RecipeDetailEvent()
    object LoadMoreReviews : RecipeDetailEvent()
    object ShowReviewDialog : RecipeDetailEvent()
    object HideReviewDialog : RecipeDetailEvent()
    object ErrorDismissed : RecipeDetailEvent()
}