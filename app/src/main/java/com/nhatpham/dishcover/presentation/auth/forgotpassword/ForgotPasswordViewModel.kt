package com.nhatpham.dishcover.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.ConfirmPasswordResetUseCase
import com.nhatpham.dishcover.domain.usecase.ResetPasswordUseCase
import com.nhatpham.dishcover.domain.usecase.VerifyPasswordResetCodeUseCase
import com.nhatpham.dishcover.util.Resource
import com.nhatpham.dishcover.util.ValidationUtils
import com.nhatpham.dishcover.util.error.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is ForgotPasswordEvent.VerificationCodeChanged -> {
                _state.update { it.copy(verificationCode = event.code) }
            }
            is ForgotPasswordEvent.NewPasswordChanged -> {
                _state.update { it.copy(newPassword = event.password) }
            }
            is ForgotPasswordEvent.ConfirmNewPasswordChanged -> {
                _state.update { it.copy(confirmNewPassword = event.password) }
            }
            is ForgotPasswordEvent.RequestPasswordReset -> {
                submitResetPassword()
            }
            is ForgotPasswordEvent.VerifyCode -> {
                verifyCode()
            }
            is ForgotPasswordEvent.SubmitNewPassword -> {
                submitNewPassword()
            }
            is ForgotPasswordEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
            is ForgotPasswordEvent.BackToEmailStep -> {
                _state.update {
                    it.copy(
                        currentStep = PasswordResetStep.EMAIL_STEP,
                        verificationCode = "",
                        newPassword = "",
                        confirmNewPassword = "",
                        verificationCodeError = null,
                        newPasswordError = null,
                        confirmNewPasswordError = null
                    )
                }
            }
        }
    }

    private fun submitResetPassword() {
        val emailResult = validateEmail(_state.value.email)

        if (!emailResult) {
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
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isResetEmailSent = true,
                                currentStep = PasswordResetStep.VERIFICATION_STEP,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = it.error ?: "An unknown error occurred"
                            )
                        }
                    }
                    is Result.Loading -> {
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

    private fun verifyCode() {
        val codeResult = validateVerificationCode(_state.value.verificationCode)

        if (!codeResult) {
            return
        }

        viewModelScope.launch {
            verifyPasswordResetCodeUseCase(
                code = state.value.verificationCode
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                currentStep = PasswordResetStep.NEW_PASSWORD_STEP,
                                verifiedEmail = result.data,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                verificationCodeError = it.error ?: "Invalid verification code",
                                error = null
                            )
                        }
                    }
                    is Result.Loading -> {
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

    private fun validateVerificationCode(code: String): Boolean {
        return if (code.isNotBlank()) {
            _state.update { it.copy(verificationCodeError = null) }
            true
        } else {
            _state.update { it.copy(verificationCodeError = "Verification code cannot be empty") }
            false
        }
    }

    private fun submitNewPassword() {
        val passwordResult = validatePassword(_state.value.newPassword)
        val confirmPasswordResult = validateConfirmPassword(
            _state.value.newPassword,
            _state.value.confirmNewPassword
        )

        val hasError = listOf(
            passwordResult,
            confirmPasswordResult
        ).any { !it }

        if (hasError) {
            return
        }

        viewModelScope.launch {
            confirmPasswordResetUseCase(
                code = state.value.verificationCode,
                newPassword = state.value.newPassword
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isPasswordResetComplete = true,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = it.error ?: "Failed to reset password"
                            )
                        }
                    }
                    is Result.Loading -> {
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

    private fun validatePassword(password: String): Boolean {
        return if (ValidationUtils.validatePassword(password)) {
            _state.update { it.copy(newPasswordError = null) }
            true
        } else {
            _state.update {
                it.copy(newPasswordError = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, and 1 number")
            }
            false
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            _state.update { it.copy(confirmNewPasswordError = null) }
            true
        } else {
            _state.update { it.copy(confirmNewPasswordError = "Passwords do not match") }
            false
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
    val verificationCode: String = "",
    val verificationCodeError: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmNewPassword: String = "",
    val confirmNewPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val isPasswordResetComplete: Boolean = false,
    val currentStep: PasswordResetStep = PasswordResetStep.EMAIL_STEP,
    val verifiedEmail: String = "",
    val error: String? = null
)

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    data class VerificationCodeChanged(val code: String) : ForgotPasswordEvent()
    data class NewPasswordChanged(val password: String) : ForgotPasswordEvent()
    data class ConfirmNewPasswordChanged(val password: String) : ForgotPasswordEvent()
    object RequestPasswordReset : ForgotPasswordEvent()
    object VerifyCode : ForgotPasswordEvent()
    object SubmitNewPassword : ForgotPasswordEvent()
    object TogglePasswordVisibility : ForgotPasswordEvent()
    object BackToEmailStep : ForgotPasswordEvent()
}