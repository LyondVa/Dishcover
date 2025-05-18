package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyPasswordResetCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(code: String) = authRepository.verifyPasswordResetCode(code)
}