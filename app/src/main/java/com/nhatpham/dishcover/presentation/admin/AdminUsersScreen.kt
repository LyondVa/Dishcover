package com.nhatpham.dishcover.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.admin.AdminUserItem
import com.nhatpham.dishcover.domain.model.admin.UserStatus
import com.nhatpham.dishcover.presentation.components.InfiniteListHandler
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminUsersScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showModerationDialog by remember { mutableStateOf<AdminUserItem?>(null) }

    InfiniteListHandler(listState = listState) {
        viewModel.loadMoreUsers()
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
                text = "Users Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.isLoading && state.users.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(state.users) { user ->
            UserModerationCard(
                user = user,
                onModerationAction = { showModerationDialog = it }
            )
        }

        if (state.isLoading && state.users.isNotEmpty()) {
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

    showModerationDialog?.let { user ->
        UserModerationDialog(
            user = user,
            onDismiss = { showModerationDialog = null },
            onSuspend = { reason ->
                viewModel.suspendUser(user.userId, reason)
                showModerationDialog = null
            },
            onUnsuspend = {
                viewModel.unsuspendUser(user.userId)
                showModerationDialog = null
            },
            onMakeAdmin = {
                viewModel.makeAdmin(user.userId)
                showModerationDialog = null
            },
            onRemoveAdmin = {
                viewModel.removeAdmin(user.userId)
                showModerationDialog = null
            },
            onBan = { reason ->
                viewModel.banUser(user.userId, reason)
                showModerationDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserModerationCard(
    user: AdminUserItem,
    onModerationAction: (AdminUserItem) -> Unit
) {
    Card(
        onClick = { onModerationAction(user) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = user.username,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text(
                        text = "${user.postCount} posts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${user.recipeCount} recipes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${user.followerCount} followers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (user.reportCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.reportCount} reports",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Joined ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(user.createdAt.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            UserStatusChip(status = user.status)
        }
    }
}

@Composable
private fun UserStatusChip(status: UserStatus) {
    val (text, color) = when (status) {
        UserStatus.ACTIVE -> "Active" to MaterialTheme.colorScheme.primary
        UserStatus.SUSPENDED -> "Suspended" to MaterialTheme.colorScheme.secondary
        UserStatus.BANNED -> "Banned" to MaterialTheme.colorScheme.error
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
private fun UserModerationDialog(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onSuspend: (String) -> Unit,
    onUnsuspend: () -> Unit,
    onMakeAdmin: () -> Unit,
    onRemoveAdmin: () -> Unit,
    onBan: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showReasonDialog by remember { mutableStateOf<UserModerationAction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderate User") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = user.profilePicture,
                        contentDescription = user.username,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (user.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (user.isAdmin) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        text = "Status: ${user.status.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${user.postCount} posts • ${user.recipeCount} recipes • ${user.followerCount} followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (user.reportCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${user.reportCount} reports against this user",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Column {
                // USER ACTIONS according to admin flow plan
                when (user.status) {
                    UserStatus.ACTIVE -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showReasonDialog = UserModerationAction.Suspend
                                    }
                                ) {
                                    Text("Suspend")
                                }
                                Button(
                                    onClick = {
                                        showReasonDialog = UserModerationAction.Ban
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Ban")
                                }
                            }
                            Button(
                                onClick = if (user.isAdmin) onRemoveAdmin else onMakeAdmin,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text(if (user.isAdmin) "Remove Admin" else "Make Admin")
                            }
                        }
                    }
                    UserStatus.SUSPENDED -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = onUnsuspend) {
                                    Text("Unsuspend")
                                }
                                Button(
                                    onClick = {
                                        showReasonDialog = UserModerationAction.Ban
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Ban")
                                }
                            }
                            if (user.isAdmin) {
                                Button(
                                    onClick = onRemoveAdmin,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Remove Admin")
                                }
                            }
                        }
                    }
                    UserStatus.BANNED -> {
                        Column {
                            Text(
                                text = "This user has been permanently banned",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (user.isAdmin) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onRemoveAdmin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Remove Admin")
                                }
                            }
                        }
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
                UserModerationAction.Suspend -> "Suspend User"
                UserModerationAction.Ban -> "Ban User"
            },
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                when (action) {
                    UserModerationAction.Suspend -> onSuspend(reason)
                    UserModerationAction.Ban -> onBan(reason)
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

private enum class UserModerationAction {
    Suspend, Ban
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