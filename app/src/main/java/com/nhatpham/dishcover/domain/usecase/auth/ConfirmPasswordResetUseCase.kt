package com.nhatpham.dishcover.domain.usecase.auth

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class ConfirmPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(code: String, newPassword: String) =
        authRepository.confirmPasswordReset(code, newPassword)
}