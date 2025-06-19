// Updated MainContainer.kt - Respects existing function signatures
package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.nhatpham.dishcover.presentation.cookbook.addrecipes.AddRecipesToCookbookScreen
import com.nhatpham.dishcover.presentation.cookbook.create.CreateCookbookScreen
import com.nhatpham.dishcover.presentation.cookbook.detail.CookbookDetailScreen
import com.nhatpham.dishcover.presentation.cookbook.edit.EditCookbookScreen
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
import com.nhatpham.dishcover.presentation.recipe.favorites.FavoritesScreen
import com.nhatpham.dishcover.presentation.recipe.recent.RecentlyViewedScreen
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
        Screen.CreateCookbook.route,
        Screen.EditProfile.route,
        Screen.Settings.route,
        "${Screen.Profile.route}/{userId}",
        Screen.Favorites.route, 
        Screen.RecentlyViewed.route 
    )

    val hideTopBarRoutes = listOf(
        "${Screen.PostDetail.route}/{postId}",
        "${Screen.RecipeDetail.route}/{recipeId}",
        Screen.CreatePost.route,
        Screen.CreateRecipe.route,
        Screen.CreateCookbook.route,
        Screen.EditProfile.route,
        Screen.Settings.route,
        Screen.Favorites.route, 
        Screen.RecentlyViewed.route 
    )

    val shouldShowBottomNav = currentRoute !in hideBottomNavRoutes
    val shouldShowTopBar = currentRoute !in hideTopBarRoutes

    Scaffold(topBar = {
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
    }, bottomBar = {
        if (shouldShowBottomNav) {
            BottomNavigationBar(selectedRoute = currentRoute ?: Screen.Home.route,
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
                })
        }
    }, floatingActionButton = {
        if (shouldShowBottomNav) {
            ExpandableFab(isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onAddRecipe = {
                    isFabExpanded = false
                    internalNavController.navigate(Screen.CreateRecipe.route)
                },
                onAddCookbook = {
                    isFabExpanded = false
                    // Navigate to create cookbook
                    internalNavController.navigate(Screen.CreateCookbook.route)
                },
                onAddPost = {
                    isFabExpanded = false
                    internalNavController.navigate(Screen.CreatePost.route)
                })
        }
    }) { paddingValues ->
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
                HomeScreen(homeViewModel = homeViewModel, // Include existing parameter
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }, onNavigateToCategory = { category ->
                        when (category) {
                            "favorites" -> {
                                internalNavController.navigate(Screen.Favorites.route)
                            }

                            "recent" -> {
                                internalNavController.navigate(Screen.RecentlyViewed.route)
                            }

                            else -> {
                                // Handle other categories - can implement CategoryScreen later
                                // For now, navigate to recipes screen as fallback
                                internalNavController.navigate(Screen.Recipes.route)
                            }
                        }
                    }, onNavigateToProfile = {
                        internalNavController.navigate(Screen.Profile.route)
                    }, onNavigateToCreateRecipe = {
                        internalNavController.navigate(Screen.CreateRecipe.route)
                    }, onSignOut = onSignOut
                )
            }

            composable(route = Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    },
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }
                )
            }

            composable(route = Screen.RecentlyViewed.route) {
                RecentlyViewedScreen(
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    },
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }
                )
            }

            composable(
                route = Screen.Search.route,
//                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
//                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
            ) {
                SearchScreen(
                    onUserClick = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    },
                    onPostClick = { postId ->
                        internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                    },
                    onRecipeClick = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }
                )
            }

            composable(route = Screen.Feed.route) {
                FeedScreen(onNavigateToRecipeDetail = { recipeId ->
                    internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                }, onNavigateToUserProfile = { userId ->
                    internalNavController.navigate("${Screen.Profile.route}/$userId")
                }, onNavigateToPostDetail = { postId ->
                    internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                })
            }

            composable(Screen.Recipes.route) {
                RecipesScreen(
                    onNavigateToRecipeDetail = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToCookbookDetail = { cookbookId ->
                        navController.navigate("cookbook_detail/$cookbookId")
                    },
                    onNavigateToCreateCookbook = {
                        navController.navigate("create_cookbook")
                    }
                )
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(userId = null, // Current user
                    onNavigateToEditProfile = {
                        internalNavController.navigate(Screen.EditProfile.route)
                    }, onNavigateToSettings = {
                        internalNavController.navigate(Screen.Settings.route)
                    }, onNavigateToFollowers = { userId ->
                        // Use the main nav controller for top-level routes
                        navController.navigate("${Screen.Followers.route}/$userId")
                    }, onNavigateToFollowing = { userId ->
                        navController.navigate("${Screen.Following.route}/$userId")
                    }, onNavigateToRecipe = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    }, onNavigateToPostDetail = { postId ->
                        internalNavController.navigate("${Screen.PostDetail.route}/$postId")
                    }, onNavigateToUserProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    })
            }

            // Detail screens (now properly nested with navigation stack)
            composable(
                route = "${Screen.PostDetail.route}/{postId}",
                arguments = listOf(navArgument("postId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val viewModel = hiltViewModel<PostDetailViewModel>()

                PostDetailScreen(postId = postId, onNavigateBack = {
                    internalNavController.navigateUp() // ✅ This will now work correctly!
                }, onNavigateToUserProfile = { userId ->
                    internalNavController.navigate("${Screen.Profile.route}/$userId")
                }, onNavigateToRecipeDetail = { recipeId ->
                    internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                }, viewModel = viewModel
                )
            }

            composable(
                route = "${Screen.RecipeDetail.route}/{recipeId}",
                arguments = listOf(navArgument("recipeId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                val viewModel = hiltViewModel<RecipeDetailViewModel>()

                RecipeDetailScreen(recipeId = recipeId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        internalNavController.navigateUp() // ✅ This will now work correctly!
                    },
                    onNavigateToEdit = { id ->
                        // Use main nav controller for top-level edit routes
                        navController.navigate("${Screen.EditRecipe.route}/$id")
                    },
                    onNavigateToProfile = { userId ->
                        internalNavController.navigate("${Screen.Profile.route}/$userId")
                    }
                )
            }

            composable(route = Screen.CreatePost.route) {
                val viewModel = hiltViewModel<CreatePostViewModel>()
                CreatePostScreen(onNavigateBack = {
                    internalNavController.navigateUp() // ✅ This will now work correctly!
                }, onPostCreated = {
                    internalNavController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.CreatePost.route) { inclusive = true }
                    }
                }, onRecipeClick = { recipeId ->
                    internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                }, viewModel = viewModel
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
                    },
                )
            }



            composable(route = Screen.EditProfile.route) {
                val viewModel = hiltViewModel<ProfileEditViewModel>()
                ProfileEditScreen(viewModel = viewModel, onNavigateBack = {
                    internalNavController.navigateUp() // ✅ This will now work correctly!
                }, onProfileUpdated = {
                    internalNavController.navigateUp()
                })
            }

            composable(route = Screen.Settings.route) {
                val viewModel = hiltViewModel<UserSettingsViewModel>()
                SettingsScreen(viewModel = viewModel, onNavigateBack = {
                    internalNavController.navigateUp() // ✅ This will now work correctly!
                }, onNavigateToPrivacySettings = {
                    navController.navigate(Screen.PrivacySettings.route)
                }, onNavigateToNotificationSettings = {
                    navController.navigate(Screen.NotificationSettings.route)
                }, onNavigateToAccountSettings = {
                    navController.navigate(Screen.AccountSettings.route)
                }, onSignOut = onSignOut
                )
            }

            // Standalone profile view (for viewing other users)
            composable(
                route = "${Screen.Profile.route}/{userId}",
                arguments = listOf(navArgument("userId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                val viewModel = hiltViewModel<ProfileViewModel>()
                ProfileScreen(userId = userId,
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

            composable(Screen.CreateCookbook.route) {
                CreateCookbookScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCookbookCreated = { cookbookId ->
                        // Navigate to the created cookbook detail
                        navController.navigate("cookbook_detail/$cookbookId") {
                            popUpTo("recipes") { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = "${Screen.CookbookDetail.route}/{cookbookId}",
                arguments = listOf(navArgument("cookbookId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val cookbookId = backStackEntry.arguments?.getString("cookbookId") ?: ""

                CookbookDetailScreen(
                    cookbookId = cookbookId,
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    },
                    onNavigateToRecipe = { recipeId ->
                        internalNavController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                    },
                    onNavigateToEdit = {
                        internalNavController.navigate("${Screen.EditCookbook.route}/$cookbookId")
                    },
                    onNavigateToAddRecipes = {
                        internalNavController.navigate("add_recipes_to_cookbook/$cookbookId")
                    }
                )
            }

            composable(
                route = "add_recipes_to_cookbook/{cookbookId}",
                arguments = listOf(navArgument("cookbookId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val cookbookId = backStackEntry.arguments?.getString("cookbookId") ?: ""

                AddRecipesToCookbookScreen(
                    cookbookId = cookbookId,
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    }
                )
            }

            composable(
                route = "${Screen.EditCookbook.route}/{cookbookId}",
                arguments = listOf(navArgument("cookbookId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val cookbookId = backStackEntry.arguments?.getString("cookbookId") ?: ""

                EditCookbookScreen(
                    cookbookId = cookbookId,
                    onNavigateBack = {
                        internalNavController.navigateUp()
                    },
                    onCookbookUpdated = {
                        // Navigate back to cookbook detail with updated data
                        internalNavController.navigateUp()
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
    onAddPost: () -> Unit,
    onAddCookbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Background overlay when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onToggle() }
            )
        }

        // FAB Options
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 80.dp, end = 16.dp)
            ) {
                // Add Recipe
                FabOption(
                    icon = Icons.Default.MenuBook,
                    label = "Recipe",
                    onClick = onAddRecipe,
                    color = Color(0xFF4CAF50)
                )

                // Add Post
                FabOption(
                    icon = Icons.Default.DynamicFeed,
                    label = "Post",
                    onClick = onAddPost,
                    color = Color(0xFF2196F3)
                )

                // Add Cookbook
                FabOption(
                    icon = Icons.Default.Book,
                    label = "Cookbook",
                    onClick = onAddCookbook,
                    color = Color(0xFF9C27B0)
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                label = "fab_icon"
            ) { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (expanded) "Close" else "Add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        // Label
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Icon button
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
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
}