package com.example.hygieiamerchant.utils.popups

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.hygieiamerchant.utils.Communicator
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.utils.SharedViewModel
import com.example.hygieiamerchant.databinding.FragmentRedeemProductPopUpBinding
import com.google.firebase.firestore.FirebaseFirestore

class RedeemProductPopUp : DialogFragment() {
    private var _binding: FragmentRedeemProductPopUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var prodId: String
    private lateinit var radioButton : RadioButton

    private lateinit var communicator: Communicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRedeemProductPopUpBinding.inflate(inflater, container, false)

        communicator = activity as Communicator

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val rewardsCollectionRef = FirebaseFirestore.getInstance().collection("rewards")

            rewardsCollectionRef.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        prodId = document.getString("id") ?: ""
                        val product = document.getString("product_name") ?: ""
                        val discount = document.getDouble("discount_rate") ?: 0.0

                        addRadioButton(product, discount.toInt(), prodId)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors here
                    Log.e("RedeemProduct", "Error fetching rewards collection: $exception")
                }

            binding.submitBTN.setOnClickListener {
                //TO DO: validate if the user selected an item, if not display appropriate message.
                dismiss()
            }

            binding.cancelBTN.setOnClickListener {
                dismiss()
            }
        } catch (error: RuntimeException) {
            Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
            Log.e("ERRORREDEEM", error.toString())
        }
    }

    private fun addRadioButton(product: String, percentage: Int, prodId: String) {
        radioButton = RadioButton(requireContext())
        radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text))

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_regular)
        radioButton.typeface = customTypeface

        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main_green))
        CompoundButtonCompat.setButtonTintList(radioButton, colorStateList)

        radioButton.text = "$product  $percentage%"

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        layoutParams.topMargin =
            radioButton.context.resources.getDimensionPixelSize(R.dimen.tiniest)

        binding.contentLayout.addView(radioButton, layoutParams)

        radioButton.id = prodId.hashCode()
        radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                communicator.redeemProduct(prodId, "redeem")
                Log.e("REDEEM", prodId)
            }
        }
    }
}