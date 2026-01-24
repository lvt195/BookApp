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
    // bookUrl here will now be the local file path passed from DetailScreen
    // But argument name is "bookUrl" in NavHost. We can keep the name or ignore it, 
    // but logic must treat it as file path.
    // However, if we want to be safe, we should check if it's a web URL or file path.
    // Our ViewModel logic ensures we download first, so it should be a file path.
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val file = java.io.File(bookUrl)
    
    // State for page counting
    var pageCount by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var currentPage by remember { androidx.compose.runtime.mutableIntStateOf(1) }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(), // Fix status bar overlap
        contentAlignment = Alignment.Center
    ) {
        if (file.exists()) {
             androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    com.alamin5g.pdf.PDFView(ctx, null).apply {
                        fromFile(file)
                            .swipeHorizontal(false)
                            .enableSwipe(true)
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
        } else {
             androidx.compose.material3.Text("File not found: $bookUrl")
        }
    }
}
