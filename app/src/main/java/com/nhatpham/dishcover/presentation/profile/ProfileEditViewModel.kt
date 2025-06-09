package com.nhatpham.dishcover.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.user.UserProfile
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetUserProfileUseCase
import com.nhatpham.dishcover.domain.usecase.user.UpdateUserProfileUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
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
                            _state.update {
                                it.copy(
                                    userId = user.userId,
                                    initialUsername = user.username,
                                    initialBio = user.bio,
                                    profilePictureUri = user.profilePicture,
                                    isLoading = false
                                )
                            }

                            // Load full profile data if available
                            loadUserProfile(user.userId)
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

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            getUserProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { profile ->
                            if (profile is UserProfile) {
                                // If we're using the extended UserProfile model
                                _state.update {
                                    it.copy(
                                        initialFullName = profile.fullName,
                                        initialWebsite = profile.website,
                                        initialLocation = profile.location
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Just log error, don't update state to avoid losing default values
                    }
                    is Resource.Loading -> {
                        // Already handled
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileEditEvent) {
        when (event) {
            is ProfileEditEvent.ProfilePictureSelected -> {
                _state.update { it.copy(profilePictureUri = event.uri) }
            }
            is ProfileEditEvent.SaveProfile -> {
                saveProfile(
                    username = event.username,
                    fullName = event.fullName,
                    bio = event.bio,
                    website = event.website,
                    location = event.location
                )
            }

            is ProfileEditEvent.BannerImageSelected -> {
                _state.update { it.copy(bannerImageUri = event.uri) }
            }
        }
    }

    private fun saveProfile(
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

        // Since we're working with both User model and UserProfile model,
        // we'll need to update both or create appropriate adapter methods
        // For this example, we'll use the standard User model:

        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { currentUser ->
                            val updatedUser = currentUser.copy(
                                username = username,
                                bio = bio,
                                profilePicture = state.value.profilePictureUri
                                // Add other fields as needed
                            )

                            updateUserProfileUseCase(updatedUser).collect { updateResult ->
                                when (updateResult) {
                                    is Resource.Success -> {
                                        _state.update {
                                            it.copy(
                                                isSaving = false,
                                                isSuccess = true
                                            )
                                        }
                                    }
                                    is Resource.Error -> {
                                        _state.update {
                                            it.copy(
                                                error = updateResult.message,
                                                isSaving = false
                                            )
                                        }
                                    }
                                    is Resource.Loading -> {
                                        // Already set
                                    }
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message,
                                isSaving = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already set
                    }
                }
            }
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
    val isUploadingBanner: Boolean= false,
    val isUploadingProfile: Boolean= false
)

sealed class ProfileEditEvent {
    data class ProfilePictureSelected(val uri: String) : ProfileEditEvent()
    data class BannerImageSelected(val uri: String) : ProfileEditEvent()
    data class SaveProfile(
        val username: String,
        val fullName: String,
        val bio: String,
        val website: String,
        val location: String
    ) : ProfileEditEvent()
}