package com.example.hygieiamerchant.ui.popups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.hygieiamerchant.Communicator
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.SharedViewModel
import com.example.hygieiamerchant.databinding.FragmentGrantPointsPopUpBinding
import com.google.android.material.textfield.TextInputEditText

class GrantPointsPopUp : DialogFragment() {
    private var _binding: FragmentGrantPointsPopUpBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private lateinit var cancelBTN: AppCompatButton
    private lateinit var submitBTN: AppCompatButton
    private lateinit var quantity: TextInputEditText

    private lateinit var communicator: Communicator

    private val sharedViewModel: SharedViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGrantPointsPopUpBinding.inflate(inflater, container, false)
        cancelBTN = binding.root.findViewById(R.id.cancel_btn)
        submitBTN = binding.root.findViewById(R.id.submit_btn)
        quantity = binding.root.findViewById(R.id.quantity)

        communicator = activity as Communicator

        try {
            setClickListeners()
        } catch (err: Exception) {
            Log.e("GRANT POINTS ERROR", err.toString())
        }

        return binding.root
    }

    private fun setClickListeners() {
        cancelBTN.setOnClickListener {
            dismiss() // Close the dialog
        }

        submitBTN.setOnClickListener {
            val inputText = quantity.text.toString()
            if (inputText.isNotBlank()) {
                communicator.grantPointsQuantity(quantity.text.toString().toInt(), "grant")
                dismiss() // Close the dialog
            } else {
                Toast.makeText(requireContext(), "Input is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}