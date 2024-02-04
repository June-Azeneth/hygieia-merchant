package com.example.hygieiamerchant.repository


import android.util.Log
import com.example.hygieiamerchant.data_classes.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepo {
    val TAG = "UserRepoMessages"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserDetails(callback: (UserInfo?) -> Unit) {
        val userRef = currentUser?.let { firestore.collection("store").document(it.uid) }
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
                Log.e(TAG, error.toString())
            }
        }?.addOnFailureListener {
            callback(null)
        }

    }

    // Function to get the currently authenticated user
    fun getCurrentUserId(): String? {
        val currentUser = auth.currentUser
        return currentUser?.uid
    }
//
//    fun updateUserProfile(userId: String, updatedUserInfo: UserInfo, callback: (Boolean) -> Unit) {
//        val userRef = firestore.collection("consumer").document(userId)
//
//        // Create a map with only the fields you want to update
//        val updateData = mapOf(
//            "userPhoto" to updatedUserInfo.img_url,
//            "customerName" to updatedUserInfo.customerName,
//            "email" to updatedUserInfo.email,
//            "userLocation" to updatedUserInfo.userLocation
//            // Add other fields if needed
//        )
//
//        // Update the user document with the specified fields
//        userRef.update(updateData)
//            .addOnSuccessListener {
//                callback(true)
//            }
//            .addOnFailureListener {
//                callback(false)
//            }
//    }
}