package com.example.hygieiamerchant.repository

import com.example.hygieiamerchant.data_classes.Lgu
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.firestore.FirebaseFirestore


class LguRepo {

    private val logTag = "LGUREPO"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val commons: Commons = Commons()

    companion object {
        private const val COLLECTION = "lgu"
        private const val ID = "id"
        private const val ADDRESS_CITY = "address.city"
        private const val NAME = "name"
    }

    fun fetchLguBasedOnUserLocation(userCity:String, callback: (List<Lgu>?) -> Unit) {
        val query = fireStore.collection(COLLECTION)
            .whereEqualTo(ADDRESS_CITY, userCity)

        query.get()
            .addOnSuccessListener { result ->
                val lguList = mutableListOf<Lgu>()
                for (document in result) {

                    val lgu = Lgu(
                        document.getString(ID) ?: "",
                        document.getString(NAME) ?: "",
                    )
                    lguList.add(lgu)
                }
                commons.log("LGU",lguList.toString())
                callback(lguList)
            }
            .addOnFailureListener { e ->
                callback(null)
                commons.log(logTag, e.toString())
            }
    }
}