package com.pluto.bookapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.pluto.bookapp.ui.screens.admin.CategoryAddScreen
import com.pluto.bookapp.ui.screens.admin.DashboardAdminScreen
import com.pluto.bookapp.ui.screens.admin.PdfAddScreen
import com.pluto.bookapp.ui.screens.admin.PdfListAdminScreen
import com.pluto.bookapp.viewmodel.AdminViewModel
import com.pluto.bookapp.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardAdminActivity : ComponentActivity() {

    private val adminViewModel: AdminViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels() // Re-using data fetching logic
    private val authViewModel: com.pluto.bookapp.viewmodel.AuthViewModel by viewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val user = firebaseAuth.currentUser
            val email = user?.email
            
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardAdminScreen(
                        viewModel = adminViewModel,
                        userEmail = email,
                        onLogoutClick = {
                            authViewModel.logout()
                            startActivity(Intent(this@DashboardAdminActivity, MainActivity::class.java))
                            finish()
                        },
                        onProfileClick = {
                            startActivity(Intent(this@DashboardAdminActivity, ProfileActivity::class.java))
                        },
                        onAddCategoryClick = { navController.navigate("category_add") },
                        onAddPdfClick = { navController.navigate("pdf_add") },
                        onCategoryClick = { id, title -> 
                            navController.navigate("pdf_list/$id/$title") 
                        }
                    )
                }
                
                composable("category_add") {
                    CategoryAddScreen(
                        viewModel = adminViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                
                composable("pdf_add") {
                    PdfAddScreen(
                         viewModel = adminViewModel,
                         onBackClick = { navController.popBackStack() }
                    )
                }
                
                composable(
                    "pdf_list/{categoryId}/{categoryTitle}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.StringType },
                        navArgument("categoryTitle") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                    val categoryTitle = backStackEntry.arguments?.getString("categoryTitle") ?: ""
                    
                    PdfListAdminScreen(
                        adminViewModel = adminViewModel,
                        dashboardViewModel = dashboardViewModel,
                        categoryId = categoryId,
                        categoryTitle = categoryTitle,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
