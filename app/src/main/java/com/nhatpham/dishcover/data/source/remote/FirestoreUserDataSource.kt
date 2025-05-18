package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.domain.model.UserActivityLog
import com.nhatpham.dishcover.domain.model.UserPrivacySettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("USERS")
    private val userPrivacyCollection = firestore.collection("USER_PRIVACY_SETTINGS")
    private val userFollowsCollection = firestore.collection("USER_FOLLOWS")
    private val userActivityCollection = firestore.collection("USER_ACTIVITY_LOG")
    private val userNotificationCollection = firestore.collection("USER_NOTIFICATION_PREFERENCES")

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

    suspend fun updateUserPrivacySettings(settings: UserPrivacySettings): Boolean {
        return try {
            userPrivacyCollection.document(settings.userId).set(settings).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserFollowers(userId: String): List<User> {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followerIds = snapshot.documents.mapNotNull { it.getString("followerId") }

            if (followerIds.isEmpty()) {
                return emptyList()
            }

            val followers = mutableListOf<User>()

            // Firestore has a limit on 'in' queries, so we need to batch
            val batchSize = 10
            followerIds.chunked(batchSize).forEach { batch ->
                val batchSnapshot = usersCollection
                    .whereIn("userId", batch)
                    .get()
                    .await()

                batchSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }.let {
                    followers.addAll(it)
                }
            }

            followers
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserFollowing(userId: String): List<User> {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = snapshot.documents.mapNotNull { it.getString("followingId") }

            if (followingIds.isEmpty()) {
                return emptyList()
            }

            val following = mutableListOf<User>()

            // Firestore has a limit on 'in' queries, so we need to batch
            val batchSize = 10
            followingIds.chunked(batchSize).forEach { batch ->
                val batchSnapshot = usersCollection
                    .whereIn("userId", batch)
                    .get()
                    .await()

                batchSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }.let {
                    following.addAll(it)
                }
            }

            following
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun followUser(followerId: String, followingId: String): Boolean {
        return try {
            val followData = hashMapOf(
                "followerId" to followerId,
                "followingId" to followingId,
                "createdAt" to Timestamp.now()
            )

            userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.let {
                    // Already following, do nothing
                    true
                }
                ?: run {
                    // Not following yet, create follow relationship
                    userFollowsCollection.add(followData).await()
                    true
                }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Boolean {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            for (document in snapshot.documents) {
                document.reference.delete().await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserActivity(userId: String, limit: Int): List<UserActivityLog> {
        return try {
            val snapshot = userActivityCollection
                .whereEqualTo("userId", userId)
                .orderBy("activityTime", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserActivityLog::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun logUserActivity(activity: UserActivityLog): Boolean {
        return try {
            userActivityCollection.add(activity).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserStats(userId: String): Map<String, Int> {
        return try {
            val recipeCount = firestore.collection("RECIPES")
                .whereEqualTo("userId", userId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                .count
                .toInt()

            val followerCount = userFollowsCollection
                .whereEqualTo("followingId", userId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                .count
                .toInt()

            val followingCount = userFollowsCollection
                .whereEqualTo("followerId", userId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                .count
                .toInt()

            mapOf(
                "recipeCount" to recipeCount,
                "followerCount" to followerCount,
                "followingCount" to followingCount
            )
        } catch (e: Exception) {
            mapOf(
                "recipeCount" to 0,
                "followerCount" to 0,
                "followingCount" to 0
            )
        }
    }
}