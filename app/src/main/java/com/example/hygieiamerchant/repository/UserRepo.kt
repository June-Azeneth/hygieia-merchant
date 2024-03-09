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
        currentUser?.let {
            val query = fireStore.collection("store")
                .whereEqualTo("storeId", currentUser.uid)

            query.get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            val userInfo = document.toObject(UserInfo::class.java)
                            callback(userInfo)
                        } else {
                            // User document not found
                            callback(null)
                        }
                    } catch (error: Exception) {
                        Log.e(logTAG, error.toString())
                        callback(null)
                    }
                }
                .addOnFailureListener {
                    callback(null)
                }
        }
    }

    fun getCurrentUserId(): String? {
        val currentUser = auth.currentUser
        return currentUser?.uid
    }
}