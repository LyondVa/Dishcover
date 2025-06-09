// SearchViewModel.kt - Search screen view model
package com.nhatpham.dishcover.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.search.*
import com.nhatpham.dishcover.domain.usecase.search.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val unifiedSearchUseCase: UnifiedSearchUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val searchPostsUseCase: SearchPostsUseCase,
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val getTrendingSearchesUseCase: GetTrendingSearchesUseCase,
    private val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase,
    private val logSearchAnalyticsUseCase: LogSearchAnalyticsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var currentUserId: String? = null

    init {
        getCurrentUser()
        loadRecentSearches()
        loadTrendingSearches()
        setupSearchFlow()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUserId = result.data?.userId
                        result.data?.userId?.let { userId ->
                            loadRecentSearches()
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error getting current user: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Loading handled in UI
                    }
                }
            }
        }
    }

    private fun setupSearchFlow() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank() && query.length >= 2) {
                        performSearch(query)
                        loadSearchSuggestions(query)
                    } else {
                        clearSearchResults()
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        // Clear results if query is too short
        if (query.length < 2) {
            clearSearchResults()
        }

        // Update suggestions immediately for better UX
        if (query.length >= 2) {
            loadSearchSuggestions(query)
        }
    }

    fun onTabSelected(searchType: SearchType) {
        _state.update { it.copy(selectedTab = searchType) }

        // If we have a current search query, perform search for the new tab
        val currentQuery = _searchQuery.value
        if (currentQuery.isNotBlank() && currentQuery.length >= 2) {
            performSearch(currentQuery)
        }
    }

    fun onSearchSubmitted(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return

        _searchQuery.value = trimmedQuery

        // Save to search history
        currentUserId?.let { userId ->
            viewModelScope.launch {
                saveSearchQueryUseCase(userId, trimmedQuery, state.value.selectedTab).collect { /* Handle if needed */ }
            }
        }

        // Perform search immediately
        performSearch(trimmedQuery)

        // Reload recent searches to include this one
        loadRecentSearches()
    }

    fun onRecentSearchClicked(query: String) {
        onSearchSubmitted(query)
    }

    fun onSuggestionClicked(suggestion: String) {
        onSearchSubmitted(suggestion)
    }

    fun onClearSearchHistory() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                clearSearchHistoryUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.update { it.copy(recentSearches = emptyList()) }
                        }
                        is Resource.Error -> {
                            Timber.e("Error clearing search history: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Loading handled in UI
                        }
                    }
                }
            }
        }
    }

    fun onRetrySearch() {
        val currentQuery = _searchQuery.value
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        }
    }

    fun onUserClicked(userId: String) {
        // Navigate to user profile - implement based on your navigation pattern
        _state.update { it.copy(navigateToUser = userId) }
    }

    fun onPostClicked(postId: String) {
        // Navigate to post details - implement based on your navigation pattern
        _state.update { it.copy(navigateToPost = postId) }
    }

    fun onRecipeClicked(recipeId: String) {
        // Navigate to recipe details - implement based on your navigation pattern
        _state.update { it.copy(navigateToRecipe = recipeId) }
    }

    fun onNavigationHandled() {
        _state.update {
            it.copy(
                navigateToUser = null,
                navigateToPost = null,
                navigateToRecipe = null
            )
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()

        val startTime = System.currentTimeMillis()

        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (state.value.selectedTab) {
                    SearchType.ALL -> {
                        unifiedSearchUseCase(query, state.value.selectedTab).collect { result ->
                            handleUnifiedSearchResult(result, query, startTime)
                        }
                    }
                    SearchType.USERS -> {
                        searchUsersUseCase(query, currentUserId = currentUserId).collect { result ->
                            handleUserSearchResult(result, query, startTime)
                        }
                    }
                    SearchType.POSTS -> {
                        searchPostsUseCase(query, currentUserId = currentUserId).collect { result ->
                            handlePostSearchResult(result, query, startTime)
                        }
                    }
                    SearchType.RECIPES -> {
                        searchRecipesUseCase(query, currentUserId = currentUserId).collect { result ->
                            handleRecipeSearchResult(result, query, startTime)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error performing search")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Search failed"
                    )
                }
            }
        }
    }

    private fun handleUnifiedSearchResult(result: Resource<SearchResult>, query: String, startTime: Long) {
        when (result) {
            is Resource.Success -> {
                result.data?.let { searchResult ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            userResults = searchResult.users,
                            postResults = searchResult.posts,
                            recipeResults = searchResult.recipes,
                            totalResults = searchResult.totalResults,
                            hasSearched = true
                        )
                    }

                    // Log search analytics
                    logSearchAnalytics(query, SearchType.ALL, searchResult.totalResults, startTime)
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        hasSearched = true
                    )
                }
            }
            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun handleUserSearchResult(result: Resource<List<UserSearchResult>>, query: String, startTime: Long) {
        when (result) {
            is Resource.Success -> {
                result.data?.let { users ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            userResults = users,
                            totalResults = users.size,
                            hasSearched = true
                        )
                    }

                    logSearchAnalytics(query, SearchType.USERS, users.size, startTime)
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        hasSearched = true
                    )
                }
            }
            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun handlePostSearchResult(result: Resource<List<PostSearchResult>>, query: String, startTime: Long) {
        when (result) {
            is Resource.Success -> {
                result.data?.let { posts ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            postResults = posts,
                            totalResults = posts.size,
                            hasSearched = true
                        )
                    }

                    logSearchAnalytics(query, SearchType.POSTS, posts.size, startTime)
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        hasSearched = true
                    )
                }
            }
            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun handleRecipeSearchResult(result: Resource<List<RecipeSearchResult>>, query: String, startTime: Long) {
        when (result) {
            is Resource.Success -> {
                result.data?.let { recipes ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            recipeResults = recipes,
                            totalResults = recipes.size,
                            hasSearched = true
                        )
                    }

                    logSearchAnalytics(query, SearchType.RECIPES, recipes.size, startTime)
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        hasSearched = true
                    )
                }
            }
            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadRecentSearches() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                getRecentSearchesUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { searches ->
                                _state.update { it.copy(recentSearches = searches) }
                            }
                        }
                        is Resource.Error -> {
                            Timber.e("Error loading recent searches: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Loading handled in UI
                        }
                    }
                }
            }
        }
    }

    private fun loadTrendingSearches() {
        viewModelScope.launch {
            getTrendingSearchesUseCase(state.value.selectedTab).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { searches ->
                            _state.update { it.copy(trendingSearches = searches) }
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error loading trending searches: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Loading handled in UI
                    }
                }
            }
        }
    }

    private fun loadSearchSuggestions(query: String) {
        viewModelScope.launch {
            getSearchSuggestionsUseCase(query, state.value.selectedTab).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { suggestions ->
                            _state.update { it.copy(searchSuggestions = suggestions) }
                        }
                    }
                    is Resource.Error -> {
                        // Don't show error for suggestions as it's not critical
                        Timber.d("Could not load search suggestions: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Loading handled in UI
                    }
                }
            }
        }
    }

    private fun logSearchAnalytics(query: String, searchType: SearchType, resultCount: Int, startTime: Long) {
        viewModelScope.launch {
            val duration = System.currentTimeMillis() - startTime
            logSearchAnalyticsUseCase(query, searchType, resultCount, duration, currentUserId).collect {
                // Analytics logging - no need to handle result
            }
        }
    }

    private fun clearSearchResults() {
        _state.update {
            it.copy(
                userResults = emptyList(),
                postResults = emptyList(),
                recipeResults = emptyList(),
                totalResults = 0,
                hasSearched = false,
                error = null,
                searchSuggestions = emptyList()
            )
        }
    }
}

data class SearchState(
    val selectedTab: SearchType = SearchType.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,

    // Search results
    val userResults: List<UserSearchResult> = emptyList(),
    val postResults: List<PostSearchResult> = emptyList(),
    val recipeResults: List<RecipeSearchResult> = emptyList(),
    val totalResults: Int = 0,

    // Search history and suggestions
    val recentSearches: List<String> = emptyList(),
    val trendingSearches: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),

    // Navigation
    val navigateToUser: String? = null,
    val navigateToPost: String? = null,
    val navigateToRecipe: String? = null
)