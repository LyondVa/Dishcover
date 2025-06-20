package com.nhatpham.dishcover.presentation.auth.emailverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.auth.CheckEmailVerificationUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationCheckViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val checkEmailVerificationUseCase: CheckEmailVerificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationCheckState())
    val state: StateFlow<EmailVerificationCheckState> = _state.asStateFlow()

    init {
        checkVerificationStatus()
    }

    fun onEvent(event: EmailVerificationCheckEvent) {
        when (event) {
            EmailVerificationCheckEvent.CheckVerificationStatus -> {
                checkVerificationStatus()
            }
            EmailVerificationCheckEvent.NavigateToVerification -> {
                _state.update { it.copy(shouldNavigateToVerification = true) }
            }
        }
    }

    private fun checkVerificationStatus() {
        viewModelScope.launch {
            // First get current user info
            getCurrentUserUseCase().collect { userResult ->
                when (userResult) {
                    is Resource.Success -> {
                        val user = userResult.data
                        _state.update {
                            it.copy(
                                userEmail = user!!.email,
                                isLoading = false
                            )
                        }

                        // If user is already verified in our database, proceed
                        if (user!!.isVerified) {
                            _state.update { it.copy(isVerified = true) }
                        } else {
                            // Check verification status with Firebase
                            checkFirebaseVerificationStatus()
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = userResult.message ?: "Failed to get user info"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = true, error = null)
                        }
                    }
                }
            }
        }
    }

    private fun checkFirebaseVerificationStatus() {
        viewModelScope.launch {
            checkEmailVerificationUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isVerified = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Email not verified yet"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                    }
                }
            }
        }
    }
}

data class EmailVerificationCheckState(
    val isLoading: Boolean = true,
    val isVerified: Boolean = false,
    val userEmail: String = "",
    val shouldNavigateToVerification: Boolean = false,
    val error: String? = null
)

sealed class EmailVerificationCheckEvent {
    object CheckVerificationStatus : EmailVerificationCheckEvent()
    object NavigateToVerification : EmailVerificationCheckEvent()
}