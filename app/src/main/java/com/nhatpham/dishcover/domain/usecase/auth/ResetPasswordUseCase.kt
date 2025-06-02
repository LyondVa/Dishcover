package com.nhatpham.dishcover.domain.usecase.auth

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String) = authRepository.resetPassword(email)
}