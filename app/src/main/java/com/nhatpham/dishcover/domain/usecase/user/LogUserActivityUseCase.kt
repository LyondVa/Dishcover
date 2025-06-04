package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.model.user.UserActivityLog
import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class LogUserActivityUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(activity: UserActivityLog) =
        userRepository.logUserActivity(activity)
}