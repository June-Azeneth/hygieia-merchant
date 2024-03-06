package com.example.hygieiamerchant.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hygieiamerchant.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import java.util.Date
import java.util.Locale

class Commons {
    fun setPageTitle(title: String, root: View) {
        val customToolbar = root.findViewById<View>(R.id.header)
        val titleTextView: TextView = customToolbar.findViewById(R.id.titleTextView)
        titleTextView.text = title
    }

    fun setToolbarIcon(icon: Int, root: View) {
        val customToolbar = root.findViewById<View>(R.id.header)
        val iconRef: ShapeableImageView = customToolbar.findViewById(R.id.icon)
        iconRef.setImageResource(icon)
    }

    fun setToolBarIconAction(root: View) {
//        val customToolbar = root.findViewById<View>(R.id.header)
//        val iconRef: ShapeableImageView = customToolbar.findViewById(R.id.icon)
    }

    fun setOnRefreshListener(refreshLayout: SwipeRefreshLayout, refreshAction: () -> Unit) {
        refreshLayout.setOnRefreshListener {
            refreshAction.invoke()
            refreshLayout.isRefreshing = false
        }
    }

    fun setNavigationOnClickListener(view: View, actionId: Int) {
        view.setOnClickListener {
            val navController = Navigation.findNavController(view)
            navController.navigate(actionId)
        }
    }

    fun log(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getCurrentDate(): Timestamp {
        val currentDate = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(Date(calendar.timeInMillis))
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

    fun formatDateMMMDDYYYY(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun showAlertDialog(context: Context, title: String, message: String, positiveButton : String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
}