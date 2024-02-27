package com.example.hygieiamerchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hygieiamerchant.databinding.ActivityDashboardBinding
import com.example.hygieiamerchant.utils.Communicator
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoggedInActivity : AppCompatActivity(), Communicator {
    private lateinit var binding: ActivityDashboardBinding
    var bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home,
//                R.id.navigation_history,
//                R.id.navigation_qrCode,
//                R.id.navigation_rewards,
//                R.id.navigation_promos
//            )
//        )
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun grantPointsQuantity(qty: Int?, actionType: String) {

        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)

        bundle = Bundle()
        bundle.putInt("quantity", qty ?: 0)
        bundle.putString("type", actionType)

        navController.navigate(R.id.to_qr_scan, bundle)
    }

    override fun redeemProduct(productID: String, actionType: String) {
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        bundle = Bundle()
        bundle.putString("productID", productID)
        bundle.putString("type", actionType)

        navController.navigate(R.id.to_qr_scan, bundle)
    }
}