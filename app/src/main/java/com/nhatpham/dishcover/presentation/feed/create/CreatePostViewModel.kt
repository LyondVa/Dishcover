package com.nhatpham.dishcover.presentation.feed.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostType
import com.nhatpham.dishcover.domain.usecase.feed.CreatePostUseCase
import com.nhatpham.dishcover.domain.usecase.feed.UploadPostImageUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.ImageUtils
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val uploadPostImageUseCase: UploadPostImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostViewState())
    val state: StateFlow<CreatePostViewState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            _state.update {
                                it.copy(currentUserId = user.userId)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = resource.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun onCaptionChanged(caption: String) {
        _state.update { it.copy(caption = caption) }
    }

    fun onImageSelected(uri: Uri) {
        _state.update { currentState ->
            val updatedImages = currentState.selectedImages.toMutableList()
            if (updatedImages.size < MAX_IMAGES) {
                updatedImages.add(uri)
            }
            currentState.copy(selectedImages = updatedImages)
        }
    }

    fun onImageRemoved(uri: Uri) {
        _state.update { currentState ->
            val updatedImages = currentState.selectedImages.toMutableList()
            updatedImages.remove(uri)
            currentState.copy(selectedImages = updatedImages)
        }
    }

    fun onHashtagsChanged(hashtags: String) {
        val hashtagList = hashtags.split(" ")
            .filter { it.startsWith("#") && it.length > 1 }
            .map { it.removePrefix("#") }

        _state.update { it.copy(hashtags = hashtagList) }
    }

    fun onLocationChanged(location: String) {
        _state.update { it.copy(location = location.takeIf { it.isNotBlank() }) }
    }

    fun onPrivacyToggled() {
        _state.update { it.copy(isPublic = !it.isPublic) }
    }

    fun onCommentsToggled() {
        _state.update { it.copy(allowComments = !it.allowComments) }
    }

    fun createPost() {
        val currentState = _state.value

        if (currentState.currentUserId.isEmpty()) {
            _state.update { it.copy(error = "User not found") }
            return
        }

        if (currentState.caption.isBlank() && currentState.selectedImages.isEmpty()) {
            _state.update { it.copy(error = "Please add some content or images") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }

            try {
                // Create the post first with a temporary ID
                val tempPostId = UUID.randomUUID().toString()

                // Upload images first if any
                val imageUrls = if (currentState.selectedImages.isNotEmpty()) {
                    uploadImages(tempPostId, currentState.selectedImages)
                } else {
                    emptyList()
                }

                // Create the post with uploaded image URLs
                val post = Post(
                    postId = tempPostId,
                    userId = currentState.currentUserId,
                    content = currentState.caption,
                    imageUrls = imageUrls,
                    postType = if (imageUrls.isNotEmpty()) PostType.IMAGE else PostType.TEXT,
                    hashtags = currentState.hashtags,
                    location = currentState.location,
                    isPublic = currentState.isPublic,
                    allowComments = currentState.allowComments,
                    allowShares = true
                )

                createPostUseCase(post).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    isCreating = false,
                                    isPostCreated = true,
                                    createdPost = resource.data
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    isCreating = false,
                                    error = resource.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _state.update { it.copy(isCreating = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isCreating = false,
                        error = e.message ?: "Failed to create post"
                    )
                }
            }
        }
    }

    private suspend fun uploadImages(postId: String, imageUris: List<Uri>): List<String> {
        val uploadedUrls = mutableListOf<String>()

        for (uri in imageUris) {
            try {
                // Convert Uri to ByteArray using ImageUtils
                val imageData = ImageUtils.uriToByteArray(
                    context = context,
                    uri = uri,
                    maxWidth = 1024,
                    maxHeight = 1024,
                    quality = 80
                )

                if (imageData != null) {
                    // Upload the image
                    uploadPostImageUseCase(postId, imageData).collect { resource ->
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
                                // Handle loading state
                            }
                        }
                    }
                } else {
                    throw Exception("Failed to convert image to byte array")
                }
            } catch (e: Exception) {
                throw e
            }
        }

        return uploadedUrls
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetState() {
        _state.update { CreatePostViewState(currentUserId = it.currentUserId) }
    }

    companion object {
        const val MAX_IMAGES = 4
    }
}

data class CreatePostViewState(
    val currentUserId: String = "",
    val caption: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val location: String? = null,
    val isPublic: Boolean = true,
    val allowComments: Boolean = true,
    val isCreating: Boolean = false,
    val isPostCreated: Boolean = false,
    val createdPost: Post? = null,
    val error: String? = null
)