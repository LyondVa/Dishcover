// PostReferenceRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.mapper.toDomain
import com.nhatpham.dishcover.data.mapper.toDto
import com.nhatpham.dishcover.data.model.dto.feed.PostCookbookReferenceDto
import com.nhatpham.dishcover.data.model.dto.feed.PostRecipeReferenceDto
import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class PostReferenceRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postRecipeReferencesCollection = firestore.collection("POST_RECIPE_REFERENCES")
    private val postCookbookReferencesCollection = firestore.collection("POST_COOKBOOK_REFERENCES")

    // Recipe Reference Operations
    suspend fun addRecipeReference(reference: PostRecipeReference): PostRecipeReference? {
        return try {
            val referenceId = reference.referenceId.takeIf { it.isNotBlank() }
                ?: postRecipeReferencesCollection.document().id

            val updatedReference = reference.copy(
                referenceId = referenceId,
                createdAt = Timestamp.now()
            )

            postRecipeReferencesCollection.document(referenceId)
                .set(updatedReference.toDto())
                .await()

            updatedReference
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe reference")
            null
        }
    }

    suspend fun removeRecipeReference(referenceId: String): Boolean {
        return try {
            postRecipeReferencesCollection.document(referenceId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe reference")
            false
        }
    }

    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> {
        return try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .orderBy("position")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostRecipeReferenceDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post recipe references")
            emptyList()
        }
    }

    suspend fun hasPostRecipeReferences(postId: String): Boolean {
        return try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Cookbook Reference Operations
    suspend fun addCookbookReference(reference: PostCookbookReference): PostCookbookReference? {
        return try {
            val referenceId = reference.referenceId.takeIf { it.isNotBlank() }
                ?: postCookbookReferencesCollection.document().id

            val updatedReference = reference.copy(
                referenceId = referenceId,
                createdAt = Timestamp.now()
            )

            postCookbookReferencesCollection.document(referenceId)
                .set(updatedReference.toDto())
                .await()

            updatedReference
        } catch (e: Exception) {
            Timber.e(e, "Error adding cookbook reference")
            null
        }
    }

    suspend fun removeCookbookReference(referenceId: String): Boolean {
        return try {
            postCookbookReferencesCollection.document(referenceId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing cookbook reference")
            false
        }
    }

    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> {
        return try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .orderBy("position")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostCookbookReferenceDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post cookbook references")
            emptyList()
        }
    }

    suspend fun hasPostCookbookReferences(postId: String): Boolean {
        return try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Cleanup operations for post deletion
    suspend fun deletePostRecipeReferences(postId: String) {
        try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe references")
        }
    }

    suspend fun deletePostCookbookReferences(postId: String) {
        try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting cookbook references")
        }
    }

    // Utility method to save references during post creation
    suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>) {
        references.forEach { reference ->
            try {
                addRecipeReference(reference.copy(postId = postId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving recipe reference")
            }
        }
    }

    suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>) {
        references.forEach { reference ->
            try {
                addCookbookReference(reference.copy(postId = postId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving cookbook reference")
            }
        }
    }
}