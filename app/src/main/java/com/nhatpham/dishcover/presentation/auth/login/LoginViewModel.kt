package com.nhatpham.dishcover.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.usecase.SignInUseCase
import com.nhatpham.dishcover.domain.usecase.SignInWithGoogleUseCase
import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.ErrorHandler
import com.nhatpham.dishcover.util.error.ErrorRecoveryAction
import com.nhatpham.dishcover.util.error.ErrorState
import com.nhatpham.dishcover.util.error.AuthError
import com.nhatpham.dishcover.util.error.handleErrorsIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced LoginViewModel that implements our error handling system
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                clearFieldError("email")
                _state.update { it.copy(email = event.email) }
            }

            is LoginEvent.PasswordChanged -> {
                clearFieldError("password")
                _state.update { it.copy(password = event.password) }
            }

            is LoginEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }

            is LoginEvent.Submit -> {
                submitLogin()
            }

            is LoginEvent.GoogleSignIn -> {
                signInWithGoogle(event.idToken)
            }

            is LoginEvent.DismissError -> {
                _state.update {
                    it.copy(
                        error = null, errorMessage = null, recoveryAction = null, showError = false
                    )
                }
            }

            is LoginEvent.HandleRecoveryAction -> {
                handleRecoveryAction(event.action)
            }

            is LoginEvent.ResendVerificationEmail -> {
                resendVerificationEmail()
            }
        }
    }

    private fun submitLogin() {
        if (!validateInputs()) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            signInUseCase(
                email = state.value.email, password = state.value.password
            ).handleErrorsIn(viewModelScope = viewModelScope,
                errorHandler = errorHandler,
                updateState = { error, errorMessage, recoveryAction ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                            errorMessage = errorMessage,
                            recoveryAction = recoveryAction,
                            showError = errorMessage != null
                        )
                    }

                    // Handle specific field errors
                    when (error) {
                        is AuthError.InvalidEmailError -> {
                            _state.update { it.copy(emailError = errorMessage) }
                        }

                        is AppError.AuthError.InvalidCredentialsError -> {
                            _state.update { it.copy(passwordError = errorMessage) }
                        }

                        is AuthError.EmailVerificationRequiredError -> {

                        }

                        else -> {

                        }
                    }
                },
                onSuccess = { user: User ->
                    _state.update {
                        it.copy(
                            isLoading = false, isSuccess = true
                        )
                    }
                })
        }
    }

    private fun signInWithGoogle(idToken: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            signInWithGoogleUseCase(idToken).handleErrorsIn(viewModelScope = viewModelScope,
                errorHandler = errorHandler,
                updateState = { error: AppError?, errorMessage: String?, recoveryAction: ErrorRecoveryAction? ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                            errorMessage = errorMessage,
                            recoveryAction = recoveryAction,
                            showError = errorMessage != null
                        )
                    }
                },
                onSuccess = { user: User ->
                    _state.update {
                        it.copy(
                            isLoading = false, isSuccess = true
                        )
                    }
                })
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate email
        if (state.value.email.isBlank()) {
            _state.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!isValidEmail(state.value.email)) {
            _state.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        // Validate password
        if (state.value.password.isBlank()) {
            _state.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }

        return isValid
    }

    private fun clearFieldError(field: String) {
        when (field) {
            "email" -> _state.update { it.copy(emailError = null) }
            "password" -> _state.update { it.copy(passwordError = null) }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }

    private fun handleRecoveryAction(action: ErrorRecoveryAction) {
        when (action) {
            is ErrorRecoveryAction.Retry -> {
                submitLogin()
            }

            is ErrorRecoveryAction.Navigate -> {
                // Navigation is handled by the UI layer
                _state.update { it.copy(navigationDestination = action.destination) }
            }

            is ErrorRecoveryAction.Custom -> {
                // This would be passed to the UI to handle
            }

            is ErrorRecoveryAction.Dismiss -> {
                onEvent(LoginEvent.DismissError)
            }

            is ErrorRecoveryAction.MultiAction -> {
                // The primary action is handled based on its type
                handleRecoveryAction(action.primary)
            }
        }
    }

    private fun resendVerificationEmail() {
        // Implementation would call a use case to resend the verification email
        // For now, just dismiss the error
        _state.update {
            it.copy(
                error = null, errorMessage = null, recoveryAction = null, showError = false
            )
        }
    }
}

/**
 * Enhanced LoginState that includes error handling state
 */
data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,

    // Error handling fields
    override val error: AppError? = null,
    override val errorMessage: String? = null,
    override val showError: Boolean = false,
    override val recoveryAction: ErrorRecoveryAction? = null,

    // Navigation
    val navigationDestination: String? = null
) : ErrorState

/**
 * Enhanced LoginEvent that includes error handling events
 */
sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object Submit : LoginEvent()
    data class GoogleSignIn(val idToken: String) : LoginEvent()

    // Error handling events
    object DismissError : LoginEvent()
    data class HandleRecoveryAction(val action: ErrorRecoveryAction) : LoginEvent()

    // Custom recovery actions
    object ResendVerificationEmail : LoginEvent()
}