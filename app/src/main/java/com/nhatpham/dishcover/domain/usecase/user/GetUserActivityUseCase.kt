package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class GetUserActivityUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String, limit: Int = 10) =
        userRepository.getUserActivity(userId, limit)
}