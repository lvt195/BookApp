package com.pluto.bookapp.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pluto.bookapp.model.ModelCategory
import com.pluto.bookapp.model.ModelPdf
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

@Singleton
class BookRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) {

    fun getCategories(): Flow<List<ModelCategory>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Categories")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = ArrayList<ModelCategory>()
                
                // Add static categories
                val modelAll = ModelCategory("01", "All", "", 1)
                val modelMostViewed = ModelCategory("02", "Most Viewed", "", 1)
                val modelMostDownloaded = ModelCategory("03", "Most Downloaded", "", 1)

                categoryList.add(modelAll)
                categoryList.add(modelMostViewed)
                categoryList.add(modelMostDownloaded)

                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    if (model != null) {
                        categoryList.add(model)
                    }
                }
                trySend(categoryList)
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getAllBooks(): Flow<List<ModelPdf>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Books")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelPdf>()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) list.add(model)
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getBooksByCategory(categoryId: String): Flow<List<ModelPdf>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Books")
        val query = ref.orderByChild("categoryId").equalTo(categoryId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelPdf>()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) list.add(model)
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getMostViewedBooks(): Flow<List<ModelPdf>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Books")
        val query = ref.orderByChild("viewCount").limitToLast(10)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelPdf>()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) list.add(model)
                }
                list.reverse()
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getMostDownloadedBooks(): Flow<List<ModelPdf>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Books")
        val query = ref.orderByChild("downloadCount").limitToLast(10)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelPdf>()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) list.add(model)
                }
                list.reverse()
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addCategory(category: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val hashMap = HashMap<String, Any>()
            hashMap["id"] = "$timestamp"
            hashMap["category"] = category
            hashMap["timestamp"] = timestamp
            hashMap["uid"] = "${firebaseAuth.uid}"

            val ref = firebaseDatabase.getReference("Categories")
            ref.child("$timestamp").setValue(hashMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            val ref = firebaseDatabase.getReference("Categories")
            ref.child(categoryId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPdf(
        pdfUri: Uri,
        title: String,
        description: String,
        categoryId: String
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val filePathAndName = "Books/$timestamp"
            val storageReference = firebaseStorage.getReference(filePathAndName)
            
            storageReference.putFile(pdfUri).await()
            val url = storageReference.downloadUrl.await().toString()

            val uid = firebaseAuth.uid
            val hashMap = HashMap<String, Any>()
            hashMap["uid"] = "$uid"
            hashMap["id"] = "$timestamp"
            hashMap["title"] = title
            hashMap["description"] = description
            hashMap["categoryId"] = categoryId
            hashMap["url"] = url
            hashMap["timestamp"] = timestamp
            hashMap["viewCount"] = 0
            hashMap["downloadCount"] = 0

            val ref = firebaseDatabase.getReference("Books")
            ref.child("$timestamp").setValue(hashMap).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBook(bookId: String, bookUrl: String): Result<Unit> {
        return try {
            val storageReference = firebaseStorage.getReferenceFromUrl(bookUrl)
            storageReference.delete().await()

            val ref = firebaseDatabase.getReference("Books")
            ref.child(bookId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementViewCount(bookId: String) {
        try {
            val ref = firebaseDatabase.getReference("Books")
            val snapshot = ref.child(bookId).get().await()
            
            var viewCount = "${snapshot.child("viewCount").value}"
            if (viewCount == "" || viewCount == "null") {
                viewCount = "0"
            }

            val newViewCount = viewCount.toLong() + 1
            val hashMap = HashMap<String, Any>()
            hashMap["viewCount"] = newViewCount

            ref.child(bookId).updateChildren(hashMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeFromFavorites(bookId: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.uid ?: throw Exception("Not logged in")
            val ref = firebaseDatabase.getReference("Users").child(uid).child("Favorites")
            ref.child(bookId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToFavorites(bookId: String): Result<Unit> {
        return try {
            val uid = firebaseAuth.uid ?: throw Exception("Not logged in")
            val timestamp = System.currentTimeMillis()
            val hashMap = HashMap<String, Any>()
            hashMap["bookId"] = bookId
            hashMap["timestamp"] = timestamp
            
            val ref = firebaseDatabase.getReference("Users").child(uid).child("Favorites")
            ref.child(bookId).setValue(hashMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isFavorite(bookId: String): Flow<Boolean> = callbackFlow {
        val uid = firebaseAuth.uid
        if (uid == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val ref = firebaseDatabase.getReference("Users").child(uid).child("Favorites").child(bookId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                // If permission denied (common on logout), just close gracefully or log
                if (error.code == DatabaseError.PERMISSION_DENIED) {
                     close() 
                } else {
                     close(error.toException())
                }
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getPdfFile(bookId: String): java.io.File {
        val filename = "$bookId.pdf"
        return java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), filename)
    }

    // Download with progress flow
    fun downloadBookToFile(bookUrl: String, bookId: String): Flow<DownloadStatus> = callbackFlow {
        try {
            val file = getPdfFile(bookId)
            if (file.exists()) {
                trySend(DownloadStatus.Success(file))
                close()
                return@callbackFlow
            }

            val storageReference = firebaseStorage.getReferenceFromUrl(bookUrl)
            
            val task = storageReference.getFile(file)
            task.addOnProgressListener { snapshot ->
                val progress = if (snapshot.totalByteCount > 0) {
                    (100f * snapshot.bytesTransferred) / snapshot.totalByteCount
                } else {
                    0f
                }
                trySend(DownloadStatus.Progress(progress.toInt()))
            }
            
            task.addOnSuccessListener {
                trySend(DownloadStatus.Success(file))
                close()
            }
            
            task.addOnFailureListener {
                trySend(DownloadStatus.Error(it.message ?: "Download failed"))
                close()
            }
            
            awaitClose { 
                // task.cancel() // Optional: cancel if flow is cancelled
            }
        } catch (e: Exception) {
            trySend(DownloadStatus.Error(e.message ?: "Unknown error"))
            close()
        }
    }

    sealed class DownloadStatus {
        data class Progress(val percentage: Int) : DownloadStatus()
        data class Success(val file: java.io.File) : DownloadStatus()
        data class Error(val message: String) : DownloadStatus()
    }

    // Keep existing downloadBook for Public Downloads (Export) if needed, 
    // or we can remove it if "Download" button also just saves to internal? 
    // User asked "if it is downloading, show dialog".
    // Existing downloadBook uses DownloadManager which shows notification but not in-app dialog directly linked to View.
    // Let's keep existing for "Export" feature if user wants file in "Downloads" folder public.
    // But for "Read", we use the new internal logic.
    
    suspend fun downloadBook(bookUrl: String, bookTitle: String, bookId: String) {
        try {
            val filename = "$bookId.pdf"
            val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val uri = Uri.parse(bookUrl)
            
            val request = android.app.DownloadManager.Request(uri)
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setTitle(bookTitle)
            request.setDescription("Downloading...")
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, filename)
            
            downloadManager.enqueue(request)
            
            // Increment download count
            incrementDownloadCount(bookId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    
    private suspend fun incrementDownloadCount(bookId: String) {
        try {
             val ref = firebaseDatabase.getReference("Books")
            val snapshot = ref.child(bookId).get().await()
            
            var downloads = "${snapshot.child("downloadCount").value}"
            if (downloads == "" || downloads == "null") {
                downloads = "0"
            }

            val newCount = downloads.toLong() + 1
            val hashMap = HashMap<String, Any>()
            hashMap["downloadCount"] = newCount

            ref.child(bookId).updateChildren(hashMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
