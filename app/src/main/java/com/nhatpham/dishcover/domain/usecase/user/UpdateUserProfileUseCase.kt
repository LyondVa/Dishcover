package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(user: User) =
        userRepository.updateUser(user)
}