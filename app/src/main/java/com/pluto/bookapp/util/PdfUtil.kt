package com.pluto.bookapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

object PdfUtil {
    private const val TAG = "PdfUtil"
    private const val MAX_BYTES_PDF: Long = 50 * 1024 * 1024 // 50MB - generous limit for header/first page fetch if needed, but we try to fetch less if possible

    fun formatTimeStamp(timestamp: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp
        return DateFormat.format("dd/MM/yyyy", cal).toString()
    }

    suspend fun getPdfSize(pdfUrl: String): String = withContext(Dispatchers.IO) {
        try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            val metadata = ref.metadata.await()
            val bytes = metadata.sizeBytes.toDouble()
            
            val kb = bytes / 1024
            val mb = kb / 1024

            when {
                mb >= 1 -> String.format("%.2f MB", mb)
                kb >= 1 -> String.format("%.2f KB", kb)
                else -> String.format("%.2f bytes", bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPdfSize: ", e)
            "N/A"
        }
    }

    suspend fun generateThumbnail(context: Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Check Memory/Disk Cache
        val cacheFileName = "thumb_${pdfUrl.hashCode()}.png"
        val cacheDir = File(context.cacheDir, "thumbnails")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        val cacheFile = File(cacheDir, cacheFileName)
        
        if (cacheFile.exists()) {
            try {
                 return@withContext android.graphics.BitmapFactory.decodeFile(cacheFile.absolutePath)
            } catch (e: Exception) {
                // If decode fails, proceed to generate
                Log.e(TAG, "Failed to decode cached bitmap", e)
            }
        }

        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        var tempFile: File? = null

        try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            // Fetch first 5MB or less to ensure we get the first page data. 
            // Warning: fetching partial encodings of PDF might fail if the linearized structure isn't at the start.
            // Safer to fetch enough.
            val bytes = ref.getBytes(MAX_BYTES_PDF).await()

            tempFile = File.createTempFile("thumbnail", ".pdf", context.cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(bytes)
            fos.close()

            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                currentPage = pdfRenderer.openPage(0)
                val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                // 2. Save to Cache
                try {
                    val cacheOut = FileOutputStream(cacheFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, cacheOut)
                    cacheOut.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save thumbnail cache", e)
                }
                
                return@withContext bitmap
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Expected when scrolling fast, just ignore
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "generateThumbnail: ", e)
        } finally {
            try {
                currentPage?.close()
                pdfRenderer?.close()
                fileDescriptor?.close()
                tempFile?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "cleanup: ", e)
            }
        }
        return@withContext null
    }
}
