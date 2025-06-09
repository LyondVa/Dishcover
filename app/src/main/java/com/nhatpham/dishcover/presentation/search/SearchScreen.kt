// SearchScreen.kt - Main search screen with 3-tab layout
package com.nhatpham.dishcover.presentation.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhatpham.dishcover.domain.model.search.SearchType
import com.nhatpham.dishcover.presentation.search.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle navigation events
    LaunchedEffect(state.navigateToUser, state.navigateToPost, state.navigateToRecipe) {
        state.navigateToUser?.let { userId ->
            onUserClick(userId)
            viewModel.onNavigationHandled()
        }
        state.navigateToPost?.let { postId ->
            onPostClick(postId)
            viewModel.onNavigationHandled()
        }
        state.navigateToRecipe?.let { recipeId ->
            onRecipeClick(recipeId)
            viewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Handle main container top bar padding for small screens
//            .statusBarsPadding()
    ) {
        // Search Bar Section
        SearchBarSection(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            onSearch = { viewModel.onSearchSubmitted(searchQuery) },
            onClearQuery = { viewModel.onSearchQueryChanged("") },
            suggestions = state.searchSuggestions,
            onSuggestionClick = viewModel::onSuggestionClicked,
            isLoading = state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Search Tabs
        SearchTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = viewModel::onTabSelected,
            userCount = state.userResults.size,
            postCount = state.postResults.size,
            recipeCount = state.recipeResults.size,
            modifier = Modifier.fillMaxWidth()
        )

        // Content Section
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                // Show initial state when no search has been performed
                !state.hasSearched && searchQuery.isBlank() -> {
                    SearchInitialContent(
                        recentSearches = state.recentSearches,
                        trendingSearches = state.trendingSearches,
                        onRecentSearchClick = viewModel::onRecentSearchClicked,
                        onTrendingSearchClick = viewModel::onRecentSearchClicked,
                        onClearHistory = viewModel::onClearSearchHistory,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Show loading state
                state.isLoading -> {
                    SearchLoadingContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Show error state
                state.error != null -> {
                    SearchErrorContent(
                        error = state.error!!,
                        onRetry = viewModel::onRetrySearch,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Show empty results
                state.hasSearched && state.totalResults == 0 -> {
                    SearchEmptyContent(
                        query = searchQuery,
                        searchType = state.selectedTab,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Show search results
                else -> {
                    SearchResultsContent(
                        state = state,
                        onUserClick = viewModel::onUserClicked,
                        onPostClick = viewModel::onPostClicked,
                        onRecipeClick = viewModel::onRecipeClicked,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTabRow(
    selectedTab: SearchType,
    onTabSelected: (SearchType) -> Unit,
    userCount: Int,
    postCount: Int,
    recipeCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        SearchTab("Users", SearchType.USERS, userCount),
        SearchTab("Posts", SearchType.POSTS, postCount),
        SearchTab("Recipes", SearchType.RECIPES, recipeCount)
    )

    TabRow(
        selectedTabIndex = when (selectedTab) {
            SearchType.USERS -> 0
            SearchType.POSTS -> 1
            SearchType.RECIPES -> 2
            SearchType.ALL -> 0 // Default to users
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedTab) {
                    SearchType.USERS -> 0
                    SearchType.POSTS -> 1
                    SearchType.RECIPES -> 2
                    SearchType.ALL -> 0
                }]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = when (selectedTab) {
                    SearchType.USERS -> index == 0
                    SearchType.POSTS -> index == 1
                    SearchType.RECIPES -> index == 2
                    SearchType.ALL -> index == 0
                },
                onClick = { onTabSelected(tab.type) },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab.title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == tab.type) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == tab.type) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )

//                    if (tab.count > 0) {
//                        Text(
//                            text = tab.count.toString(),
//                            fontSize = 12.sp,
//                            color = if (selectedTab == tab.type) {
//                                MaterialTheme.colorScheme.primary
//                            } else {
//                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
//                            }
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    state: SearchState,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        when (state.selectedTab) {
            SearchType.USERS -> {
                items(
                    items = state.userResults,
                    key = { it.userId }
                ) { user ->
                    UserSearchResultItem(
                        user = user,
                        onClick = { onUserClick(user.userId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SearchType.POSTS -> {
                items(
                    items = state.postResults,
                    key = { it.postId }
                ) { post ->
                    PostSearchResultItem(
                        post = post,
                        onClick = { onPostClick(post.postId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SearchType.RECIPES -> {
                items(
                    items = state.recipeResults,
                    key = { it.recipeId }
                ) { recipe ->
                    RecipeSearchResultItem(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.recipeId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SearchType.ALL -> {
                // Show all results in sections
                if (state.userResults.isNotEmpty()) {
                    item {
                        SearchSectionHeader(
                            title = "Users",
                            count = state.userResults.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(
                        items = state.userResults.take(3), // Show top 3 in ALL view
                        key = { "user_${it.userId}" }
                    ) { user ->
                        UserSearchResultItem(
                            user = user,
                            onClick = { onUserClick(user.userId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.postResults.isNotEmpty()) {
                    item {
                        SearchSectionHeader(
                            title = "Posts",
                            count = state.postResults.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(
                        items = state.postResults.take(3), // Show top 3 in ALL view
                        key = { "post_${it.postId}" }
                    ) { post ->
                        PostSearchResultItem(
                            post = post,
                            onClick = { onPostClick(post.postId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.recipeResults.isNotEmpty()) {
                    item {
                        SearchSectionHeader(
                            title = "Recipes",
                            count = state.recipeResults.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(
                        items = state.recipeResults.take(3), // Show top 3 in ALL view
                        key = { "recipe_${it.recipeId}" }
                    ) { recipe ->
                        RecipeSearchResultItem(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.recipeId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = count.toString(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SearchLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Searching...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SearchErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Search Error",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun SearchEmptyContent(
    query: String,
    searchType: SearchType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Results Found",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            val searchTypeText = when (searchType) {
                SearchType.USERS -> "users"
                SearchType.POSTS -> "posts"
                SearchType.RECIPES -> "recipes"
                SearchType.ALL -> "results"
            }

            Text(
                text = "No $searchTypeText found for \"$query\"",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try searching with different keywords",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class SearchTab(
    val title: String,
    val type: SearchType,
    val count: Int
)