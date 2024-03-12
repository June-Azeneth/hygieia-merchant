package com.example.hygieiamerchant.pages.scanQR.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.databinding.FragmentGrantPointsBinding
import com.example.hygieiamerchant.pages.scanQR.ScanQrCodeViewModel
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons

class GrantPointsFragment : Fragment() {
    private var _binding: FragmentGrantPointsBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()
    private val userRepo: UserRepo = UserRepo()
    private val commons: Commons = Commons()
    private lateinit var customerId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGrantPointsBinding.inflate(inflater, container, false)

        setUpUi()
        setUpOnClickListeners()

        return binding.root
    }

    private fun setUpOnClickListeners() {
        binding.submitForm.setOnClickListener {
            createTransaction()
        }

        binding.cancel.setOnClickListener {
            scanQrCodeViewModel.clearCustomerData()
            scanQrCodeViewModel.setSelectedAction("cancel")
        }
    }

    private fun createTransaction() {
        if (validate()) {
            val data = Transaction(
                addedOn = Commons().getDateAndTime().toDate(),
                customerId = customerId,
                storeId = userRepo.getCurrentUserId().toString(),
                pointsGranted = binding.quantity.text.toString().toDouble(),
                rewardId = ""
            )

            scanQrCodeViewModel.createGrantTransaction(data) { (success, message) ->
                if (success) {
                    commons.showLoader(
                        requireContext(),
                        LayoutInflater.from(requireContext()),
                        false
                    )
                    commons.showAlertDialogWithCallback(
                        this,
                        "Success",
                        message,
                        "Okay",
                        positiveButtonCallback = {
                            scanQrCodeViewModel.setSelectedAction("success")
                        }
                    )
                } else {
                    commons.showAlertDialogWithCallback(
                        this,
                        "Failed",
                        message,
                        "Okay",
                        positiveButtonCallback = {
                            scanQrCodeViewModel.setSelectedAction("fail")
                        }
                    )
                    commons.showLoader(
                        requireContext(),
                        LayoutInflater.from(requireContext()),
                        false
                    )
                }
            }
        }
    }

    private fun validate(): Boolean {
        val quantity: String = binding.quantity.text.toString()
        val quantityCont = binding.quantityCont
        return if (quantity.isEmpty()) {
            quantityCont.error = "Required"
            false
        } else {
            quantityCont.isErrorEnabled = false
            commons.showLoader(
                requireContext(),
                LayoutInflater.from(requireContext()),
                true
            )
            true
        }
    }

    private fun setUpUi() {
        scanQrCodeViewModel.customerData.observe(viewLifecycleOwner) { customer ->
            if (customer != null) {
                binding.customerId.text = getString(R.string.id_id, customer.id)
                binding.customerName.text = getString(R.string.name, customer.name)
                binding.currentBalance.text =
                    getString(R.string.balance, customer.currentBalance.toString())
                customerId = customer.id
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanQrCodeViewModel.clearCustomerData()
        _binding = null
    }
}