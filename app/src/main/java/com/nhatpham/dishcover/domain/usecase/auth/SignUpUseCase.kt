package com.nhatpham.dishcover.domain.usecase.auth

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String, username: String) =
        authRepository.signUp(email, password, username)
}