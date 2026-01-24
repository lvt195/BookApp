package com.pluto.bookapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    isEmailVerified: Boolean,
    onResendEmail: () -> Unit,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()
    // Assume AuthViewModel is also passed or injected, but here we can't easily access it without modifying signature.
    // However, ProfileViewModel likely doesn't have it. Let's check ProfileViewModel.
    // Better to pass verification status to ProfileScreen from navigation host, or inject AuthViewModel here.
    // Given the signature: fun ProfileScreen(viewModel: ProfileViewModel...), we should probably stick to what we have or modify call site.
    // But wait, the user instructions implied ProfileScreen update.
    // Let's modify ProfileScreen to accept isEmailVerified and onResendEmail.


    var showEditDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Enter new name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotEmpty()) {
                        viewModel.updateProfile(newName)
                        showEditDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        newName = userData["name"] ?: ""
                        showEditDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                 Icon(
                     Icons.Default.Person, 
                     contentDescription = null, 
                     modifier = Modifier.padding(24.dp),
                     tint = MaterialTheme.colorScheme.onSurfaceVariant
                 )
                 // Note: Integrate Coil/Glide for actual image using userData["profileImage"]
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = userData["name"] ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = userData["email"] ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val userType = userData["userType"] ?: "User"
            if (!userType.equals("user", ignoreCase = true)) {
                Text(
                    text = userType,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Account Status", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isEmailVerified) {
                                Text("Verified", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Check, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Unverified", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onResendEmail, 
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Verify Now", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Favorite Books", style = MaterialTheme.typography.titleMedium)
                        Text("$favoriteCount", fontWeight = FontWeight.Bold)
                    }
                     HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Member Since", style = MaterialTheme.typography.titleMedium)
                         // Format timestamp
                         val timestampStr = userData["timestamp"]
                         val formattedDate = try {
                             val timestamp = timestampStr?.toLongOrNull() ?: 0L
                             if (timestamp > 0) {
                                 com.pluto.bookapp.MyApplication.formatTimeStamp(timestamp)
                             } else {
                                 "N/A"
                             }
                         } catch (e: Exception) {
                             "N/A"
                         }
                        Text(formattedDate, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
