package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nhatpham.dishcover.presentation.auth.AuthViewModel
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordViewModel
import com.nhatpham.dishcover.presentation.auth.login.LoginScreen
import com.nhatpham.dishcover.presentation.auth.login.LoginViewModel
import com.nhatpham.dishcover.presentation.auth.register.RegisterScreen
import com.nhatpham.dishcover.presentation.auth.register.RegisterViewModel
import com.nhatpham.dishcover.presentation.feed.create.CreatePostScreen
import com.nhatpham.dishcover.presentation.feed.create.CreatePostViewModel
import com.nhatpham.dishcover.presentation.feed.detail.PostDetailScreen
import com.nhatpham.dishcover.presentation.home.HomeScreen
import com.nhatpham.dishcover.presentation.home.HomeViewModel
import com.nhatpham.dishcover.presentation.profile.ProfileEditScreen
import com.nhatpham.dishcover.presentation.profile.ProfileEditViewModel
import com.nhatpham.dishcover.presentation.profile.ProfileScreen
import com.nhatpham.dishcover.presentation.profile.ProfileViewModel
import com.nhatpham.dishcover.presentation.profile.settings.SettingsScreen
import com.nhatpham.dishcover.presentation.profile.settings.UserSettingsViewModel
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateScreen
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailScreen
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailViewModel
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditScreen
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditViewModel
import com.nhatpham.dishcover.presentation.recipe.shared.SharedRecipeScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onGoogleSignIn: () -> Unit,
    setLoginViewModel: (LoginViewModel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication screens
        composable(route = Screen.Login.route) {
            val viewModel = hiltViewModel<LoginViewModel>()
            setLoginViewModel(viewModel)

            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoogleSignIn = onGoogleSignIn
            )
        }

        composable(route = Screen.Register.route) {
            val viewModel = hiltViewModel<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            val viewModel = hiltViewModel<ForgotPasswordViewModel>()
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app with bottom navigation
        composable(route = "main") {
            MainContainer(
                navController = navController,
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                },
                onNavigateToCreateRecipe = {
                    navController.navigate(Screen.CreateRecipe.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // Recipe detail screen (authenticated users)
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
                    navController.navigateUp()
                },
                onNavigateToEdit = { id ->
                    navController.navigate("${Screen.EditRecipe.route}/$id")
                },
                onRecipeDeleted = {
                    navController.navigateUp()
                }
            )
        }

        // Shared recipe screen (public access via deep links)
        composable(
            route = "${Screen.SharedRecipe.route}/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val viewModel = hiltViewModel<RecipeDetailViewModel>()

            SharedRecipeScreen(
                recipeId = recipeId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToApp = {
                    // Navigate to main app or login if not authenticated
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = false }
                    }
                }
            )
        }

        // Recipe creation and editing
        composable(route = Screen.CreateRecipe.route) {
            val viewModel = hiltViewModel<RecipeCreateViewModel>()

            RecipeCreateScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onRecipeCreated = { recipeId ->
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId") {
                        popUpTo(Screen.CreateRecipe.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.EditRecipe.route}/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val viewModel = hiltViewModel<RecipeEditViewModel>()

            RecipeEditScreen(
                recipeId = recipeId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onRecipeUpdated = {
                    navController.navigateUp()
                }
            )
        }

        // Profile and settings
        composable(route = Screen.EditProfile.route) {
            val viewModel = hiltViewModel<ProfileEditViewModel>()
            ProfileEditScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProfileUpdated = {
                    navController.navigateUp()
                }
            )
        }

        composable(route = Screen.Settings.route) {
            val viewModel = hiltViewModel<UserSettingsViewModel>()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
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
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Additional placeholder screens
        composable(route = Screen.PrivacySettings.route) {
            PrivacySettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = "${Screen.Followers.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            FollowersScreen(
                userId = userId ?: "",
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("${Screen.Profile.route}/$userId")
                }
            )
        }

        composable(
            route = "${Screen.Following.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            FollowingScreen(
                userId = userId ?: "",
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("${Screen.Profile.route}/$userId")
                }
            )
        }

        composable(route = Screen.Home.route) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                },
                onNavigateToCategory = { category ->
                    navController.navigate("${Screen.Category.route}/$category")
                },
                onNavigateToAllRecipes = {
                    navController.navigate(Screen.Recipes.route)
                }
            )
        }

        composable(route = Screen.CreatePost.route) {
            val viewModel = hiltViewModel<CreatePostViewModel>()
            CreatePostScreen(
                onNavigateBack = { navController.navigateUp() },
                onPostCreated = {
                    navController.navigate("main") {
                        popUpTo(Screen.CreatePost.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "${Screen.PostDetail.route}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.navigateUp() },
                // ... other parameters
            )
        }
    }
}

// Placeholder screens
@Composable
fun PrivacySettingsScreen(onNavigateBack: () -> Unit) {
    // Placeholder implementation
}

@Composable
fun FollowersScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    // Placeholder implementation
}

@Composable
fun FollowingScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    // Placeholder implementation
}