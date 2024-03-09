package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.utils.Commons
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardRepo {
    private val logTag = "REWARDSREPO"
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
    }

    fun getAllRewards(category: String?, callback: (List<Reward>?) -> Unit) {
        currentUser?.let { user ->
            val baseQuery = fireStore.collection(COLLECTION)
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
        val docRef = fireStore.collection("reward").document(id)
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
            "name" to data.name,
            "addedOn" to data.addedOn?.toDate(),
            "photo" to data.photo,
            "category" to data.category,
            "pointsRequired" to data.pointsRequired,
            "discount" to data.discount,
            "discountedPrice" to data.discountedPrice,
            "price" to data.price,
            "storeName" to data.storeName,
            "storeId" to data.storeId
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
        val docRef = fireStore.collection("reward").document(id)
        val updateData = mapOf(
            "name" to data.name,
            "updatedOn" to data.updatedOn?.toDate(),
            "photo" to data.photo,
            "category" to data.category,
            "pointsRequired" to data.pointsRequired,
            "discount" to data.discount,
            "discountedPrice" to data.discountedPrice,
            "price" to data.price,
            "storeName" to data.storeName
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
        val docRef = fireStore.collection("reward").document(id)
        docRef.delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}