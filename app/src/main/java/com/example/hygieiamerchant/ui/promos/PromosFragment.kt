package com.example.hygieiamerchant.ui.promos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.adapters.PromoAdapter
import com.example.hygieiamerchant.data_classes.Promos
import com.example.hygieiamerchant.databinding.FragmentPromosBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class PromosFragment : Fragment() {
    val TAG = "PROMOSFRAGMENT"
    private var _binding: FragmentPromosBinding? = null
    private val binding get() = _binding!!
    private lateinit var promoList: ArrayList<Promos>
    private var selectedCategory: String = ""
    private lateinit var dialog: AlertDialog
    private lateinit var networkManager: NetworkManager
    private lateinit var commons: Commons
    private val promosViewModel: PromosViewModel by activityViewModels()
    private lateinit var recyclerViewAdapter: PromoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkManager = NetworkManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPromosBinding.inflate(inflater, container, false)

        //Initialize
        commons = Commons()
        promoList = ArrayList()
        dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MaterialAlertDialog_Rounded
        ).setView(R.layout.connectivity_dialog_box).setCancelable(true).create()

        //common operations
        commons.setPageTitle("Promos", binding.root)
        commons.setNavigationOnClickListener(binding.addPromo, R.id.action_promo_to_create_promo)
        commons.setOnRefreshListener(binding.swipeRefreshLayout) {
            //Refresh the data here
            refreshPage()
        }

        //method calls
        dropDown(binding.root)
        setUpRecyclerView()
        setUpNetworkObservation()

        return binding.root
    }

    private fun observePromoDetails() {
        promosViewModel.promoDetails.observe(viewLifecycleOwner) { promo ->
            var imgMessage: ShapeableImageView = binding.imageMessage
            imgMessage.setImageResource(R.drawable.no_data)
            var message: TextView = binding.message
            message.text = "No records found"
            if (promo != null) {
                clearRewardList()
                promoList.addAll(promo)
                recyclerViewAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                imgMessage.visibility = INVISIBLE
                message.visibility = INVISIBLE
                commons.log(TAG, promo.toString())

                if (promoList.isEmpty()) {
                    imgMessage.visibility = VISIBLE
                    message.visibility = VISIBLE
                }
            } else {
                //Show appropriate message
            }
        }
    }

    private fun setUpRecyclerView() {
        try {
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            promoList = arrayListOf()
            recyclerViewAdapter = PromoAdapter(promoList)
            recyclerView.adapter = recyclerViewAdapter

            binding.progressBar.visibility = View.VISIBLE

            promosViewModel.getQueryResult(selectedCategory)
        } catch (error: Exception) {
            commons.log(TAG, error.toString())
        }
    }

    private fun dropDown(root: View) {
        try {
            val spinnerData = arrayOf("All", "Ongoing", "Upcoming", "Paused", "Passed")

            val spinner: Spinner = root.findViewById(R.id.spinner)
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerData)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            selectedCategory = "All"

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    selectedCategory = spinnerData[position]
                    lifecycleScope.launch {
                        promosViewModel.getQueryResult(selectedCategory)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing here
                }
            }
        } catch (error: Exception) {
            commons.log(TAG, error.toString())
        }
    }

    private fun refreshPage() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                clearRewardList()
            } else {
                promosViewModel.getQueryResult(selectedCategory)
            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing) dialog.show()
    }

    private fun clearRewardList() {
        promoList.clear()
    }

    private fun setUpNetworkObservation() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                clearRewardList()
            } else {
                setUpRecyclerView()
                observePromoDetails()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}