package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.utils.Commons
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardRepo {
    private val logTag = "REWARDS_REPO"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION = "reward"
        private const val STORE_ID = "storeId"
        private const val PHOTO = "photo"
        private const val NAME = "name"
        private const val PRICE = "price"
        private const val DISCOUNT_RATE = "discount"
        private const val DISCOUNTED_PRICE = "discountedPrice"
        private const val POINTS_REQUIRED = "pointsRequired"
        private const val CATEGORY = "category"
        private const val STATUS = "status"
        private const val ADDED_ON = "addedOn"
        private const val UPDATED_ON = "updatedOn"
    }

    fun getAllRewards(category: String?, callback: (List<Reward>?) -> Unit) {
        currentUser?.let { user ->
            val baseQuery = fireStore.collection(COLLECTION)
                .whereEqualTo(STORE_ID, user.uid)
                .whereEqualTo(STATUS, "active")

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
                                document.id,
                                document.getString(PHOTO) ?: "",
                                document.getString(NAME) ?: "",
                                document.getDouble(DISCOUNT_RATE) ?: 0.0,
                                document.getDouble(DISCOUNTED_PRICE) ?: 0.0,
                                document.getDouble(POINTS_REQUIRED) ?: 0.0
                            )
                            rewardList.add(reward)
                        } catch (e: Exception) {
                            Log.d(logTag, "Error creating Rewards object: ${e.message}")
                        }
                    }
                    callback(rewardList)
                }
                .addOnFailureListener { exception ->
                    Log.w(logTag, "Error getting rewards: ", exception)
                    callback(null)
                }
        }
    }

    fun getReward(id: String, callback: (Reward?) -> Unit) {
        val docRef = fireStore.collection(COLLECTION)
            .document(id)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val reward = Reward(
                        document.id,
                        document.getString(PHOTO) ?: "",
                        document.getString(NAME) ?: "",
                        document.getDouble(DISCOUNT_RATE) ?: 0.0,
                        document.getDouble(DISCOUNTED_PRICE) ?: 0.0,
                        document.getDouble(POINTS_REQUIRED) ?: 0.0,
                        document.getDouble(PRICE) ?: 0.0,
                        document.getString(CATEGORY) ?: "",
                    )
                    callback(reward)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun addReward(data: Reward, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection("reward")
        val addData = mapOf(
            NAME to data.name,
            ADDED_ON to data.addedOn?.toDate(),
            PHOTO to data.photo,
            CATEGORY to data.category,
            POINTS_REQUIRED to data.pointsRequired,
            DISCOUNT_RATE to data.discount,
            DISCOUNTED_PRICE to data.discountedPrice,
            PRICE to data.price,
            STORE_ID to data.storeId,
            STATUS to "active"
        )
        docRef.add(addData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateReward(id: String, data: Reward, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val updateData = mapOf(
            NAME to data.name,
            UPDATED_ON to data.updatedOn?.toDate(),
            PHOTO to data.photo,
            CATEGORY to data.category,
            POINTS_REQUIRED to data.pointsRequired,
            DISCOUNT_RATE to data.discount,
            DISCOUNTED_PRICE to data.discountedPrice,
            PRICE to data.price,
        )
        docRef.update(updateData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {error ->
                Commons().log(logTag,error.toString())
                callback(false)
            }
    }

    fun deleteReward(id: String, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val data = mapOf(
            "status" to "deleted"
        )
        docRef.update(data)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}