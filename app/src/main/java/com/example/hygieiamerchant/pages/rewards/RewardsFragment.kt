package com.example.hygieiamerchant.pages.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.databinding.FragmentRewardBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class RewardsFragment : Fragment() {
    private val logTag = "REWARD FRAGMENT"
    private var _binding: FragmentRewardBinding? = null
    private val binding get() = _binding!!
    private lateinit var rewardList: ArrayList<Reward>
    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private lateinit var recyclerViewAdapter : RewardsAdapter
    private lateinit var commons: Commons
    private var selectedCategory: String = ""
    private lateinit var dialog: AlertDialog
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkManager = NetworkManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRewardBinding.inflate(inflater, container, false)

        initializeVariables()
        dropDown(binding.root)
        observeNetwork()
        commonActions()

        return binding.root
    }

    private fun initializeVariables() {
        commons = Commons()
        rewardList = ArrayList()
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .setCancelable(true)
            .create()
    }

    private fun commonActions() {
        commons.setNavigationOnClickListener(binding.addReward, R.id.rewardsTab_to_addReward)

        binding.addReward.setOnClickListener{
            rewardsViewModel.setAction("create")
            findNavController().navigate(R.id.rewardsTab_to_addReward)
        }

        commons.setOnRefreshListener(binding.swipeRefreshLayout) {
            observeNetwork()
        }
    }

    private fun observeNetwork() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                clearRewardList()
            } else {
                setUpRecyclerView()
                observeRewardDetails()
            }
        }
    }

    private fun showMessage(show : Boolean){
        if(show){
            binding.imageMessage.visibility = VISIBLE
        }
        else{
            binding.imageMessage.visibility = INVISIBLE
        }
    }

    private fun observeRewardDetails() {
        rewardsViewModel.rewardDetails.observe(viewLifecycleOwner) { rewards ->
            val imgMessage: ShapeableImageView = binding.imageMessage
            imgMessage.setImageResource(R.drawable.no_data)

            if (rewards != null) {
                clearRewardList()
                rewardList.addAll(rewards)
                recyclerViewAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE

                if(rewardList.isEmpty()){
                    showMessage(true)
                }
                else{
                    showMessage(false)
                }
            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun clearRewardList() {
        rewardList.clear()
    }

    private fun dropDown(root: View) {
        try {
            val spinnerData = arrayOf(
                "All",
                "Food and Beverages",
                "Electronics",
                "Clothing",
                "Appliances",
                "Household Necessities",
                "Entertainment",
                "Accessories",
                "Others"
            )

            val spinner: Spinner = root.findViewById(R.id.spinner)
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, spinnerData)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = spinnerData[position]
                    lifecycleScope.launch {
                        rewardsViewModel.fetchAllRewards(selectedCategory)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing here
                }
            }
        } catch (error: Exception) {
            commons.log(logTag, error.toString())
        }
    }

    private fun setUpRecyclerView() {
        try {
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            rewardList = arrayListOf()
            recyclerViewAdapter = RewardsAdapter(
                rewardList,
                object : RewardsAdapter.OnItemClickListener {
                    override fun onEditClick(item: Reward) {
                        rewardsViewModel.setAction("update")
                        rewardsViewModel.fetchReward(item.id)
                        findNavController().navigate(R.id.rewardsTab_to_addReward)
                    }
                },
                object : RewardsAdapter.OnDeleteClickListener {
                    override fun onDeleteClick(item: Reward) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Delete Reward")
                            .setMessage("Are you sure you want to delete this reward?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                rewardsViewModel.deleteReward(item.id)
                                rewardsViewModel.fetchAllRewards("All")
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            )

            recyclerView.adapter = recyclerViewAdapter
            binding.progressBar.visibility = VISIBLE
            rewardsViewModel.fetchAllRewards(selectedCategory)
        } catch (error: Exception) {
            commons.log(logTag, error.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}