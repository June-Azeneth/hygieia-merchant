package com.example.hygieiamerchant.pages.ads_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.databinding.FragmentAdsManagerBinding
import com.example.hygieiamerchant.repository.AdsRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons

class AdsManagerFragment : Fragment() {
    private var _binding: FragmentAdsManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: AdsAdapter
    private lateinit var adList: ArrayList<Ads>
    private val adsViewModel: AdsViewModel by activityViewModels()
    private val adsRepo: AdsRepo = AdsRepo()
    private val userRepo: UserRepo = UserRepo()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsManagerBinding.inflate(inflater, container, false)

        binding.createAd.setOnClickListener {
            adsViewModel.setAction("create")
            findNavController().navigate(R.id.action_nav_ads_to_advertisementDetailsFormFragment)
        }

        observeData()
        setUpRecyclerView()
        refreshListener()
        adsViewModel.fetchAllAds(userRepo.getCurrentUserId().toString())

        return binding.root
    }

    private fun refreshListener() {
        Commons().setOnRefreshListener(binding.swipeRefreshLayout){
            adsViewModel.fetchAllAds(userRepo.getCurrentUserId().toString())
        }
    }

    private fun setUpRecyclerView() {
        try {
            adsViewModel.fetchAllAds(userRepo.getCurrentUserId().toString())
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            adList = arrayListOf()
            recyclerViewAdapter = AdsAdapter(
                adList,
                object : AdsAdapter.OnItemClickListener {
                    override fun onEditClick(ad: Ads) {
                        adsViewModel.setAction("update")
                        adsViewModel.fetchAllAds(ad.id)
                        adsViewModel.setSelectedAd(ad)
                        findNavController().navigate(R.id.action_nav_ads_to_advertisementDetailsFormFragment)
                    }
                },
                object : AdsAdapter.OnDeleteClickListener {
                    override fun onDeleteClick(ad: Ads) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Delete Advertisement")
                            .setMessage("Are you sure you want to delete this advertisement?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                adsRepo.deleteAd(ad.id){success->
                                    if(success){
                                        adsViewModel.fetchAllAds(userRepo.getCurrentUserId().toString())
                                        Commons().showToast("Advertisement successfully deleted!", requireContext())
                                        dialog.dismiss()
                                    }else{
                                        Commons().showToast("An error occurred. Please try again later.", requireContext())
                                    }
                                }
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

            binding.progressBar.visibility = View.VISIBLE

            adsViewModel.fetchAllAds(userRepo.getCurrentUserId().toString())
        } catch (error: Exception) {
            Commons().showToast(error.message.toString(), requireContext())
        }
    }

    private fun observeData() {
        showLoader(true)
        adsViewModel.adDetails.observe(viewLifecycleOwner) { ads ->
            if (ads != null) {
                adList.clear()
                adList.addAll(ads)
                recyclerViewAdapter.notifyDataSetChanged()
                showLoader(false)
                if (adList.isEmpty()) {
                    showNoDataMessage(true)
                } else {
                    showNoDataMessage(false)
                }
            }
        }
    }

    private fun showLoader(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showNoDataMessage(show: Boolean) {
        if (show) {
            binding.recyclerView.visibility = View.GONE
            binding.imageMessage.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.imageMessage.visibility = View.GONE
        }
    }
}