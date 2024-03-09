package com.example.hygieiamerchant.pages.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentHomeBinding
import com.example.hygieiamerchant.utils.Commons

class DashboardFragment : Fragment() {

    private val logTag = "Dashboard"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private val commons: Commons = Commons()
    lateinit var type: Array<String>
    lateinit var date: Array<String>
    lateinit var amount: Array<String>

    private lateinit var storeName: TextView
    private lateinit var storeAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setOnClickListeners()
        observeDataChange()
        loadUi()
        initializeVariables()

        return binding.root
    }

    private fun initializeVariables() {
        storeName = binding.shopName
        storeAddress = binding.address
    }

    private fun loadUi() {
        dashboardViewModel.fetchUserInfo()
    }

    private fun onRefresh() {
//        commons.setOnRefreshListener()
    }

    private fun observeDataChange() {
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            storeName.text = details.name
            storeAddress.text = commons.formatAddress(details.address,"short")
        }
    }

    private fun setOnClickListeners() {
        setNavigationOnClickListener(R.id.requestPickUp, R.id.action_to_pickup_request_list)
    }

    private fun setNavigationOnClickListener(viewId: Int, actionId: Int) {
        val view: ImageView = binding.root.findViewById(viewId)
        view.setOnClickListener {
            findNavController().navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
