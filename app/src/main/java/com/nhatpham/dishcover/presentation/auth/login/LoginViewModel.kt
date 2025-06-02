package com.nhatpham.dishcover.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.auth.SignInUseCase
import com.nhatpham.dishcover.domain.usecase.auth.SignInWithGoogleUseCase
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
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is LoginEvent.PasswordChanged -> {
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
        }
    }

    private fun submitLogin() {
        val emailResult = validateEmail(_state.value.email)
        val passwordResult = validatePassword(_state.value.password)

        val hasError = listOf(
            emailResult,
            passwordResult
        ).any { !it }

        if (hasError) {
            return
        }

        login()
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

    private fun validatePassword(password: String): Boolean {
        return if (password.isNotBlank()) {
            _state.update { it.copy(passwordError = null) }
            true
        } else {
            _state.update { it.copy(passwordError = "Password cannot be empty") }
            false
        }
    }

    private fun login() {
        viewModelScope.launch {
            signInUseCase(
                email = state.value.email,
                password = state.value.password
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            }
        }
    }
}

data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object Submit : LoginEvent()
    data class GoogleSignIn(val idToken: String) : LoginEvent()
}