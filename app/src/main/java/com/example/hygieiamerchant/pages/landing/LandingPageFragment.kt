package com.example.hygieiamerchant.pages.landing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentLandingPageBinding
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LandingPageFragment : Fragment() {
    private lateinit var dialog : AlertDialog
    private lateinit var progressBar: ProgressBar
    private lateinit var getStartedLayout: RelativeLayout
    private lateinit var getStartedText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        progressBar = binding.root.findViewById(R.id.progressBar)
        getStartedLayout = binding.root.findViewById(R.id.get_started)
        getStartedText = binding.root.findViewById(R.id.getStartedText)

        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .create()

        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner){
            if(!it){
                showLoader(true)
                if(!dialog.isShowing)
                    dialog.show()

                getStartedLayout.isClickable = false
            }
            else{
                showLoader(false)
                if(dialog.isShowing)
                    dialog.hide()

                getStartedLayout.setOnClickListener {
                    findNavController().navigate(R.id.action_landingPageFragment_to_loginFragment)
                }
            }
        }
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

}