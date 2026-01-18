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
import com.pluto.bookapp.ui.screens.DashboardScreen
import com.pluto.bookapp.ui.screens.PdfDetailScreen
import com.pluto.bookapp.ui.screens.PdfViewScreen
import com.pluto.bookapp.viewmodel.DashboardViewModel
import com.pluto.bookapp.viewmodel.PdfDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class DashboardUserActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val pdfDetailViewModel: PdfDetailViewModel by viewModels()
    private val authViewModel: com.pluto.bookapp.viewmodel.AuthViewModel by viewModels()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val user = firebaseAuth.currentUser
            val userEmail = user?.email
            
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        userEmail = userEmail,
                        onLogoutClick = {
                            authViewModel.logout()
                            startActivity(Intent(this@DashboardUserActivity, MainActivity::class.java))
                            finish()
                        },
                        onProfileClick = {
                            startActivity(Intent(this@DashboardUserActivity, ProfileActivity::class.java))
                        },
                        onBookClick = { book ->
                             // Ensure arguments are safe (encode info if needed or just pass ID and fetch)
                             // For simplicity we will pass everything as arguments.
                             // URLs need encoding!
                             val encodedUrl = URLEncoder.encode(book.url, StandardCharsets.UTF_8.toString())
                             val encodedTitle = URLEncoder.encode(book.title, StandardCharsets.UTF_8.toString())
                             val encodedDescription = URLEncoder.encode(book.description, StandardCharsets.UTF_8.toString())
                             // Add others...
                             
                             // Alternatively, just pass ID and make ViewModel fetch details? 
                             // We already setup VM to fetch details by ID. 
                             // So passing just basic info + ID is safer.
                             // But let's stick to what DetailScreen expects currently.
                             // It expects many args.
                             // Let's refactor DetailScreen arguments to be minimal or handle them well.
                             
                             // Passing complex data in Nav arguments is tricky. 
                             // Best practice: Pass ID. Fetch in DetailVM.
                             // DetailVM already has loadBookData(bookId).
                             // We should update DetailScreen to observe "bookDetails" from VM instead of parameters.
                             // For now, I will pass just ID and essential non-data logic params.
                             // But my DetailScreen currently takes ALL arguments. 
                             // I will update DetailScreen signature in next step if possible, OR just pass ID and blank for others 
                             // knowing VM will update UI? No, Composable parameters are state.
                             
                             // Better: Pass everything URL-encoded.
                             val route = "detail/${book.id}/$encodedTitle/$encodedDescription/$encodedUrl/${book.timestamp}/${book.downloadCount}/${book.viewCount}"
                             navController.navigate(route)
                        }
                    )
                }
                
                composable(
                    "detail/{bookId}/{title}/{description}/{url}/{timestamp}/{downloads}/{views}",
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("description") { type = NavType.StringType },
                        navArgument("url") { type = NavType.StringType },
                        navArgument("timestamp") { type = NavType.StringType },
                        navArgument("downloads") { type = NavType.StringType }, // Long as String
                        navArgument("views") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val bookUrl = backStackEntry.arguments?.getString("url") ?: ""
                    
                    PdfDetailScreen(
                        viewModel = pdfDetailViewModel,
                        bookId = backStackEntry.arguments?.getString("bookId") ?: "",
                        bookTitle = backStackEntry.arguments?.getString("title") ?: "",
                        bookDescription = backStackEntry.arguments?.getString("description") ?: "",
                        bookUrl = bookUrl,
                        bookTimestamp = backStackEntry.arguments?.getString("timestamp") ?: "",
                        bookDownloads = backStackEntry.arguments?.getString("downloads") ?: "0",
                        bookViews = backStackEntry.arguments?.getString("views") ?: "0",
                        onBackClick = { navController.popBackStack() },
                        onReadClick = { 
                             val encodedUrl = URLEncoder.encode(bookUrl, StandardCharsets.UTF_8.toString())
                             navController.navigate("view/$encodedUrl") 
                        },
                        onDownloadClick = { url, title ->
                            pdfDetailViewModel.downloadBook(url, title, backStackEntry.arguments?.getString("bookId") ?: "")
                        }
                    )
                }
                
                composable(
                    "view/{bookUrl}",
                    arguments = listOf(navArgument("bookUrl") { type = NavType.StringType })
                ) { backStackEntry ->
                    val url = backStackEntry.arguments?.getString("bookUrl") ?: ""
                    PdfViewScreen(bookUrl = url)
                }
            }
        }
    }
}
