package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String) =
        authRepository.signIn(email, password)
}