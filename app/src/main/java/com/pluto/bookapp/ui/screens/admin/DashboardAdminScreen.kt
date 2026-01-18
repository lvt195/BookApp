package com.pluto.bookapp.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.model.ModelCategory
import com.pluto.bookapp.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    viewModel: AdminViewModel,
    userEmail: String?,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onAddPdfClick: () -> Unit,
    onCategoryClick: (String, String) -> Unit // id, title
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter categories based on search
    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.category.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            text = userEmail ?: "Admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } 
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
           // We could have two FABs or a Speed Dial. For now simple Column of FABs?
           // Or just one FAB that navigates to Add PDF, and Add Category somewhere else?
           // Original app has "Add Category" as a button/activity and "Add PDF" as another.
           Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                   onClick = onAddCategoryClick,
                   containerColor = MaterialTheme.colorScheme.secondaryContainer,
                   modifier = Modifier.padding(bottom = 16.dp)
               ) {
                   Text("Add Cat", modifier = Modifier.padding(horizontal = 8.dp))
               }
               
               FloatingActionButton(onClick = onAddPdfClick) {
                   Icon(Icons.Default.Add, contentDescription = "Add PDF")
               }
           }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Categories") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                 items(filteredCategories) { category ->
                     CategoryItem(
                         category = category, 
                         onClick = { onCategoryClick(category.id, category.category) },
                         onDeleteClick = { viewModel.deleteCategory(category.id) }
                     )
                 }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: ModelCategory,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.category, style = MaterialTheme.typography.titleMedium)
            
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
