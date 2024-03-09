package com.example.hygieiamerchant.pages.password_reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentSetNewPassBinding
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentSetNewPassBinding? = null
    private val binding get() = _binding!!

    private lateinit var email: TextInputEditText
    private lateinit var submit: AppCompatButton
    private lateinit var emailString: String
    private val commons: Commons = Commons()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetNewPassBinding.inflate(inflater, container, false)

        initializeVariable()
        setupOnClickListener()

        return binding.root
    }

    private fun initializeVariable() {
        email = binding.email
        submit = binding.submit
    }


    private fun setupOnClickListener() {
        binding.toLogin.setOnClickListener {
            findNavController().navigate(R.id.action_setNewPassFragment_to_loginFragment)
        }

        submit.setOnClickListener {
            emailString = email.text.toString().trim()
            if (emailString.isNotEmpty()) {
                resetPassword(emailString)
            } else {
                commons.showToast("Email field can not be empty", requireContext())
            }
        }
    }

    private fun resetPassword(email: String) {
        if (!commons.validateEmail(emailString)) {
            commons.showToast("Invalid email", requireContext())
        } else {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        commons.showAlertDialog(
                            requireContext(),
                            "Reset Successful",
                            "A reset password email has been sent to your email address.",
                            "Okay"
                        )
                    } else {
                        commons.showAlertDialog(
                            requireContext(),
                            "Reset Failed",
                            "Something went wrong. Try again later.",
                            "Okay"
                        )
                    }
                }
                .addOnFailureListener {error ->
                    commons.showAlertDialog(
                        requireContext(),
                        "Reset Failed",
                        "An error occurred: $error",
                        "Okay"
                    )
                }
        }
    }
}