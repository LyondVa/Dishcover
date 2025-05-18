package com.nhatpham.dishcover.data.repository

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserActivityLog
import com.nhatpham.dishcover.domain.model.UserPrivacySettings
import com.nhatpham.dishcover.domain.repository.UserRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
}