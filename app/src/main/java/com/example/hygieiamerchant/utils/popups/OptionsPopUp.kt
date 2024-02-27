package com.example.hygieiamerchant.utils.popups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.hygieiamerchant.utils.Communicator
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.OptionsPopUpBinding

class OptionsPopUp : DialogFragment() {

    private var _binding: OptionsPopUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var grantPTS: AppCompatButton
    private lateinit var redeemProd: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private lateinit var communicator: Communicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = OptionsPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        grantPTS = binding.root.findViewById(R.id.grant_points)
        redeemProd = binding.root.findViewById(R.id.redeem)
        communicator = activity as Communicator
        val grantPTSDialog = GrantPointsPopUp()
        val redeemDialog = RedeemProductPopUp()

        try {
            grantPTS.setOnClickListener {
                grantPTSDialog.show(
                    (activity as AppCompatActivity).supportFragmentManager,
                    "showGrantPointsPopUp"
                )
                dismiss()
            }

            redeemProd.setOnClickListener {
                redeemDialog.show(
                    (activity as AppCompatActivity).supportFragmentManager,
                    "showRedeemProductPopUp"
                )
                dismiss()
            }


        } catch (err: Exception) {
            err.printStackTrace()
            Toast.makeText(requireContext(), err.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}