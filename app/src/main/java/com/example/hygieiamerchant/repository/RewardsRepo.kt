package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Rewards
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RewardsRepo {
    private val tag = "REWARDSREPO"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION_NAME = "reward"
        private const val STORE_ID = "storeId"
        private const val ID = "id"
        private const val PHOTO = "photo"
        private const val NAME = "name"
        private const val PRICE = "discountedPrice"
        private const val DISCOUNT_RATE = "discount"
        private const val POINTS_REQUIRED = "pointsRequired"
    }

    fun fetchAllRewards(category: String?, callback: (List<Rewards>?) -> Unit) {
        currentUser?.let { user ->
            val baseQuery = firestore.collection(COLLECTION_NAME)
                .whereEqualTo(STORE_ID, user.uid)

            val query = if (category.isNullOrEmpty() || category == "All") {
                baseQuery // No category filter or "All" selected, use the base query
            } else {
                baseQuery.whereEqualTo("category", category)
            }

            query.get()
                .addOnSuccessListener { result ->
                    val rewardsList = mutableListOf<Rewards>()
                    for (document in result) {
                        try {
                            val rewards = Rewards(
                                document.getString(ID) ?: "",
                                document.getString(PHOTO) ?: "",
                                document.getString(NAME) ?: "",
                                document.getDouble(DISCOUNT_RATE) ?: 0.0,
                                document.getDouble(PRICE) ?: 0.0,
                                document.getDouble(POINTS_REQUIRED) ?: 0.0
                            )
                            rewardsList.add(rewards)
                        } catch (e: Exception) {
                            Log.d(tag, "Error creating Rewards object: ${e.message}")
                            // Handle the error or skip this document
                        }
                    }
                    callback(rewardsList)
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "Error getting rewards: ", exception)
                    // Handle the failure
                    callback(null)
                }
        }
    }
}