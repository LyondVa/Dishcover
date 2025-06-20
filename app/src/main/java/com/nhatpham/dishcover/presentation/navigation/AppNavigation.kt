package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.presentation.admin.AdminScreen
import com.nhatpham.dishcover.presentation.auth.emailverification.EmailVerificationScreen
import com.nhatpham.dishcover.presentation.auth.emailverification.EmailVerificationCheckScreen
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordViewModel
import com.nhatpham.dishcover.presentation.auth.login.LoginScreen
import com.nhatpham.dishcover.presentation.auth.login.LoginViewModel
import com.nhatpham.dishcover.presentation.auth.register.RegisterScreen
import com.nhatpham.dishcover.presentation.auth.register.RegisterViewModel
import com.nhatpham.dishcover.presentation.cookbook.create.CreateCookbookScreen
import com.nhatpham.dishcover.presentation.cookbook.detail.CookbookDetailScreen
import com.nhatpham.dishcover.presentation.profile.followers.FollowersScreen
import com.nhatpham.dishcover.presentation.profile.followers.FollowersViewModel
import com.nhatpham.dishcover.presentation.profile.following.FollowingScreen
import com.nhatpham.dishcover.presentation.profile.following.FollowingViewModel
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailScreen
import com.nhatpham.dishcover.presentation.recipe.detail.RecipeDetailViewModel
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditScreen
import com.nhatpham.dishcover.presentation.recipe.edit.RecipeEditViewModel
import com.nhatpham.dishcover.presentation.recipe.shared.SharedRecipeScreen
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

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
                onNavigateToVerificationCheck = {
                    navController.navigate(Screen.EmailVerificationCheck.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.EmailVerificationCheck.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.EmailVerificationCheck.route) {
            EmailVerificationCheckScreen(
                onVerified = {
                    navController.navigate("main") {
                        popUpTo(Screen.EmailVerificationCheck.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }

        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            EmailVerificationScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onVerificationComplete = {
                    navController.navigate(Screen.EmailVerificationCheck.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
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

        // Main app container with admin detection
        composable(route = "main") {
            MainAppContainer(
                navController = navController,
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // Top-level screens for deep linking and special cases

        // Recipe editing (requires authentication)
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
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = false }
                    }
                }
            )
        }

        // Social screens for deep linking
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
            "cookbook_detail/{cookbookId}",
            arguments = listOf(navArgument("cookbookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cookbookId = backStackEntry.arguments?.getString("cookbookId") ?: ""
            CookbookDetailScreen(
                cookbookId = cookbookId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecipe = { recipeId ->
                    navController.navigate("${Screen.RecipeDetail.route}/$recipeId")
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
                onNavigateToProfile = { userId ->
                    navController.navigate("${Screen.Profile.route}/$userId")
                }
            )
        }

    }
}

/**
 * Main app container that detects admin users and shows appropriate interface
 */
@Composable
private fun MainAppContainer(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    val adminCheckViewModel = hiltViewModel<AdminCheckViewModel>()
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Check if current user is admin
    LaunchedEffect(Unit) {
        adminCheckViewModel.getCurrentUserUseCase().collect { result ->
            when (result) {
                is Resource.Success -> {
                    isAdmin = result.data?.isAdmin == true
                    isLoading = false
                    error = null
                    Timber.d("User admin status checked - isAdmin: $isAdmin, username: ${result.data?.username}")
                }
                is Resource.Error -> {
                    isLoading = false
                    error = result.message
                    Timber.e("Failed to check admin status: ${result.message}")
                }
                is Resource.Loading -> {
                    isLoading = true
                    error = null
                }
            }
        }
    }

    when {
        isLoading -> {
            // Show loading indicator while checking admin status
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            // Show error state with retry option
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Failed to load user data",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = onSignOut
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }

        isAdmin -> {
            // Show admin interface
            AdminScreen(
                onSignOut = onSignOut
            )
        }

        else -> {
            // Show normal user interface
            MainContainer(
                navController = navController,
                onNavigateToRecipeDetail = { /* handled internally */ },
                onNavigateToCreateRecipe = { /* handled internally */ },
                onNavigateToEditProfile = { /* handled internally */ },
                onNavigateToSettings = { /* handled internally */ },
                onSignOut = onSignOut
            )
        }
    }
}

/**
 * ViewModel to check current user admin status
 */
@HiltViewModel
class AdminCheckViewModel @Inject constructor(
    val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel()


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