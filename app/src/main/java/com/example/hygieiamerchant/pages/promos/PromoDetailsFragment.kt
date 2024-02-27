package com.example.hygieiamerchant.pages.promos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hygieiamerchant.databinding.FragmentPromoDetailsBinding

class PromoDetailsFragment : Fragment() {

    private var _binding: FragmentPromoDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPromoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
}