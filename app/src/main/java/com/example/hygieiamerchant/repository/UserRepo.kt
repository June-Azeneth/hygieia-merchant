package com.example.hygieiamerchant.repository


import android.util.Log
import com.example.hygieiamerchant.data_classes.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepo {
    private val logTAG = "USER_REPO"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserDetails(callback: (UserInfo?) -> Unit) {
        val userRef = currentUser?.let { fireStore.collection("store").document(it.uid) }
        userRef?.get()?.addOnSuccessListener { documentSnapshot ->
            try {
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(UserInfo::class.java)
                    callback(user)
                } else {
                    // User document not found
                    callback(null)
                }
            } catch (error: Exception) {
                Log.e(logTAG, error.toString())
            }
        }?.addOnFailureListener {
            callback(null)
        }

    }

    fun getCurrentUserId(): String? {
        val currentUser = auth.currentUser
        return currentUser?.uid
    }
}