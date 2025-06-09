// SearchUseCases.kt - Search use cases
package com.nhatpham.dishcover.domain.usecase.search

import com.nhatpham.dishcover.domain.model.search.*
import com.nhatpham.dishcover.domain.repository.SearchRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Unified search across all content types
 */
class UnifiedSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        searchType: SearchType = SearchType.ALL,
        filters: SearchFilters = SearchFilters(),
        pagination: SearchPagination = SearchPagination()
    ): Flow<Resource<SearchResult>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(
                Resource.Success(SearchResult(query = query, searchType = searchType))
            )
        }

        val updatedFilters = filters.copy(searchType = searchType)
        return searchRepository.searchAll(query, updatedFilters, pagination)
    }
}

/**
 * Search users by username and bio content
 */
class SearchUsersUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<UserSearchResult>>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }

        return searchRepository.searchUsers(query, limit, currentUserId)
    }
}

/**
 * Search posts by content, hashtags, and metadata
 */
class SearchPostsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        postTypes: List<String> = emptyList(),
        hasImages: Boolean? = null,
        isPublicOnly: Boolean = true,
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<PostSearchResult>>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }

        val filters = SearchFilters(
            searchType = SearchType.POSTS,
            postTypes = postTypes,
            isPublicOnly = isPublicOnly,
            hasImages = hasImages,
            maxResults = limit
        )

        return searchRepository.searchPosts(query, filters, limit, currentUserId)
    }
}

/**
 * Search recipes by title, description, and tags
 */
class SearchRecipesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        difficultyLevels: List<String> = emptyList(),
        isFeaturedOnly: Boolean = false,
        isPublicOnly: Boolean = true,
        minLikes: Int? = null,
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Resource<List<RecipeSearchResult>>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }

        val filters = SearchFilters(
            searchType = SearchType.RECIPES,
            difficultyLevels = difficultyLevels,
            isFeaturedOnly = isFeaturedOnly,
            isPublicOnly = isPublicOnly,
            minLikes = minLikes,
            maxResults = limit
        )
        Timber.tag("Search Use Case").d("query: $query, filters: $filters, limit: $limit, currentUserId: $currentUserId")
        return searchRepository.searchRecipes(query, filters, limit, currentUserId)
    }
}

/**
 * Get recent search history for user
 */
class GetRecentSearchesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        userId: String,
        limit: Int = 10
    ): Flow<Resource<List<String>>> {
        return searchRepository.getRecentSearches(userId, limit)
    }
}

/**
 * Save search query to user history
 */
class SaveSearchQueryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        userId: String,
        query: String,
        searchType: SearchType
    ): Flow<Resource<Unit>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(Unit))
        }

        return searchRepository.saveSearchQuery(userId, query, searchType)
    }
}

/**
 * Clear search history for user
 */
class ClearSearchHistoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Unit>> {
        return searchRepository.clearSearchHistory(userId)
    }
}

/**
 * Get trending searches
 */
class GetTrendingSearchesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        searchType: SearchType = SearchType.ALL,
        limit: Int = 10
    ): Flow<Resource<List<String>>> {
        return searchRepository.getTrendingSearches(searchType, limit)
    }
}

/**
 * Get search suggestions for autocomplete
 */
class GetSearchSuggestionsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        partialQuery: String,
        searchType: SearchType = SearchType.ALL,
        limit: Int = 5
    ): Flow<Resource<List<String>>> {
        if (partialQuery.length < 2) {
            return kotlinx.coroutines.flow.flowOf(Resource.Success(emptyList()))
        }

        return searchRepository.getSearchSuggestions(partialQuery, searchType, limit)
    }
}

/**
 * Log search analytics
 */
class LogSearchAnalyticsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        searchType: SearchType,
        resultCount: Int,
        searchDuration: Long,
        userId: String?
    ): Flow<Resource<Unit>> {
        val analytics = SearchAnalytics(
            query = query,
            searchType = searchType,
            resultCount = resultCount,
            searchDuration = searchDuration,
            userId = userId
        )

        return searchRepository.logSearchAnalytics(analytics)
    }
}