package com.example.hygieiamerchant.pages.scanQR.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hygieiamerchant.databinding.FragmentOperationOptionsBinding
import com.example.hygieiamerchant.pages.scanQR.ScanQrCodeViewModel
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.QRCodeScan
import com.example.hygieiamerchant.utils.cameraPermissionRequest
import com.example.hygieiamerchant.utils.isCameraPermissionGranted
import com.example.hygieiamerchant.utils.openPermissionSetting

class OperationOptionsFragment : Fragment() {
    private var _binding: FragmentOperationOptionsBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()
    private val cameraPermission = android.Manifest.permission.CAMERA

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                //DO NOTHING
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOperationOptionsBinding.inflate(inflater, container, false)
        requestCameraAndStartScanner()
        setUpOnClickListeners()
        return binding.root
    }

    private fun setUpOnClickListeners() {
        binding.grant.setOnClickListener {
            startScanner { customerId ->
                if (customerId.contains('/')) {
                    Commons().showToast("Invalid QR Code", requireContext())
                } else {
                    scanQrCodeViewModel.fetchCustomerDetails(customerId) { success, errorMessage ->
                        if (success) {
                            scanQrCodeViewModel.setSelectedAction("grant")
                        } else {
                            Commons().showToast(errorMessage, requireContext())
                        }
                    }
                }
            }
        }

        binding.redeem.setOnClickListener {
            startScanner { customerId ->
                if (customerId.contains('/')) {
                    Commons().showToast("Invalid QR Code", requireContext())
                } else {
                    scanQrCodeViewModel.fetchCustomerDetails(customerId) { success, errorMessage ->
                        if (success) {
                            scanQrCodeViewModel.setSelectedAction("redeem")
                        } else {
                            Commons().showToast(errorMessage, requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun startScanner(callback: (String) -> Unit) {
        QRCodeScan.startScanner(requireContext()) { barcodes ->
            val barcodeData = barcodes.firstOrNull()?.rawValue ?: ""
            callback(barcodeData)
        }
    }

    private fun requestCameraAndStartScanner() {
        if (requireContext().isCameraPermissionGranted(cameraPermission)) {
            //DO NOTHING
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                requireContext().cameraPermissionRequest {
                    requireContext().openPermissionSetting()
                }
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }
}