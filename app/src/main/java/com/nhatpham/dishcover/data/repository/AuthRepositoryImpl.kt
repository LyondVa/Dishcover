package com.nhatpham.dishcover.data.repository

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.domain.repository.AuthRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.model.user.UserPrivacySettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {

    override fun sendEmailVerification(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuthDataSource.sendEmailVerification()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send verification email"))
        }
    }

    override fun checkEmailVerification(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val currentUser = firebaseAuthDataSource.getCurrentUser()
            if (currentUser != null) {
                // Reload user to get the latest verification status from Firebase
                firebaseAuthDataSource.reloadUser()
                val reloadedUser = firebaseAuthDataSource.getCurrentUser()

                if (reloadedUser?.isEmailVerified == true) {
                    // Update user verification status in Firestore
                    val user = firestoreUserDataSource.getUserById(reloadedUser.uid)
                    user?.let { existingUser ->
                        val updatedUser = existingUser.copy(
                            isVerified = true,
                            updatedAt = Timestamp.now()
                        )
                        val success = firestoreUserDataSource.updateUser(updatedUser)
                        if (success) {
                            emit(Resource.Success(Unit))
                        } else {
                            emit(Resource.Error("Failed to update verification status"))
                        }
                    } ?: emit(Resource.Error("User not found in database"))
                } else {
                    emit(Resource.Error("Email not verified. Please check your email and click the verification link."))
                }
            } else {
                emit(Resource.Error("No authenticated user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to check verification status"))
        }
    }

    override fun verifyEmailCode(code: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val currentUser = firebaseAuthDataSource.getCurrentUser()
            if (currentUser != null) {
                // Reload user to get latest verification status
                firebaseAuthDataSource.reloadUser()
                val reloadedUser = firebaseAuthDataSource.getCurrentUser()

                if (reloadedUser?.isEmailVerified == true) {
                    // Update user verification status in Firestore
                    val user = firestoreUserDataSource.getUserById(reloadedUser.uid)
                    user?.let { existingUser ->
                        val updatedUser = existingUser.copy(
                            isVerified = true,
                            updatedAt = Timestamp.now()
                        )
                        val success = firestoreUserDataSource.updateUser(updatedUser)
                        if (success) {
                            emit(Resource.Success(Unit))
                        } else {
                            emit(Resource.Error("Failed to update verification status"))
                        }
                    }
                } else {
                    emit(Resource.Error("Email not verified. Please check your email and try again."))
                }
            } else {
                emit(Resource.Error("No authenticated user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Verification failed"))
        }
    }

    override fun signIn(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.signInWithEmailAndPassword(email, password)
            if (firebaseUser != null) {
                // Check if user exists in Firestore
                var user = firestoreUserDataSource.getUserById(firebaseUser.uid)

                if (user == null) {
                    // Create user in Firestore if it doesn't exist (in case user was created in Firebase Auth directly)
                    val timestamp = Timestamp.now()
                    user = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: email.substringBefore('@'),
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = "email",
                        isAdmin = false // Default non-admin user
                    )
                    val created = firestoreUserDataSource.createUser(user)
                    if (!created) {
                        emit(Resource.Error("Failed to create user profile"))
                        return@flow
                    }

                    // Create default privacy settings
                    val userPrivacySettings = UserPrivacySettings(
                        userId = firebaseUser.uid,
                        updatedAt = timestamp
                    )
                    firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)
                }

                // Log admin status for debugging
                Timber.d("User signed in - Admin: ${user.isAdmin}, Username: ${user.username}")

                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Authentication failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }


    override fun signUp(email: String, password: String, username: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.createUserWithEmailAndPassword(email, password)
            if (firebaseUser != null) {
                val timestamp = Timestamp.now()
                val user = User(
                    userId = firebaseUser.uid,
                    email = email,
                    username = username,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    isVerified = false,
                    isActive = true,
                    authProvider = "email",
                    isAdmin = false // New users are not admin by default
                )

                val created = firestoreUserDataSource.createUser(user)
                if (created) {
                    // Create default privacy settings
                    val userPrivacySettings = UserPrivacySettings(
                        userId = firebaseUser.uid,
                        updatedAt = timestamp
                    )
                    firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)

                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("Failed to create user profile"))
                }
            } else {
                emit(Resource.Error("Failed to create account"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun resetPassword(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuthDataSource.sendPasswordResetEmail(email)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun verifyPasswordResetCode(code: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val email = firebaseAuthDataSource.verifyPasswordResetCode(code)
            emit(Resource.Success(email))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Invalid or expired verification code"))
        }
    }

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuthDataSource.confirmPasswordReset(code, newPassword)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to reset password"))
        }
    }

    override fun signOut(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuthDataSource.signOut()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getCurrentUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.getCurrentUser()
            if (firebaseUser != null) {
                var user = firestoreUserDataSource.getUserById(firebaseUser.uid)
                if (user != null) {
                    Timber.tag("AuthRepositoryImpl").d("user: $user")
                    emit(Resource.Success(user))
                } else {
                    // Create user in Firestore if it doesn't exist
                    val timestamp = Timestamp.now()
                    val newUser = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "User",
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = if (firebaseUser.providerData.any { it.providerId == "google.com" }) "google" else "email"
                    )
                    val created = firestoreUserDataSource.createUser(newUser)
                    if (created) {
                        // Create default privacy settings
                        val userPrivacySettings = UserPrivacySettings(
                            userId = firebaseUser.uid,
                            updatedAt = timestamp
                        )
                        firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)

                        emit(Resource.Success(newUser))
                    } else {
                        emit(Resource.Error("Failed to create user profile"))
                    }
                }
            } else {
                emit(Resource.Error("No authenticated user"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun signInWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.signInWithGoogle(idToken)
            if (firebaseUser != null) {
                var user = firestoreUserDataSource.getUserById(firebaseUser.uid)
                val timestamp = Timestamp.now()

                if (user == null) {
                    // Create new user from Google account
                    val displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "User"
                    user = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = displayName,
                        profilePicture = firebaseUser.photoUrl?.toString(),
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = "google",
                        isAdmin = false // Google sign-in users are not admin by default
                    )
                    val created = firestoreUserDataSource.createUser(user)
                    if (!created) {
                        emit(Resource.Error("Failed to create user profile"))
                        return@flow
                    }

                    // Create default privacy settings
                    val userPrivacySettings = UserPrivacySettings(
                        userId = firebaseUser.uid,
                        updatedAt = timestamp
                    )
                    firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)
                }

                // Log admin status for debugging
                Timber.d("Google user signed in - Admin: ${user.isAdmin}, Username: ${user.username}")

                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Google authentication failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}