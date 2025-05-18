package com.nhatpham.dishcover

import android.content.Intent
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
import com.nhatpham.dishcover.presentation.navigation.NavGraph
import com.nhatpham.dishcover.presentation.navigation.Screen
import com.nhatpham.dishcover.ui.theme.DishcoverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentNavController: NavHostController? = null
    private var currentLoginViewModel: LoginViewModel? = null

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

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            DishcoverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    currentNavController = navController

                    val authViewModel: AuthViewModel = hiltViewModel()
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(key1 = true) {
                        authViewModel.getCurrentUser().collect { resource ->
                            startDestination = if (resource.data != null) {
                                Screen.Home.route
                            } else {
                                Screen.Login.route
                            }
                        }
                    }

                    startDestination?.let {
                        NavGraph(
                            navController = navController,
                            startDestination = it,
                            onGoogleSignIn = ::launchGoogleSignIn,
                            setLoginViewModel = { viewModel ->
                                currentLoginViewModel = viewModel
                            }
                        )
                    }
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
}