package com.teamloci.loci

import android.os.Bundle
import androidx.compose.material3.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.teamloci.loci.ui.theme.LociTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teamloci.loci.ui.auth.AuthViewModel
import com.teamloci.loci.ui.auth.PhoneLoginScreen
import com.teamloci.loci.ui.auth.SignUpHandleScreen
import com.teamloci.loci.ui.auth.SignUpNicknameScreen
import com.teamloci.loci.ui.auth.WelcomeScreen
import com.teamloci.loci.ui.home.HomeScreen
import com.teamloci.loci.ui.post.CreatePostScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LociNavigation()
                }
            }
        }
    }
}

@Composable
fun LociNavigation() {
    val navController = rememberNavController()

    val sharedAuthViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "welcome") {

        composable("welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("phone_login") }
            )
        }

        composable("phone_login") {
            PhoneLoginScreen(
                viewModel = sharedAuthViewModel,
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("home") { popUpTo(0) }
                },
                onNeedSignUp = {
                    navController.navigate("signup_nickname")
                }
            )
        }

        composable("signup_nickname") {
            SignUpNicknameScreen(
                viewModel = sharedAuthViewModel,
                onNext = { navController.navigate("signup_handle") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("signup_handle") {
            SignUpHandleScreen(
                viewModel = sharedAuthViewModel,
                onSignUpSuccess = {
                    navController.navigate("home") { popUpTo(0) }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onCreatePostClick = {
                    navController.navigate("create_post")
                }
            )
        }

        composable("create_post") {
            CreatePostScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LociTheme {
        Greeting("Android")
    }
}