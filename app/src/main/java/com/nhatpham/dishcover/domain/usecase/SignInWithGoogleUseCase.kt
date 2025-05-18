package com.nhatpham.dishcover.domain.usecase

import com.nhatpham.dishcover.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String) = authRepository.signInWithGoogle(idToken)
}