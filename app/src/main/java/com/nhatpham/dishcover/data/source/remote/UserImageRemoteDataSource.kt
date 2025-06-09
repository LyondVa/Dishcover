package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class UserImageRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    suspend fun uploadProfileImage(userId: String, imageData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("user_images")
                .child("profile_pictures")
                .child("${userId}_${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putBytes(imageData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading profile image for user: $userId")
            null
        }
    }

    suspend fun uploadBannerImage(userId: String, imageData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("user_images")
                .child("banner_images")
                .child("${userId}_${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putBytes(imageData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading banner image for user: $userId")
            null
        }
    }

    suspend fun deleteUserImage(imageUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user image: $imageUrl")
            false
        }
    }
}