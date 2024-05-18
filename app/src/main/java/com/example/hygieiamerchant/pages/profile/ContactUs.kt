package com.example.hygieiamerchant.pages.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.repository.TransactionRepo

class ContactUs(
    context: Context,
): Dialog(context){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_uc_layout)
    }
}