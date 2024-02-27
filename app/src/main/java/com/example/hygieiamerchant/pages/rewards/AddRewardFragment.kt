package com.example.hygieiamerchant.pages.rewards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentAddRewardBinding

class AddRewardFragment : Fragment() {
    private var _binding: FragmentAddRewardBinding? = null
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
        _binding = FragmentAddRewardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setPageTitle(root)
        dropDown(root)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPageTitle(root: View) {
        val customToolbar = root.findViewById<View>(R.id.header)
        val titleTextView: TextView = customToolbar.findViewById(R.id.titleTextView)
        titleTextView.text = "Add Product Reward"
    }

    private fun dropDown(root: View) {
        val spinnerData = arrayOf("Meals", "Snacks", "Beverages")

        val spinner: Spinner = root.findViewById(R.id.spinner)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

}