package com.nhatpham.dishcover.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.auth.ResetPasswordUseCase
import com.nhatpham.dishcover.util.Resource
import com.nhatpham.dishcover.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            ForgotPasswordEvent.RequestPasswordReset -> {
                submitResetPassword()
            }
        }
    }

    private fun submitResetPassword() {
        val emailResource = validateEmail(_state.value.email)

        if (!emailResource) {
            return
        }

        requestPasswordReset()
    }

    private fun validateEmail(email: String): Boolean {
        return if (ValidationUtils.validateEmail(email)) {
            _state.update { it.copy(emailError = null) }
            true
        } else {
            _state.update { it.copy(emailError = "Invalid email address") }
            false
        }
    }

    private fun requestPasswordReset() {
        viewModelScope.launch {
            resetPasswordUseCase(
                email = state.value.email
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isResetEmailSent = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to send reset email"
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

enum class PasswordResetStep {
    EMAIL_STEP,
    VERIFICATION_STEP,
    NEW_PASSWORD_STEP
}

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val error: String? = null
)

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    object RequestPasswordReset : ForgotPasswordEvent()
}
