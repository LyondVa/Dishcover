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
import androidx.navigation.NavHostController
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
    var isFabExpanded by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            ExpandableFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onAddRecipe = {
                    isFabExpanded = false
                    onNavigateToCreateRecipe()
                },
                onAddCookbook = {
                    isFabExpanded = false
                    // TODO: Navigate to create cookbook when implemented
                },
                onAddPost = {
                    isFabExpanded = false
                    navController.navigate(Screen.CreatePost.route)
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
                        onNavigateToCreateRecipe = onNavigateToCreateRecipe,
                        onSignOut = onSignOut
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
                        },
                        onNavigateToPostDetail = { postId ->
                            navController.navigate("${Screen.PostDetail.route}/$postId")
                        }
                    )
                }
                Screen.Recipes.route -> {
                    RecipesScreen(
                        onNavigateToRecipeDetail = onNavigateToRecipeDetail,
                    )
                }
                Screen.Profile.route -> {
                    ProfileScreen(
                        userId = null, // null means current user
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToFollowers = { userId ->
                            navController.navigate("${Screen.Followers.route}/$userId")
                        },
                        onNavigateToFollowing = { userId ->
                            navController.navigate("${Screen.Following.route}/$userId")
                        },
                        onNavigateToRecipe = { recipeId ->
                            navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                        },
                        onNavigateToPostDetail = { postId ->
                            navController.navigate("${Screen.PostDetail.route}/$postId")
                        },
                        onNavigateToUserProfile = { userId ->
                            navController.navigate("${Screen.Profile.route}/$userId")
                        },
                        onNavigateBack = {
                            // For main profile, don't go back, just stay
                        }
                    )
                }
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