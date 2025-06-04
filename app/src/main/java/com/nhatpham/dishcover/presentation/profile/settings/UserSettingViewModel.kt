package com.nhatpham.dishcover.presentation.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.user.UserNotificationPreferences
import com.nhatpham.dishcover.domain.model.user.UserPrivacySettings
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.auth.SignOutUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetUserPrivacySettingsUseCase
import com.nhatpham.dishcover.domain.usecase.user.UpdateUserPrivacySettingsUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserPrivacySettingsUseCase: GetUserPrivacySettingsUseCase,
    private val updateUserPrivacySettingsUseCase: UpdateUserPrivacySettingsUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserSettingsState())
    val state: StateFlow<UserSettingsState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _state.update { it.copy(
                                userId = user.userId,
                                username = user.username,
                                email = user.email,
                                isLoading = false
                            ) }
                            loadPrivacySettings(user.userId)
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

    private fun loadPrivacySettings(userId: String) {
        viewModelScope.launch {
            getUserPrivacySettingsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { settings ->
                            _state.update { it.copy(privacySettings = settings) }
                        }
                    }
                    is Resource.Error -> {
                        // Just log error, don't update state to keep the UI stable
                    }
                    is Resource.Loading -> {
                        // Already handled above
                    }
                }
            }
        }
    }

    fun updatePrivacySettings(settings: UserPrivacySettings) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }

            updateUserPrivacySettingsUseCase(settings).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { updatedSettings ->
                            _state.update {
                                it.copy(
                                    privacySettings = updatedSettings,
                                    isUpdating = false,
                                    updateSuccessMessage = "Privacy settings updated successfully"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                updateError = result.message ?: "Failed to update settings",
                                isUpdating = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already set above
                    }
                }
            }
        }
    }

    fun updateNotificationPreferences(preferences: UserNotificationPreferences) {
        // Implement notification preferences update
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isSignedOut = true) }
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

    fun clearUpdateMessage() {
        _state.update { it.copy(updateSuccessMessage = null, updateError = null) }
    }
}

data class UserSettingsState(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val privacySettings: UserPrivacySettings? = null,
    val notificationPreferences: UserNotificationPreferences? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isSignedOut: Boolean = false,
    val error: String? = null,
    val updateError: String? = null,
    val updateSuccessMessage: String? = null
)