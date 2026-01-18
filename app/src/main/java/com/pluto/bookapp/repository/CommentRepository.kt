package com.pluto.bookapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pluto.bookapp.model.ModelComment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {

    fun getComments(bookId: String): Flow<List<ModelComment>> = callbackFlow {
        val ref = firebaseDatabase.getReference("Books").child(bookId).child("Comments")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelComment>()
                for (ds in snapshot.children) {
                     val model = ds.getValue(ModelComment::class.java)
                     if (model != null) list.add(model)
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addComment(bookId: String, comment: String): Result<Unit> {
        return try {
            val timestamp = "${System.currentTimeMillis()}"
            val uid = firebaseAuth.uid ?: throw Exception("User not logged in")
            
            // Fetch user name and profile image
            val userRef = firebaseDatabase.getReference("Users").child(uid)
            val userSnapshot = userRef.get().await()
            val name = userSnapshot.child("name").value.toString()
            val profileImage = userSnapshot.child("profileImage").value.toString()

            val hashMap = HashMap<String, Any>()
            hashMap["id"] = timestamp
            hashMap["bookId"] = bookId
            hashMap["timestamp"] = timestamp
            hashMap["comment"] = comment
            hashMap["uid"] = uid
            hashMap["name"] = name
            hashMap["profileImage"] = profileImage
            
            val ref = firebaseDatabase.getReference("Books").child(bookId).child("Comments")
            ref.child(timestamp).setValue(hashMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(bookId: String, commentId: String): Result<Unit> {
         return try {
            val ref = firebaseDatabase.getReference("Books").child(bookId).child("Comments")
            ref.child(commentId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
