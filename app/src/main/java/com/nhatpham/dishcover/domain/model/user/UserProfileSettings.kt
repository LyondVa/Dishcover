package com.nhatpham.dishcover.domain.model.user

import java.util.Date

data class UserProfileSettings(
    val settingId: String? = null, //Document ID on firestore
    val userId: String? = null,  // Firebase Auth UID, also FK to USERS table
    val profilePublic: Boolean = false,
    val showFavorites: Boolean = true,
    val allowComments: Boolean = true,
    val allowSharing: Boolean = true,
    val updatedAt: Date? = null
)