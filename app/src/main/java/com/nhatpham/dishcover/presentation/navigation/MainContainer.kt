// Updated MainContainer.kt - Respects existing function signatures
package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nhatpham.dishcover.presentation.home.HomeScreen
import com.nhatpham.dishcover.presentation.home.HomeViewModel
import com.nhatpham.dishcover.presentation.search.SearchScreen
import com.nhatpham.dishcover.presentation.feed.FeedScreen
import com.nhatpham.dishcover.presentation.feed.detail.PostDetailScreen
import com.nhatpham.dishcover.presentation.feed.detail.PostDetailViewModel
import com.nhatpham.dishcover.presentation.feed.create.CreatePostScreen
import com.nhatpham.dishcover.presentation.feed.create.CreatePostViewModel
import com.nhatpham.dishcover.presentation.profile.ProfileScreen
import com.nhatpham.dishcover.presentation.profile.ProfileEditScreen
import com.nhatpham.dishcover.presentation.profile.ProfileEditViewModel
import com.nhatpham.dishcover.presentation.profile.ProfileViewModel
import com.nhatpham.dishcover.presentation.profile.settings.SettingsScreen
import com.nhatpham.dishcover.presentation.profile.settings.UserSettingsViewModel
import com.nhatpham.dishcover.presentation.recipe.RecipesScreen
import com.nhatpham.dishcover.presentation.recipe.RecipesViewModel
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailScreen
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailViewModel
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateScreen
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    navController: NavHostController, // Keep existing parameter name
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToAllRecipes: () -> Unit = {},
    onNavigateToCreateRecipe: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    // Create internal navigation controller for proper navigation stack
    val internalNavController = rememberNavController()
    val currentBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var isFabExpanded by remember { mutableStateOf(false) }

    // Determine when to hide UI elements
    val hideBottomNavRoutes = listOf(
        "${Screen.PostDetail.route}/{postId}",
        "${Screen.RecipeDetail.route}/{recipeId}",
        Screen.CreatePost.route,
        Screen.CreateRecipe.route,
        Screen.EditProfile.route,
        Screen.Settings.route,
        "${Screen.Profile.route}/{userId}"
    )

    val hideTopBarRoutes = listOf(
        "${Screen.PostDetail.route}/{postId}",
        "${Screen.RecipeDetail.route}/{recipeId}",
        Screen.CreatePost.route,
        Screen.CreateRecipe.route,
        Screen.EditProfile.route,
        Screen.Settings.route
    )

    val shouldShowBottomNav = currentRoute !in hideBottomNavRoutes
    val shouldShowTopBar = currentRoute !in hideTopBarRoutes

    Scaffold(
        topBar = {
            if (shouldShowTopBar) {
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
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavigationBar(
                    selectedRoute = currentRoute ?: Screen.Home.route,
                    onNavigateToHome = {
                        internalNavController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToSearch = {
                        internalNavController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFeed = {
                        internalNavController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRecipes = {
                        internalNavController.navigate(Screen.Recipes.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        internalNavController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (shouldShowBottomNav) {
                ExpandableFab(
                    isExpanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    onAddRecipe = {
                        isFabExpanded = false
                        internalNavController.navigate(Screen.CreateRecipe.route)
                    },
                    onAddCookbook = {
                        isFabExpanded = false
                        // TODO: Navigate to create cookbook when implemented
                    },
                    onAddPost = {
                        isFabExpanded = false
                        internalNavController.navigate(Screen.CreatePost.route)
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = internalNavController,
            startDestination = Screen.Home.route,
            modifier = if (shouldShowTopBar) {
                Modifier.padding(paddingValues)
            } else {
                Modifier // No padding for screens with their own TopAppBar
            }
        ) {
            // Bottom navigation screens - using exact existing function signatures
            composable(route = Screen.Home.route) {
                val homeViewModel = hiltViewModel<HomeViewModel>()
                HomeScreen(
                    homeViewModel = homeViewModel, // Include existing parameter
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToCategory = onNavigateToCategory,
                    onNavigateToAllRecipes = {
                        internalNavController.navigate(Screen.Recipes.route)
                    },
                    onNavigateToProfile = {
                        internalNavController.navigate(Screen.Profile.route)
                    },
                    onNavigateToCreateRecipe = {
                        internalNavController.navigate(Screen.CreateRecipe.route)
                    },
                    onSignOut = onSignOut
                )
            }

            composable(route = Screen.Search.route) {
                SearchScreen(
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    },
                    onNavigateToHome = {
                        internalNavController.navigate(Screen.Home.route)
                    },
                    onNavigateToFeed = {
                        internalNavController.navigate(Screen.Feed.route)
                    },
                    onNavigateToProfile = {
                        internalNavController.navigate(Screen.Profile.route)
                    },
                    onNavigateToRecipes = {
                        internalNavController.navigate(Screen.Recipes.route)
                    }
                )
            }

            composable(route = Screen.Feed.route) {
                FeedScreen(
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToUserProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    },
                    onNavigateToPostDetail = { postId ->
                        internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                    }
                )
            }

            composable(route = Screen.Recipes.route) {
                val viewModel = hiltViewModel<RecipesViewModel>()
                RecipesScreen(
                    viewModel = viewModel,
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }
                )
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    userId = null, // Current user
                    onNavigateToEditProfile = {
                        internalNavController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToSettings = {
                        internalNavController.navigate(Screen.Settings.route)
                    },
                    onNavigateToFollowers = { userId ->
                        // Use the main nav controller for top-level routes
                        navController.navigate("${Screen.Followers.route}/$userId")
                    },
                    onNavigateToFollowing = { userId ->
                        navController.navigate("${Screen.Following.route}/$userId")
                    },
                    onNavigateToRecipe = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToPostDetail = { postId ->
                        internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                    },
                    onNavigateToUserProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    }
                )
            }

            // Detail screens (now properly nested with navigation stack)
            composable(
                route = "${Screen.PostDetail.route}/{postId}",
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val viewModel = hiltViewModel<PostDetailViewModel>()

                PostDetailScreen(
                    postId = postId,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onNavigateToUserProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    },
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    viewModel = viewModel
                )
            }

            composable(
                route = "${Screen.RecipeDetail.route}/{recipeId}",
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                val viewModel = hiltViewModel<RecipeDetailViewModel>()

                RecipeDetailScreen(
                    recipeId = recipeId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onNavigateToEdit = { id ->
                        // Use main nav controller for top-level edit routes
                        navController.navigate("${Screen.EditRecipe.route}/$id")
                    },
                    onRecipeDeleted = {
                        internalNavController.navigateUp()
                    }
                )
            }

            composable(route = Screen.CreatePost.route) {
                val viewModel = hiltViewModel<CreatePostViewModel>()
                CreatePostScreen(
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onPostCreated = {
                        internalNavController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.CreatePost.route) { inclusive = true }
                        }
                    },
                    onRecipeClick = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    viewModel = viewModel
                )
            }

            composable(route = Screen.CreateRecipe.route) {
                val viewModel = hiltViewModel<RecipeCreateViewModel>()

                RecipeCreateScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onRecipeCreated = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId") {
                            popUpTo(Screen.CreateRecipe.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.EditProfile.route) {
                val viewModel = hiltViewModel<ProfileEditViewModel>()
                ProfileEditScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onProfileUpdated = {
                        internalNavController.navigateUp()
                    }
                )
            }

            composable(route = Screen.Settings.route) {
                val viewModel = hiltViewModel<UserSettingsViewModel>()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onNavigateToPrivacySettings = {
                        navController.navigate(Screen.PrivacySettings.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToAccountSettings = {
                        navController.navigate(Screen.AccountSettings.route)
                    },
                    onSignOut = onSignOut
                )
            }

            // Standalone profile view (for viewing other users)
            composable(
                route = "${Screen.Profile.route}/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                val viewModel = hiltViewModel<ProfileViewModel>()
                ProfileScreen(
                    userId = userId,
                    viewModel = viewModel,
                    onNavigateToFollowers = { userId ->
                        navController.navigate("${Screen.Followers.route}/$userId")
                    },
                    onNavigateToFollowing = { userId ->
                        navController.navigate("${Screen.Following.route}/$userId")
                    },
                    onNavigateToRecipe = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToPostDetail = { postId ->
                        internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                    },
                    onNavigateToUserProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    },
                    onNavigateToSettings = {
                        internalNavController.navigate(Screen.Settings.route)
                    },
                    onNavigateToEditProfile = {
                        internalNavController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    }
                )
            }
        }
    }
}

@Composable
fun ExpandableFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddRecipe: () -> Unit,
    onAddCookbook: () -> Unit,
    onAddPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Backdrop overlay when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onToggle() }
            )
        }

        // Main FAB - always at bottom right
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Add content",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Expanded options positioned above the main FAB
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .offset(y = (-76).dp) // Position above main FAB (56dp + 20dp spacing)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabOption(
                    icon = Icons.Default.RssFeed,
                    label = "Add Post",
                    onClick = onAddPost
                )

                FabOption(
                    icon = Icons.Default.MenuBook,
                    label = "Add Cookbook",
                    onClick = onAddCookbook
                )

                FabOption(
                    icon = Icons.Default.Restaurant,
                    label = "Add Recipe",
                    onClick = onAddRecipe
                )
            }
        }
    }
}

@Composable
fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Mini FAB
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}