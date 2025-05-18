package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class UnfollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(currentUserId: String, targetUserId: String) =
        userRepository.unfollowUser(currentUserId, targetUserId)
}