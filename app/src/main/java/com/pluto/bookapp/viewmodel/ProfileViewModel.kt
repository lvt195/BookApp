package com.pluto.bookapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    // Ideally we should have a UserRepository. For speed, accessing firebase directly here or move to AuthRepository?
    // AuthRepository handles Auth. User data management could be in UserRepository.
    // Given the constraints/plan, I'll put logic here or reuse AuthRepo if it has it. 
    // AuthRepo has get user type. Let's add getUserInfo there? 
    // Or just keep it here as it's specific to Profile.

    private val _userData = MutableStateFlow<Map<String, String>>(emptyMap())
    val userData: StateFlow<Map<String, String>> = _userData.asStateFlow()
    
    // User stats
    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = firebaseAuth.uid ?: return
        val ref = firebaseDatabase.getReference("Users").child(uid)
        
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, String>()
                map["name"] = snapshot.child("name").value?.toString() ?: ""
                map["email"] = snapshot.child("email").value?.toString() ?: ""
                map["profileImage"] = snapshot.child("profileImage").value?.toString() ?: ""
                map["userType"] = snapshot.child("userType").value?.toString() ?: ""
                map["timestamp"] = snapshot.child("timestamp").value?.toString() ?: ""
                map["uid"] = snapshot.child("uid").value?.toString() ?: ""
                _userData.value = map
                
                // Also get favorites count
                val favCount = snapshot.child("Favorites").childrenCount.toInt()
                _favoriteCount.value = favCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun updateProfile(name: String) {
        // Simple update name for now
        viewModelScope.launch {
            try {
                val uid = firebaseAuth.uid ?: return@launch
                val hashMap = HashMap<String, Any>()
                hashMap["name"] = name
                firebaseDatabase.getReference("Users").child(uid).updateChildren(hashMap).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
