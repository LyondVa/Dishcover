package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.nhatpham.dishcover.presentation.auth.forgotpassword.ForgotPasswordViewModel
import com.nhatpham.dishcover.presentation.auth.login.LoginScreen
import com.nhatpham.dishcover.presentation.auth.login.LoginViewModel
import com.nhatpham.dishcover.presentation.auth.register.RegisterScreen
import com.nhatpham.dishcover.presentation.auth.register.RegisterViewModel
import com.nhatpham.dishcover.presentation.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    onGoogleSignIn: () -> Unit,
    setLoginViewModel: (LoginViewModel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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

        composable(route = Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}