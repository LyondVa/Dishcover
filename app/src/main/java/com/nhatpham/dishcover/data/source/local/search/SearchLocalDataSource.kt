// SearchDataSources.kt - Search data source interfaces
package com.nhatpham.dishcover.data.source.local.search

import com.nhatpham.dishcover.domain.model.search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Local data source for search caching and user search history
 */
class SearchLocalDataSource @Inject constructor() {

    // Search result caches
    private val userSearchCache = mutableMapOf<String, List<UserSearchResult>>()
    private val postSearchCache = mutableMapOf<String, List<PostSearchResult>>()
    private val recipeSearchCache = mutableMapOf<String, List<RecipeSearchResult>>()
    private val unifiedSearchCache = mutableMapOf<String, SearchResult>()

    // User search history - Map<userId, List<query>>
    private val searchHistoryCache = mutableMapOf<String, MutableList<String>>()

    // Trending searches cache
    private val trendingSearchesCache = mutableMapOf<SearchType, List<String>>()

    // Search suggestions cache
    private val suggestionsCache = mutableMapOf<String, List<String>>()

    // User search operations
    suspend fun cacheUserSearchResults(query: String, results: List<UserSearchResult>) = withContext(Dispatchers.IO) {
        userSearchCache[query.lowercase().trim()] = results
    }

    suspend fun getUserSearchResults(query: String): List<UserSearchResult> = withContext(Dispatchers.IO) {
        return@withContext userSearchCache[query.lowercase().trim()] ?: emptyList()
    }

    // Post search operations
    suspend fun cachePostSearchResults(query: String, results: List<PostSearchResult>) = withContext(Dispatchers.IO) {
        postSearchCache[query.lowercase().trim()] = results
    }

    suspend fun getPostSearchResults(query: String): List<PostSearchResult> = withContext(Dispatchers.IO) {
        return@withContext postSearchCache[query.lowercase().trim()] ?: emptyList()
    }

    // Recipe search operations
    suspend fun cacheRecipeSearchResults(query: String, results: List<RecipeSearchResult>) = withContext(Dispatchers.IO) {
        recipeSearchCache[query.lowercase().trim()] = results
    }

    suspend fun getRecipeSearchResults(query: String): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        return@withContext recipeSearchCache[query.lowercase().trim()] ?: emptyList()
    }

    // Unified search operations
    suspend fun cacheUnifiedSearchResults(query: String, result: SearchResult) = withContext(Dispatchers.IO) {
        unifiedSearchCache[query.lowercase().trim()] = result
    }

    suspend fun getUnifiedSearchResults(query: String): SearchResult? = withContext(Dispatchers.IO) {
        return@withContext unifiedSearchCache[query.lowercase().trim()]
    }

    // Search history operations
    suspend fun saveSearchQuery(userId: String, query: String) = withContext(Dispatchers.IO) {
        val userHistory = searchHistoryCache.getOrPut(userId) { mutableListOf() }

        // Remove query if it already exists to avoid duplicates
        userHistory.remove(query)

        // Add to the beginning (most recent first)
        userHistory.add(0, query)

        // Keep only last 50 searches
        if (userHistory.size > 50) {
            userHistory.removeAt(userHistory.size - 1)
        }
    }

    suspend fun getRecentSearches(userId: String, limit: Int = 10): List<String> = withContext(Dispatchers.IO) {
        return@withContext searchHistoryCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun clearSearchHistory(userId: String) = withContext(Dispatchers.IO) {
        searchHistoryCache.remove(userId)
    }

    // Trending searches operations
    suspend fun cacheTrendingSearches(searchType: SearchType, searches: List<String>) = withContext(Dispatchers.IO) {
        trendingSearchesCache[searchType] = searches
    }

    suspend fun getTrendingSearches(searchType: SearchType): List<String> = withContext(Dispatchers.IO) {
        return@withContext trendingSearchesCache[searchType] ?: emptyList()
    }

    // Search suggestions operations
    suspend fun cacheSearchSuggestions(partialQuery: String, suggestions: List<String>) = withContext(Dispatchers.IO) {
        suggestionsCache[partialQuery.lowercase().trim()] = suggestions
    }

    suspend fun getSearchSuggestions(partialQuery: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext suggestionsCache[partialQuery.lowercase().trim()] ?: emptyList()
    }

    // Cache management
    suspend fun clearSearchCache() = withContext(Dispatchers.IO) {
        userSearchCache.clear()
        postSearchCache.clear()
        recipeSearchCache.clear()
        unifiedSearchCache.clear()
    }

    suspend fun clearTrendingCache() = withContext(Dispatchers.IO) {
        trendingSearchesCache.clear()
    }

    suspend fun clearSuggestionsCache() = withContext(Dispatchers.IO) {
        suggestionsCache.clear()
    }

    // Cache size information for debugging
    suspend fun getCacheSizes(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "userSearches" to userSearchCache.size,
            "postSearches" to postSearchCache.size,
            "recipeSearches" to recipeSearchCache.size,
            "unifiedSearches" to unifiedSearchCache.size,
            "searchHistories" to searchHistoryCache.values.sumOf { it.size },
            "trendingSearches" to trendingSearchesCache.values.sumOf { it.size },
            "suggestions" to suggestionsCache.size
        )
    }

    // Check if results are cached
    suspend fun hasUserSearchResults(query: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userSearchCache.containsKey(query.lowercase().trim())
    }

    suspend fun hasPostSearchResults(query: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postSearchCache.containsKey(query.lowercase().trim())
    }

    suspend fun hasRecipeSearchResults(query: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext recipeSearchCache.containsKey(query.lowercase().trim())
    }

    suspend fun hasUnifiedSearchResults(query: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext unifiedSearchCache.containsKey(query.lowercase().trim())
    }
}