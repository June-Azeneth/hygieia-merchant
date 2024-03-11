package com.example.hygieiamerchant.pages.scanQR

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentScanQRCodeBinding
import com.example.hygieiamerchant.pages.scanQR.screens.GrantPointsFragment
import com.example.hygieiamerchant.pages.scanQR.screens.OperationOptionsFragment
import com.example.hygieiamerchant.pages.scanQR.screens.RedeemRewardFragment

class ScanQRCodeFragment : Fragment() {
    private var _binding: FragmentScanQRCodeBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanQRCodeBinding.inflate(inflater, container, false)
        switchTabsManager(OperationOptionsFragment::class.java)
        observeAction()
        return binding.root
    }

    private fun observeAction() {
        scanQrCodeViewModel.action.observe(viewLifecycleOwner) { action ->
            when (action) {
                "grant" -> {
                    switchTabsManager(GrantPointsFragment::class.java)
                }
                "redeem" -> {
                    switchTabsManager(RedeemRewardFragment::class.java)
                }
                else -> {
                    switchTabsManager(OperationOptionsFragment::class.java)
                }
            }
        }
    }

    private fun switchTabsManager(fragmentClass: Class<*>) {
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()

        val fragment = fragmentClass.getDeclaredConstructor().newInstance() as Fragment

        transaction.replace(R.id.host, fragment)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanQrCodeViewModel.setSelectedAction("close")
        scanQrCodeViewModel.clearCustomerData()
        _binding = null
    }
}