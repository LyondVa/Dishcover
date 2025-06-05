// PostAnalyticsLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local.feed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostAnalyticsLocalDataSource @Inject constructor() {

    // Analytics caches (using simple maps)
    private val postAnalyticsCache = mutableMapOf<String, Map<String, Any>>()
    private val userPostAnalyticsCache = mutableMapOf<String, Map<String, Any>>()

    // Post analytics operations
    suspend fun getPostAnalytics(postId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        return@withContext postAnalyticsCache[postId]
    }

    suspend fun savePostAnalytics(postId: String, analytics: Map<String, Any>) = withContext(Dispatchers.IO) {
        postAnalyticsCache[postId] = analytics
    }

    // User post analytics operations
    suspend fun getUserPostAnalytics(userId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        return@withContext userPostAnalyticsCache[userId]
    }

    suspend fun saveUserPostAnalytics(userId: String, analytics: Map<String, Any>) = withContext(Dispatchers.IO) {
        userPostAnalyticsCache[userId] = analytics
    }

    // Cache management for post deletion
    suspend fun deletePostAnalytics(postId: String) = withContext(Dispatchers.IO) {
        postAnalyticsCache.remove(postId)
    }

    // Cache management for user
    suspend fun clearUserAnalyticsCache(userId: String) = withContext(Dispatchers.IO) {
        userPostAnalyticsCache.remove(userId)
    }

    suspend fun clearAllAnalyticsCache() = withContext(Dispatchers.IO) {
        postAnalyticsCache.clear()
        userPostAnalyticsCache.clear()
    }

    // Validation methods
    suspend fun isPostAnalyticsCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postAnalyticsCache.containsKey(postId)
    }

    suspend fun isUserAnalyticsCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userPostAnalyticsCache.containsKey(userId)
    }

    // Cache size information
    suspend fun getAnalyticsCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "postAnalytics" to postAnalyticsCache.size,
            "userAnalytics" to userPostAnalyticsCache.size
        )
    }

    // Helper methods for analytics updates
    suspend fun updatePostAnalytics(postId: String, key: String, value: Any) = withContext(Dispatchers.IO) {
        val currentAnalytics = postAnalyticsCache[postId]?.toMutableMap() ?: mutableMapOf()
        currentAnalytics[key] = value
        postAnalyticsCache[postId] = currentAnalytics
    }

    suspend fun incrementPostAnalyticsValue(postId: String, key: String, increment: Int = 1) = withContext(Dispatchers.IO) {
        val currentAnalytics = postAnalyticsCache[postId]?.toMutableMap() ?: mutableMapOf()
        val currentValue = (currentAnalytics[key] as? Int) ?: 0
        currentAnalytics[key] = currentValue + increment
        postAnalyticsCache[postId] = currentAnalytics
    }

    suspend fun updateUserAnalytics(userId: String, key: String, value: Any) = withContext(Dispatchers.IO) {
        val currentAnalytics = userPostAnalyticsCache[userId]?.toMutableMap() ?: mutableMapOf()
        currentAnalytics[key] = value
        userPostAnalyticsCache[userId] = currentAnalytics
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }
}