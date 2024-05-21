package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.firestore.FirebaseFirestore

class AdsRepo {
    private val logTag = "ADS"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepo: UserRepo = UserRepo()

    companion object {
        private const val COLLECTION = "ads"
        private const val POSTER = "poster"
        private const val STORE_ID = "storeId"
        private const val TITLE = "title"
        private const val DETAILS = "details"
        private const val IS_DELETED = "isDeleted"
    }

    fun getAllAds(storeId: String, callback: (List<Ads>?) -> Unit) {
        fireStore.collection(COLLECTION)
            .whereEqualTo(STORE_ID, storeId)
            .whereEqualTo(IS_DELETED, false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Commons().log("QUERY SNAPSHOT", querySnapshot.size().toString())
                val ads = mutableListOf<Ads>()
                for (document in querySnapshot) {
                    try {
                        val currentDate = System.currentTimeMillis()
                        val startDate = document.getTimestamp("startDate")?.toDate()?.time ?: 0
                        val endDate = document.getTimestamp("endDate")?.toDate()?.time ?: 0

                        val status = when {
                            currentDate < (startDate) -> "Upcoming"
                            currentDate in (startDate)..(endDate) -> "Ongoing"
                            else -> "Passed"
                        }

                        val ad = Ads(
                            document.id,
                            document.getTimestamp("startDate")?.toDate(),
                            document.getTimestamp("endDate")?.toDate(),
                            document.getString(POSTER) ?: "",
                            document.getString(STORE_ID) ?: "",
                            status,
                            document.getString(TITLE) ?: "",
                            document.getString(DETAILS) ?: "",
                        )
                        ads.add(ad)
                    } catch (exception: Exception) {
                        Commons().log("Exception", "Error parsing document: ${exception.message}")
                        callback(null)
                        continue
                    }
                }
                Commons().log("LIST SIZE", ads.size.toString())
                callback(ads)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun updateAd(id: String, data: Ads, callback: (Boolean) -> Unit) {
        Commons().log("ADS REPO", id)
        val docRef = fireStore.collection(COLLECTION).document(id)
        val updateData = mapOf(
            "details" to data.details,
            "title" to data.title,
            "startDate" to data.startDate,
            "endDate" to data.endDate,
            "poster" to data.poster
        )
        docRef.update(updateData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun deleteAd(id: String, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION).document(id)
        val data = mapOf(
            IS_DELETED to true
        )
        docRef.update(data)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun createAd(data: Ads, callback: (Boolean) -> Unit) {
        val docRef = fireStore.collection(COLLECTION)
        val addData = mapOf(
            "details" to data.details,
            "storeId" to userRepo.getCurrentUserId(),
            "title" to data.title,
            "startDate" to data.startDate,
            "endDate" to data.endDate,
            "isDeleted" to false,
            "poster" to data.poster
        )
        docRef.add(addData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}