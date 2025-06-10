package com.nhatpham.dishcover.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.admin.AdminContentItem
import com.nhatpham.dishcover.domain.model.admin.ContentStatus
import com.nhatpham.dishcover.presentation.components.InfiniteListHandler
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminPostsScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showModerationDialog by remember { mutableStateOf<AdminContentItem?>(null) }

    InfiniteListHandler(listState = listState) {
        viewModel.loadMorePosts()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Posts Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.isLoading && state.posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(state.posts) { post ->
            PostModerationCard(
                post = post,
                onModerationAction = { showModerationDialog = it }
            )
        }

        if (state.isLoading && state.posts.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    showModerationDialog?.let { post ->
        PostModerationDialog(
            post = post,
            onDismiss = { showModerationDialog = null },
            onHide = { reason ->
                viewModel.hidePost(post.contentId, reason)
                showModerationDialog = null
            },
            onUnhide = {
                viewModel.unhidePost(post.contentId)
                showModerationDialog = null
            },
            onRemove = { reason ->
                viewModel.removePost(post.contentId, reason)
                showModerationDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostModerationCard(
    post: AdminContentItem,
    onModerationAction: (AdminContentItem) -> Unit
) {
    Card(
        onClick = { onModerationAction(post) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${post.username}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = post.content.take(100) + if (post.content.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(status = post.status)
            }

            if (post.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(post.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    if (post.isFlagged) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Flagged",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (post.reportCount > 0) {
                        Text(
                            text = "${post.reportCount} reports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ContentStatus) {
    val (text, color) = when (status) {
        ContentStatus.VISIBLE -> "Visible" to MaterialTheme.colorScheme.primary
        ContentStatus.HIDDEN -> "Hidden" to MaterialTheme.colorScheme.secondary
        ContentStatus.REMOVED -> "Removed" to MaterialTheme.colorScheme.error
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun PostModerationDialog(
    post: AdminContentItem,
    onDismiss: () -> Unit,
    onHide: (String) -> Unit,
    onUnhide: () -> Unit,
    onRemove: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showReasonDialog by remember { mutableStateOf<ModerationAction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderate Post") },
        text = {
            Column {
                Text("Post by @${post.username}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.content.take(200) + if (post.content.length > 200) "..." else "",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Current Status: ${post.status.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Column {
                // POST ACTIONS according to admin flow plan
                when (post.status) {
                    ContentStatus.VISIBLE -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showReasonDialog = ModerationAction.Hide
                                }
                            ) {
                                Text("Hide")
                            }
                            Button(
                                onClick = {
                                    showReasonDialog = ModerationAction.Remove
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                    ContentStatus.HIDDEN -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = onUnhide) {
                                Text("Unhide")
                            }
                            Button(
                                onClick = {
                                    showReasonDialog = ModerationAction.Remove
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                    ContentStatus.REMOVED -> {
                        Text(
                            text = "This post has been permanently removed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    showReasonDialog?.let { action ->
        ReasonDialog(
            title = when (action) {
                ModerationAction.Hide -> "Hide Post"
                ModerationAction.Remove -> "Remove Post"
            },
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                when (action) {
                    ModerationAction.Hide -> onHide(reason)
                    ModerationAction.Remove -> onRemove(reason)
                }
                reason = ""
                showReasonDialog = null
            },
            onDismiss = {
                reason = ""
                showReasonDialog = null
            }
        )
    }
}

private enum class ModerationAction {
    Hide, Remove
}

@Composable
private fun ReasonDialog(
    title: String,
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Please provide a reason for this action:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    placeholder = { Text("Enter reason...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = reason.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}