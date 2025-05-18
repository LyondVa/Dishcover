package com.nhatpham.dishcover.domain.usecase.user

import com.nhatpham.dishcover.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserPrivacySettingsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(settings: com.nhatpham.dishcover.domain.model.UserPrivacySettings) =
        userRepository.updateUserPrivacySettings(settings)
}
