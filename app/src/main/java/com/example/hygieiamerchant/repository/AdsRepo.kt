package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AdsRepo {
    private val logTag = "ADS"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val COLLECTION = "ads"
        private const val DURATION = "duration"
        private const val POSTER = "poster"
        private const val STORE_ID = "storeId"
        private const val IS_DELETED = "isDeleted"
    }

    fun getAllAds(storeId: String, callback: (List<Ads>?) -> Unit) {
        Commons().log("STORE ID", storeId)
        fireStore.collection("ads")
            .whereEqualTo(STORE_ID, storeId)
            .whereEqualTo(IS_DELETED, false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Commons().log("QUERY SNAPSHOT", querySnapshot.size().toString())
                val ads = mutableListOf<Ads>()
                for (document in querySnapshot) {
                    try {
                        val adDetails =document.data
//                        val currentDate = System.currentTimeMillis()
//                        val duration = document.data[DURATION] as? Map<String, Date>
//                        val startDateMillis = duration?.get("startDate") as? Long
//                        val endDateMillis = duration?.get("endDate") as? Long
//
//                        val startDate = startDateMillis?.let { Date(it) }
//                        val endDate = endDateMillis?.let { Date(it) }
//
//                        val status = when {
//                            currentDate < (startDate?.time ?: 0) -> "Upcoming"
//                            currentDate in (startDate?.time ?: 0)..(endDate?.time ?: 0) -> "Ongoing"
//                            else -> "Passed"
//                        }

                        val ad = Ads(
                            adDetails?.get("duration") as? Map<String, Date>?,
                            document.getString(POSTER) ?: "",
                            document.getString(STORE_ID) ?: "",
                            document.getString("status") ?: "",
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

    fun editAds() {

    }

    fun deleteAd(id: String) {

    }
}