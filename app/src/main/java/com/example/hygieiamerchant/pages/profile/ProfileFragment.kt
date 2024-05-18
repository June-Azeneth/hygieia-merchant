package com.example.hygieiamerchant.pages.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hygieiamerchant.MainActivity
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentProfileBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.pages.transactions.TransactionDetailsDialog
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var storeName: TextView
    private lateinit var address: TextView
    private lateinit var email: TextView
    private lateinit var emailReset : String
    private lateinit var id: TextView
    private lateinit var editProfile: AppCompatButton
    private lateinit var changePass: AppCompatButton
    private lateinit var logout: AppCompatButton
    private lateinit var profile: ShapeableImageView
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        initializeVariables()
        populateFields()
        setUpOnClickListener()
        setUpRefreshListener()
        return binding.root
    }

    private fun setUpOnClickListener() {
        editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_editProfileFragment)
        }

        changePass.setOnClickListener {
            resetPassword(emailReset)
        }

        logout.setOnClickListener {
            logout()
        }

        binding.contactUs.setOnClickListener {
            val dialog = ContactUs(requireContext())
            dialog.show()
        }
    }

    private fun initializeVariables() {
        id = binding.id
        storeName = binding.storeName
        email = binding.email
        address = binding.address
        editProfile = binding.editProfile
        changePass = binding.changePass
        logout = binding.logout
        profile = binding.profile
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .setCancelable(true)
            .create()
    }

    private fun setUpRefreshListener() {
        Commons().setOnRefreshListener(binding.swipeRefreshLayout) {
            populateFields()
        }
    }

    private fun populateFields() {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { network ->
            if (network) {
                dashboardViewModel.fetchUserInfo()
                dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
                    if (details != null) {
                        emailReset = details.email
                        id.text = getString(R.string.profile_id, details.storeId)
                        storeName.text = details.name
                        email.text = "Email: ${details.email}"
                        address.text = "Address: ${details.address}"
                        Glide.with(this)
                            .load(details.photo)
                            .into(profile)

                        val recyclablesString = details.recyclable.joinToString(", ")
                        binding.recyclables.text = "Recyclables: $recyclablesString"
                    }
                }
            } else {
                showConnectivityDialog()
            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun resetPassword(email: String) {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { network ->
            if (network) {
                Commons().log("RESET PASS", email)
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Commons().showAlertDialogWithCallback(this,
                            "Password Reset Email Sent",
                            "A reset password email has been sent to your email address. \n\nWould you like to logout? Or keep you logged in?",
                            "Keep me logged in",
                            "Logout",
                            positiveButtonCallback = {},
                            negativeButtonCallback = {
                                logout()
                            })
                    } else {
                        Commons().showAlertDialog(
                            requireContext(),
                            "Reset Failed",
                            "Something went wrong. Try again later.",
                            "Okay"
                        )
                    }
                }.addOnFailureListener { error ->
                    Commons().showAlertDialog(
                        requireContext(), "Reset Failed", "An error occurred: $error", "Okay"
                    )
                }
            } else {
                showConnectivityDialog()
            }
        }
    }

    private fun logout() {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { network ->
            if (network) {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                showConnectivityDialog()
            }
        }
    }
}