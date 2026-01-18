package com.pluto.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.alamin5g.pdf.PDFView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.HashMap
import java.util.Locale

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val appCheckProviderFactory = if (BuildConfig.DEBUG) {
            com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(appCheckProviderFactory)
    }

    companion object {
        const val TAG_DOWN = "DOWN_BOOK"

        @JvmStatic
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        @JvmStatic
        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val TAG = "DELETE_BOOK"
            Log.d(TAG, "deleteBook: ")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    val reference = FirebaseDatabase.getInstance().getReference("Books")
                    reference.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Book delete successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                }
        }

        @JvmStatic
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "BOOK_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "onSuccess: $pdfTitle")

                    val bytes = storageMetadata.sizeBytes.toDouble()
                    val kb = bytes / 1024
                    val mb = kb / 1024

                    if (mb >= 1) {
                        sizeTv.text = String.format("%.2f MB", mb)
                    } else if (kb >= 1) {
                        sizeTv.text = String.format("%.2f KB", kb)
                    } else {
                        sizeTv.text = String.format("%.2f bytes", bytes)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "onFailure: " + e.message)
                }
        }

        @JvmStatic
        fun loadBannerPdf(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pageTv: TextView?
        ) {
            val TAG = "LOAD_BANNER_PDF"
            Log.d(TAG, "loadBannerPdf: url la: $pdfUrl")

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "onSuccess: load$pdfTitle")

                    pdfView.fromBytes(bytes)
                        .pages(0) // show ra trang dau tien cua pdf
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t: Throwable ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "onError: " + t.message)
                        }
                        // .onPageError { page, t -> ... } // Not supported in this version
                        .onLoad { nbPages: Int ->
                            progressBar.visibility = View.INVISIBLE
                            pageTv?.text = "$nbPages"
                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "onFailure: " + e.message)
                }
        }

        @JvmStatic
        fun loadCategory(categoryId: String, categoryTv: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        @JvmStatic
        fun incrementBookCount(bookId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewCount = "${snapshot.child("viewCount").value}"
                        if (viewCount == "" || viewCount == "null") {
                            viewCount = "0"
                        }

                        val newViewCount = viewCount.toLong() + 1
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewCount"] = newViewCount

                        val reference = FirebaseDatabase.getInstance().getReference("Books")
                        reference.child(bookId).updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        @JvmStatic
        fun downloadBook(context: Context, bookId: String, bookTitle: String, bookUrl: String) {
            val namepdf = "$bookTitle.pdf"
            Log.d(TAG_DOWN, "downloadBook: $namepdf")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Downloading $namepdf")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG_DOWN, "onSuccess: Saving book")
                    saveDownloadBook(context, progressDialog, bytes, namepdf, bookId)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed to download " + e.message, Toast.LENGTH_SHORT).show()
                }
        }

        @JvmStatic
        private fun saveDownloadBook(
            context: Context,
            progressDialog: ProgressDialog,
            bytes: ByteArray,
            namepdf: String,
            bookId: String
        ) {
            Log.d(TAG_DOWN, "saveDownloadBook: Saving download book")
            try {
                val downloadFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadFolder.mkdirs()

                val filePath = downloadFolder.path + "/" + namepdf
                val out = FileOutputStream(filePath)
                out.write(bytes)
                out.close()

                Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()

                incrementBookDownloadCount(bookId)

            } catch (e: Exception) {
                Log.d(TAG_DOWN, "saveDownloadBook: Failed saving to Download Folder: " + e.message)
                Toast.makeText(context, "Failed saving to Download: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }

        @JvmStatic
        private fun incrementBookDownloadCount(bookId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var downloadCount = "${snapshot.child("downloadCount").value}"
                        Log.d(TAG_DOWN, "onDataChange: DL count: $downloadCount")
                        if (downloadCount == "" || downloadCount == "null") {
                            downloadCount = "0"
                        }

                        val newDownloadCount = downloadCount.toLong() + 1
                        val hashMap = HashMap<String, Any>()
                        hashMap["downloadCount"] = newDownloadCount

                        val reference = FirebaseDatabase.getInstance().getReference("Books")
                        reference.child(bookId)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                Log.d(TAG_DOWN, "onSuccess: incrementBookDownload ")
                            }
                            .addOnFailureListener { e ->
                                Log.d(TAG_DOWN, "onFailure: failed update " + e.message)
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        @JvmStatic
        fun addToFavorite(context: Context, bookId: String) {
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show()
            } else {
                val timestamp = System.currentTimeMillis()

                val hashMap = HashMap<String, Any>()
                hashMap["bookId"] = bookId
                hashMap["timestamp"] = "$timestamp"

                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Added to your favorite list", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed add to your favorite list " + e.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        @JvmStatic
        fun removeFromFavorite(context: Context, bookId: String) {
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show()
            } else {
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Removed from your favorite list", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to remove from your favorite list " + e.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
