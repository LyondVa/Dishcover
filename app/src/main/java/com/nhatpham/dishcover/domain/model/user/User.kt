package com.nhatpham.dishcover.domain.model.user

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val passwordHash: String? = "",  // Note: This is stored in Firebase Auth, not in Firestore
    val profilePicture: String? = "",
    val bio: String? = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val authProvider: String = "email"
)