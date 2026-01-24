package com.pluto.bookapp.ui.screens.auth

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.activities.DashboardAdminActivity
import com.pluto.bookapp.activities.DashboardUserActivity
import com.pluto.bookapp.activities.MainActivity
import com.pluto.bookapp.viewmodel.AuthState
import com.pluto.bookapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToUserDashboard: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val userType by viewModel.userType.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.checkUserStatus()
        
        // Fallback safety: if auth doesn't resolve in 10 seconds, force move to main
        // This handles cases where DB might hang (though we used await())
        delay(8000) 
        if (authState is AuthState.Idle) {
             android.util.Log.d("SplashScreen", "Auth check timed out, forcing navigation to Main")
             onNavigateToMain()
        }
    }

    LaunchedEffect(authState, userType) {
        if (authState is AuthState.Unauthenticated) {
            onNavigateToMain()
        } else if (authState is AuthState.Authenticated && userType != null) {
            if (userType == "user") {
                onNavigateToUserDashboard()
            } else if (userType == "admin") {
                onNavigateToAdminDashboard()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Pluto Reader",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
