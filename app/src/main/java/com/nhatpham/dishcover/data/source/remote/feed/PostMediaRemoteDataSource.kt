// PostMediaRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class PostMediaRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    suspend fun uploadPostImage(postId: String, imageData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("post_images")
                .child("$postId/${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putBytes(imageData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post image")
            null
        }
    }

    suspend fun uploadPostVideo(postId: String, videoData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("post_videos")
                .child("$postId/${UUID.randomUUID()}.mp4")

            val uploadTask = storageRef.putBytes(videoData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post video")
            null
        }
    }

    suspend fun deletePostMedia(mediaUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(mediaUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post media")
            false
        }
    }
}