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
fun AdminRecipesScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showModerationDialog by remember { mutableStateOf<AdminContentItem?>(null) }

    InfiniteListHandler(listState = listState) {
        viewModel.loadMoreRecipes()
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
                text = "Recipes Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.isLoading && state.recipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(state.recipes) { recipe ->
            RecipeModerationCard(
                recipe = recipe,
                onModerationAction = { showModerationDialog = it }
            )
        }

        if (state.isLoading && state.recipes.isNotEmpty()) {
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

    showModerationDialog?.let { recipe ->
        RecipeModerationDialog(
            recipe = recipe,
            onDismiss = { showModerationDialog = null },
            onHide = { reason ->
                viewModel.hideRecipe(recipe.contentId, reason)
                showModerationDialog = null
            },
            onUnhide = {
                viewModel.unhideRecipe(recipe.contentId)
                showModerationDialog = null
            },
            onFeature = {
                viewModel.featureRecipe(recipe.contentId)
                showModerationDialog = null
            },
            onUnfeature = {
                viewModel.unfeatureRecipe(recipe.contentId)
                showModerationDialog = null
            },
            onRemove = { reason ->
                viewModel.removeRecipe(recipe.contentId, reason)
                showModerationDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeModerationCard(
    recipe: AdminContentItem,
    onModerationAction: (AdminContentItem) -> Unit
) {
    Card(
        onClick = { onModerationAction(recipe) }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${recipe.username}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (recipe.isFeatured) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Featured",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = recipe.content.take(80) + if (recipe.content.length > 80) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = recipe.status)
            }

            if (recipe.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = recipe.imageUrls.first(),
                    contentDescription = recipe.title,
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
                        .format(recipe.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    if (recipe.isFlagged) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Flagged",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (recipe.reportCount > 0) {
                        Text(
                            text = "${recipe.reportCount} reports",
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
    var showReasonDialog by remember { mutableStateOf<RecipeModerationAction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderate Recipe") },
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
                // RECIPE ACTIONS according to admin flow plan
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
                                        showReasonDialog = RecipeModerationAction.Hide
                                    }
                                ) {
                                    Text("Hide")
                                }
                                Button(
                                    onClick = {
                                        showReasonDialog = RecipeModerationAction.Remove
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
                                        showReasonDialog = RecipeModerationAction.Remove
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
                RecipeModerationAction.Hide -> "Hide Recipe"
                RecipeModerationAction.Remove -> "Remove Recipe"
            },
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                when (action) {
                    RecipeModerationAction.Hide -> onHide(reason)
                    RecipeModerationAction.Remove -> onRemove(reason)
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

private enum class RecipeModerationAction {
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