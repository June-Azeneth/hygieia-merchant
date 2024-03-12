package com.example.hygieiamerchant.pages.scanQR.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.databinding.FragmentRedeemRewardBinding
import com.example.hygieiamerchant.pages.promos.PromosViewModel
import com.example.hygieiamerchant.pages.rewards.RewardsViewModel
import com.example.hygieiamerchant.pages.scanQR.ScanQrCodeViewModel
import com.example.hygieiamerchant.repository.TransactionRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons

class RedeemFragment : Fragment() {
    private var _binding: FragmentRedeemRewardBinding? = null
    private val binding get() = _binding!!
    private val scanQrCodeViewModel: ScanQrCodeViewModel by activityViewModels()
    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private val promosViewModel: PromosViewModel by activityViewModels()
    private val userRepo: UserRepo = UserRepo()
    private val commons: Commons = Commons()
    private lateinit var customerId: String
    private lateinit var rewardList: ArrayList<Reward>
    private lateinit var promoList: ArrayList<Promo>
    private lateinit var recyclerViewAdapter: RedeemRewardAdapter
    private lateinit var recyclerRedeemViewAdapter: RedeemPromoAdapter
    private var id: String = ""
    private var promoName: String = ""
    private var discount: Double = 0.0
    private var discountedPrice: Double = 0.0
    private var pointsRequired: Double = 0.0
    private var product: String = ""
    private var option: String = "reward"

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

        binding.reward.setOnClickListener {
            option = "reward"
            changeTabs(option)
            id = ""
        }

        binding.promo.setOnClickListener {
            option = "promo"
            changeTabs(option)
            id = ""
        }
    }

    private fun changeTabs(tab: String) {
        if (tab == "reward") {
            binding.promo.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            binding.reward.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.simple_button
                )
            )
            binding.rewardsList.visibility = View.VISIBLE
            binding.promoList.visibility = View.GONE
        } else {
            binding.reward.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            binding.promo.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.simple_button
                )
            )
            binding.rewardsList.visibility = View.GONE
            binding.promoList.visibility = View.VISIBLE
        }
    }

    private fun createTransaction() {
        if (validate()) {
            if (option == "reward") {
                val data = Transaction(
                    addedOn = commons.getDateAndTime().toDate(),
                    customerId = customerId,
                    storeId = userRepo.getCurrentUserId().toString(),
                    rewardId = id,
                    total = discountedPrice,
                    discount = discount,
                    pointsRequired = pointsRequired,
                    product = product
                )

                scanQrCodeViewModel.createRewardTransaction(data) { (success, message) ->
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
                                //DO NOTHING
                            }
                        )
                        commons.showLoader(
                            requireContext(),
                            LayoutInflater.from(requireContext()),
                            false
                        )
                    }
                }
            } else {
                val data = Transaction(
                    addedOn = commons.getDateAndTime().toDate(),
                    customerId = customerId,
                    storeId = userRepo.getCurrentUserId().toString(),
                    promoId = id,
                    total = discountedPrice,
                    discount = discount,
                    pointsRequired = pointsRequired,
                    product = product,
                    promoName = promoName
                )

                scanQrCodeViewModel.createPromoTransaction(data) { (success, message) ->
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
                                //DO NOTHING
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
    }

    private fun validate(): Boolean {
        if (id.isEmpty()) {
            commons.showAlertDialogWithCallback(
                this,
                "Oops!",
                "Please select an item first",
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
                    id = item.id
                    discount = item.discount
                    discountedPrice = item.discountedPrice
                    pointsRequired = item.pointsRequired
                    product = item.name
                }
            }
        )
        recyclerView.adapter = recyclerViewAdapter
        rewardsViewModel.fetchAllRewards("All")

        val promoListRecyclerView = binding.promoList
        promoListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        promoListRecyclerView.setHasFixedSize(true)

        promoList = arrayListOf()
        recyclerRedeemViewAdapter = RedeemPromoAdapter(
            promoList,
            object : RedeemPromoAdapter.OnItemClickListener {
                override fun onItemClick(item: Promo) {
                    id = item.id
                    discount = item.discountRate
                    discountedPrice = item.discountedPrice
                    pointsRequired = item.pointsRequired
                    product = item.product
                    promoName = item.promoName
                }
            }
        )
        promoListRecyclerView.adapter = recyclerRedeemViewAdapter
        promosViewModel.fetchOngoingPromos()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeRewardDetails() {
        rewardsViewModel.rewardDetails.observe(viewLifecycleOwner) { rewards ->
            if (rewards != null) {
                rewardList.clear()
                rewardList.addAll(rewards)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }

        promosViewModel.promoDetails.observe(viewLifecycleOwner) { promos ->
            if (promos != null) {
                promoList.clear()
                promoList.addAll(promos)
                recyclerRedeemViewAdapter.notifyDataSetChanged()
            }
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