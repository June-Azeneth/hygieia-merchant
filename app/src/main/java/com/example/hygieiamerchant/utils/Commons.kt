package com.example.hygieiamerchant.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.repository.UserRepo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date
import java.util.Locale

class Commons {

    private var loadingDialog: AlertDialog? = null
    private val userRepo: UserRepo = UserRepo()
    private var storage: FirebaseStorage = Firebase.storage

    fun setOnRefreshListener(refreshLayout: SwipeRefreshLayout, refreshAction: () -> Unit) {
        refreshLayout.setOnRefreshListener {
            refreshAction.invoke()
            refreshLayout.isRefreshing = false
        }
    }

    fun setNavigationOnClickListener(view: View, actionId: Int) {
        view.setOnClickListener {
            val navController = findNavController(view)
            navController.navigate(actionId)
        }
    }

    fun log(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatAddress(address: Map<String, String>?, format: String): String {
        val city = address?.get("city") ?: ""
        val province = address?.get("province") ?: ""
        val sitio = address?.get("sitio") ?: ""
        val barangay = address?.get("barangay") ?: ""
        return when (format) {
            "full" -> {
                "$sitio $barangay, $city, $province"
            }
            "short" -> {
                "$city, $province"
            }
            else -> {
                "Invalid Format"
            }
        }
    }

    fun formatDecimalNumber(number: Double): String {
        return if (number % 1 == 0.0) {
            String.format("%.0f", number)
        } else {
            String.format("%.1f", number)
        }
    }

    private fun getCurrentDate(): Timestamp {
        val currentDate = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(Date(calendar.timeInMillis))
    }

    fun uploadImage(
        imageUri: Uri,
        path: String,
        callback: (String) -> Unit
    ) {
        val storageRef =
            storage.reference.child(path)

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                callback(imageUrl.toString())
            }.addOnFailureListener {
                callback("")
            }

        }.addOnFailureListener {
            callback("")
        }
    }

    fun getDateAndTime(): Timestamp {
        val currentDateTime = Calendar.getInstance().time
        return Timestamp(Date(currentDateTime.time))
    }

    fun dateFormatMMMDDYYYY(): String? {
        val currentDate = getCurrentDate().toDate() // Convert Timestamp to Date
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    fun dateFormatMMMDDYYYY(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun dateFormatMMMDDYYYY(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.format(calendar)
    }

    fun dateFormatMMMDDYYYY(date: Long): String {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun showAlertDialog(context: Context, title: String, message: String, positiveButton: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun observeNetwork(context: Context, lifecycleOwner: LifecycleOwner, callback: (Boolean) -> Unit) {
        val networkManager = NetworkManager(context)
        networkManager.observe(lifecycleOwner) { isNetworkAvailable ->
            callback(isNetworkAvailable)
        }
    }

    fun showLoader(context: Context, layoutInflater: LayoutInflater, show: Boolean) {
        if (loadingDialog == null) {
            val builder = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.loading, null)
            builder.setView(dialogView)
            loadingDialog = builder.create()
            loadingDialog!!.setCancelable(false)
        }

        if (show) {
            loadingDialog!!.show()
        } else {
            loadingDialog!!.dismiss()
        }
    }

    fun showAlertDialogWithCallback(
        fragment: Fragment,
        title: String,
        message: String,
        positiveButton: String,
        negativeButton: String,
        positiveButtonCallback: (() -> Unit)? = null,
        negativeButtonCallback: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                dialog.dismiss()
                positiveButtonCallback?.invoke()
            }
            .setNegativeButton(negativeButton) { dialog, _ ->
                dialog.dismiss()
                negativeButtonCallback?.invoke()
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun showAlertDialogWithCallback(
        fragment: Fragment,
        title: String,
        message: String,
        positiveButton: String,
        positiveButtonCallback: (() -> Unit)? = null,
    ) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                dialog.dismiss()
                positiveButtonCallback?.invoke()
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")
        return emailRegex.matches(email)
    }
}