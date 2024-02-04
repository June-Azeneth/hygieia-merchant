package com.example.hygieiamerchant

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.databinding.FragmentLoginBinding
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginFragment : Fragment() {
    val TAG = "LOGIN"
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var intent: Intent
    private lateinit var auth: FirebaseAuth;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        val loginBtn: AppCompatButton = binding.root.findViewById(R.id.loginBtn)
        val forgotPass: TextView = binding.root.findViewById(R.id.to_forgot_pass)

        try {
            loginBtn.setOnClickListener {
                loginUser()
            }

            forgotPass.setOnClickListener {
                findNavController().navigate(R.id.action_landingPageFragment_to_loginFragment)
            }
        } catch (error: Exception) {
            error.printStackTrace()
        }
        return binding.root
    }

    private fun loginUser() {
        val email = binding.email.text.toString()
        val pass = binding.password.text.toString()

        try {
            when {
                email.isEmpty() -> {
                    Commons().showToast("Please provide an email", requireContext())
                }

                pass.isEmpty() -> {
                    Commons().showToast("Please provide a password", requireContext())
                }

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Commons().showToast("Please provide a valid email", requireContext())
                }

                else -> {
//                    text.visibility = INVISIBLE
//                    loader.visibility = VISIBLE

                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(requireActivity()) { task ->
//                            text.visibility = VISIBLE
//                            loader.visibility = INVISIBLE

                            if (task.isSuccessful) {
                                val intent =
                                    Intent(requireView().context, Dashboard::class.java)
                                startActivity(intent)
                                binding.email.text = Editable.Factory.getInstance().newEditable("")
                                binding.password.text =
                                    Editable.Factory.getInstance().newEditable("")
                            } else {
                                // If sign in fails, handle the specific exception
                                // Handle different exception cases
                                when (task.exception) {
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        Commons().showToast("Invalid email or password", requireContext())
                                    }

                                    else -> {
                                        Commons().showToast("Authentication failed.", requireContext())
                                    }
                                }
                            }
                        }
                }
            }
        } catch (error: Exception) {
            Log.d(TAG,error.toString())
        }
    }
}