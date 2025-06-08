package com.nhatpham.dishcover.presentation.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nhatpham.dishcover.presentation.components.EmptyState
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.feed.components.PostItem
import com.nhatpham.dishcover.presentation.notifications.NotificationButton
import com.nhatpham.dishcover.presentation.notifications.NotificationFloatingCard
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToPostDetail: (String) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main feed content
        Column(modifier = Modifier.fillMaxSize()) {
            // Feed tabs with real-time indicators
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                FeedTab.entries.forEach { tab ->
                    Tab(selected = state.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (tab) {
                                        FeedTab.FOLLOWING -> "Following"
                                        FeedTab.POPULAR -> "Popular"
                                        FeedTab.RECENT -> "Recent"
                                    }
                                )
                                if (tab == FeedTab.FOLLOWING && state.hasNewUpdates) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text("•")
                                    }
                                }
                            }
                        })
                }
            }

            // New updates banner
            if (state.hasNewUpdates) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { viewModel.clearNewUpdates() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "New posts available • Tap to refresh",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Feed content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items = state.feedItems, key = { it.feedItemId }) { feedItem ->
                    PostItem(
                        feedItem = feedItem,
                        currentUserId = state.currentUserId,
                        realTimeEngagement = state.realTimeEngagements[feedItem.feedItemId],
                        onLike = { postId, isLiked ->
                            viewModel.onLikePost(postId, isLiked)
                        },
                        onComment = { postId ->
                            onNavigateToPostDetail(postId)
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
                        onPostClick = { postId ->
                            onNavigateToPostDetail(postId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Track post view
                    LaunchedEffect(feedItem.feedItemId) {
                        viewModel.onPostViewed(feedItem.feedItemId)
                    }
                }
            }
        }

        // Floating notification
        state.showNotificationFloater?.let { notification ->
            NotificationFloatingCard(notification = notification,
                onDismiss = { viewModel.dismissNotificationFloater() },
                onTap = {
                    viewModel.markNotificationAsRead(notification.notificationId)
                    viewModel.dismissNotificationFloater()
                    // Navigate to relevant content
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Handle lifecycle events
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.setUserOnlineStatus(true)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.setUserOnlineStatus(false)
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}