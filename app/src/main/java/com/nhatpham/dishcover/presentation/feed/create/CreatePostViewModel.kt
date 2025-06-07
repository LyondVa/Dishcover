// CreatePostViewModel.kt - Complete fixed version
package com.nhatpham.dishcover.presentation.feed.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.feed.CreatePostWithRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.feed.UploadPostImageUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.ImageUtils
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostWithRecipesUseCase: CreatePostWithRecipesUseCase,
    private val uploadPostImageUseCase: UploadPostImageUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

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
                        }
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Failed to load user: ${result.message}"
                        )
                    }

                    is Resource.Loading -> {
                        // Handle if needed
                    }
                }
            }
        }
    }

    fun onEvent(event: CreatePostEvent) {
        when (event) {
            is CreatePostEvent.CaptionChanged -> {
                _state.value = _state.value.copy(caption = event.caption)
            }

            is CreatePostEvent.HashtagsChanged -> {
                _state.value = _state.value.copy(hashtags = event.hashtags)
            }

            is CreatePostEvent.LocationChanged -> {
                _state.value = _state.value.copy(location = event.location)
            }

            is CreatePostEvent.PrivacyToggled -> {
                _state.value = _state.value.copy(isPublic = !_state.value.isPublic)
            }

            is CreatePostEvent.CommentsToggled -> {
                _state.value = _state.value.copy(allowComments = !_state.value.allowComments)
            }

            is CreatePostEvent.ImageSelected -> {
                addImage(event.uri)
            }

            is CreatePostEvent.ImageRemoved -> {
                removeImage(event.uri)
            }

            is CreatePostEvent.RecipeAdded -> {
                addRecipe(event.recipe)
            }

            is CreatePostEvent.RecipeRemoved -> {
                removeRecipe(event.recipe)
            }

            is CreatePostEvent.ClearSelectedRecipes -> {
                _state.value = _state.value.copy(selectedRecipes = emptyList())
            }

            is CreatePostEvent.CreatePost -> {
                createPostInternal()
            }

            is CreatePostEvent.ResetState -> {
                resetState()
            }
        }
    }

    private fun addImage(uri: Uri) {
        val currentImages = _state.value.selectedImages
        if (currentImages.size < 10 && !currentImages.contains(uri)) {
            _state.value = _state.value.copy(
                selectedImages = currentImages + uri
            )
        }
    }

    private fun removeImage(uri: Uri) {
        _state.value =
            _state.value.copy(selectedImages = _state.value.selectedImages.filter { it != uri })
    }

    private fun addRecipe(recipe: RecipeListItem) {
        val currentRecipes = _state.value.selectedRecipes
        val maxRecipes = _state.value.maxRecipes

        if (currentRecipes.size < maxRecipes && !currentRecipes.any { it.recipeId == recipe.recipeId }) {
            _state.value = _state.value.copy(
                selectedRecipes = currentRecipes + recipe
            )
            Timber.d("Recipe added: ${recipe.title}, Total: ${currentRecipes.size + 1}")
        }
    }

    private fun removeRecipe(recipe: RecipeListItem) {
        _state.value = _state.value.copy(selectedRecipes = _state.value.selectedRecipes.filter {
            it.recipeId != recipe.recipeId
        })
        Timber.d("Recipe removed: ${recipe.title}")
    }

    fun uploadImagesAndCreatePost(context: Context) {
        val userId = currentUserId
        if (userId == null) {
            _state.value = _state.value.copy(
                error = "User not authenticated"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isCreating = true, error = null
            )

            try {
                // Upload images first if any
                var imageUrls = emptyList<String>()
                if (_state.value.selectedImages.isNotEmpty()) {
                    _state.value = _state.value.copy(isUploadingImages = true)

                    try {
                        imageUrls = uploadImages(context, _state.value.selectedImages)
                        _state.value = _state.value.copy(isUploadingImages = false)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isUploadingImages = false,
                            imageUploadError = e.message,
                            isCreating = false
                        )
                        return@launch
                    }
                }

                // Create the post
                val post = Post(
                    postId = UUID.randomUUID().toString(),
                    userId = userId,
                    username = "", // Will be populated by repository
                    content = _state.value.caption,
                    imageUrls = imageUrls,
                    videoUrl = null,
                    hashtags = parseHashtags(_state.value.hashtags),
                    location = _state.value.location.takeIf { it.isNotBlank() },
                    isPublic = _state.value.isPublic,
                    allowComments = _state.value.allowComments,
                    likeCount = 0,
                    commentCount = 0,
                    shareCount = 0,
                    viewCount = 0,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    recipeReferences = emptyList() // Will be set by the use case
                )

                // Create post with recipe references
                createPostWithRecipesUseCase(
                    post, _state.value.selectedRecipes
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            // Keep creating state
                        }

                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isCreating = false,
                                isCreated = true,
                                createdPostId = resource.data?.postId
                            )
                            Timber.d("Post created successfully with ${_state.value.selectedRecipes.size} recipe references")
                        }

                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isCreating = false, error = resource.message
                            )
                            Timber.e("Failed to create post: ${resource.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCreating = false, error = e.localizedMessage ?: "Failed to create post"
                )
                Timber.e(e, "Exception creating post")
            }
        }
    }

    private fun createPostInternal() {
        // This method now just delegates to uploadImagesAndCreatePost
        // Context will need to be passed from UI
        _state.value = _state.value.copy(
            error = "Use uploadImagesAndCreatePost(context) instead"
        )
    }

    private suspend fun uploadImages(context: Context, imageUris: List<Uri>): List<String> {
        val uploadedUrls = mutableListOf<String>()
        val tempPostId = UUID.randomUUID().toString()

        for (uri in imageUris) {
            val imageData = ImageUtils.uriToByteArray(
                context = context, uri = uri, maxWidth = 1024, maxHeight = 1024, quality = 80
            )

            if (imageData != null) {
                uploadPostImageUseCase(tempPostId, imageData).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { url ->
                                uploadedUrls.add(url)
                            }
                        }

                        is Resource.Error -> {
                            throw Exception("Failed to upload image: ${resource.message}")
                        }

                        is Resource.Loading -> {
                            // Continue
                        }
                    }
                }
            } else {
                throw Exception("Failed to convert image to byte array")
            }
        }

        return uploadedUrls
    }

    private fun resetState() {
        _state.value = CreatePostState()
    }

    private fun parseHashtags(input: String): List<String> {
        return input.split("\\s+".toRegex()).filter { it.startsWith("#") && it.length > 1 }
            .map { it.removePrefix("#") }
    }

    // Convenience methods for easier access
    fun onCaptionChanged(caption: String) = onEvent(CreatePostEvent.CaptionChanged(caption))
    fun onHashtagsChanged(hashtags: String) = onEvent(CreatePostEvent.HashtagsChanged(hashtags))
    fun onLocationChanged(location: String) = onEvent(CreatePostEvent.LocationChanged(location))
    fun onPrivacyToggled() = onEvent(CreatePostEvent.PrivacyToggled)
    fun onCommentsToggled() = onEvent(CreatePostEvent.CommentsToggled)
    fun onImageSelected(uri: Uri) = onEvent(CreatePostEvent.ImageSelected(uri))
    fun onImageRemoved(uri: Uri) = onEvent(CreatePostEvent.ImageRemoved(uri))
    fun onRecipeAdded(recipe: RecipeListItem) = onEvent(CreatePostEvent.RecipeAdded(recipe))
    fun onRecipeRemoved(recipe: RecipeListItem) = onEvent(CreatePostEvent.RecipeRemoved(recipe))

    // Note: Use uploadImagesAndCreatePost(context) instead of createPost() for proper image upload
    fun createPost() = onEvent(CreatePostEvent.CreatePost)
}
