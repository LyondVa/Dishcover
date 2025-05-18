package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserActivityLog
import com.nhatpham.dishcover.domain.model.UserPrivacySettings
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // User profile operations
    fun getUserById(userId: String): Flow<Resource<User>>
    fun updateUser(user: User): Flow<Resource<User>>

    // Privacy settings
    fun getUserPrivacySettings(userId: String): Flow<Resource<UserPrivacySettings>>
    fun updateUserPrivacySettings(settings: UserPrivacySettings): Flow<Resource<UserPrivacySettings>>

    // Social features
    fun getUserFollowers(userId: String): Flow<Resource<List<User>>>
    fun getUserFollowing(userId: String): Flow<Resource<List<User>>>
    fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>
    fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>

    // Activity tracking
    fun getUserActivity(userId: String, limit: Int = 10): Flow<Resource<List<UserActivityLog>>>
    fun logUserActivity(activity: UserActivityLog): Flow<Resource<Unit>>
}