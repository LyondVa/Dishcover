package com.nhatpham.dishcover.presentation.feed.detail

import PostDetailContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.feed.detail.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToRecipeDetail: (String) -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    // Handle errors
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar and clear error
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.post != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Post header and content
                    item {
                        PostDetailHeader(
                            post = state.post!!,
                            author = state.author,
                            onUserClick = onNavigateToUserProfile,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Post content with expandable text
                    item {
                        PostDetailContent(
                            post = state.post!!,
                            onRecipeClick = onNavigateToRecipeDetail,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Interaction stats (like count, comment count)
                    if (state.post!!.likeCount > 0 || state.post!!.commentCount > 0) {
                        item {
                            PostDetailStats(
                                post = state.post!!,
                                onLikesClick = { viewModel.loadPostLikes() },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    // Action buttons (Like, Comment, Share)
                    item {
                        PostDetailActions(
                            post = state.post!!,
                            isLikedByCurrentUser = state.isLikedByCurrentUser,
                            isSharedByCurrentUser = state.isSharedByCurrentUser,
                            onLike = { viewModel.toggleLike() },
                            onComment = { /* Focus comment input */ },
                            onShare = { viewModel.sharePost() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Comment input
                    item {
                        PostDetailCommentInput(
                            currentUserProfilePicture = state.currentUserProfilePicture,
                            onAddComment = { content ->
                                viewModel.addComment(content)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Comments section
                    if (state.isLoadingComments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(
                            items = state.comments,
                            key = { it.commentId }
                        ) { comment ->
                            PostDetailCommentItem(
                                comment = comment,
                                onLikeComment = { commentId ->
                                    viewModel.toggleCommentLike(commentId)
                                },
                                onReplyToComment = { commentId ->
                                    viewModel.replyToComment(commentId)
                                },
                                onUserClick = onNavigateToUserProfile,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Load more comments button
                    if (state.hasMoreComments) {
                        item {
                            TextButton(
                                onClick = { viewModel.loadMoreComments() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Load more comments")
                            }
                        }
                    }
                }
            }
            else -> {
                // Error state or post not found
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Post not found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This post may have been deleted or you don't have permission to view it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}