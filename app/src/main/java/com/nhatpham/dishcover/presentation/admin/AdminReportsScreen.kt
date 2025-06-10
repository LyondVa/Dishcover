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
import com.nhatpham.dishcover.domain.model.admin.AdminContentType
import com.nhatpham.dishcover.domain.model.admin.ContentStatus
import com.nhatpham.dishcover.presentation.components.InfiniteListHandler
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showModerationDialog by remember { mutableStateOf<AdminContentItem?>(null) }

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
                text = "Reports & Flagged Content",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.isLoading && state.flaggedContent.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (state.flaggedContent.isEmpty() && !state.isLoading) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Flagged Content",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "All content has been reviewed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(state.flaggedContent) { content ->
            FlaggedContentCard(
                content = content,
                onModerationAction = { showModerationDialog = it }
            )
        }

        if (state.isLoading && state.flaggedContent.isNotEmpty()) {
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

    showModerationDialog?.let { content ->
        when (content.contentType) {
            AdminContentType.POST -> {
                PostModerationDialog(
                    post = content,
                    onDismiss = { showModerationDialog = null },
                    onHide = { reason ->
                        viewModel.hidePost(content.contentId, reason)
                        showModerationDialog = null
                    },
                    onUnhide = {
                        viewModel.unhidePost(content.contentId)
                        showModerationDialog = null
                    },
                    onRemove = { reason ->
                        viewModel.removePost(content.contentId, reason)
                        showModerationDialog = null
                    }
                )
            }
            AdminContentType.RECIPE -> {
                RecipeModerationDialog(
                    recipe = content,
                    onDismiss = { showModerationDialog = null },
                    onHide = { reason ->
                        viewModel.hideRecipe(content.contentId, reason)
                        showModerationDialog = null
                    },
                    onUnhide = {
                        viewModel.unhideRecipe(content.contentId)
                        showModerationDialog = null
                    },
                    onFeature = {
                        viewModel.featureRecipe(content.contentId)
                        showModerationDialog = null
                    },
                    onUnfeature = {
                        viewModel.unfeatureRecipe(content.contentId)
                        showModerationDialog = null
                    },
                    onRemove = { reason ->
                        viewModel.removeRecipe(content.contentId, reason)
                        showModerationDialog = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlaggedContentCard(
    content: AdminContentItem,
    onModerationAction: (AdminContentItem) -> Unit
) {
    Card(
        onClick = { onModerationAction(content) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Flagged",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = content.contentType.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusChip(status = content.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${content.username}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (content.contentType == AdminContentType.RECIPE && content.isFeatured) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Featured",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (content.title.isNotEmpty()) {
                        Text(
                            text = content.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = content.content.take(100) + if (content.content.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (content.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = content.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }

            content.flagReason?.let { reason ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Flag Reason:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(content.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (content.reportCount > 0) {
                        Icon(
                            Icons.Default.ReportProblem,
                            contentDescription = "Reports",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${content.reportCount} reports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
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

// Reuse the moderation dialogs from other screens
@Composable
private fun PostModerationDialog(
    post: AdminContentItem,
    onDismiss: () -> Unit,
    onHide: (String) -> Unit,
    onUnhide: () -> Unit,
    onRemove: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showReasonDialog by remember { mutableStateOf<ReportModerationAction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderate Flagged Post") },
        text = {
            Column {
                Text("Post by @${post.username}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.content.take(200) + if (post.content.length > 200) "..." else "",
                    style = MaterialTheme.typography.bodySmall
                )

                post.flagReason?.let { flagReason ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Flag Reason: $flagReason",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
                when (post.status) {
                    ContentStatus.VISIBLE -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showReasonDialog = ReportModerationAction.Hide
                                }
                            ) {
                                Text("Hide")
                            }
                            Button(
                                onClick = {
                                    showReasonDialog = ReportModerationAction.Remove
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
                                    showReasonDialog = ReportModerationAction.Remove
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
                ReportModerationAction.Hide -> "Hide Post"
                ReportModerationAction.Remove -> "Remove Post"
            },
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                when (action) {
                    ReportModerationAction.Hide -> onHide(reason)
                    ReportModerationAction.Remove -> onRemove(reason)
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

@Composable
private fun RecipeModerationDialog(
    recipe: AdminContentItem,
    onDismiss: () -> Unit,
    onHide: (String) -> Unit,
    onUnhide: () -> Unit,
    onFeature: () -> Unit,
    onUnfeature: () -> Unit,
    onRemove: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showReasonDialog by remember { mutableStateOf<ReportModerationAction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderate Flagged Recipe") },
        text = {
            Column {
                Text("Recipe by @${recipe.username}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.content.take(200) + if (recipe.content.length > 200) "..." else "",
                    style = MaterialTheme.typography.bodySmall
                )

                recipe.flagReason?.let { flagReason ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Flag Reason: $flagReason",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(
                        text = "Status: ${recipe.status.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recipe.isFeatured) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Featured",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column {
                when (recipe.status) {
                    ContentStatus.VISIBLE -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showReasonDialog = ReportModerationAction.Hide
                                    }
                                ) {
                                    Text("Hide")
                                }
                                Button(
                                    onClick = {
                                        showReasonDialog = ReportModerationAction.Remove
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Remove")
                                }
                            }
                            Button(
                                onClick = if (recipe.isFeatured) onUnfeature else onFeature,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text(if (recipe.isFeatured) "Unfeature" else "Feature")
                            }
                        }
                    }
                    ContentStatus.HIDDEN -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = onUnhide) {
                                    Text("Unhide")
                                }
                                Button(
                                    onClick = {
                                        showReasonDialog = ReportModerationAction.Remove
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Remove")
                                }
                            }
                            if (recipe.isFeatured) {
                                Button(
                                    onClick = onUnfeature,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Unfeature")
                                }
                            }
                        }
                    }
                    ContentStatus.REMOVED -> {
                        Text(
                            text = "This recipe has been permanently removed",
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
                ReportModerationAction.Hide -> "Hide Recipe"
                ReportModerationAction.Remove -> "Remove Recipe"
            },
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                when (action) {
                    ReportModerationAction.Hide -> onHide(reason)
                    ReportModerationAction.Remove -> onRemove(reason)
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

private enum class ReportModerationAction {
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