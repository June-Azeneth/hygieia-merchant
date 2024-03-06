package com.example.hygieiamerchant.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.content.ContextCompat

fun Context.isCameraPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

//fun Context.isGalleryPermissionGranted(permission: String): Boolean {
//    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
//}

inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Camera Permission Required")
        .setMessage("Grant camera permission to scan QR Codes")
        .setPositiveButton("Allow Camera") { _, _ ->
            positive.invoke()
        }.setNegativeButton("Cancel") { _, _ ->
        }.show()
}

//inline fun Context.galleryPermissionRequest(crossinline positive: () -> Unit){
//    AlertDialog.Builder(this)
//        .setTitle("Gallery Permission Required")
//        .setMessage("Grant permission to Hygieia to access your device's media files.")
//        .setPositiveButton("Allow Access") { _, _ ->
//            positive.invoke()
//        }.setNegativeButton("Cancel") { _, _ ->
//        }.show()
//}

fun Context.openPermissionSetting() {
    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
        val uri: Uri = Uri.fromParts("package", packageName, null)
        it.data = uri
        startActivity(it)
    }
}