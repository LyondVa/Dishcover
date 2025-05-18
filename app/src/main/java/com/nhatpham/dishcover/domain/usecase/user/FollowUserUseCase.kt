package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class FollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(currentUserId: String, targetUserId: String) =
        userRepository.followUser(currentUserId, targetUserId)
}