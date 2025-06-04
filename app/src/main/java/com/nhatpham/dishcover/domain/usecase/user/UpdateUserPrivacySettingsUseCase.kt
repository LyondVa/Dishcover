package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.model.user.UserPrivacySettings
import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserPrivacySettingsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(settings: UserPrivacySettings) =
        userRepository.updateUserPrivacySettings(settings)
}
