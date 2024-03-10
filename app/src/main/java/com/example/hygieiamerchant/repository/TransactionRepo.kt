package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TransactionRepo {
    private val logTAG = "USER_REPO"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val COLLECTION_NAME = "transaction"
        private const val ADDED_ON = "addedOn"
        private const val POINTS_GRANTED = "pointsEarned"
        private const val POINTS_REQUIRED = "pointsSpent"
        private const val CUSTOMER_ID = "customerId"
        private const val TYPE = "type"
        private const val PRODUCT = "product"
        private const val STORE_ID = "storeId"
        private const val TOTAL = "total"
    }

    private suspend fun getCustomerName(id: String): String? {
        return withContext(Dispatchers.IO) {
            val docRef = fireStore.collection("consumer").document(id)
            try {
                val document = docRef.get().await()
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    "$firstName $lastName".trim()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getAllTransactions(): List<Transaction>? {
        val transactions = fetchAll() ?: return null

        transactions.forEach { transaction ->
            val customerName = getCustomerName(transaction.customerId)
            if (customerName != null) {
                transaction.customerName = customerName
            }
        }
        return transactions
    }

    private suspend fun fetchAll(): List<Transaction>? {
        return withContext(Dispatchers.IO) {
            currentUser?.let { user ->
                val query = fireStore.collection(COLLECTION_NAME)
                    .whereEqualTo(STORE_ID, user.uid)

                try {
                    val result = query.get().await()
                    val transactionList = mutableListOf<Transaction>()
                    for (document in result) {
                        try {
                            val transaction = Transaction(
                                document.id,
                                "",
                                document.getString(CUSTOMER_ID) ?: "",
                                document.getString(TYPE) ?: "",
                                document.getString(PRODUCT) ?: "",
                                document.getTimestamp(ADDED_ON)?.toDate(),
                                document.getDouble(POINTS_REQUIRED) ?: 0.0,
                                document.getDouble(POINTS_GRANTED) ?: 0.0,
                                document.getDouble(TOTAL) ?: 0.0,
                            )
                            transactionList.add(transaction)
                        } catch (error: Error) {
                            Commons().log(logTAG, "${error.message}")
                        }
                    }
                    transactionList
                } catch (e: Exception) {
                    null
                }
            }
        }
    }


//    fun getCustomerName(id:String,callback: (String?) -> Unit){
//        val docRef = fireStore.collection("consumer").document(id)
//        docRef.get()
//            .addOnSuccessListener { document ->
//                callback(document.getString("name") ?: "")
//            }
//            .addOnFailureListener {
//                callback(null)
//            }
//    }

//    fun getSingleTransaction(id: String, callback: (Transaction?) -> Unit) {
//        val docRef = fireStore.collection(COLLECTION_NAME).document(id)
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val transaction = Transaction(
//                        document.id,
//                        document.getString(CUSTOMER_NAME) ?: "",
//                        document.getString(CUSTOMER_ID) ?: "",
//                        document.getString(TYPE) ?: "",
//                        document.getString(PRODUCT) ?: "",
//                        document.getTimestamp(ADDED_ON)?.toDate(),
//                        document.getDouble(POINTS_REQUIRED) ?: 0.0,
//                        document.getDouble(POINTS_GRANTED) ?: 0.0,
//                        document.getDouble(TOTAL) ?: 0.0,
//                    )
//                    callback(transaction)
//                } else {
//                    callback(null)
//                }
//            }
//            .addOnFailureListener {
//                callback(null)
//            }
//    }
}