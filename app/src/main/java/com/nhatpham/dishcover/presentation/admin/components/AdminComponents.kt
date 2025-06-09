package com.nhatpham.dishcover.presentation.admin.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.admin.*
import com.nhatpham.dishcover.presentation.admin.ContentModerationAction
import com.nhatpham.dishcover.presentation.admin.UserModerationAction
import com.nhatpham.dishcover.presentation.components.EmptyState
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardContent(
    stats: AdminDashboardStats?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    if (isLoading && stats == null) {
        LoadingIndicator()
        return
    }

    if (error != null) {
        EmptyState(
            message = error,
            icon = Icons.Default.Error,
        )
        return
    }

    if (stats == null) {
        EmptyState(
            message = "No dashboard data available",
            icon = Icons.Default.Dashboard
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "Total Users",
                    value = stats.totalUsers.toString(),
                    subtitle = "${stats.newUsersToday} new today",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    title = "Active Users",
                    value = stats.activeUsers.toString(),
                    subtitle = "Currently active",
                    icon = Icons.Default.PersonAdd,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "Total Posts",
                    value = stats.totalPosts.toString(),
                    subtitle = "${stats.publicPosts} public",
                    icon = Icons.Default.Article,
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    title = "Total Recipes",
                    value = stats.totalRecipes.toString(),
                    subtitle = "${stats.featuredRecipes} featured",
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "Flagged Content",
                    value = stats.flaggedPosts.toString(),
                    subtitle = "Needs review",
                    icon = Icons.Default.Flag,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    title = "Pending Reports",
                    value = stats.pendingReports.toString(),
                    subtitle = "Awaiting action",
                    icon = Icons.Default.Report,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AdminContentManagement(
    contentItems: List<AdminContentItem>,
    isLoading: Boolean,
    error: String?,
    onContentAction: (String, AdminContentType, ContentModerationAction) -> Unit,
    onLoadMore: () -> Unit
) {
    if (isLoading && contentItems.isEmpty()) {
        LoadingIndicator()
        return
    }

    if (error != null) {
        EmptyState(
            message = error,
            icon = Icons.Default.Error
        )
        return
    }

    if (contentItems.isEmpty()) {
        EmptyState(
            message = "No content items found",
            icon = Icons.Default.Article
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Content Management",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(
            items = contentItems,
            key = { "${it.contentType.name}-${it.contentId}" }
        ) { content ->
            ContentItemCard(
                content = content,
                onAction = { action ->
                    onContentAction(content.contentId, content.contentType, action)
                }
            )
        }
    }
}

@Composable
fun ContentItemCard(
    content: AdminContentItem,
    onAction: (ContentModerationAction) -> Unit
) {
    var showActionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActionDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (content.title.isNotBlank()) content.title else "Untitled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "@${content.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = content.status)
            }

            if (content.content.isNotBlank()) {
                Text(
                    text = content.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (content.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = content.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeBadge(type = content.contentType)

                    if (content.isFlagged) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Flagged",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault())
                        .format(content.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showActionDialog) {
        ContentActionDialog(
            content = content,
            onDismiss = { showActionDialog = false },
            onAction = { action ->
                onAction(action)
                showActionDialog = false
            }
        )
    }
}

@Composable
fun StatusBadge(status: ContentStatus) {
    val (color, text) = when (status) {
        ContentStatus.ACTIVE -> MaterialTheme.colorScheme.primary to "Active"
        ContentStatus.HIDDEN -> MaterialTheme.colorScheme.secondary to "Hidden"
        ContentStatus.REMOVED -> MaterialTheme.colorScheme.error to "Removed"
        ContentStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.tertiary to "Under Review"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = color.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TypeBadge(type: AdminContentType) {
    val (icon, text) = when (type) {
        AdminContentType.POST -> Icons.Default.Article to "Post"
        AdminContentType.RECIPE -> Icons.Default.Restaurant to "Recipe"
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ContentActionDialog(
    content: AdminContentItem,
    onDismiss: () -> Unit,
    onAction: (ContentModerationAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Moderate Content")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Choose an action for this ${content.contentType.name.lowercase()}:")

                Button(
                    onClick = {
                        onAction(ContentModerationAction.UpdateStatus(ContentStatus.ACTIVE))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Approve")
                }

                Button(
                    onClick = {
                        onAction(ContentModerationAction.UpdateStatus(ContentStatus.HIDDEN))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Hide")
                }

                Button(
                    onClick = {
                        onAction(ContentModerationAction.UpdateStatus(ContentStatus.REMOVED))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }

                if (content.contentType == AdminContentType.RECIPE) {
                    Button(
                        onClick = {
                            onAction(ContentModerationAction.Feature(true))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Feature Recipe")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminUserManagement(
    users: List<AdminUserItem>,
    isLoading: Boolean,
    error: String?,
    onUserAction: (String, UserModerationAction) -> Unit,
    onSearchUsers: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchUsers(it)
            },
            label = { Text("Search users") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        if (isLoading && users.isEmpty()) {
            LoadingIndicator()
            return
        }

        if (error != null) {
            EmptyState(
                message = error,
                icon = Icons.Default.Error
            )
            return
        }

        if (users.isEmpty()) {
            EmptyState(
                message = "No users found",
                icon = Icons.Default.People
            )
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = users,
                key = { it.userId }
            ) { user ->
                UserItemCard(
                    user = user,
                    onAction = { action ->
                        onUserAction(user.userId, action)
                    }
                )
            }
        }
    }
}

@Composable
fun UserItemCard(
    user: AdminUserItem,
    onAction: (UserModerationAction) -> Unit
) {
    var showActionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActionDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (user.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (user.isAdmin) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${user.postCount} posts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${user.recipeCount} recipes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${user.followerCount} followers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusBadge(status = when (user.status) {
                    UserStatus.ACTIVE -> ContentStatus.ACTIVE
                    UserStatus.SUSPENDED -> ContentStatus.HIDDEN
                    UserStatus.BANNED -> ContentStatus.REMOVED
                })

                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault())
                        .format(user.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showActionDialog) {
        UserActionDialog(
            user = user,
            onDismiss = { showActionDialog = false },
            onAction = { action ->
                onAction(action)
                showActionDialog = false
            }
        )
    }
}

@Composable
fun UserActionDialog(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onAction: (UserModerationAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Moderate User: @${user.username}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Choose an action for this user:")

                Button(
                    onClick = {
                        onAction(UserModerationAction.UpdateStatus(UserStatus.ACTIVE))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activate")
                }

                Button(
                    onClick = {
                        onAction(UserModerationAction.UpdateStatus(UserStatus.SUSPENDED))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Suspend")
                }

                Button(
                    onClick = {
                        onAction(UserModerationAction.UpdateStatus(UserStatus.BANNED))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ban")
                }

                if (!user.isAdmin) {
                    Button(
                        onClick = {
                            onAction(UserModerationAction.UpdateAdminStatus(true))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Make Admin")
                    }
                } else {
                    Button(
                        onClick = {
                            onAction(UserModerationAction.UpdateAdminStatus(false))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Remove Admin")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminReportsContent(
    flaggedContent: List<AdminContentItem>,
    isLoading: Boolean,
    error: String?,
    onResolveReport: (String, AdminContentType, ContentModerationAction) -> Unit
) {
    if (isLoading && flaggedContent.isEmpty()) {
        LoadingIndicator()
        return
    }

    if (error != null) {
        EmptyState(
            message = error,
            icon = Icons.Default.Error
        )
        return
    }

    if (flaggedContent.isEmpty()) {
        EmptyState(
            message = "No flagged content found",
            icon = Icons.Default.Flag
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Flagged Content",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(
            items = flaggedContent,
            key = { "${it.contentType.name}-${it.contentId}" }
        ) { content ->
            ContentItemCard(
                content = content,
                onAction = { action ->
                    onResolveReport(content.contentId, content.contentType, action)
                }
            )
        }
    }
}