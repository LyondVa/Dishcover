package com.nhatpham.dishcover.presentation.auth.emailverification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.auth.SendEmailVerificationUseCase
import com.nhatpham.dishcover.domain.usecase.auth.CheckEmailVerificationUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val checkEmailVerificationUseCase: CheckEmailVerificationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    init {
        val email = savedStateHandle.get<String>("email") ?: ""
        _state.update { it.copy(email = email) }
    }

    fun onEvent(event: EmailVerificationEvent) {
        when (event) {
            EmailVerificationEvent.SendVerificationEmail -> {
                sendVerificationEmail()
            }
            EmailVerificationEvent.CheckVerificationStatus -> {
                checkVerificationStatus()
            }
            EmailVerificationEvent.ResendEmail -> {
                resendEmail()
            }
        }
    }

    private fun sendVerificationEmail() {
        viewModelScope.launch {
            sendEmailVerificationUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isEmailSent = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to send verification email"
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

    private fun checkVerificationStatus() {
        viewModelScope.launch {
            checkEmailVerificationUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isVerificationComplete = true,
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

    private fun resendEmail() {
        _state.update {
            it.copy(
                isEmailSent = false,
                error = null
            )
        }
        sendVerificationEmail()
    }
}

data class EmailVerificationState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val isVerificationComplete: Boolean = false,
    val error: String? = null
)

sealed class EmailVerificationEvent {
    object SendVerificationEmail : EmailVerificationEvent()
    object CheckVerificationStatus : EmailVerificationEvent()
    object ResendEmail : EmailVerificationEvent()
}