package com.nhatpham.dishcover.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserPrivacySettings
import com.nhatpham.dishcover.domain.repository.AuthRepository
import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.Result
import com.nhatpham.dishcover.util.error.AuthError
import com.nhatpham.dishcover.util.error.AuthErrorHandler
import com.nhatpham.dishcover.util.error.runCatchingFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of AuthRepository using the error handling system
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val authErrorHandler: AuthErrorHandler
) : AuthRepository {

    override fun signIn(email: String, password: String): Flow<Result<User>> = runCatchingFlow {
        try {
            val firebaseUser = firebaseAuthDataSource.signInWithEmailAndPassword(email, password)
                ?: throw AppError.AuthError.InvalidCredentialsError()

            // Check if user has verified their email
            if (!firebaseUser.isEmailVerified) {
                throw AuthError.EmailVerificationRequiredError()
            }

            // Get user profile from Firestore
            val user = firestoreUserDataSource.getUserById(firebaseUser.uid)
                ?: throw AppError.DomainError.NotFoundError(
                    entityType = "User",
                    identifier = firebaseUser.uid
                )

            return@runCatchingFlow user
        } catch (e: FirebaseAuthInvalidUserException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        }
    }

    override fun signUp(email: String, password: String, username: String): Flow<Result<User>> = runCatchingFlow {
        try {
            // Validate password strength
            if (!isPasswordStrong(password)) {
                throw AuthError.WeakPasswordError(
                    requirements = listOf(
                        "At least 8 characters",
                        "At least one uppercase letter",
                        "At least one lowercase letter",
                        "At least one number",
                        "At least one special character"
                    )
                )
            }

            // Create user with Firebase Auth
            val firebaseUser = firebaseAuthDataSource.createUserWithEmailAndPassword(email, password)
                ?: throw AppError.SystemError.UnexpectedError("Failed to create user")

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
            if (!userCreated) {
                throw AppError.DataError.SyncError(
                    message = "Failed to create user profile",
                    entity = "User",
                    operation = "create"
                )
            }

            // Create default privacy settings
            val userPrivacySettings = UserPrivacySettings(
                userId = firebaseUser.uid,
                updatedAt = timestamp
            )
            firestoreUserDataSource.createUserPrivacySettings(userPrivacySettings)

            // Send email verification
            firebaseAuthDataSource.sendEmailVerification()

            return@runCatchingFlow newUser

        } catch (e: FirebaseAuthUserCollisionException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        }
    }

    override fun resetPassword(email: String): Flow<Result<Unit>> = runCatchingFlow {
        try {
            // Validate email
            if (!isValidEmail(email)) {
                throw AuthError.InvalidEmailError(email)
            }

            firebaseAuthDataSource.sendPasswordResetEmail(email)

        } catch (e: FirebaseAuthInvalidUserException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw authErrorHandler.convertFirebaseAuthException(e)
        }
    }

    override fun verifyPasswordResetCode(code: String): Flow<Result<String>> = runCatchingFlow {
        try {
            firebaseAuthDataSource.verifyPasswordResetCode(code)
        } catch (e: Exception) {
            throw AuthError.TokenExpiredError(cause = e)
        }
    }

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>> = runCatchingFlow {
        try {
            // Validate password strength
            if (!isPasswordStrong(newPassword)) {
                throw AuthError.WeakPasswordError(
                    requirements = listOf(
                        "At least 8 characters",
                        "At least one uppercase letter",
                        "At least one lowercase letter",
                        "At least one number",
                        "At least one special character"
                    )
                )
            }

            firebaseAuthDataSource.confirmPasswordReset(code, newPassword)

        } catch (e: Exception) {
            when {
                e.message?.contains("expired", ignoreCase = true) == true ->
                    throw AuthError.TokenExpiredError(cause = e)

                else -> throw authErrorHandler.convertFirebaseAuthException(e)
            }
        }
    }

    override fun signOut(): Flow<Result<Unit>> = runCatchingFlow {
        firebaseAuthDataSource.signOut()
    }

    override fun getCurrentUser(): Flow<Result<User>> = runCatchingFlow {
        val firebaseUser = firebaseAuthDataSource.getCurrentUser()
            ?: throw AppError.AuthError.SessionError()

        val user = firestoreUserDataSource.getUserById(firebaseUser.uid)

        if (user != null) {
            return@runCatchingFlow user
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
            return@runCatchingFlow newUser
        }
    }

    override fun signInWithGoogle(idToken: String): Flow<Result<User>> = runCatchingFlow {
        try {
            val firebaseUser = firebaseAuthDataSource.signInWithGoogle(idToken)
                ?: throw AuthError.SocialLoginError(provider = "Google")

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

            return@runCatchingFlow user

        } catch (e: Exception) {
            throw AuthError.SocialLoginError(
                provider = "Google",
                message = "Google sign-in failed: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Validate password strength
     */
    private fun isPasswordStrong(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(passwordRegex.toRegex())
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }
}