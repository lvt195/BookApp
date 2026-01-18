package com.pluto.bookapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.model.ModelPdf
import com.pluto.bookapp.viewmodel.AdminViewModel
import com.pluto.bookapp.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfListAdminScreen(
    adminViewModel: AdminViewModel,
    dashboardViewModel: DashboardViewModel, // Reusing DashboardViewModel to fetch books by category
    categoryId: String,
    categoryTitle: String,
    onBackClick: () -> Unit
) {
    // We need to fetch books. DashboardViewModel has getBooksForCategory. 
    // We can create a temporary category object or just add getBooksByCategoryId to VM.
    // DashboardViewModel.getBooksForCategory expects ModelCategory.
    // Let's rely on dashboardViewModel for reading data, and adminViewModel for deletes.
    
    // Quick fix: create dummy category object since we only need ID for fetching if logic is simple
    // Actually DashboardViewModel checks category name for "All", etc.
    // Here we passed a specific category ID.
    
    // Better approach: Add getBooksByCategoryId to DashboardViewModel or AdminViewModel.
    // AdminViewModel relies on BookRepository which HAS getBooksByCategory.
    // Let's add `books` flow to AdminViewModel or just use a helper here.
    
    // Ideally AdminViewModel should handle this to keep Admin flow isolated.
    // But for now, we can just use a LaunchedEffect to collect a flow from Repository directly? 
    // No, strictly use ViewModel.
    // Let's Assume we added a simple fetch method to AdminViewModel or we can use DashboardViewModel.
    // Using DashboardViewModel is fine as it is for "Dashboard" data (which implies viewing).
    // But deletion is Admin.
    
    // Let's go with: Using DashboardViewModel to VIEW.
    val booksState = remember(categoryId) { 
        // We need to construct a model or overload function.
        // Let's just update DashboardViewModel to have a simpler overload?
        // Or construct dummy.
        val dummyCat = com.pluto.bookapp.model.ModelCategory(categoryId, categoryTitle, "", 1)
        dashboardViewModel.getBooksForCategory(dummyCat)
    }
    
    val books by booksState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                AdminBookItem(
                    book = book,
                    onDeleteClick = { adminViewModel.deleteBook(book.id, book.url) }
                )
            }
            if (books.isEmpty()) {
                item {
                     Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No books found")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookItem(
    book: ModelPdf,
    onDeleteClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Book", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
