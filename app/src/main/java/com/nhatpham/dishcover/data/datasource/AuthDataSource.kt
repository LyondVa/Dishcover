package com.nhatpham.dishcover.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.nhatpham.dishcover.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import com.nhatpham.dishcover.domain.model.UserProfileSettings
import com.nhatpham.dishcover.domain.model.UserActivityLog

class AuthDataSource(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun signUpWithEmailAndPassword(email: String, password: String, username: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            firebaseUser?.let { user ->
                // Hash password before storing it
                val passwordHash = hashPassword(password)  // Implement hashPassword function

                // Create a User object and save additional data to Firestore
                val newUser = User(
                    userId = user.uid,
                    email = email,
                    username = username,
                    passwordHash = passwordHash,
                    profilePicture = null, // Set default or let user update later
                    bio = null,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    isVerified = false,
                    isActive = true,
                    authProvider = "email"
                )

                firestore.collection("users").document(user.uid)
                    .set(newUser)
                    .await()

                // Create default user privacy settings
                val defaultSettings = UserProfileSettings(
                    userId = user.uid,
                    profilePublic = false,
                    showFavorites = true,
                    allowComments = true,
                    allowSharing = true,
                    updatedAt = Date()
                )

                firestore.collection("user_privacy_settings").document() // Use Firestore's auto-ID
                    .set(defaultSettings)
                    .await()

                Result.success(newUser) // Return the newly created User
            } ?: Result.failure(Exception("Failed to create user."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        // TODO: Implement a strong password hashing algorithm (e.g., bcrypt, Argon2)
        // NEVER store passwords in plain text.
        // For demonstration purposes, using a simple hash:
        return password.hashCode().toString() //Replace with secure hashing
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            firebaseUser?.let { user ->
                // Retrieve additional user data from Firestore
                val document = firestore.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    val fetchedUser = document.toObject(User::class.java)
                    fetchedUser?.let {
                        //Verify the hashed password from DB with the user provided password
                        if (verifyPassword(password, it.passwordHash ?: "")){
                            //Log successful login activity
                            logUserActivity(user.uid, "login", "Successful login from email/password")
                            Result.success(it)
                        } else {
                            logUserActivity(user.uid, "login", "Failed login from email/password - incorrect password")
                            Result.failure(Exception("Invalid credentials"))
                        }
                    } ?: Result.failure(Exception("Failed to fetch user data from Firestore."))

                } else {
                    Result.failure(Exception("User data not found in Firestore."))
                }
            } ?: Result.failure(Exception("Sign-in failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun verifyPassword(password: String, hashedPasswordFromDB: String): Boolean{
        //Implement the same password hashing algorithm from singup for verification
        return password.hashCode().toString() == hashedPasswordFromDB //Replace with secure hashing verification
    }


    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            //Fetch additional user information from Firestore
            val document = firestore.collection("users").document(it.uid).get().result
            if (document != null && document.exists()) {
                document.toObject(User::class.java)
            } else {
                // Fallback to creating a User object with limited information
                User(
                    userId = it.uid,
                    email = it.email.toString(),
                    username = it.displayName.toString() // Assuming you're saving the username as display name
                )
            }

        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            firebaseUser?.let { user ->
                // Check if user exists in Firestore, if not, create the user
                val document = firestore.collection("users").document(user.uid).get().await()
                if (!document.exists()) {
                    val newUser = User(
                        userId = user.uid,
                        email = user.email.toString(),
                        username = user.displayName.toString(),
                        profilePicture = user.photoUrl?.toString(),
                        passwordHash = null, // Not applicable for Google Sign-in
                        bio = null,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                        isVerified = true,  // Assuming Google sign-in implies verification
                        isActive = true,
                        authProvider = "google"
                    )
                    firestore.collection("users").document(user.uid).set(newUser).await()

                    // Create default user privacy settings
                    val defaultSettings = UserProfileSettings(
                        userId = user.uid,
                        profilePublic = false,
                        showFavorites = true,
                        allowComments = true,
                        allowSharing = true,
                        updatedAt = Date()
                    )

                    firestore.collection("user_privacy_settings").document() // Use Firestore's auto-ID
                        .set(defaultSettings)
                        .await()
                    Result.success(newUser)

                } else {
                    val fetchedUser = document.toObject(User::class.java)
                    fetchedUser?.let {
                        //Log successful login activity
                        logUserActivity(user.uid, "login", "Successful login from Google")
                        Result.success(it)
                    } ?: Result.failure(Exception("Failed to fetch user data from Firestore."))
                }
            } ?: Result.failure(Exception("Google Sign-in failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun logUserActivity(userId: String, activityType: String, activityDetails: String) {
        try {
            val userActivityLog = UserActivityLog(
                userId = userId,
                activityType = activityType,
                activityDetails = activityDetails,
                activityTime = Date(),
                ipAddress = getIpAddress() // Implement getIpAddress()
            )

            firestore.collection("user_activity_log").document()
                .set(userActivityLog)
                .await()

        } catch (e: Exception) {
            // Handle error logging activity
            println("Error logging user activity: ${e.message}")
        }
    }

    private fun getIpAddress(): String? {
        //TODO: Implement a function to get the user's IP address
        return "127.0.0.1" // Replace with actual IP address retrieval
    }
}