package com.example.hygieiamerchant.pages.password_reset

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentSetNewPassBinding

class SetNewPassFragment : Fragment() {

    private var _binding: FragmentSetNewPassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetNewPassBinding.inflate(inflater, container, false)

        binding.toLogin.setOnClickListener{
            findNavController().navigate(R.id.action_setNewPassFragment_to_loginFragment)
        }

        return binding.root
    }
}