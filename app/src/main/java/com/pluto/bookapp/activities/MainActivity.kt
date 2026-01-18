package com.pluto.bookapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pluto.bookapp.ui.screens.auth.LoginScreen
import com.pluto.bookapp.ui.screens.auth.RegisterScreen
import com.pluto.bookapp.ui.screens.auth.SplashScreen
import com.pluto.bookapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthNavigation(authViewModel = authViewModel)
        }
    }

    @Composable
    fun AuthNavigation(authViewModel: AuthViewModel) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(
                    viewModel = authViewModel,
                    onNavigateToMain = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToUserDashboard = {
                        startActivity(Intent(this@MainActivity, DashboardUserActivity::class.java))
                        finish()
                    },
                    onNavigateToAdminDashboard = {
                        startActivity(Intent(this@MainActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        authViewModel.resetState()
                        navController.navigate("register")
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate("forgot_password")
                    },
                    onLoginSuccess = {
                         navController.navigate("splash") {
                             popUpTo("login") { inclusive = true }
                         }
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        authViewModel.resetState()
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                         navController.navigate("splash") {
                             popUpTo("register") { inclusive = true }
                         }
                    }
                )
            }
            composable("forgot_password") {
                com.pluto.bookapp.ui.screens.auth.ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackClick = {
                        authViewModel.resetState()
                        navController.popBackStack() 
                    }
                )
            }
        }
    }
}
