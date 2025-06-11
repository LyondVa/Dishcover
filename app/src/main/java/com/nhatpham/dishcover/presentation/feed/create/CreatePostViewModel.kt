// CreatePostViewModel.kt - Complete fixed version
package com.nhatpham.dishcover.presentation.feed.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.usecase.feed.CreatePostWithCookbooksUseCase
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
    private val createPostWithCookbooksUseCase: CreatePostWithCookbooksUseCase,
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
            is CreatePostEvent.CookbookAdded -> {
                addCookbook(event.cookbook)
            }
            is CreatePostEvent.CookbookRemoved -> {
                removeCookbook(event.cookbook)
            }
            is CreatePostEvent.ClearSelectedRecipes -> {
                _state.value = _state.value.copy(selectedRecipes = emptyList())
            }
            is CreatePostEvent.ClearSelectedCookbooks -> {
                _state.value = _state.value.copy(selectedCookbooks = emptyList())
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

    private fun addCookbook(cookbook: CookbookListItem) {
        val currentCookbooks = _state.value.selectedCookbooks
        val maxCookbooks = _state.value.maxCookbooks

        if (currentCookbooks.size < maxCookbooks && !currentCookbooks.any { it.cookbookId == cookbook.cookbookId }) {
            _state.value = _state.value.copy(
                selectedCookbooks = currentCookbooks + cookbook
            )
            Timber.d("Cookbook added: ${cookbook.title}, Total: ${currentCookbooks.size + 1}")
        }
    }

    private fun removeCookbook(cookbook: CookbookListItem) {
        _state.value = _state.value.copy(
            selectedCookbooks = _state.value.selectedCookbooks.filter {
                it.cookbookId != cookbook.cookbookId
            }
        )
        Timber.d("Cookbook removed: ${cookbook.title}")
    }

    fun uploadImagesAndCreatePost(context: Context) {
        if (_state.value.selectedImages.isEmpty()) {
            // No images to upload, create post directly
            createPostInternal()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isCreating = true,
                isUploadingImages = true,
                error = null
            )

            val imageUrls = mutableListOf<String>()
            var hasError = false

            for (imageUri in _state.value.selectedImages) {
                try {
                    val byteArray = ImageUtils.uriToByteArray(context, imageUri)
                    byteArray?.let { data ->
                        val postId = UUID.randomUUID().toString()
                        uploadPostImageUseCase(postId, data).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    result.data?.let { url ->
                                        imageUrls.add(url)
                                    }
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        isCreating = false,
                                        isUploadingImages = false,
                                        imageUploadError = result.message
                                    )
                                    hasError = true
                                }
                                is Resource.Loading -> {
                                    // Continue uploading
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        isUploadingImages = false,
                        imageUploadError = e.localizedMessage
                    )
                    hasError = true
                    break
                }
            }

            if (!hasError) {
                _state.value = _state.value.copy(
                    isUploadingImages = false,
                    selectedImages = emptyList()
                )
                createPostInternal(imageUrls)
            }
        }
    }

    private fun createPostInternal(imageUrls: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isCreating = true, error = null)

                val userId = currentUserId ?: throw IllegalStateException("User not logged in")

                val post = Post(
                    postId = UUID.randomUUID().toString(),
                    userId = userId,
                    content = _state.value.caption.trim(),
                    imageUrls = imageUrls,
                    hashtags = extractHashtags(_state.value.hashtags),
                    location = _state.value.location.takeIf { it.isNotBlank() },
                    isPublic = _state.value.isPublic,
                    allowComments = _state.value.allowComments,
                    likeCount = 0,
                    commentCount = 0,
                    shareCount = 0,
                    viewCount = 0,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    recipeReferences = emptyList(), // Will be set by use cases
                    cookbookReferences = emptyList() // Will be set by use cases
                )

                // Determine which use case to use based on what references we have
                when {
                    _state.value.hasRecipes && _state.value.hasCookbooks -> {
                        // Both recipes and cookbooks - use recipes first, then add cookbooks
                        createPostWithRecipesUseCase(post, _state.value.selectedRecipes).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    // Now add cookbook references
                                    result.data?.let { createdPost ->
                                        createPostWithCookbooksUseCase(createdPost, _state.value.selectedCookbooks).collect { cookbookResult ->
                                            when (cookbookResult) {
                                                is Resource.Success -> {
                                                    _state.value = _state.value.copy(
                                                        isCreating = false,
                                                        isCreated = true,
                                                        createdPostId = cookbookResult.data?.postId
                                                    )
                                                }
                                                is Resource.Error -> {
                                                    _state.value = _state.value.copy(
                                                        isCreating = false,
                                                        error = cookbookResult.message
                                                    )
                                                }
                                                is Resource.Loading -> {
                                                    // Continue
                                                }
                                            }
                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        isCreating = false,
                                        error = result.message
                                    )
                                }
                                is Resource.Loading -> {
                                    // Continue
                                }
                            }
                        }
                    }
                    _state.value.hasRecipes -> {
                        // Only recipes
                        createPostWithRecipesUseCase(post, _state.value.selectedRecipes).collect { result ->
                            handlePostCreationResult(result)
                        }
                    }
                    _state.value.hasCookbooks -> {
                        // Only cookbooks
                        createPostWithCookbooksUseCase(post, _state.value.selectedCookbooks).collect { result ->
                            handlePostCreationResult(result)
                        }
                    }
                    else -> {
                        // No references, use regular recipe use case with empty list
                        createPostWithRecipesUseCase(post, emptyList()).collect { result ->
                            handlePostCreationResult(result)
                        }
                    }
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCreating = false,
                    error = e.localizedMessage ?: "Unknown error occurred"
                )
                Timber.e(e, "Error creating post")
            }
        }
    }

    private fun handlePostCreationResult(result: Resource<Post>) {
        when (result) {
            is Resource.Success -> {
                _state.value = _state.value.copy(
                    isCreating = false,
                    isCreated = true,
                    createdPostId = result.data?.postId
                )
                Timber.d("Post created successfully with ${_state.value.selectedRecipes.size} recipes and ${_state.value.selectedCookbooks.size} cookbooks")
            }
            is Resource.Error -> {
                _state.value = _state.value.copy(
                    isCreating = false,
                    error = result.message
                )
                Timber.e("Failed to create post: ${result.message}")
            }
            is Resource.Loading -> {
                // Keep creating state
            }
        }
    }

    private fun extractHashtags(text: String): List<String> {
        return text.split("#")
            .drop(1) // Remove the first empty element
            .map { it.trim().split(" ")[0] } // Take only the first word after #
            .filter { it.isNotBlank() }
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
    fun onCookbookAdded(cookbook: CookbookListItem) = onEvent(CreatePostEvent.CookbookAdded(cookbook))
    fun onCookbookRemoved(cookbook: CookbookListItem) = onEvent(CreatePostEvent.CookbookRemoved(cookbook))

    // Note: Use uploadImagesAndCreatePost(context) instead of createPost() for proper image upload
    fun createPost() = onEvent(CreatePostEvent.CreatePost)
}
