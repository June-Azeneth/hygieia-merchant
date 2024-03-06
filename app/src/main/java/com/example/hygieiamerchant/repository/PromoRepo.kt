package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Promo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PromoRepo {
    private val tag = "PROMOSREPO"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION = "promo"
        private const val STORE_ID = "store_id"
        private const val ID = "id"
        private const val PRODUCT = "product"
        private const val PHOTO = "image_url"
        private const val NAME = "name"
        private const val PRICE = "discount_price"
        private const val DISCOUNT_RATE = "discount_rate"
        private const val POINTS_REQUIRED = "points_required"
        private const val PROMO_START = "promo_start"
        private const val PROMO_END = "promo_end"
        private const val DATE_PAUSED = "date_paused"
        private const val DATE_RESUME = "date_resume"
    }

    fun fetchAllPromos(statusFilter: String, callback: (List<Promo>?) -> Unit) {
        currentUser?.let { user ->
            val currentDate = System.currentTimeMillis()

            val query = firestore.collection(COLLECTION)
                .whereEqualTo(STORE_ID, user.uid)

            query.get()
                .addOnSuccessListener { result ->
                    val promoList = mutableListOf<Promo>()
                    for (document in result) {
                        try {
                            val startDate = document.getTimestamp(PROMO_START)?.toDate()?.time ?: 0
                            val endDate = document.getTimestamp(PROMO_END)?.toDate()?.time ?: 0
                            val pausedDate = document.getTimestamp(DATE_PAUSED)?.toDate()?.time ?: 0
                            val resumeDate = document.getTimestamp(DATE_RESUME)?.toDate()?.time ?: 0

                            val promoStatus = when {
                                currentDate < startDate -> "Upcoming"
                                currentDate in startDate..endDate -> {
                                    if (currentDate in pausedDate..resumeDate) "Paused" else "Ongoing"
                                }

                                else -> "Passed"
                            }

                            // Filter based on promo status (e.g., show only active promos)
                            if (statusFilter == "All" || promoStatus == statusFilter) {
                                val promo = Promo(
                                    document.getString(STORE_ID) ?: "",
                                    document.getString(ID) ?: "",
                                    document.getString(PRODUCT) ?: "",
                                    document.getString(PHOTO) ?: "",
                                    document.getString(NAME) ?: "",
                                    document.getDouble(PRICE) ?: 0.0,
                                    document.getDouble(DISCOUNT_RATE) ?: 0.0,
                                    document.getDouble(POINTS_REQUIRED) ?: 0.0,
                                    document.getTimestamp(PROMO_START)?.toDate(),
                                    document.getTimestamp(PROMO_END)?.toDate(),
                                    promoStatus
                                )
                                promoList.add(promo)
                            }
                        } catch (e: Exception) {
                            Log.d(tag, "Error creating Promos object: ${e.message}")
                            // Handle the error or skip this document
                        }
                    }
                    callback(promoList)
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "Error getting promos: ", exception)
                    // Handle the failure
                    callback(null)
                }
        }
    }
}