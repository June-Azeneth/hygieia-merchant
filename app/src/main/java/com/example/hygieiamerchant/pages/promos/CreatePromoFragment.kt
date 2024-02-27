package com.example.hygieiamerchant.pages.promos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.databinding.FragmentCreatePromoBinding

class CreatePromoFragment : Fragment() {

    private var _binding: FragmentCreatePromoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreatePromoBinding.inflate(inflater, container, false)

        Commons().setPageTitle("Create Promo", binding.root)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}