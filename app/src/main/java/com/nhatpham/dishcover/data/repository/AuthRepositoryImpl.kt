package com.nhatpham.dishcover.data.repository

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.domain.repository.AuthRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserPrivacySettings

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {

    override fun signIn(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.signInWithEmailAndPassword(email, password)
            if (firebaseUser != null) {
                var user = firestoreUserDataSource.getUserById(firebaseUser.uid)
                if (user == null) {
                    // Create user in Firestore if it doesn't exist (in case user was created in Firebase Auth directly)
                    val timestamp = Timestamp.now()
                    user = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: "",
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = "email"
                    )
                    firestoreUserDataSource.createUser(user)
                }
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Authentication failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun signUp(
        email: String,
        password: String,
        username: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuthDataSource.createUserWithEmailAndPassword(email, password)
            if (firebaseUser != null) {
                // Update display name
                firebaseAuthDataSource.updateUserProfile(username)

                // Create user in Firestore
                val timestamp = Timestamp.now()
                val newUser = User(
                    userId = firebaseUser.uid,
                    email = email,
                    username = username,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    isVerified = false,
                    isActive = true,
                    authProvider = "email"
                )

                val userCreated = firestoreUserDataSource.createUser(newUser)

                // Create default privacy settings
                val userPrivacySettings = UserPrivacySettings(
                    userId = firebaseUser.uid,
                    updatedAt = timestamp
                )
                firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)

                // Send email verification
                firebaseAuthDataSource.sendEmailVerification()

                if (userCreated) {
                    emit(Resource.Success(newUser))
                } else {
                    emit(Resource.Error("Failed to create user profile"))
                }
            } else {
                emit(Resource.Error("Registration failed"))
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
                val user = firestoreUserDataSource.getUserById(firebaseUser.uid)
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    // Create user in Firestore if it doesn't exist
                    val timestamp = Timestamp.now()
                    val newUser = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: "",
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = "email"
                    )
                    firestoreUserDataSource.createUser(newUser)
                    emit(Resource.Success(newUser))
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
                if (user == null) {
                    // Create user in Firestore if it doesn't exist
                    val timestamp = Timestamp.now()
                    user = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: "",
                        createdAt = timestamp,
                        updatedAt = timestamp,
                        isVerified = firebaseUser.isEmailVerified,
                        isActive = true,
                        authProvider = "google"
                    )
                    firestoreUserDataSource.createUser(user)

                    // Create default privacy settings
                    val userPrivacySettings = UserPrivacySettings(
                        userId = firebaseUser.uid,
                        updatedAt = timestamp
                    )
                    firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)
                }
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Google authentication failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}