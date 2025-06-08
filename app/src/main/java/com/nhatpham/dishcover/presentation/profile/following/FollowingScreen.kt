package com.nhatpham.dishcover.presentation.profile.following

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.component.EmptyState
import com.nhatpham.dishcover.presentation.component.LoadingIndicator
import com.nhatpham.dishcover.presentation.profile.followers.UserListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: FollowingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadFollowing(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Following",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.targetUser != null) {
                            Text(
                                text = "@${state.targetUser!!.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.error != null -> {
                EmptyState(
                    message = state.error ?: "Failed to load following",
                    icon = Icons.Default.Error
                )
            }
            state.following.isEmpty() -> {
                EmptyState(
                    message = if (state.isCurrentUser) {
                        "You're not following anyone yet"
                    } else {
                        "${state.targetUser?.username ?: "This user"} isn't following anyone yet"
                    },
                    icon = Icons.Default.PersonAdd
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(
                        items = state.following,
                        key = { it.userId }
                    ) { user ->
                        UserListItem(
                            user = user,
                            currentUserId = state.currentUserId,
                            isFollowing = state.followingStatus[user.userId] ?: false,
                            isUpdatingFollow = state.updatingFollowStatus.contains(user.userId),
                            onUserClick = { onNavigateToProfile(user.userId) },
                            onToggleFollow = { viewModel.toggleFollow(user.userId) }
                        )
                    }
                }
            }
        }
    }
}