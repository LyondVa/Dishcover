// SearchRepository.kt - Search repository interface
package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.search.*
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    /**
     * Unified search across all content types
     */
    fun searchAll(
        query: String,
        filters: SearchFilters = SearchFilters(),
        pagination: SearchPagination = SearchPagination()
    ): Flow<Resource<SearchResult>>

    /**
     * Search users by username, email (if permitted), bio
     * Uses existing User domain fields only
     */
    fun searchUsers(
        query: String,
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<UserSearchResult>>>

    /**
     * Search posts by content, hashtags, location, tagged users
     * Uses existing Post domain fields only
     */
    fun searchPosts(
        query: String,
        filters: SearchFilters = SearchFilters(),
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<PostSearchResult>>>

    /**
     * Search recipes by title, description, tags, difficulty
     * Uses existing Recipe domain fields only
     */
    fun searchRecipes(
        query: String,
        filters: SearchFilters = SearchFilters(),
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<RecipeSearchResult>>>

    /**
     * Get recent search queries for a user
     */
    fun getRecentSearches(
        userId: String,
        limit: Int = 10
    ): Flow<Resource<List<String>>>

    /**
     * Save search query for user history
     */
    fun saveSearchQuery(
        userId: String,
        query: String,
        searchType: SearchType
    ): Flow<Resource<Unit>>

    /**
     * Clear search history for user
     */
    fun clearSearchHistory(userId: String): Flow<Resource<Unit>>

    /**
     * Get popular/trending search queries
     */
    fun getTrendingSearches(
        searchType: SearchType = SearchType.ALL,
        limit: Int = 10
    ): Flow<Resource<List<String>>>

    /**
     * Get search suggestions based on partial query
     */
    fun getSearchSuggestions(
        partialQuery: String,
        searchType: SearchType = SearchType.ALL,
        limit: Int = 5
    ): Flow<Resource<List<String>>>

    /**
     * Log search analytics for optimization
     */
    fun logSearchAnalytics(analytics: SearchAnalytics): Flow<Resource<Unit>>
}