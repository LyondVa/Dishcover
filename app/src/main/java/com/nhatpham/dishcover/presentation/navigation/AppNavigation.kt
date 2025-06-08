// Updated AppNavigation.kt - Clean version that works with the fixed MainContainer
package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordViewModel
import com.nhatpham.dishcover.presentation.auth.login.LoginScreen
import com.nhatpham.dishcover.presentation.auth.login.LoginViewModel
import com.nhatpham.dishcover.presentation.auth.register.RegisterScreen
import com.nhatpham.dishcover.presentation.auth.register.RegisterViewModel
//import com.nhatpham.dishcover.presentation.recipe.shared.SharedRecipeScreen
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailViewModel
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditScreen
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditViewModel
import com.nhatpham.dishcover.presentation.profile.followers.FollowersScreen
import com.nhatpham.dishcover.presentation.profile.followers.FollowersViewModel
import com.nhatpham.dishcover.presentation.profile.following.FollowingScreen
import com.nhatpham.dishcover.presentation.profile.following.FollowingViewModel

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

        // Main app container - now handles its own internal navigation
        composable(route = "main") {
            MainContainer(
                navController = navController, // Keep existing parameter name
                onNavigateToRecipeDetail = { recipeId ->
                    // Not needed anymore - handled internally by MainContainer
                },
                onNavigateToCreateRecipe = {
                    // Not needed anymore - handled internally by MainContainer
                },
                onNavigateToEditProfile = {
                    // Not needed anymore - handled internally by MainContainer
                },
                onNavigateToSettings = {
                    // Not needed anymore - handled internally by MainContainer
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // Screens that need to be at the top level (mostly for deep linking or special cases)

        // Recipe editing (might need user authentication checks)
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

        // Shared recipe screen (public access via deep links)
//        composable(
//            route = "${Screen.SharedRecipe.route}/{recipeId}",
//            arguments = listOf(
//                navArgument("recipeId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
//            val viewModel = hiltViewModel<RecipeDetailViewModel>()
//
//            SharedRecipeScreen(
//                recipeId = recipeId,
//                viewModel = viewModel,
//                onNavigateBack = {
//                    navController.navigateUp()
//                },
//                onNavigateToApp = {
//                    navController.navigate("main") {
//                        popUpTo(0) { inclusive = false }
//                    }
//                }
//            )
//        }

        // Social screens that might be better at top level for deep linking
        composable(
            route = "${Screen.Followers.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = hiltViewModel<FollowersViewModel>()

            FollowersScreen(
                userId = userId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate("${Screen.Profile.route}/$targetUserId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "${Screen.Following.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = hiltViewModel<FollowingViewModel>()

            FollowingScreen(
                userId = userId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate("${Screen.Profile.route}/$targetUserId")
                },
                viewModel = viewModel
            )
        }

        // Settings screens (top level to avoid deep nesting)
        composable(route = Screen.PrivacySettings.route) {
            PrivacySettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(route = Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(route = Screen.AccountSettings.route) {
            AccountSettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

// Placeholder screens - replace with actual implementations
@Composable
fun PrivacySettingsScreen(onNavigateBack: () -> Unit) {
    // Placeholder implementation
}

@Composable
fun NotificationSettingsScreen(onNavigateBack: () -> Unit) {
    // Placeholder implementation
}

@Composable
fun AccountSettingsScreen(onNavigateBack: () -> Unit) {
    // Placeholder implementation
}