package com.example.hygieiamerchant.pages.scanQR.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.databinding.FragmentRedeemRewardBinding
import com.example.hygieiamerchant.pages.rewards.RewardsViewModel
import com.example.hygieiamerchant.pages.scanQR.ScanQrCodeViewModel
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons

class RedeemRewardFragment : Fragment() {
    private var _binding: FragmentRedeemRewardBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()
    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private val userRepo: UserRepo = UserRepo()
    private val commons: Commons = Commons()
    private lateinit var customerId: String
    private lateinit var rewardList: ArrayList<Reward>
    private lateinit var recyclerViewAdapter: RedeemRewardAdapter
    private var rewardId: String = ""
    private var discount: Double = 0.0
    private var discountedPrice: Double = 0.0
    private var pointsRequired: Double = 0.0
    private var product: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRedeemRewardBinding.inflate(inflater, container, false)

        setUpUi()
        setUpOnClickListeners()
        setUpRecyclerView()
        observeRewardDetails()

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
                rewardId = rewardId,
                total = discountedPrice,
                discount = discount,
                pointsRequired = pointsRequired,
                product = product
            )

            scanQrCodeViewModel.createTransaction(data, "redeem") { (success, message) ->
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
//                            scanQrCodeViewModel.setSelectedAction("fail")
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
        if (rewardId.isEmpty()) {
            commons.showAlertDialogWithCallback(
                this,
                "",
                "Please select a reward first",
                "Okay",
                positiveButtonCallback = {
                    //PASS NOTHING HERE
                }
            )
            return false
        } else {
            commons.showLoader(
                requireContext(),
                LayoutInflater.from(requireContext()),
                true
            )
            return true
        }
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.rewardsList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        rewardList = arrayListOf()
        recyclerViewAdapter = RedeemRewardAdapter(
            rewardList,
            object : RedeemRewardAdapter.OnItemClickListener {
                override fun onItemClick(item: Reward) {
                    rewardId = item.id
                    discount = item.discount
                    discountedPrice = item.discountedPrice
                    pointsRequired = item.pointsRequired
                    product = item.name
                }
            }
        )

        recyclerView.adapter = recyclerViewAdapter
        rewardsViewModel.fetchAllRewards("All")
    }

    private fun observeRewardDetails() {
        rewardsViewModel.rewardDetails.observe(viewLifecycleOwner) { rewards ->

            if (rewards != null) {
                clearRewardList()
                rewardList.addAll(rewards)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun clearRewardList() {
        rewardList.clear()
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