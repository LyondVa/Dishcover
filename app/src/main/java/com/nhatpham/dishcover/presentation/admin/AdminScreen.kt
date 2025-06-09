package com.nhatpham.dishcover.presentation.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhatpham.dishcover.presentation.admin.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin header
        AdminTopBar(
            currentUser = state.currentUser,
            onSignOut = onSignOut
        )

        // Tab row
        AdminTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Content based on selected tab
        when (selectedTab) {
            AdminTab.DASHBOARD -> {
                AdminDashboardContent(
                    stats = state.dashboardStats,
                    isLoading = state.isLoading,
                    error = state.error,
                    onRefresh = { viewModel.loadDashboardStats() }
                )
            }
            AdminTab.POSTS -> {
                AdminPostsManagement(
                    posts = state.posts,
                    isLoading = state.isLoading,
                    error = state.error,
                    onPostAction = { postId, action ->
                        viewModel.moderatePost(postId, action)
                    },
                    onLoadMore = { viewModel.loadMorePosts() }
                )
            }
            AdminTab.RECIPES -> {
                AdminRecipesManagement(
                    recipes = state.recipes,
                    isLoading = state.isLoading,
                    error = state.error,
                    onRecipeAction = { recipeId, action ->
                        viewModel.moderateRecipe(recipeId, action)
                    },
                    onLoadMore = { viewModel.loadMoreRecipes() }
                )
            }
            AdminTab.USERS -> {
                AdminUserManagement(
                    users = state.users,
                    isLoading = state.isLoading,
                    error = state.error,
                    onUserAction = { userId, action ->
                        viewModel.moderateUser(userId, action)
                    },
                    onSearchUsers = { query ->
                        viewModel.searchUsers(query)
                    },
                    onLoadMore = { viewModel.loadMoreUsers() }
                )
            }
            AdminTab.REPORTS -> {
                AdminReportsContent(
                    flaggedContent = state.flaggedContent,
                    isLoading = state.isLoading,
                    error = state.error,
                    onResolveReport = { contentId, contentType, action ->
                        viewModel.moderateContent(contentId, contentType, action)
                    }
                )
            }
        }
    }
}

enum class AdminTab {
    DASHBOARD, POSTS, RECIPES, USERS, REPORTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    currentUser: String?,
    onSignOut: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Admin Console",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentUser != null) {
                    Text(
                        text = "Logged in as $currentUser",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSignOut) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AdminTabRow(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        AdminTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = when (tab) {
                            AdminTab.DASHBOARD -> "Dashboard"
                            AdminTab.POSTS -> "Posts"
                            AdminTab.RECIPES -> "Recipes"
                            AdminTab.USERS -> "Users"
                            AdminTab.REPORTS -> "Reports"
                        },
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            AdminTab.DASHBOARD -> Icons.Default.Dashboard
                            AdminTab.POSTS -> Icons.Default.Article
                            AdminTab.RECIPES -> Icons.Default.Restaurant
                            AdminTab.USERS -> Icons.Default.People
                            AdminTab.REPORTS -> Icons.Default.Flag
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}