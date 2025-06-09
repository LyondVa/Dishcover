// Enhanced FeedScreen.kt - Modern, visually engaging social feed
package com.nhatpham.dishcover.presentation.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Enhanced Feed Header with Stories-like Online Users
//            FeedHeader(
//                currentUsername = state.currentUsername,
//                onlineUsers = state.onlineUsers,
//                unreadNotificationCount = state.unreadNotificationCount,
//                onNavigateToUserProfile = onNavigateToUserProfile
//            )

            // Modern Tab Bar with indicators and animations
            EnhancedTabRow(
                selectedTab = state.selectedTab,
                hasNewUpdates = state.hasNewUpdates,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            // New updates banner with slide animation
            AnimatedVisibility(
                visible = state.hasNewUpdates,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                NewUpdatesBanner(
                    onClick = {
                        viewModel.clearNewUpdates()
                        viewModel.refreshFeed()
                    }
                )
            }

            // Feed content with improved spacing and animations
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoadingFeed -> {
                        FeedLoadingState()
                    }
                    state.feedError != null -> {
                        FeedErrorState(
                            error = state.feedError!!,
                            onRetry = { viewModel.refreshFeed() }
                        )
                    }
                    state.feedItems.isEmpty() -> {
                        FeedEmptyState(selectedTab = state.selectedTab)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                items = state.feedItems,
                                key = { feedItem ->
                                    // FIX: Ensure unique keys even if feedItemId is empty
                                    feedItem.feedItemId.ifEmpty {
                                        feedItem.post?.postId ?: "empty_${feedItem.hashCode()}"
                                    }
                                }
                            ) { feedItem ->
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
                                    onPostViewed = { postId ->
                                        // Track post view for analytics
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
//                                        .animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-time notification floater
        state.showNotificationFloater?.let { notification ->
            NotificationFloatingCard(
                notification = notification,
                onDismiss = { viewModel.dismissNotificationFloater() },
                modifier = Modifier.align(Alignment.TopCenter),
                onTap = {  }
            )
        }
    }
}

@Composable
private fun FeedHeader(
    currentUsername: String,
    onlineUsers: Set<String>,
    unreadNotificationCount: Int,
    onNavigateToUserProfile: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title with gradient
                Text(
                    text = "DISHCOVER",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                // Notification button
                NotificationButton(
                    unreadCount = unreadNotificationCount,
                    onClick = { /* Navigate to notifications */ }
                )
            }

            // Online users row (Stories-like)
            if (onlineUsers.isNotEmpty()) {
                OnlineUsersRow(
                    onlineUsers = onlineUsers,
                    onUserClick = onNavigateToUserProfile
                )
            }
        }
    }
}

@Composable
private fun OnlineUsersRow(
    onlineUsers: Set<String>,
    onUserClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Active now",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(onlineUsers.take(10).toList()) { userId ->
                OnlineUserAvatar(
                    userId = userId,
                    onClick = { onUserClick(userId) }
                )
            }

            if (onlineUsers.size > 10) {
                item {
                    MoreOnlineUsersIndicator(
                        count = onlineUsers.size - 10
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineUserAvatar(
    userId: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable { onClick() }
    ) {
        // Animated ring around avatar
        val infiniteTransition = rememberInfiniteTransition(label = "online_ring")
        val ringScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ring_scale"
        )

        // Online indicator ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // User avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userId.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Online status indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.BottomEnd)
                .background(Color(0xFF4CAF50), CircleShape)
                .background(
                    Color.White,
                    CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
        }
    }
}

@Composable
private fun MoreOnlineUsersIndicator(count: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EnhancedTabRow(
    selectedTab: FeedTab,
    hasNewUpdates: Boolean,
    onTabSelected: (FeedTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            FeedTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (tab) {
                                    FeedTab.FOLLOWING -> "Following"
                                    FeedTab.POPULAR -> "Popular"
                                    FeedTab.RECENT -> "Recent"
                                },
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )

                            // New updates indicator for Following tab
                            if (tab == FeedTab.FOLLOWING && hasNewUpdates) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = Color(0xFFFF4444),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NewUpdatesBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "New posts available",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Tap to refresh",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FeedLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading your feed...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeedErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
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

@Composable
private fun FeedEmptyState(selectedTab: FeedTab) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (selectedTab) {
                    FeedTab.FOLLOWING -> Icons.Default.PeopleOutline
                    FeedTab.POPULAR -> Icons.Default.TrendingUp
                    FeedTab.RECENT -> Icons.Default.AccessTime
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (selectedTab) {
                FeedTab.FOLLOWING -> "No posts from people you follow"
                FeedTab.POPULAR -> "No popular posts yet"
                FeedTab.RECENT -> "No recent posts"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (selectedTab) {
                FeedTab.FOLLOWING -> "Follow other users to see their posts here"
                FeedTab.POPULAR -> "Be the first to create trending content!"
                FeedTab.RECENT -> "Check back later for new posts"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}