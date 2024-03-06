package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Reward
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardRepo {
    private val tag = "REWARDSREPO"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION = "reward"
        private const val STORE_ID = "storeId"
        private const val ID = "id"
        private const val PHOTO = "photo"
        private const val NAME = "name"
        private const val PRICE = "discountedPrice"
        private const val DISCOUNT_RATE = "discount"
        private const val POINTS_REQUIRED = "pointsRequired"
    }

    fun fetchAllRewards(category: String?, callback: (List<Reward>?) -> Unit) {
        currentUser?.let { user ->
            val baseQuery = firestore.collection(COLLECTION)
                .whereEqualTo(STORE_ID, user.uid)

            val query = if (category.isNullOrEmpty() || category == "All") {
                baseQuery // No category filter or "All" selected, use the base query
            } else {
                baseQuery.whereEqualTo("category", category)
            }

            query.get()
                .addOnSuccessListener { result ->
                    val rewardList = mutableListOf<Reward>()
                    for (document in result) {
                        try {
                            val reward = Reward(
                                document.getString(ID) ?: "",
                                document.getString(PHOTO) ?: "",
                                document.getString(NAME) ?: "",
                                document.getDouble(DISCOUNT_RATE) ?: 0.0,
                                document.getDouble(PRICE) ?: 0.0,
                                document.getDouble(POINTS_REQUIRED) ?: 0.0
                            )
                            rewardList.add(reward)
                        } catch (e: Exception) {
                            Log.d(tag, "Error creating Rewards object: ${e.message}")
                        }
                    }
                    callback(rewardList)
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "Error getting rewards: ", exception)
                    callback(null)
                }
        }
    }
}