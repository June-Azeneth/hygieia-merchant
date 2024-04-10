package com.example.hygieiamerchant.pages.announcement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Announcement
import com.example.hygieiamerchant.databinding.FragmentAnnouncementBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AnnouncementFragment : Fragment() {
    private var _binding: FragmentAnnouncementBinding? = null
    private val binding get() = _binding!!

    private lateinit var announcementList: ArrayList<Announcement>
    private val announcementViewModel: AnnouncementViewModel by activityViewModels()

    private val adapter by lazy { AnnouncementAdapter(arrayListOf(), onItemClickListener) }
    private val onItemClickListener = object : AnnouncementAdapter.OnItemClickListener {
        override fun onItemClick(item: Announcement) {
            announcementViewModel.fetchAnnouncement(item.id)
            findNavController().navigate(R.id.action_nav_announcement_to_announcementDetailsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)

        setUpList()
        observeNetwork()
        setUpUi()

        return binding.root
    }

    private fun setUpUi() {
        Commons().setOnRefreshListener(binding.actualLayout) {
            observeNetwork()
        }
    }

    private fun observeNetwork() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                binding.placeholder.visibility = View.VISIBLE
                binding.actualLayout.visibility = View.GONE
                showConnectivityDialog(true)
            } else {
                showConnectivityDialog(false)
                binding.placeholder.visibility = View.GONE
                binding.actualLayout.visibility = View.VISIBLE
                observeDataSetChange()
            }
        }
    }

    private fun showConnectivityDialog(show: Boolean) {
        val dialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                .setView(R.layout.connectivity_dialog_box)
                .setCancelable(true)
                .create()
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }

    private fun setUpList() {
        val recyclerView = binding.recyclerView

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        announcementList = arrayListOf()

        recyclerView.adapter = adapter

        announcementViewModel.getAllAnnouncements()
    }

    private fun observeDataSetChange() {
        announcementViewModel.announcementDetails.observe(viewLifecycleOwner) { announcements ->
            if (announcements != null) {
                announcementList.clear()
                announcementList.addAll(announcements)
                adapter.setData(announcementList)
                showNoDataMessage(false)
            } else {
                showNoDataMessage(true)
            }
        }
    }

    private fun showNoDataMessage(show: Boolean) {
        binding.progressBar.visibility = View.GONE
        if (show) {
            binding.imageMessage.setImageResource(R.drawable.no_data)
            binding.imageMessage.visibility = View.VISIBLE
        } else {
            binding.imageMessage.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}