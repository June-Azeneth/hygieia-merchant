package com.example.hygieiamerchant.pages.scanQR.screens

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.UserInfo
import com.example.hygieiamerchant.databinding.FragmentOperationOptionsBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.pages.scanQR.ScanQrCodeViewModel
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.QRCodeScan
import com.example.hygieiamerchant.utils.cameraPermissionRequest
import com.example.hygieiamerchant.utils.isCameraPermissionGranted
import com.example.hygieiamerchant.utils.openPermissionSetting
import java.util.Date
import java.util.Locale

class OperationOptionsFragment : Fragment() {
    private var _binding: FragmentOperationOptionsBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()
    private val cameraPermission = android.Manifest.permission.CAMERA
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

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

        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                validateDate(user)
            }
        }

        requestCameraAndStartScanner()
        setUpOnClickListeners()
        return binding.root
    }

    private fun validateDate(user: UserInfo) {
        val currentDay = Commons().getDateAndTime()
        val date = Date(
            currentDay.seconds * 1000
        )
        val dayFormat =
            SimpleDateFormat("EEEE", Locale.getDefault()) // Format to get day name (full name)
        val dayToday = dayFormat.format(date)

        binding.message.text = getString(R.string.uh_oh_no_qr_code_scanning_allowed_on, dayToday)

        if (user.days.isNotEmpty()) {
            val currentDateInDb = user.days.any { it == dayToday }
            if (currentDateInDb) {
                showOptions(true)
            } else {
                showOptions(false)
            }
        }

        binding.proceed.setOnClickListener {
            showOptions(true)
        }

        binding.editSchedule.setOnClickListener {
            dashboardViewModel.setFromPage("scanOptions")
            findNavController().navigate(R.id.action_nav_scan_qr_code_to_editScehduleFragment)
        }
    }

    private fun showOptions(show: Boolean) {
        if (show) {
            binding.optionsPage.visibility = View.VISIBLE
            binding.warningPage.visibility = View.GONE
            binding.editSchedule.visibility = View.GONE
        } else {
            binding.optionsPage.visibility = View.GONE
            binding.warningPage.visibility = View.VISIBLE
            binding.editSchedule.visibility = View.VISIBLE
        }
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