package com.nhatpham.dishcover.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.components.EmptyState
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.feed.components.CommentDialog
import com.nhatpham.dishcover.presentation.feed.components.PostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCommentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.feedItems.isEmpty() && !state.isLoadingFeed) {
            viewModel.refreshFeed()
        }
    }

    // Show error snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
            viewModel.clearError()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Feed tabs
        TabRow(selectedTabIndex = state.selectedTab.ordinal) {
            FeedTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                FeedTab.FOLLOWING -> "Following"
                                FeedTab.POPULAR -> "Popular"
                                FeedTab.RECENT -> "Recent"
                            }
                        )
                    }
                )
            }
        }

        // Content
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.feedItems.isEmpty() -> {
                EmptyState(
                    message = when (state.selectedTab) {
                        FeedTab.FOLLOWING -> "Follow some users to see their posts here!"
                        FeedTab.POPULAR -> "No popular posts available"
                        FeedTab.RECENT -> "No recent posts available"
                    },
                    icon = Icons.Default.RssFeed
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = state.feedItems,
                        key = { it.feedItemId }
                    ) { feedItem ->
                        PostItem(
                            feedItem = feedItem,
                            currentUserId = state.currentUserId,
                            onLike = { postId, isLiked ->
                                viewModel.onLikePost(postId, isLiked)
                            },
                            onComment = { postId ->
                                // Load comments for this post and show dialog
                                viewModel.loadCommentsForPost(postId)
                                showCommentDialog = true
                            },
                            onShare = { postId ->
                                viewModel.onSharePost(postId)
                            },
                            onUserClick = { userId ->
                                onNavigateToUserProfile(userId)
                            },
                            onRecipeClick = { recipeId ->
                                onNavigateToRecipeDetail(recipeId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Comment dialog
    if (showCommentDialog && state.selectedPostId.isNotEmpty()) {
        CommentDialog(
            postId = state.selectedPostId,
            comments = state.currentPostComments,
            isLoading = state.isLoadingComments,
            onAddComment = { comment ->
                viewModel.onAddComment(state.selectedPostId, comment)
            },
            onDismiss = {
                showCommentDialog = false
                viewModel.onCommentDialogClosed()
            }
        )
    }
}