package com.pluto.bookapp.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.pluto.bookapp.ui.screens.ProfileScreen
import com.pluto.bookapp.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { finish() },
                onEditProfileClick = {
                    // Start Edit Profile logic/screen
                    // For now just toggle a dialog or separate activity/screen?
                    // We can retain the activity structure and launch ProfileEditActivity if we migrate it,
                    // or just add navigation here.
                    // Let's assume ProfileActivity is simple for now. 
                    // TODO: Implement Edit Profile screen.
                }
            )
        }
    }
}
