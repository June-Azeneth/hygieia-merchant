package com.example.hygieiamerchant.repository

import android.util.Log
import com.example.hygieiamerchant.data_classes.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnnouncementRepo {
    private val logTag = "ANNOUNCEMENTS"
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val COLLECTION = "announcement"
        private const val TITLE = "title"
        private const val BODY = "body"
        private const val DATE = "datePosted"
        private const val STATUS = "status"
    }

    suspend fun fetchAllAnnouncements(callback: (List<Announcement>?) -> Unit) {
        val querySnapshot = fireStore.collection(COLLECTION)
            .whereEqualTo(STATUS, "active")
            .get()
            .await()

        val announcementList = mutableListOf<Announcement>()
        for (document in querySnapshot.documents) {
            try {
                val announcement = Announcement(
                    document.id,
                    document.getString(TITLE) ?: "",
                    document.getString(BODY) ?: "",
                    document.getTimestamp(DATE)?.toDate(),
                )
                announcementList.add(announcement)
            } catch (error: Exception) {
                Log.e(logTag, "Error parsing reward document: ${error.message}")
                callback(null)
            }
        }
        callback(announcementList)
    }

    fun fetchAnnouncement(id: String, callback: (Announcement?) -> Unit) {
        val docRef = fireStore.collection(COLLECTION)
            .document(id)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val announcement = Announcement(
                        document.id,
                        document.getString(TITLE) ?: "",
                        document.getString(BODY) ?: "",
                        document.getTimestamp(DATE)?.toDate(),
                    )
                    callback(announcement)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}