// PostReferenceLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostReferenceLocalDataSource @Inject constructor() {

    // Cache maps for references
    private val postRecipeReferencesCache = mutableMapOf<String, List<PostRecipeReference>>()
    private val postCookbookReferencesCache = mutableMapOf<String, List<PostCookbookReference>>()

    // Recipe reference operations
    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> = withContext(Dispatchers.IO) {
        return@withContext postRecipeReferencesCache[postId] ?: emptyList()
    }

    suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>) = withContext(Dispatchers.IO) {
        postRecipeReferencesCache[postId] = references
    }

    suspend fun addRecipeReference(postId: String, reference: PostRecipeReference) = withContext(Dispatchers.IO) {
        val currentRefs = postRecipeReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        currentRefs.add(reference)
        postRecipeReferencesCache[postId] = currentRefs
    }

    suspend fun removeRecipeReference(postId: String, referenceId: String) = withContext(Dispatchers.IO) {
        val currentRefs = postRecipeReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        currentRefs.removeAll { it.referenceId == referenceId }
        postRecipeReferencesCache[postId] = currentRefs
    }

    suspend fun hasRecipeReferences(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postRecipeReferencesCache[postId]?.isNotEmpty() == true
    }

    // Cookbook reference operations
    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> = withContext(Dispatchers.IO) {
        return@withContext postCookbookReferencesCache[postId] ?: emptyList()
    }

    suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>) = withContext(Dispatchers.IO) {
        postCookbookReferencesCache[postId] = references
    }

    suspend fun addCookbookReference(postId: String, reference: PostCookbookReference) = withContext(Dispatchers.IO) {
        val currentRefs = postCookbookReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        currentRefs.add(reference)
        postCookbookReferencesCache[postId] = currentRefs
    }

    suspend fun removeCookbookReference(postId: String, referenceId: String) = withContext(Dispatchers.IO) {
        val currentRefs = postCookbookReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        currentRefs.removeAll { it.referenceId == referenceId }
        postCookbookReferencesCache[postId] = currentRefs
    }

    suspend fun hasCookbookReferences(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postCookbookReferencesCache[postId]?.isNotEmpty() == true
    }

    // Cache management for post deletion
    suspend fun deletePostReferences(postId: String) = withContext(Dispatchers.IO) {
        postRecipeReferencesCache.remove(postId)
        postCookbookReferencesCache.remove(postId)
    }

    // Clear all references cache
    suspend fun clearAllReferencesCache() = withContext(Dispatchers.IO) {
        postRecipeReferencesCache.clear()
        postCookbookReferencesCache.clear()
    }

    // Validation methods
    suspend fun areRecipeReferencesCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postRecipeReferencesCache.containsKey(postId)
    }

    suspend fun areCookbookReferencesCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postCookbookReferencesCache.containsKey(postId)
    }

    // Cache size information
    suspend fun getReferencesCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "recipeReferences" to postRecipeReferencesCache.values.sumOf { it.size },
            "cookbookReferences" to postCookbookReferencesCache.values.sumOf { it.size }
        )
    }

    // Utility methods for reference management
    suspend fun updateRecipeReference(postId: String, updatedReference: PostRecipeReference) = withContext(Dispatchers.IO) {
        val currentRefs = postRecipeReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        val index = currentRefs.indexOfFirst { it.referenceId == updatedReference.referenceId }
        if (index >= 0) {
            currentRefs[index] = updatedReference
            postRecipeReferencesCache[postId] = currentRefs
        }
    }

    suspend fun updateCookbookReference(postId: String, updatedReference: PostCookbookReference) = withContext(Dispatchers.IO) {
        val currentRefs = postCookbookReferencesCache[postId]?.toMutableList() ?: mutableListOf()
        val index = currentRefs.indexOfFirst { it.referenceId == updatedReference.referenceId }
        if (index >= 0) {
            currentRefs[index] = updatedReference
            postCookbookReferencesCache[postId] = currentRefs
        }
    }

    suspend fun getRecipeReferenceById(postId: String, referenceId: String): PostRecipeReference? = withContext(Dispatchers.IO) {
        return@withContext postRecipeReferencesCache[postId]?.find { it.referenceId == referenceId }
    }

    suspend fun getCookbookReferenceById(postId: String, referenceId: String): PostCookbookReference? = withContext(Dispatchers.IO) {
        return@withContext postCookbookReferencesCache[postId]?.find { it.referenceId == referenceId }
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }
}