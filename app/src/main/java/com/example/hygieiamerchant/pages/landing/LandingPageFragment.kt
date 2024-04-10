package com.example.hygieiamerchant.pages.landing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.MainActivity2
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentLandingPageBinding
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LandingPageFragment : Fragment() {
    private lateinit var dialog: AlertDialog
    private lateinit var progressBar: ProgressBar
    private lateinit var getStartedLayout: RelativeLayout
    private lateinit var getStartedText: TextView
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        initializeVariables()
        observeNetwork()

        return binding.root
    }

    private fun showLoader(show: Boolean) {
        if (show) {
            progressBar.visibility = ProgressBar.VISIBLE
            getStartedText.visibility = View.INVISIBLE
        } else {
            progressBar.visibility = ProgressBar.GONE
            getStartedText.visibility = View.VISIBLE
        }
    }

    private fun initializeVariables() {
        auth = Firebase.auth
        progressBar = binding.progressBar
        getStartedLayout = binding.getStarted
        getStartedText = binding.getStartedText
    }

    private fun isUserLoggedIn() {
        val currentUser = auth.currentUser
        showLoader(true)
        if (currentUser != null) {
            val intent = Intent(requireContext(), MainActivity2::class.java)
            startActivity(intent)
        } else {
            showLoader(false)
        }
    }

    private fun observeNetwork() {
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .create()
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) {
            if (!it) {
                showLoader(true)
                if (!dialog.isShowing)
                    dialog.show()
                getStartedLayout.isClickable = false
            } else {
                showLoader(false)
                if (dialog.isShowing)
                    dialog.hide()

                getStartedLayout.setOnClickListener {
                    findNavController().navigate(R.id.action_landingPageFragment_to_loginFragment)
                }

                isUserLoggedIn()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}