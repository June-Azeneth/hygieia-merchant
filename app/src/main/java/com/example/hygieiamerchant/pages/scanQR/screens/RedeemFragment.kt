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
    private lateinit var recyclerViewRewardAdapter: RedeemRewardAdapter
    private lateinit var recyclerViewPromoAdapter: RedeemPromoAdapter
    private var id: String = ""
    private var promoName: String = ""
    private var discount: Double = 0.0
    private var total: Double = 0.0
    private var totalPointsSpent: Double = 0.0
    private var discountedPrice: Double = 0.0
    private var pointsRequired: Double = 0.0
    private var product: String = ""
    private var option: String = "reward"
    private lateinit var products: Map<String, Double>
    private var isProductsSelected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
            binding.duePrice.text = "Due: ₱0.0"
            recyclerViewRewardAdapter.clearSelection()

            binding.promo.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.lightGray
                )
            )
            binding.promo.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_color
                )
            )
            binding.reward.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.color.sub_text
                )
            )
            binding.reward.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            binding.rewardsList.visibility = View.VISIBLE
            binding.promoList.visibility = View.GONE
        } else {
            binding.duePrice.text = "Due: ₱0.0"
            recyclerViewPromoAdapter.clearSelection()

            binding.promo.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.sub_text
                )
            )
            binding.promo.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            binding.reward.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.color.lightGray
                )
            )
            binding.reward.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_color
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
                    total = total,
                    totalPointsSpent = totalPointsSpent,
                )

                val selectedRewards = recyclerViewRewardAdapter.getSelectedItems()
                val redeemedRewardsData = hashMapOf<String, Any>()

                selectedRewards.forEachIndexed { index, reward ->
                    redeemedRewardsData["product${index + 1}"] = hashMapOf(
                        "name" to reward.name,
                        "discount" to reward.discount,
                        "discountedPrice" to reward.discountedPrice,
                        "pointsRequired" to reward.pointsRequired
                    )
                }

                scanQrCodeViewModel.createRewardTransaction(
                    data, redeemedRewardsData
                ) { (success, message) ->
                    commons.showLoader(
                        requireContext(), LayoutInflater.from(requireContext()), false
                    )
                    if (success) {
                        commons.showAlertDialogWithCallback(this,
                            "Success",
                            message,
                            "Okay",
                            positiveButtonCallback = {
                                scanQrCodeViewModel.setSelectedAction("success")
                            })
                    } else {
                        commons.showAlertDialogWithCallback(this,
                            "Failed",
                            message,
                            "Okay",
                            positiveButtonCallback = {
                                //DO NOTHING
                            })
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
                            requireContext(), LayoutInflater.from(requireContext()), false
                        )
                        commons.showAlertDialogWithCallback(this,
                            "Success",
                            message,
                            "Okay",
                            positiveButtonCallback = {
                                scanQrCodeViewModel.setSelectedAction("success")
                            })
                    } else {
                        commons.showAlertDialogWithCallback(this,
                            "Failed",
                            message,
                            "Okay",
                            positiveButtonCallback = {
                                //DO NOTHING
                            })
                        commons.showLoader(
                            requireContext(), LayoutInflater.from(requireContext()), false
                        )
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        if (isProductsSelected) {
            commons.showAlertDialogWithCallback(this,
                "Oops!",
                "Please select an item first",
                "Okay",
                positiveButtonCallback = {
                    //PASS NOTHING HERE
                })
            return false
        } else {
            commons.showLoader(
                requireContext(), LayoutInflater.from(requireContext()), true
            )
            return true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpRecyclerView() {
        val recyclerView = binding.rewardsList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        rewardList = arrayListOf()
        recyclerViewRewardAdapter =
            RedeemRewardAdapter(rewardList, object : RedeemRewardAdapter.OnItemClickListener {
                override fun onItemClick(item: Reward) {
                    val selectedRewards = recyclerViewRewardAdapter.getSelectedItems()
                    isProductsSelected = selectedRewards.isEmpty()

                    var totalAmountDue = 0.0
                    var totalPoints = 0.0
                    for (reward in selectedRewards) {
                        totalAmountDue += reward.discountedPrice
                        totalPoints += reward.pointsRequired
                    }

                    total = totalAmountDue
                    totalPointsSpent = totalPoints
                    binding.duePrice.text = "Total Price: ₱${totalAmountDue}"
                    binding.duePoints.text = "Total Points: ₱${totalPoints}"
                }
            })
        recyclerView.adapter = recyclerViewRewardAdapter
        rewardsViewModel.fetchAllRewards("All")

        val promoListRecyclerView = binding.promoList
        promoListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        promoListRecyclerView.setHasFixedSize(true)

        promoList = arrayListOf()
        recyclerViewPromoAdapter =
            RedeemPromoAdapter(promoList, object : RedeemPromoAdapter.OnItemClickListener {
                override fun onItemClick(item: Promo) {
                    id = item.id
                    discount = item.discountRate
                    discountedPrice = item.discountedPrice
                    pointsRequired = item.pointsRequired
                    product = item.product
                    promoName = item.promoName
                    binding.duePrice.text = "Due: ₱${item.discountedPrice}"
                    binding.duePoints.text = "Total Points: ₱${pointsRequired}"
                }
            })
        promoListRecyclerView.adapter = recyclerViewPromoAdapter
        promosViewModel.fetchOngoingPromos()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeRewardDetails() {
        rewardsViewModel.rewardDetails.observe(viewLifecycleOwner) { rewards ->
            if (rewards != null) {
                rewardList.clear()
                rewardList.addAll(rewards)
                recyclerViewRewardAdapter.notifyDataSetChanged()
            }
        }

        promosViewModel.promoDetails.observe(viewLifecycleOwner) { promos ->
            if (promos != null) {
                promoList.clear()
                promoList.addAll(promos)
                recyclerViewPromoAdapter.notifyDataSetChanged()
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