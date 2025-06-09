package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.model.user.UserActivityLog
import com.nhatpham.dishcover.domain.model.user.UserPrivacySettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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
            // First try to retrieve the user to avoid overwriting existing data
            val existingUserDoc = usersCollection.document(user.userId).get().await()

            if (existingUserDoc.exists()) {
                // Update only specific fields if user exists
                val updates = mapOf(
                    "updatedAt" to Timestamp.now(),
                    "isActive" to true
                )

                // Don't overwrite username if it exists and is not empty
                val existingUsername = existingUserDoc.getString("username")
                if (user.username.isNotBlank() && (existingUsername.isNullOrBlank())) {
                    (updates as MutableMap<String, Any>)["username"] = user.username
                }

                // Update only required fields
                usersCollection.document(user.userId)
                    .set(updates, SetOptions.merge())
                    .await()
            } else {
                // Create new user
                usersCollection.document(user.userId)
                    .set(user)
                    .await()

                // Log creation
                Timber.d("Created new user in Firestore: ${user.userId}")
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error creating user in Firestore: ${user.userId}")
            false
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                Timber.d("User not found in Firestore: $userId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user from Firestore: $userId")
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            val updatedUser = user.copy(updatedAt = Timestamp.now())
            usersCollection.document(user.userId).set(updatedUser).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating user in Firestore: ${user.userId}")
            false
        }
    }

    suspend fun createUserPrivacySettings(userPrivacySettings: UserPrivacySettings): Boolean {
        return try {
            // Check if settings already exist
            val existingDoc = userPrivacyCollection.document(userPrivacySettings.userId).get().await()

            if (existingDoc.exists()) {
                // Update with merge to preserve existing fields
                userPrivacyCollection.document(userPrivacySettings.userId)
                    .set(userPrivacySettings, SetOptions.merge())
                    .await()
            } else {
                // Create new settings
                userPrivacyCollection.document(userPrivacySettings.userId)
                    .set(userPrivacySettings)
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error creating privacy settings in Firestore: ${userPrivacySettings.userId}")
            false
        }
    }

    suspend fun getUserPrivacySettings(userId: String): UserPrivacySettings? {
        return try {
            val document = userPrivacyCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(UserPrivacySettings::class.java)
            } else {
                // Return default settings if none found
                UserPrivacySettings(userId = userId, updatedAt = Timestamp.now())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting privacy settings from Firestore: $userId")
            null
        }
    }

    suspend fun updateUserPrivacySettings(settings: UserPrivacySettings): Boolean {
        return try {
            userPrivacyCollection.document(settings.userId).set(settings).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating privacy settings in Firestore: ${settings.userId}")
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
            Timber.e(e, "Error getting followers from Firestore: $userId")
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
            Timber.e(e, "Error getting followed users from Firestore: $userId")
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

            val existingQuery = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            if (existingQuery.documents.isNotEmpty()) {
                // Already following, do nothing
                true
            } else {
                // Not following yet, create follow relationship
                userFollowsCollection.add(followData).await()

                // Update follower counts would be done here in a production app
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating follow relationship in Firestore: $followerId -> $followingId")
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

            var success = true
            for (document in snapshot.documents) {
                try {
                    document.reference.delete().await()
                } catch (e: Exception) {
                    Timber.e(e, "Error deleting follow document: ${document.id}")
                    success = false
                }
            }

            // Update follower counts would be done here in a production app
            success
        } catch (e: Exception) {
            Timber.e(e, "Error removing follow relationship in Firestore: $followerId -> $followingId")
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
            Timber.e(e, "Error getting user activity from Firestore: $userId")
            emptyList()
        }
    }

    suspend fun logUserActivity(activity: UserActivityLog): Boolean {
        return try {
            userActivityCollection.add(activity).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error logging user activity in Firestore")
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
            Timber.e(e, "Error getting user stats from Firestore: $userId")
            mapOf(
                "recipeCount" to 0,
                "followerCount" to 0,
                "followingCount" to 0
            )
        }
    }

    suspend fun searchUsers(query: String, limit: Int): List<User> {
        return try {
            val lowerQuery = query.lowercase()

            // Get users with active status
            val snapshot = usersCollection
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit((limit * 3).toLong()) // Get more to filter locally
                .get()
                .await()

            val matchingUsers = mutableListOf<User>()

            snapshot.documents.forEach { doc ->
                try {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        val username = user.username.lowercase()
                        val bio = user.bio?.lowercase() ?: ""
                        val email = user.email.lowercase()

                        // Check if query matches any searchable field
                        val matchesUsername = username.contains(lowerQuery)
                        val matchesBio = bio.contains(lowerQuery)
                        val matchesEmail = email.contains(lowerQuery) // Only for admin/self viewing

                        if (matchesUsername || matchesBio || matchesEmail) {
                            matchingUsers.add(user)
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error processing user search result: ${doc.id}")
                }
            }

            // Sort by relevance (username matches first, then bio)
            matchingUsers.sortedWith { a, b ->
                val aUsernameMatch = a.username.lowercase().contains(lowerQuery)
                val bUsernameMatch = b.username.lowercase().contains(lowerQuery)

                when {
                    aUsernameMatch && !bUsernameMatch -> -1
                    !aUsernameMatch && bUsernameMatch -> 1
                    else -> a.username.compareTo(b.username, ignoreCase = true)
                }
            }.take(limit)

        } catch (e: Exception) {
            Timber.e(e, "Error searching users")
            emptyList()
        }
    }
}