package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserPrivacySettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("USERS")
    private val userPrivacyCollection = firestore.collection("USER_PRIVACY_SETTINGS")

    suspend fun createUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            val updatedUser = user.copy(updatedAt = Timestamp.now())
            usersCollection.document(user.userId).set(updatedUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createUserPrivacySettings(userPrivacySettings: UserPrivacySettings): Boolean {
        return try {
            userPrivacyCollection.document(userPrivacySettings.userId).set(userPrivacySettings).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserPrivacySettings(userId: String): UserPrivacySettings? {
        return try {
            val document = userPrivacyCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(UserPrivacySettings::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}