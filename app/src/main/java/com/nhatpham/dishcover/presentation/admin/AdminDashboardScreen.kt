package com.nhatpham.dishcover.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        state.error?.let { error ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        state.dashboardStats?.let { stats ->
            item {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(getOverviewStats(stats)) { stat ->
                        StatCard(
                            title = stat.title,
                            value = stat.value,
                            icon = stat.icon,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Content Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(getContentStats(stats)) { stat ->
                        StatCard(
                            title = stat.title,
                            value = stat.value,
                            icon = stat.icon,
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "User Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(getUserStats(stats)) { stat ->
                        StatCard(
                            title = stat.title,
                            value = stat.value,
                            icon = stat.icon,
                            modifier = Modifier.width(130.dp)
                        )
                    }
                }
            }

            if (stats.pendingReports > 0 || stats.flaggedPosts > 0) {
                item {
                    Text(
                        text = "Requires Attention",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(getAttentionStats(stats)) { stat ->
                            StatCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                modifier = Modifier.width(140.dp),
                                isAlert = true
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.loadDashboardStats() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = if (isAlert) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isAlert) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAlert) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private data class StatItem(
    val title: String,
    val value: String,
    val icon: ImageVector
)

private fun getOverviewStats(stats: com.nhatpham.dishcover.domain.model.admin.AdminDashboardStats): List<StatItem> {
    return listOf(
        StatItem("Total Users", stats.totalUsers.toString(), Icons.Default.People),
        StatItem("Total Posts", stats.totalPosts.toString(), Icons.Default.Article),
        StatItem("Total Recipes", stats.totalRecipes.toString(), Icons.Default.MenuBook),
        StatItem("New Today", stats.newUsersToday.toString(), Icons.Default.PersonAdd)
    )
}

private fun getContentStats(stats: com.nhatpham.dishcover.domain.model.admin.AdminDashboardStats): List<StatItem> {
    return listOf(
        StatItem("Visible Posts", stats.visiblePosts.toString(), Icons.Default.Visibility),
        StatItem("Hidden Posts", stats.hiddenPosts.toString(), Icons.Default.VisibilityOff),
        StatItem("Removed Posts", stats.removedPosts.toString(), Icons.Default.Delete),
        StatItem("Visible Recipes", stats.visibleRecipes.toString(), Icons.Default.Visibility),
        StatItem("Featured Recipes", stats.featuredRecipes.toString(), Icons.Default.Star)
    )
}

private fun getUserStats(stats: com.nhatpham.dishcover.domain.model.admin.AdminDashboardStats): List<StatItem> {
    return listOf(
        StatItem("Active Users", stats.activeUsers.toString(), Icons.Default.CheckCircle),
        StatItem("Suspended", stats.suspendedUsers.toString(), Icons.Default.Pause),
        StatItem("Banned", stats.bannedUsers.toString(), Icons.Default.Block)
    )
}

private fun getAttentionStats(stats: com.nhatpham.dishcover.domain.model.admin.AdminDashboardStats): List<StatItem> {
    return buildList {
        if (stats.pendingReports > 0) {
            add(StatItem("Pending Reports", stats.pendingReports.toString(), Icons.Default.ReportProblem))
        }
        if (stats.flaggedPosts > 0) {
            add(StatItem("Flagged Content", stats.flaggedPosts.toString(), Icons.Default.Flag))
        }
    }
}