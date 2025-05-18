package com.nhatpham.dishcover.domain.model

import com.google.firebase.Timestamp

data class UserPrivacySettings(
    val settingId: String = "",
    val userId: String = "",
    val profilePublic: Boolean = true,
    val showFavorites: Boolean = true,
    val allowComments: Boolean = true,
    val allowSharing: Boolean = true,
    val updatedAt: Timestamp = Timestamp.now()
)