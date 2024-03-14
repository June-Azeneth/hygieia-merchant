package com.example.hygieiamerchant.repository


import android.util.Log
import com.example.hygieiamerchant.data_classes.Customer
import com.example.hygieiamerchant.data_classes.UserInfo
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepo {
    private val logTAG = "USER_REPO"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    sealed class CustomerDetailsResult {
        data class Success(val customer: Customer) : CustomerDetailsResult()
        data class Error(val errorMessage: String) : CustomerDetailsResult()
    }

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
                            userInfo?.let { it1 -> Commons().log("STOREDETAILS", it1.lguId) }
                            callback(userInfo)
                        } else {
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

    fun getCustomerDetails(id: String, callback: (CustomerDetailsResult) -> Unit) {
        val docRef = fireStore.collection("consumer").document(id)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val status = document.getString("status") ?: ""
                    if (status == "active") {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val name = "$firstName $lastName"
                        val customer = Customer(
                            document.id,
                            name,
                            document.getDouble("currentBalance") ?: 0.0
                        )
                        callback(CustomerDetailsResult.Success(customer))
                    }
                    else {
                        callback(CustomerDetailsResult.Error("Customer account inactive"))
                    }
                } else {
                    callback(CustomerDetailsResult.Error("Customer not found"))
                }
            }
            .addOnFailureListener { exception ->
                callback(CustomerDetailsResult.Error(exception.message ?: "Unknown error"))
            }
    }
}