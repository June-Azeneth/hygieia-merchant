package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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
        private const val REWARD_ID = "rewardId"
        private const val STORE_ID = "storeId"
        private const val TOTAL = "total"
        private const val PRODUCT = "product"
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
                    .orderBy(ADDED_ON, Query.Direction.DESCENDING)

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
                                document.getString(REWARD_ID) ?: "",
                                document.getTimestamp(ADDED_ON)?.toDate(),
                                document.getDouble(POINTS_REQUIRED) ?: 0.0,
                                document.getDouble(POINTS_GRANTED) ?: 0.0,
                                document.getDouble(TOTAL) ?: 0.0,
                                document.getString(PRODUCT) ?: ""
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

    private suspend fun updateCustomerBalance(
        id: String,
        pointsRequired: Double,
        pointsGranted: Double,
        type: String
    ): Pair<Boolean, String> {
        val docRef = fireStore.collection("consumer").document(id)
        try {
            val document = docRef.get().await()
            if (document.exists()) {
                val currentBalance = document.getDouble("currentBalance") ?: 0.0
                val updatedBalance = when (type) {
                    "grant" -> currentBalance + pointsGranted
                    "redeem" -> {
                        // Check if current balance is sufficient for redemption
                        if (currentBalance >= pointsRequired) {
                            currentBalance - pointsRequired
                        } else {
                            return Pair(false, "Insufficient balance for redemption")
                        }
                    }

                    else -> currentBalance
                }
                val updateData = hashMapOf(
                    "currentBalance" to updatedBalance
                )
                docRef.set(updateData, SetOptions.merge()).await()
                return Pair(true, "Transaction successful.")
            } else {
                return Pair(false, "Customer not found")
            }
        } catch (e: Exception) {
            return Pair(false, "An error occurred")
        }
    }

    suspend fun createTransaction(
        data: (Transaction),
        type: String,
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        try {
            val (success, message) = updateCustomerBalance(
                data.customerId,
                data.pointsRequired,
                data.pointsGranted,
                type
            )

            if (!success) {
                callback(Pair(false, message))
                return
            }

            val docRef = fireStore.collection("transaction")
            val map = HashMap<String, Any>()
            data.addedOn?.let { map.put("addedOn", it) }
            map["customerId"] = data.customerId
            map["storeId"] = data.storeId
            map["type"] = type

            when (type) {
                "grant" -> {
                    map["pointsEarned"] = data.pointsGranted
                }

                "redeem" -> {
                    map["pointsSpent"] = data.pointsRequired
                    map["rewardId"] = data.rewardId
                    map["product"] = data.product
                    map["discount"] = data.discount
                    map["total"] = data.total
                }
            }

            docRef.add(map)
                .addOnSuccessListener {
                    callback(Pair(true, message))
                }
                .addOnFailureListener {
                    callback(Pair(false, it.message ?: "Unknown error"))
                }
        } catch (e: Exception) {
            callback(Pair(false, e.message ?: "Unknown error"))
        }
    }
}