package com.pluto.bookapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding


import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
fun PdfViewScreen(bookUrl: String) {
    var downloadedFile by remember { mutableStateOf<java.io.File?>(null) }
    var downloadProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(bookUrl) {
        isLoading = true
        errorMessage = null
        try {
            val ref = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            
            // Create a temp file in cache directory
            val fileName = "book_${System.currentTimeMillis()}.pdf"
            val localFile = java.io.File(context.cacheDir, fileName)
            
            val task = ref.getFile(localFile)
            task.addOnProgressListener { snapshot ->
                val progress = if (snapshot.totalByteCount > 0) {
                    (100f * snapshot.bytesTransferred) / snapshot.totalByteCount
                } else {
                    0f
                }
                downloadProgress = progress
            }
            
            task.await()
            downloadedFile = localFile
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load PDF"
        } finally {
            isLoading = false
        }
    }

    // State for page counting
    var pageCount by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var currentPage by remember { androidx.compose.runtime.mutableIntStateOf(1) }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(), // Fix status bar overlap
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                androidx.compose.material3.Text("Downloading ${downloadProgress.toInt()}%")
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                )
            }
        } else if (errorMessage != null) {
            androidx.compose.material3.Text(
                text = errorMessage!!,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else if (downloadedFile != null) {
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    com.alamin5g.pdf.PDFView(ctx, null).apply {
                        fromFile(downloadedFile)
                            .swipeHorizontal(false)
                            .enableSwipe(true)
                            // .scrollHandle(com.alamin5g.pdf.scroll.DefaultScrollHandle(ctx)) // TODO: Fix unresolved reference
                            .onError { t: Throwable ->
                                android.widget.Toast.makeText(ctx, "Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                            .onPageChange { page, total ->
                                currentPage = page + 1 // Pages are 0-indexed
                                pageCount = total
                            }
                            .onLoad { nbPages: Int ->
                                pageCount = nbPages
                            }
                            .load()
                    }
                },
                update = { view ->
                    // Static load
                }
            )
            
            // Page Count Overlay
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    androidx.compose.material3.Text(
                        text = "$currentPage / $pageCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
