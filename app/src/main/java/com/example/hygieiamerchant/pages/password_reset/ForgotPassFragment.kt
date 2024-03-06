package com.example.hygieiamerchant.pages.password_reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hygieiamerchant.databinding.FragmentForgotPassBinding

class ForgotPassFragment : Fragment() {
    private var _binding: FragmentForgotPassBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPassBinding.inflate(inflater, container, false)


        return binding.root
    }

    private fun validateEmail(){

    }

    private fun sendChangePassEmail(){

    }
}