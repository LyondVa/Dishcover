package com.nhatpham.dishcover.data.repository

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.model.user.UserActivityLog
import com.nhatpham.dishcover.domain.model.user.UserPrivacySettings
import com.nhatpham.dishcover.domain.repository.UserRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : UserRepository {

    override fun getUserById(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val user = firestoreUserDataSource.getUserById(userId)
            if (user != null) {
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val success = firestoreUserDataSource.updateUser(user)
            if (success) {
                // Update display name in Firebase Auth if username changed
                firebaseAuthDataSource.updateUserProfile(user.username, user.profilePicture)

                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Failed to update user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserPrivacySettings(userId: String): Flow<Resource<UserPrivacySettings>> = flow {
        emit(Resource.Loading())
        try {
            val settings = firestoreUserDataSource.getUserPrivacySettings(userId)
            if (settings != null) {
                emit(Resource.Success(settings))
            } else {
                // Create default settings if not found
                val defaultSettings = UserPrivacySettings(
                    userId = userId,
                    updatedAt = Timestamp.now()
                )
                val created = firestoreUserDataSource.createUserPrivacySettings(defaultSettings)
                if (created) {
                    emit(Resource.Success(defaultSettings))
                } else {
                    emit(Resource.Error("Failed to create default privacy settings"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun updateUserPrivacySettings(settings: UserPrivacySettings): Flow<Resource<UserPrivacySettings>> = flow {
        emit(Resource.Loading())
        try {
            val updatedSettings = settings.copy(updatedAt = Timestamp.now())
            val success = firestoreUserDataSource.updateUserPrivacySettings(updatedSettings)
            if (success) {
                emit(Resource.Success(updatedSettings))
            } else {
                emit(Resource.Error("Failed to update privacy settings"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserFollowers(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val followers = firestoreUserDataSource.getUserFollowers(userId)
            emit(Resource.Success(followers))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserFollowing(userId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val following = firestoreUserDataSource.getUserFollowing(userId)
            emit(Resource.Success(following))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val success = firestoreUserDataSource.followUser(currentUserId, targetUserId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to follow user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val success = firestoreUserDataSource.unfollowUser(currentUserId, targetUserId)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to unfollow user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getUserActivity(userId: String, limit: Int): Flow<Resource<List<UserActivityLog>>> = flow {
        emit(Resource.Loading())
        try {
            val activities = firestoreUserDataSource.getUserActivity(userId, limit)
            emit(Resource.Success(activities))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun logUserActivity(activity: UserActivityLog): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val success = firestoreUserDataSource.logUserActivity(activity)
            if (success) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to log user activity"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun isFollowingUser(currentUserId: String, targetUserId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            // Check local cache first for quick response
            val localResult = getUserFollowing(currentUserId).firstOrNull()
            when (localResult) {
                is Resource.Success -> {
                    val isFollowing = localResult.data?.any { it.userId == targetUserId } ?: false
                    emit(Resource.Success(isFollowing))
                }
                else -> {
                    // If local data not available, fetch from remote
                    val remoteFollowing = firestoreUserDataSource.getUserFollowing(currentUserId)
                    val isFollowing = remoteFollowing.any { it.userId == targetUserId }
                    emit(Resource.Success(isFollowing))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking follow status")
            emit(Resource.Error(e.message ?: "Failed to check follow status"))
        }
    }

    override fun searchUsers(query: String, limit: Int): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            // This would require implementing search in FirestoreUserDataSource
            // For now, return empty list as placeholder
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error searching users")
            emit(Resource.Error(e.message ?: "Failed to search users"))
        }
    }

    override fun getSuggestedUsers(currentUserId: String, limit: Int): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            // This would require implementing user suggestions algorithm
            // For now, return empty list as placeholder
            emit(Resource.Success(emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error getting suggested users")
            emit(Resource.Error(e.message ?: "Failed to get suggested users"))
        }
    }
}