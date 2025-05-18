package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getCurrentUser()
}