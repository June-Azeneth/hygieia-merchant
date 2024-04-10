package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Request
import com.google.firebase.firestore.FirebaseFirestore

class RequestRepo {
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION = "request"
        private const val STORE_ID = "storeId"
        private const val LGU_ID = "lguId"
        private const val DATE = "date"
        private const val NOTES = "notes"
        private const val ADDRESS = "address"
        private const val STATUS = "status"
        private const val PHONE = "phone"
    }

//    sealed class RequestDetailsResult {
//        data class Success(val requests: MutableList<Request>) : RequestDetailsResult()
//        data class Error(val errorMessage: String) : RequestDetailsResult()
//    }

    fun getRequestDetails(storeId: String, callback: (Request?) -> Unit) {
        fireStore.collection(COLLECTION)
            .whereEqualTo(STORE_ID, storeId)
            .whereIn(STATUS, listOf("pending", "active"))
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    try {
                        val documentSnapshot = querySnapshot.documents[0] // Get the first document
                        val data = documentSnapshot.data
                        val request = Request(
                            documentSnapshot.id,
                            documentSnapshot.getString(STORE_ID) ?: "",
                            documentSnapshot.getTimestamp(DATE)?.toDate(),
                            documentSnapshot.getString(NOTES) ?: "",
                            data?.get(ADDRESS) as? Map<*, *>?,
                            documentSnapshot.getString(STATUS) ?: ""
                        )
                        callback(request)
                    } catch (e: Exception) {
                        Log.d("REQUESTS", "Error creating Request object: ${e.message}")
                        callback(null)
                    }
                } else {
                    // No document matching the query criteria
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("REQUESTS", "Error retrieving request details: ${exception.message}")
                callback(null)
            }
    }

    fun addRequest(data: Request, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION)
        val addData = mapOf(
            STORE_ID to data.storeId,
            DATE to data.date,
            NOTES to data.notes,
            ADDRESS to data.address,
            PHONE to data.phone,
            STATUS to "pending",
        )
        docRef.add(addData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun cancelRequest(id: String, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val data = mapOf(
            "status" to "canceled"
        )
        docRef.update(data)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun editRequest(id: String, data: Request, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val updateData = mapOf(
            DATE to data.date,
            NOTES to data.notes,
            PHONE to data.phone
        )
        docRef.update(updateData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}