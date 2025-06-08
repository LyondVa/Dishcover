// RecipeDetailViewModel.kt - Fixed visibility issues
package com.nhatpham.dishcover.presentation.recipe.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeRatingAggregate
import com.nhatpham.dishcover.domain.model.recipe.RecipeReview
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo
import com.nhatpham.dishcover.domain.usecase.recipe.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import com.nhatpham.dishcover.util.ShareUtils
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

    fun onEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.LoadRecipe -> loadRecipe(event.recipeId)
            is RecipeDetailEvent.ServingsChanged -> onServingsChanged(event.servings)
            is RecipeDetailEvent.RatingSubmitted -> onRatingSubmitted(event.rating)
            is RecipeDetailEvent.ReviewSubmitted -> onReviewSubmitted(event.rating, event.comment, event.images, event.verified)
            is RecipeDetailEvent.ReviewHelpful -> onReviewHelpful(event.reviewId, event.helpful)
            is RecipeDetailEvent.CalculateNutrition -> onCalculateNutrition()
            is RecipeDetailEvent.LoadMoreReviews -> onLoadMoreReviews()
            is RecipeDetailEvent.ShowReviewDialog -> onShowReviewDialog()
            is RecipeDetailEvent.HideReviewDialog -> onHideReviewDialog()
            is RecipeDetailEvent.ErrorDismissed -> onErrorDismissed()
            is RecipeDetailEvent.ShareRecipe -> shareRecipe()
            is RecipeDetailEvent.ClearShareSuccess -> clearShareSuccess()
        }
    }

    // Make these functions public
    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

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
                                    error = null,
                                    canViewRecipe = true,
                                    isCurrentUserOwner = recipe.userId == currentUserId
                                )
                            }

                            loadRatings(recipeId)
                            loadReviews(recipeId)
                            loadNutritionalInfo(recipeId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load recipe",
                                canViewRecipe = false
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

    fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
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

    fun onReviewSubmitted(rating: Int, comment: String, images: List<String> = emptyList(), verified: Boolean = false) {
        val recipeId = _state.value.recipe?.recipeId ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch {
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

    fun onLoadMoreReviews() {
        val recipeId = _state.value.recipe?.recipeId ?: return
        loadReviews(recipeId, loadMore = true)
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

    fun onShowReviewDialog() {
        _state.update { it.copy(showReviewDialog = true) }
    }

    fun onHideReviewDialog() {
        _state.update { it.copy(showReviewDialog = false) }
    }

    fun shareRecipe() {
        // Implement share functionality
        // This would typically integrate with platform sharing capabilities
        fun shareRecipe(context: Context) {
            val recipe = state.value.recipe ?: return

            if (!recipe.isPublic) {
                _state.update {
                    it.copy(error = "This recipe is private and cannot be shared")
                }
                return
            }

            try {
                val shareLink = ShareUtils.generateWebShareLink(recipe.recipeId)
                val shareText = ShareUtils.buildShareText(
                    title = recipe.title,
                    description = recipe.description,
                    prepTime = recipe.prepTime,
                    cookTime = recipe.cookTime,
                    servings = recipe.servings,
                    difficulty = recipe.difficultyLevel,
                    shareLink = shareLink
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe: ${recipe.title}")
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Share Recipe")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                // Show success message
                _state.update {
                    it.copy(shareSuccess = "Recipe shared successfully!")
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to share recipe: ${e.message}")
                }
            }
        }
    }

    private fun clearShareSuccess() {
        _state.update { it.copy(shareSuccess = null) }
    }

    // Private helper functions
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
}

data class RecipeDetailState(
    val isCurrentUserOwner: Boolean = false,
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
    val error: String? = null,
    val shareSuccess: String? = null,
    val canViewRecipe: Boolean = false
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
    data class ShareRecipe(val context: Context) : RecipeDetailEvent()
    object ClearShareSuccess : RecipeDetailEvent()
}