// SearchRepositoryImpl.kt - Search repository implementation
package com.nhatpham.dishcover.data.repository

import com.nhatpham.dishcover.data.source.remote.search.SearchRemoteDataSource
import com.nhatpham.dishcover.data.source.local.search.SearchLocalDataSource
import com.nhatpham.dishcover.domain.model.search.*
import com.nhatpham.dishcover.domain.repository.SearchRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val searchLocalDataSource: SearchLocalDataSource,
    private val searchRemoteDataSource: SearchRemoteDataSource
) : SearchRepository {

    override fun searchAll(
        query: String,
        filters: SearchFilters,
        pagination: SearchPagination
    ): Flow<Resource<SearchResult>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                emit(Resource.Success(SearchResult(query = trimmedQuery, searchType = filters.searchType)))
                return@flow
            }

            // Check cache first for quick response
            val cachedResult = searchLocalDataSource.getUnifiedSearchResults(trimmedQuery)
            if (cachedResult != null) {
                emit(Resource.Success(cachedResult))
            }

            // Always fetch fresh results from remote
            val remoteResult = searchRemoteDataSource.searchAll(trimmedQuery, filters, pagination)

            // Cache the fresh results
            searchLocalDataSource.cacheUnifiedSearchResults(trimmedQuery, remoteResult)

            // Emit fresh results if different from cache or if cache was empty
            if (cachedResult == null || remoteResult.totalResults != cachedResult.totalResults) {
                emit(Resource.Success(remoteResult))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error in unified search")

            // Try to return cached results as fallback
            val cachedResult = searchLocalDataSource.getUnifiedSearchResults(query.trim())
            if (cachedResult != null) {
                emit(Resource.Success(cachedResult))
            } else {
                emit(Resource.Error(e.message ?: "Search failed"))
            }
        }
    }

    override fun searchUsers(
        query: String,
        limit: Int,
        currentUserId: String?
    ): Flow<Resource<List<UserSearchResult>>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Check cache first
            val cachedResults = searchLocalDataSource.getUserSearchResults(trimmedQuery)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Fetch from remote
            val remoteResults = searchRemoteDataSource.searchUsers(trimmedQuery, limit)

            // Cache results
            searchLocalDataSource.cacheUserSearchResults(trimmedQuery, remoteResults)

            // Emit fresh results if different from cache or cache was empty
            if (cachedResults.isEmpty() || remoteResults != cachedResults) {
                emit(Resource.Success(remoteResults))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error searching users")

            // Fallback to cached results
            val cachedResults = searchLocalDataSource.getUserSearchResults(query.trim())
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            } else {
                emit(Resource.Error(e.message ?: "User search failed"))
            }
        }
    }

    override fun searchPosts(
        query: String,
        filters: SearchFilters,
        limit: Int,
        currentUserId: String?
    ): Flow<Resource<List<PostSearchResult>>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Check cache first
            val cachedResults = searchLocalDataSource.getPostSearchResults(trimmedQuery)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Fetch from remote
            val remoteResults = searchRemoteDataSource.searchPosts(trimmedQuery, filters, limit)

            // Cache results
            searchLocalDataSource.cachePostSearchResults(trimmedQuery, remoteResults)

            // Emit fresh results if different from cache or cache was empty
            if (cachedResults.isEmpty() || remoteResults != cachedResults) {
                emit(Resource.Success(remoteResults))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error searching posts")

            // Fallback to cached results
            val cachedResults = searchLocalDataSource.getPostSearchResults(query.trim())
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            } else {
                emit(Resource.Error(e.message ?: "Post search failed"))
            }
        }
    }

    override fun searchRecipes(
        query: String,
        filters: SearchFilters,
        limit: Int,
        currentUserId: String?
    ): Flow<Resource<List<RecipeSearchResult>>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Check cache first
            val cachedResults = searchLocalDataSource.getRecipeSearchResults(trimmedQuery)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            }

            // Fetch from remote
            val remoteResults = searchRemoteDataSource.searchRecipes(trimmedQuery, filters, limit)

            // Cache results
            searchLocalDataSource.cacheRecipeSearchResults(trimmedQuery, remoteResults)

            // Emit fresh results if different from cache or cache was empty
            if (cachedResults.isEmpty() || remoteResults != cachedResults) {
                emit(Resource.Success(remoteResults))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error searching recipes")

            // Fallback to cached results
            val cachedResults = searchLocalDataSource.getRecipeSearchResults(query.trim())
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults))
            } else {
                emit(Resource.Error(e.message ?: "Recipe search failed"))
            }
        }
    }

    override fun getRecentSearches(
        userId: String,
        limit: Int
    ): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        try {
            // Try local cache first for quick response
            val localResults = searchLocalDataSource.getRecentSearches(userId, limit)
            if (localResults.isNotEmpty()) {
                emit(Resource.Success(localResults))
            }

            // Get from remote to ensure we have the latest
            val remoteResults = searchRemoteDataSource.getRecentSearches(userId, limit)

            // Update local cache with remote results
            remoteResults.forEach { query ->
                searchLocalDataSource.saveSearchQuery(userId, query)
            }

            // Emit remote results if different from local or local was empty
            if (localResults.isEmpty() || remoteResults != localResults) {
                emit(Resource.Success(remoteResults))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error getting recent searches")

            // Fallback to local cache
            val localResults = searchLocalDataSource.getRecentSearches(userId, limit)
            if (localResults.isNotEmpty()) {
                emit(Resource.Success(localResults))
            } else {
                emit(Resource.Error(e.message ?: "Failed to get recent searches"))
            }
        }
    }

    override fun saveSearchQuery(
        userId: String,
        query: String,
        searchType: SearchType
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                emit(Resource.Success(Unit))
                return@flow
            }

            // Save to local cache immediately for quick access
            searchLocalDataSource.saveSearchQuery(userId, trimmedQuery)

            // Save to remote for persistence across devices
            searchRemoteDataSource.saveSearchQuery(userId, trimmedQuery, searchType)

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            Timber.e(e, "Error saving search query")
            emit(Resource.Error(e.message ?: "Failed to save search query"))
        }
    }

    override fun clearSearchHistory(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Clear local cache
            searchLocalDataSource.clearSearchHistory(userId)

            // Note: For remote clearing, you'd need to implement a batch delete operation
            // in the remote data source based on your Firestore security rules

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            Timber.e(e, "Error clearing search history")
            emit(Resource.Error(e.message ?: "Failed to clear search history"))
        }
    }

    override fun getTrendingSearches(
        searchType: SearchType,
        limit: Int
    ): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        try {
            // Check cache first
            val cachedResults = searchLocalDataSource.getTrendingSearches(searchType)
            if (cachedResults.isNotEmpty()) {
                emit(Resource.Success(cachedResults.take(limit)))
            }

            // For now, return empty list as trending searches would require
            // aggregation analytics that aren't implemented yet
            val trendingSearches = emptyList<String>()

            // Cache the results
            searchLocalDataSource.cacheTrendingSearches(searchType, trendingSearches)

            if (cachedResults.isEmpty()) {
                emit(Resource.Success(trendingSearches))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error getting trending searches")
            emit(Resource.Error(e.message ?: "Failed to get trending searches"))
        }
    }

    override fun getSearchSuggestions(
        partialQuery: String,
        searchType: SearchType,
        limit: Int
    ): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        try {
            val trimmedQuery = partialQuery.trim()
            if (trimmedQuery.length < 2) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Check cache first
            val cachedSuggestions = searchLocalDataSource.getSearchSuggestions(trimmedQuery)
            if (cachedSuggestions.isNotEmpty()) {
                emit(Resource.Success(cachedSuggestions.take(limit)))
            }

            // For now, return empty list as search suggestions would require
            // a more sophisticated implementation with search history analysis
            val suggestions = emptyList<String>()

            // Cache the results
            searchLocalDataSource.cacheSearchSuggestions(trimmedQuery, suggestions)

            if (cachedSuggestions.isEmpty()) {
                emit(Resource.Success(suggestions))
            }

        } catch (e: Exception) {
            Timber.e(e, "Error getting search suggestions")
            emit(Resource.Error(e.message ?: "Failed to get search suggestions"))
        }
    }

    override fun logSearchAnalytics(analytics: SearchAnalytics): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Log analytics to remote for aggregation and insights
            searchRemoteDataSource.logSearchAnalytics(analytics)

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            Timber.e(e, "Error logging search analytics")
            // Don't emit error for analytics as it's not critical for user experience
            emit(Resource.Success(Unit))
        }
    }
}