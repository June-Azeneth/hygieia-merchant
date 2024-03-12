package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Promo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PromoRepo {
    private val tag = "PROMOS_REPO"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        private const val COLLECTION = "promo"
        private const val STORE_ID = "storeId"
        private const val PRODUCT = "product"
        private const val PHOTO = "photo"
        private const val NAME = "name"
        private const val PRICE = "price"
        private const val DISCOUNT_RATE = "discountRate"
        private const val POINTS_REQUIRED = "pointsRequired"
        private const val PROMO_START = "promoStart"
        private const val PROMO_END = "promoEnd"
        private const val DATE_PAUSED = "datePaused"
        private const val DATE_RESUME = "dateResume"
        private const val ADDED_ON = "addedOn"
        private const val DISCOUNTED_PRICE = "discountedPrice"
        private const val UPDATED_ON = "updatedOn"
        private const val STATUS = "status"
    }

    fun getAllPromos(statusFilter: String, callback: (List<Promo>?) -> Unit) {
        currentUser?.let { user ->
            val currentDate = System.currentTimeMillis()

            val query = fireStore.collection(COLLECTION)
                .whereEqualTo(STORE_ID, user.uid)
                .whereEqualTo(STATUS , "active")

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
                                    document.id,
                                    document.getString(PRODUCT) ?: "",
                                    document.getString(PHOTO) ?: "",
                                    document.getString(NAME) ?: "",
                                    document.getDouble(PRICE) ?: 0.0,
                                    document.getDouble(DISCOUNTED_PRICE) ?: 0.0,
                                    document.getDouble(DISCOUNT_RATE) ?: 0.0,
                                    document.getDouble(POINTS_REQUIRED) ?: 0.0,
                                    document.getTimestamp(PROMO_START)?.toDate(),
                                    document.getTimestamp(PROMO_END)?.toDate(),
                                    promoStatus
                                )
                                promoList.add(promo)
                            }
                        } catch (e: Exception) {
                            Log.d(tag, "Error fetching Promos object: ${e.message}")
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

//    fun pausePromo(id: String, pause: Date?, end: Date?) {
//
//    }

    fun addPromo(data: Promo, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION)
        val addData = mapOf(
            NAME to data.promoName,
            ADDED_ON to data.addedOn?.toDate(),
            PHOTO to data.photo,
            POINTS_REQUIRED to data.pointsRequired,
            DISCOUNT_RATE to data.discountRate,
            DISCOUNTED_PRICE to data.discountedPrice,
            PRICE to data.price,
            STORE_ID to data.storeId,
            PROMO_START to data.dateStart,
            PROMO_END to data.dateEnd,
            PRODUCT to data.product,
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

    fun getPromo(id: String, callback: (Promo?) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val promo = Promo(
                        document.getString(STORE_ID) ?: "",
                        document.id,
                        document.getString(PRODUCT) ?: "",
                        document.getString(PHOTO) ?: "",
                        document.getString(NAME) ?: "",
                        document.getDouble(PRICE) ?: 0.0,
                        document.getDouble(DISCOUNTED_PRICE) ?: 0.0,
                        document.getDouble(DISCOUNT_RATE) ?: 0.0,
                        document.getDouble(POINTS_REQUIRED) ?: 0.0,
                        document.getTimestamp(PROMO_START)?.toDate(),
                        document.getTimestamp(PROMO_END)?.toDate(),
                    )
                    callback(promo)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun deletePromo(id: String, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val data = mapOf(
            STATUS to "deleted"
        )
        docRef.update(data)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updatePromo(id: String, data: Promo, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val updateData = mapOf(
            NAME to data.promoName,
            PRODUCT to data.product,
            UPDATED_ON to data.updatedOn?.toDate(),
            PHOTO to data.photo,
            POINTS_REQUIRED to data.pointsRequired,
            DISCOUNT_RATE to data.discountRate,
            DISCOUNTED_PRICE to data.discountedPrice,
            PRICE to data.price,
            PROMO_START to data.dateStart,
            PROMO_END to data.dateEnd
        )
        docRef.update(updateData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {error ->
                callback(false)
            }
    }
}