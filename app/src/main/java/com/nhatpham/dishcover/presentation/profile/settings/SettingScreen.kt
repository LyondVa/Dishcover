package com.nhatpham.dishcover.presentation.profile.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.component.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: UserSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmText = "Sign Out",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.signOut()
                onSignOut()
                showSignOutDialog = false
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to delete your account? This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                // Handle delete account
                showDeleteAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Account Section
            SettingsSectionHeader(title = "Account")

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Account Information",
                onClick = onNavigateToAccountSettings
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Privacy",
                onClick = onNavigateToPrivacySettings
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                onClick = onNavigateToNotificationSettings
            )

            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                onClick = { showSignOutDialog = true }
            )

            // Preferences Section
            SettingsSectionHeader(title = "Preferences")

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = "Light",
                onClick = { /* Open theme dialog */ }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "English",
                onClick = { /* Open language dialog */ }
            )

            SettingsItem(
                icon = Icons.Default.Restaurant,
                title = "Units",
                subtitle = "Metric",
                onClick = { /* Open units dialog */ }
            )

            // Support section
            SettingsSectionHeader(title = "Support")

            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & FAQ",
                onClick = { /* Open help page */ }
            )

            SettingsItem(
                icon = Icons.Default.Feedback,
                title = "Send Feedback",
                onClick = { /* Open feedback form */ }
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About Dishcover",
                onClick = { /* Open about page */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Danger Zone
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteAccountDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: androidx.compose.ui.graphics.Color = LocalContentColor.current,
    titleColor: androidx.compose.ui.graphics.Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Divider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}