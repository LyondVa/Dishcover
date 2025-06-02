package com.nhatpham.dishcover.domain.usecase.auth

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.signOut()
}