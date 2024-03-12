package com.example.hygieiamerchant

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.hygieiamerchant.databinding.ActivityMain2Binding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.google.firebase.firestore.core.View

class MainActivity2 : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding
    private var dashboardViewModel: DashboardViewModel = DashboardViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val navigationView: NavigationView = findViewById(com.google.android.material.R.id.navigation_header_container)
//        val navigationView: NavigationView = findViewById(R.layout.nav_header_main)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        //Set nav header details
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(this) { details ->
            val view : android.view.View? = navView.getHeaderView(0)
            val storeName : TextView = view?.findViewById(R.id.shopName) ?: TextView(this)
            val storeEmail : TextView = view?.findViewById(R.id.shopEmail) ?: TextView(this)
            storeName.text = details.name
            storeEmail.text = details.email
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard,
                R.id.nav_transactions,
                R.id.nav_scan_qr_code,
                R.id.nav_rewards,
                R.id.nav_promos,
                R.id.nav_profile,
                R.id.nav_logout,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}