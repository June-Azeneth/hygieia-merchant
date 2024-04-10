package com.example.hygieiamerchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    //    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            var intent = Intent(this, MainActivity2::class.java)
//            startActivity(intent)
//        }
    }
}