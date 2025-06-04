package com.nhatpham.dishcover

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.nhatpham.dishcover.presentation.auth.AuthViewModel
import com.nhatpham.dishcover.presentation.auth.login.LoginEvent
import com.nhatpham.dishcover.presentation.auth.login.LoginViewModel
import com.nhatpham.dishcover.presentation.navigation.Screen
import com.nhatpham.dishcover.presentation.navigation.AppNavigation
import com.nhatpham.dishcover.ui.theme.DishcoverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentLoginViewModel: LoginViewModel? = null
    private var pendingDeepLink: String? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                currentLoginViewModel?.onEvent(LoginEvent.GoogleSignIn(token))
            }
        } catch (e: ApiException) {
            // Handle error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupGoogleSignIn()

        // Handle deep link from intent
        handleIntent(intent)

        setContent {
            DishcoverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    var startDestination by remember { mutableStateOf<String?>(null) }
                    var isUserAuthenticated by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        authViewModel.getCurrentUser().collect { resource ->
                            isUserAuthenticated = resource.data != null
                            startDestination = if (resource.data != null) {
                                "main"
                            } else {
                                Screen.Login.route
                            }
                        }
                    }

                    // Handle deep link after user is authenticated
                    LaunchedEffect(isUserAuthenticated, pendingDeepLink) {
                        if (isUserAuthenticated && pendingDeepLink != null) {
                            navigateToDeepLink(navController, pendingDeepLink!!)
                            pendingDeepLink = null
                        }
                    }

                    startDestination?.let { destination ->
                        AppNavigation(
                            navController = navController,
                            startDestination = destination,
                            onGoogleSignIn = ::initiateGoogleSignIn,
                            setLoginViewModel = { viewModel ->
                                currentLoginViewModel = viewModel
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.data

        if (action == Intent.ACTION_VIEW && data != null) {
            handleDeepLink(data)
        }
    }

    private fun handleDeepLink(uri: Uri) {
        when {
            // Handle web links: https://dishcover.app/recipe/{recipeId}
            uri.scheme == "https" && uri.host == "dishcover.app" -> {
                if (uri.pathSegments.size >= 2 && uri.pathSegments[0] == "recipe") {
                    val recipeId = uri.pathSegments[1]
                    pendingDeepLink = "${Screen.RecipeDetail.route}/$recipeId"
                }
            }
            // Handle custom scheme: dishcover://recipe/{recipeId}
            uri.scheme == "dishcover" -> {
                if (uri.pathSegments.size >= 2 && uri.pathSegments[0] == "recipe") {
                    val recipeId = uri.pathSegments[1]
                    pendingDeepLink = "${Screen.RecipeDetail.route}/$recipeId"
                }
            }
        }
    }

    private fun navigateToDeepLink(navController: NavHostController, deepLink: String) {
        try {
            navController.navigate(deepLink)
        } catch (e: Exception) {
            // Handle navigation error
            e.printStackTrace()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initiateGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
}
