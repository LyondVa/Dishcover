package com.nhatpham.dishcover.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class AdminDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : AdminDestination("admin_dashboard", "Dashboard", Icons.Default.Dashboard)
    object Posts : AdminDestination("admin_posts", "Posts", Icons.Default.Article)
    object Recipes : AdminDestination("admin_recipes", "Recipes", Icons.Default.MenuBook)
    object Users : AdminDestination("admin_users", "Users", Icons.Default.People)
    object Reports : AdminDestination("admin_reports", "Reports", Icons.Default.Flag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val destinations = listOf(
        AdminDestination.Dashboard,
        AdminDestination.Posts,
        AdminDestination.Recipes,
        AdminDestination.Users,
        AdminDestination.Reports
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = destinations.find {
                            currentDestination?.route == it.route
                        }?.title ?: "Admin"
                    )
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(destination.icon, contentDescription = destination.title)
                        },
                        label = { Text(destination.title) },
                        selected = currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AdminDestination.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AdminDestination.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminDestination.Dashboard.route) {
                AdminDashboardScreen()
            }
            composable(AdminDestination.Posts.route) {
                AdminPostsScreen()
            }
            composable(AdminDestination.Recipes.route) {
                AdminRecipesScreen()
            }
            composable(AdminDestination.Users.route) {
                AdminUsersScreen()
            }
            composable(AdminDestination.Reports.route) {
                AdminReportsScreen()
            }
        }
    }
}