package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Lgu
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.firestore.FirebaseFirestore


class LguRepo {

    private val logTag = "LGU_REPO"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val COLLECTION = "client"
        private const val ID = "id"
        private const val ADDRESS_CITY = "address.city"
        private const val NAME = "name"
    }

    fun fetchLguBasedOnUserLocation(userCity: String, callback: (List<Pair<String, String>>?) -> Unit) {
        val query = fireStore.collection(COLLECTION)
            .whereEqualTo(ADDRESS_CITY, userCity)

        query.get()
            .addOnSuccessListener { result ->
                val lguList = mutableListOf<Pair<String, String>>()
                for (document in result) {
                    val lguId = document.getString(ID) ?: ""
                    val lguName = document.getString(NAME) ?: ""
                    lguList.add(Pair(lguId, lguName))
                }
                callback(lguList)
            }
            .addOnFailureListener { e ->
                callback(null)
                Commons().log(logTag, e.toString())
            }
    }

}