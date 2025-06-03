package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nhatpham.dishcover.presentation.home.HomeScreen
import com.nhatpham.dishcover.presentation.search.SearchScreen
import com.nhatpham.dishcover.presentation.feed.FeedScreen
import com.nhatpham.dishcover.presentation.profile.ProfileScreen
import com.nhatpham.dishcover.presentation.recipe.RecipesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    navController: NavHostController,
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToAllRecipes: () -> Unit = {},
    onNavigateToCreateRecipe: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var selectedBottomNavRoute by remember { mutableStateOf(Screen.Home.route) }

    Scaffold(
        topBar = {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "DISHCOVER",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
//                actions = {
//                    IconButton(onClick = {
//                        showAddMenu = true
//                    }) {
//                        Icon(Icons.Default.Add, contentDescription = "Add")
//                    }
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Default.Search, contentDescription = "Search")
//                    }
//                    IconButton(onClick = onNavigateToProfile) {
//                        Icon(
//                            Icons.Default.Person,
//                            contentDescription = "Profile",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                    IconButton(onClick = { showLogoutDialog = true }) {
//                        Icon(
//                            Icons.Default.Logout,
//                            contentDescription = "Logout",
//                            tint = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = selectedBottomNavRoute,
                onNavigateToHome = {
                    selectedBottomNavRoute = Screen.Home.route
                },
                onNavigateToSearch = {
                    selectedBottomNavRoute = Screen.Search.route
                },
                onNavigateToFeed = {
                    selectedBottomNavRoute = Screen.Feed.route
                },
                onNavigateToRecipes = {
                    selectedBottomNavRoute = Screen.Recipes.route
                },
                onNavigateToProfile = {
                    selectedBottomNavRoute = Screen.Profile.route
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedBottomNavRoute) {
                Screen.Home.route -> {
                    HomeScreen(
                        onNavigateToRecipeDetail = onNavigateToRecipeDetail,
                        onNavigateToCategory = onNavigateToCategory,
                        onNavigateToAllRecipes = onNavigateToAllRecipes,
                    )
                }
                Screen.Search.route -> {
                    SearchScreen(
                        onNavigateToRecipeDetail = onNavigateToRecipeDetail,
                        onNavigateBack = {
                            selectedBottomNavRoute = Screen.Home.route
                        },
                        onNavigateToHome = {
                            selectedBottomNavRoute = Screen.Home.route
                        },
                        onNavigateToFeed = {
                            selectedBottomNavRoute = Screen.Feed.route
                        },
                        onNavigateToRecipes = {
                            selectedBottomNavRoute = Screen.Recipes.route
                        },
                        onNavigateToProfile = {
                            selectedBottomNavRoute = Screen.Profile.route
                        }
                    )
                }
                Screen.Feed.route -> {
                    FeedScreen(
                        onNavigateToRecipeDetail = onNavigateToRecipeDetail,
                        onNavigateToUserProfile = { userId ->
                            navController.navigate("${Screen.Profile.route}/$userId")
                        }
                    )
                }
                Screen.Recipes.route -> {
                    RecipesScreen(
                        onNavigateToRecipeDetail = onNavigateToRecipeDetail,
                        onNavigateToCreateRecipe = onNavigateToCreateRecipe
                    )
                }
                Screen.Profile.route -> {
                    ProfileScreen(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
        }
    }
}