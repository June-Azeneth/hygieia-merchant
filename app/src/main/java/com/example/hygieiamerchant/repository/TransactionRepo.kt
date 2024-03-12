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
        private const val PROMO_ID = "promoId"
        private const val PROMO_NAME = "promoName"
        private const val DISCOUNT = "discount"
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
                                document.getString(PRODUCT) ?: "",
                                document.getString(PROMO_NAME) ?: "",

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

    suspend fun createPromoTransaction(
        data: (Transaction),
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        try {
            val (success, message) = updateCustomerBalance(
                data.customerId,
                data.pointsRequired,
                0.0,
                "redeem"
            )

            if (!success) {
                callback(Pair(false, message))
                return
            }

            val docRef = fireStore.collection("transaction")
            val data = mapOf(
                ADDED_ON to data.addedOn,
                CUSTOMER_ID to data.customerId,
                STORE_ID to data.storeId,
                TYPE to "redeem",
                POINTS_REQUIRED to data.pointsRequired,
                PROMO_ID to data.promoId,
                PROMO_NAME to data.promoName,
                PRODUCT to data.product,
                DISCOUNT to data.discount,
                TOTAL to data.total
            )

            docRef.add(data)
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

    suspend fun createRewardTransaction(
        data: (Transaction),
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        try {
            val (success, message) = updateCustomerBalance(
                data.customerId,
                data.pointsRequired,
                0.0,
                "redeem"
            )

            if (!success) {
                callback(Pair(false, message))
                return
            }

            val docRef = fireStore.collection("transaction")
            val data = mapOf(
                ADDED_ON to data.addedOn,
                CUSTOMER_ID to data.customerId,
                STORE_ID to data.storeId,
                TYPE to "redeem",
                POINTS_REQUIRED to data.pointsRequired,
                REWARD_ID to data.rewardId,
                PRODUCT to data.product,
                DISCOUNT to data.discount,
                TOTAL to data.total
            )

            docRef.add(data)
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

    suspend fun createGrantTransaction(
        data: (Transaction),
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        try {
            val (success, message) = updateCustomerBalance(
                data.customerId,
                data.pointsRequired,
                data.pointsGranted,
                "grant"
            )

            if (!success) {
                callback(Pair(false, message))
                return
            }
            val docRef = fireStore.collection("transaction")
            val data = mapOf(
                ADDED_ON to data.addedOn,
                CUSTOMER_ID to data.customerId,
                STORE_ID to data.storeId,
                TYPE to "grant",
                POINTS_GRANTED to data.pointsGranted,
            )

            docRef.add(data)
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