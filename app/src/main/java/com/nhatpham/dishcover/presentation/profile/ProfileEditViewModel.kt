package com.nhatpham.dishcover.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.user.UpdateUserProfileUseCase
import com.nhatpham.dishcover.domain.usecase.user.UploadProfileImageUseCase
import com.nhatpham.dishcover.domain.usecase.user.UploadBannerImageUseCase
import com.nhatpham.dishcover.util.ImageUtils
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val uploadBannerImageUseCase: UploadBannerImageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileEditState())
    val state: StateFlow<ProfileEditState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            Timber.tag("ProfileEditViewModel").d("User data loaded: $user")
                            _state.update {
                                it.copy(
                                    userId = user.userId,
                                    initialUsername = user.username,
                                    initialBio = user.bio,
                                    profilePictureUri = user.profilePicture,
                                    bannerImageUri = user.bannerImage, // Load existing banner
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileEditEvent) {
        when (event) {
            is ProfileEditEvent.ProfilePictureSelected -> {
                _state.update { it.copy(profilePictureUri = event.uri) }
                Timber.tag("ProfileEditViewModel").d("Profile picture selected: ${event.uri}")
            }
            is ProfileEditEvent.BannerImageSelected -> {
                _state.update { it.copy(bannerImageUri = event.uri) }
                Timber.tag("ProfileEditViewModel").d("Banner image selected: ${event.uri}")
            }
            is ProfileEditEvent.SaveProfile -> {
                saveProfileWithImages(
                    context = event.context,
                    username = event.username,
                    fullName = event.fullName,
                    bio = event.bio,
                    website = event.website,
                    location = event.location
                )
            }
        }
    }

    private fun saveProfileWithImages(
        context: Context,
        username: String,
        fullName: String,
        bio: String,
        website: String,
        location: String
    ) {
        if (username.isBlank()) {
            _state.update { it.copy(error = "Username cannot be empty") }
            return
        }

        val userId = state.value.userId
        if (userId.isBlank()) {
            _state.update { it.copy(error = "User ID not available") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // Get current user data
                getCurrentUserUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { currentUser ->
                                // Upload images if they were changed
                                var profileImageUrl = currentUser.profilePicture
                                var bannerImageUrl = currentUser.bannerImage

                                // Upload profile image if changed
                                val currentProfileUri = state.value.profilePictureUri
                                if (currentProfileUri != null && currentProfileUri != currentUser.profilePicture) {
                                    profileImageUrl = uploadProfileImage(context, Uri.parse(currentProfileUri), userId)
                                }

                                // Upload banner image if changed
                                val currentBannerUri = state.value.bannerImageUri
                                if (currentBannerUri != null && currentBannerUri != currentUser.bannerImage) {
                                    bannerImageUrl = uploadBannerImage(context, Uri.parse(currentBannerUri), userId)
                                }

                                // Update user with new data
                                val updatedUser = currentUser.copy(
                                    username = username,
                                    bio = bio,
                                    profilePicture = profileImageUrl,
                                    bannerImage = bannerImageUrl
                                )

                                updateUserProfileUseCase(updatedUser).collect { updateResult ->
                                    when (updateResult) {
                                        is Resource.Success -> {
                                            _state.update {
                                                it.copy(
                                                    isSaving = false,
                                                    isSuccess = true,
                                                    isUploadingProfile = false,
                                                    isUploadingBanner = false
                                                )
                                            }
                                        }
                                        is Resource.Error -> {
                                            _state.update {
                                                it.copy(
                                                    error = updateResult.message,
                                                    isSaving = false,
                                                    isUploadingProfile = false,
                                                    isUploadingBanner = false
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
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    error = result.message,
                                    isSaving = false,
                                    isUploadingProfile = false,
                                    isUploadingBanner = false
                                )
                            }
                        }
                        is Resource.Loading -> {
                            // Already handled
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isSaving = false,
                        isUploadingProfile = false,
                        isUploadingBanner = false
                    )
                }
            }
        }
    }

    private suspend fun uploadProfileImage(context: Context, uri: Uri, userId: String): String? {
        return try {
            _state.update { it.copy(isUploadingProfile = true) }

            val imageData = ImageUtils.uriToByteArray(
                context = context,
                uri = uri,
                maxWidth = 512,
                maxHeight = 512,
                quality = 90
            )

            if (imageData != null) {
                var uploadedUrl: String? = null
                uploadProfileImageUseCase(userId, imageData).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            uploadedUrl = resource.data
                        }
                        is Resource.Error -> {
                            throw Exception("Failed to upload profile image: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            // Continue
                        }
                    }
                }
                uploadedUrl
            } else {
                throw Exception("Failed to convert profile image to byte array")
            }
        } catch (e: Exception) {
            _state.update { it.copy(isUploadingProfile = false) }
            throw e
        }
    }

    private suspend fun uploadBannerImage(context: Context, uri: Uri, userId: String): String? {
        return try {
            _state.update { it.copy(isUploadingBanner = true) }

            val imageData = ImageUtils.uriToByteArray(
                context = context,
                uri = uri,
                maxWidth = 1920,
                maxHeight = 1080,
                quality = 85
            )

            if (imageData != null) {
                var uploadedUrl: String? = null
                uploadBannerImageUseCase(userId, imageData).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            uploadedUrl = resource.data
                        }
                        is Resource.Error -> {
                            throw Exception("Failed to upload banner image: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            // Continue
                        }
                    }
                }
                uploadedUrl
            } else {
                throw Exception("Failed to convert banner image to byte array")
            }
        } catch (e: Exception) {
            _state.update { it.copy(isUploadingBanner = false) }
            throw e
        }
    }
}

data class ProfileEditState(
    val userId: String = "",
    val initialUsername: String = "",
    val initialFullName: String = "",
    val initialBio: String? = null,
    val initialWebsite: String? = null,
    val initialLocation: String? = null,
    val profilePictureUri: String? = null,
    val bannerImageUri: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isUploadingBanner: Boolean = false,
    val isUploadingProfile: Boolean = false
)

sealed class ProfileEditEvent {
    data class ProfilePictureSelected(val uri: String) : ProfileEditEvent()
    data class BannerImageSelected(val uri: String) : ProfileEditEvent()
    data class SaveProfile(
        val context: Context, // Added context for image uploads
        val username: String,
        val fullName: String,
        val bio: String,
        val website: String,
        val location: String
    ) : ProfileEditEvent()
}