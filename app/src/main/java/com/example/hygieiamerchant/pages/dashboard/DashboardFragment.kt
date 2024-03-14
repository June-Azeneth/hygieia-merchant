package com.example.hygieiamerchant.pages.dashboard

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentHomeBinding
import com.example.hygieiamerchant.repository.RewardRepo
import com.example.hygieiamerchant.utils.Commons

class DashboardFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private var rewardsRepo : RewardRepo = RewardRepo()
    private val commons: Commons = Commons()
    lateinit var type: Array<String>
    lateinit var date: Array<String>

    private lateinit var storeName: TextView
    private lateinit var storeAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initializeVariables()
        setUpRefreshListener()
        observeDataChange()
        loadUi()
        setUpRefreshListener()
        setUpRewardTable()
        setUpPromoTable()

        return binding.root
    }

    private fun initializeVariables() {
        storeName = binding.shopName
        storeAddress = binding.address
    }

    private fun setUpRefreshListener() {
        commons.setOnRefreshListener(binding.swipeRefreshLayout){
            observeDataChange()
            setUpRewardTable()
            setUpPromoTable()
        }
    }

    private fun setUpRewardTable(){
        rewardsRepo.calculateItemsSoldPerReward(object : RewardRepo.ItemsSoldCallback {
            override fun onItemsSoldPerReward(itemsSoldPerReward: Map<String, Int>) {
                val tableLayout = binding.tableLayout

                // Clear existing rows if any
                tableLayout.removeAllViews()

                // Add header row
                val headerRow = TableRow(requireContext())
                headerRow.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                // Create TextViews for header
                val productHeaderTextView = TextView(requireContext())
                productHeaderTextView.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                productHeaderTextView.text = "Product"
                productHeaderTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                productHeaderTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                productHeaderTextView.setPadding(0,16,0,16)
                productHeaderTextView.gravity = Gravity.CENTER
                headerRow.addView(productHeaderTextView)

                val itemsSoldHeaderTextView = TextView(requireContext())
                itemsSoldHeaderTextView.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemsSoldHeaderTextView.text = "Items Sold"
                itemsSoldHeaderTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                itemsSoldHeaderTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                itemsSoldHeaderTextView.setPadding(0,16,0,16)
                itemsSoldHeaderTextView.gravity = Gravity.CENTER
                headerRow.addView(itemsSoldHeaderTextView)

                // Add header row to the tableLayout
                tableLayout.addView(headerRow)

                // Create a new row for each reward and count
                for ((product, count) in itemsSoldPerReward) {
                    val row = TableRow(requireContext())
                    row.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )

                    // Create TextViews for reward and count
                    val rewardTextView = TextView(requireContext())
                    rewardTextView.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    rewardTextView.text = product
                    rewardTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                    rewardTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    rewardTextView.setPadding(5,6,0,6)
                    rewardTextView.gravity = Gravity.START
                    row.addView(rewardTextView)

                    val countTextView = TextView(requireContext())
                    countTextView.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    countTextView.text = count.toString()
                    countTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                    countTextView.gravity = Gravity.START
                    countTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    countTextView.setPadding(5,6,0,6)
                    row.addView(countTextView)

                    // Add the row to the tableLayout
                    tableLayout.addView(row)
                }
            }
        })
    }

    private fun setUpPromoTable(){
        rewardsRepo.calculateItemsSoldPerPromo(object : RewardRepo.PromosSoldCallback {
            override fun onItemsSoldPerPromo(itemsSoldPerReward: Map<String, Int>) {
                val tableLayout = binding.activePromos

                // Clear existing rows if any
                tableLayout.removeAllViews()

                // Add header row
                val headerRow = TableRow(requireContext())
                headerRow.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                // Create TextViews for header
                val productHeaderTextView = TextView(requireContext())
                productHeaderTextView.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                productHeaderTextView.text = "Product"
                productHeaderTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                productHeaderTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                productHeaderTextView.setPadding(0,16,0,16)
                productHeaderTextView.gravity = Gravity.CENTER
                headerRow.addView(productHeaderTextView)

                val itemsSoldHeaderTextView = TextView(requireContext())
                itemsSoldHeaderTextView.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                itemsSoldHeaderTextView.text = "Items Sold"
                itemsSoldHeaderTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                itemsSoldHeaderTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_orange))
                itemsSoldHeaderTextView.setPadding(0,16,0,16)
                itemsSoldHeaderTextView.gravity = Gravity.CENTER
                headerRow.addView(itemsSoldHeaderTextView)

                // Add header row to the tableLayout
                tableLayout.addView(headerRow)

                // Create a new row for each reward and count
                for ((product, count) in itemsSoldPerReward) {
                    val row = TableRow(requireContext())
                    row.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )

                    // Create TextViews for reward and count
                    val rewardTextView = TextView(requireContext())
                    rewardTextView.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    rewardTextView.text = product
                    rewardTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                    rewardTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    rewardTextView.setPadding(5,6,0,6)
                    rewardTextView.gravity = Gravity.START
                    row.addView(rewardTextView)

                    val countTextView = TextView(requireContext())
                    countTextView.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    countTextView.text = count.toString()
                    countTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                    countTextView.gravity = Gravity.START
                    countTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    countTextView.setPadding(5,6,0,6)
                    row.addView(countTextView)

                    // Add the row to the tableLayout
                    tableLayout.addView(row)
                }
            }
        })
    }

    private fun loadUi() {
        dashboardViewModel.fetchUserInfo()
    }

    private fun observeDataChange() {
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            storeName.text = details.name
            storeAddress.text = commons.formatAddress(details.address,"short")
        }
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
