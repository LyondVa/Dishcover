package com.nhatpham.dishcover.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.auth.SignUpUseCase
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
class RegisterViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is RegisterEvent.UsernameChanged -> {
                _state.update { it.copy(username = event.username) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword) }
            }
            RegisterEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
            RegisterEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !state.value.isConfirmPasswordVisible) }
            }
            is RegisterEvent.Submit -> {
                submitRegistration()
            }
        }
    }

    private fun submitRegistration() {
        val emailResource = validateEmail(_state.value.email)
        val usernameResource = validateUsername(_state.value.username)
        val passwordResource = validatePassword(_state.value.password)
        val confirmPasswordResource = validateConfirmPassword(
            _state.value.password,
            _state.value.confirmPassword
        )

        val hasError = listOf(
            emailResource,
            usernameResource,
            passwordResource,
            confirmPasswordResource
        ).any { !it }

        if (hasError) {
            return
        }

        register()
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

    private fun validateUsername(username: String): Boolean {
        return if (ValidationUtils.validateUsername(username)) {
            _state.update { it.copy(usernameError = null) }
            true
        } else {
            _state.update {
                it.copy(usernameError = "Username must be 3-20 characters, letters, numbers, and underscores only")
            }
            false
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (ValidationUtils.validatePassword(password)) {
            _state.update { it.copy(passwordError = null) }
            true
        } else {
            _state.update {
                it.copy(passwordError = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, and 1 number")
            }
            false
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            _state.update { it.copy(confirmPasswordError = null) }
            true
        } else {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            false
        }
    }

    private fun register() {
        viewModelScope.launch {
            signUpUseCase(
                email = state.value.email,
                password = state.value.password,
                username = state.value.username
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                registeredUserEmail = result.data!!.email,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Registration failed"
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

data class RegisterState(
    val email: String = "",
    val emailError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val registeredUserEmail: String = "",
    val error: String? = null
)

sealed class RegisterEvent {
    data class EmailChanged(val email: String) : RegisterEvent()
    data class UsernameChanged(val username: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object TogglePasswordVisibility : RegisterEvent()
    object ToggleConfirmPasswordVisibility : RegisterEvent()
    object Submit : RegisterEvent()
}