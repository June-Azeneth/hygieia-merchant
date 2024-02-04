package com.example.hygieiamerchant.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        Commons().setPageTitle("Profile", root)
        Commons().setToolbarIcon(R.drawable.logout, root)
        return root
    }
}