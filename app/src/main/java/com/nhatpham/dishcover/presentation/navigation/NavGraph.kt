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
import com.nhatpham.dishcover.presentation.home.HomeScreen
import com.nhatpham.dishcover.presentation.profile.ProfileEditScreen
import com.nhatpham.dishcover.presentation.profile.ProfileEditViewModel
import com.nhatpham.dishcover.presentation.profile.UserProfileScreen
import com.nhatpham.dishcover.presentation.profile.UserProfileViewModel
import com.nhatpham.dishcover.presentation.profile.settings.SettingsScreen
import com.nhatpham.dishcover.presentation.profile.settings.UserSettingsViewModel
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateScreen
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailScreen
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailViewModel
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
    onGoogleSignIn: () -> Unit,
    setLoginViewModel: (LoginViewModel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
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
                    navController.navigate(Screen.Home.route) {
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
                    navController.navigate(Screen.Home.route) {
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

        // Main app screens
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
                },
                onNavigateToCategory = { category ->
                    navController.navigate("${Screen.Search.route}?category=$category")
                },
                onNavigateToAllRecipes = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToCreateRecipe = {
                    print("Navigate to create recipe")
                    navController.navigate(Screen.CreateRecipe.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // User Profile
        composable(route = Screen.Profile.route) {
            val viewModel = hiltViewModel<UserProfileViewModel>()
            UserProfileScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
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

        composable(
            route = "${Screen.Profile.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val viewModel = hiltViewModel<UserProfileViewModel>()
            UserProfileScreen(
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
//
//        // Recipe screens
//        composable(
//            route = "${Screen.EditRecipe.route}/{recipeId}",
//            arguments = listOf(
//                navArgument("recipeId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
//            val viewModel = hiltViewModel<RecipeEditViewModel>()
//
//            RecipeEditScreen(
//                recipeId = recipeId,
//                viewModel = viewModel,
//                onNavigateBack = {
//                    navController.navigateUp()
//                },
//                onRecipeUpdated = {
//                    navController.navigateUp()
//                }
//            )
//        }
    }
}

// Placeholder composable for screens that haven't been fully implemented yet
@Composable
fun PrivacySettingsScreen(onNavigateBack: () -> Unit) {
    // Placeholder until the actual screen is implemented
}

@Composable
fun FollowersScreen(userId: String, onNavigateBack: () -> Unit, onNavigateToProfile: (String) -> Unit) {
    // Placeholder until the actual screen is implemented
}

@Composable
fun FollowingScreen(userId: String, onNavigateBack: () -> Unit, onNavigateToProfile: (String) -> Unit) {
    // Placeholder until the actual screen is implemented
}