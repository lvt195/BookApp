package com.pluto.bookapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.model.ModelComment
import com.pluto.bookapp.viewmodel.PdfDetailViewModel
import com.pluto.bookapp.ui.components.PdfThumbnail
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfDetailScreen(
    viewModel: PdfDetailViewModel,
    bookId: String,
    bookTitle: String,
    bookDescription: String,
    bookUrl: String,
    bookTimestamp: String,
    bookDownloads: String,
    bookViews: String,
    onBackClick: () -> Unit,
    onReadClick: (String) -> Unit, // bookId or Url
    onDownloadClick: (String, String) -> Unit, // url, title
    isEmailVerified: Boolean,
    onResendEmail: () -> Unit
) {
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    
    var commentText by remember { mutableStateOf("") }
    
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    
    // Navigation effect when Ready
    LaunchedEffect(viewState) {
        if (viewState is PdfDetailViewModel.ViewState.Ready) {
             val file = (viewState as PdfDetailViewModel.ViewState.Ready).file
             val encodedPath = java.net.URLEncoder.encode(file.absolutePath, java.nio.charset.StandardCharsets.UTF_8.toString())
             onReadClick(encodedPath)
             viewModel.resetViewState()
        }
    }
    
    var showVerificationDialog by remember { mutableStateOf(false) }

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text("Email Verification Required") },
            text = { Text("You need to verify your email to access this feature. Please check your inbox.") },
            confirmButton = {
                Button(onClick = { 
                    onResendEmail()
                    showVerificationDialog = false 
                }) {
                    Text("Resend Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerificationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(bookId) {
        viewModel.loadBookData(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Book Info Header
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Fetch size async
                            var bookSize by remember { mutableStateOf("Loading...") }
                            LaunchedEffect(bookUrl) {
                                bookSize = com.pluto.bookapp.util.PdfUtil.getPdfSize(bookUrl)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // PDF Thumbnail
                                PdfThumbnail(
                                   pdfUrl = bookUrl,
                                   modifier = Modifier
                                       .width(100.dp)
                                       .height(140.dp)
                                       .clip(RoundedCornerShape(8.dp))
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bookTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bookDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                     // Metadata
                                    Text(
                                        text = "Date: $bookTimestamp",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Size: $bookSize",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Views: $bookViews",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Downloads: $bookDownloads",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Favorite Button Row
                             Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { viewModel.toggleFavorite(bookId) }) {
                                    Icon(
                                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.openBook(bookId, bookUrl) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Read Book")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { 
                                if (isEmailVerified) {
                                    onDownloadClick(bookUrl, bookTitle)
                                } else {
                                    showVerificationDialog = true
                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Download Book")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Comments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Comments List
                items(comments) { comment ->
                    CommentItem(comment)
                }
            }

            // Add Comment Section
            Row(
                 modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                 verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { 
                        if (isEmailVerified) {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(bookId, commentText) 
                                commentText = ""
                            }
                        } else {
                            showVerificationDialog = true
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
    // Loading Dialog
    if (viewState is PdfDetailViewModel.ViewState.Loading || viewState is PdfDetailViewModel.ViewState.Downloading) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("Downloading Book") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    val progress = if (viewState is PdfDetailViewModel.ViewState.Downloading) {
                        (viewState as PdfDetailViewModel.ViewState.Downloading).progress
                    } else {
                        0
                    }
                    Text("Please wait... $progress%")
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun CommentItem(comment: ModelComment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            AsyncImage(
                model = comment.profileImage.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" }, // Default avatar
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.name.ifEmpty { "Unknown User" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Format Timestamp
                    val date = try {
                         com.pluto.bookapp.util.PdfUtil.formatTimeStamp(comment.timestamp.toLong())
                    } catch (e: Exception) {
                        "Just now"
                    }
                    
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = comment.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
