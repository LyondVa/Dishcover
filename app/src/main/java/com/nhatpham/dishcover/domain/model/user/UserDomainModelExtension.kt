package com.nhatpham.dishcover.domain.model.user

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val profilePicture: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val website: String? = null,
    val socialLinks: Map<String, String> = emptyMap(), // For platforms like Instagram, Twitter, etc.
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class UserNotificationPreferences(
    val userId: String = "",
    val newFollowerNotification: Boolean = true,
    val commentNotification: Boolean = true,
    val likeNotification: Boolean = true,
    val mentionNotification: Boolean = true,
    val recipeSharedNotification: Boolean = true,
    val systemNotification: Boolean = true,
    val emailNotification: Boolean = true,
    val pushNotification: Boolean = true,
    val updatedAt: Timestamp = Timestamp.now()
)