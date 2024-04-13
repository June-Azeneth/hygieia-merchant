package com.example.hygieiamerchant.pages.promos

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
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
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.databinding.FragmentPromosBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class PromosFragment : Fragment() {
    val _tag = "PROMOSFRAGMENT"
    private var _binding: FragmentPromosBinding? = null
    private val binding get() = _binding!!
    private lateinit var promoList: ArrayList<Promo>
    private var selectedCategory: String = ""
    private lateinit var dialog: AlertDialog
    private lateinit var networkManager: NetworkManager
    private var commons: Commons = Commons()
    private val promosViewModel: PromosViewModel by activityViewModels()
    private lateinit var recyclerViewAdapter: PromoAdapter

    private lateinit var createPromo: FloatingActionButton
    private lateinit var message: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkManager = NetworkManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPromosBinding.inflate(inflater, container, false)

        //method calls
        initializeVariables()
        setUpRecyclerView()
        setUpNetworkObservation()
        setUpOnClickListeners()

        return binding.root
    }

    private fun initializeVariables() {
        message = binding.imageMessage
        promoList = ArrayList()
        dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MaterialAlertDialog_Rounded
        ).setView(R.layout.connectivity_dialog_box).setCancelable(true).create()
        createPromo = binding.addPromo
        dropDown(binding.root)
        commons.setOnRefreshListener(binding.swipeRefreshLayout) {
            //Refresh the data here
            refreshPage()
        }
    }

    private fun setUpOnClickListeners() {
        createPromo.setOnClickListener {
            promosViewModel.setAction("create")
            findNavController().navigate(R.id.action_nav_promos_to_createPromoFragment)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observePromoDetails() {
        promosViewModel.promoDetails.observe(viewLifecycleOwner) { promo ->
            var imgMessage: ShapeableImageView = binding.imageMessage
            imgMessage.setImageResource(R.drawable.no_data)
            if (promo != null) {
                clearRewardList()
                promoList.addAll(promo)
                recyclerViewAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                imgMessage.visibility = INVISIBLE

                if (promoList.isEmpty()) {
                    showMessage(true)
                } else {
                    showMessage(false)
                }
            }
        }
    }

    private fun showMessage(show: Boolean) {
        if(show){
            message.visibility = View.VISIBLE
        }
        else{
            message.visibility = View.INVISIBLE
        }
    }

    private fun setUpRecyclerView() {
        try {
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            promoList = arrayListOf()
            recyclerViewAdapter = PromoAdapter(
                promoList,
                object : PromoAdapter.OnItemClickListener {
                    override fun onEditClick(promo: Promo) {
                        promosViewModel.setAction("update")
                        promosViewModel.fetchPromo(promo.id)
                        findNavController().navigate(R.id.action_nav_promos_to_createPromoFragment)
                    }
                },
                object : PromoAdapter.OnDeleteClickListener {
                    override fun onDeleteClick(promo: Promo) {
                        promosViewModel.setAction("update")
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Delete Promo")
                            .setMessage("Are you sure you want to delete this promo?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                promosViewModel.deletePromo(promo.id)
                                promosViewModel.fetchAllPromos("All")
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

            binding.progressBar.visibility = View.VISIBLE

            promosViewModel.fetchAllPromos(selectedCategory)
        } catch (error: Exception) {
            commons.log(_tag, error.toString())
        }
    }

    private fun dropDown(root: View) {
        try {
            val spinnerData = arrayOf("All", "Ongoing", "Upcoming", "Passed")

            val spinner: Spinner = root.findViewById(R.id.spinner)
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, spinnerData)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            selectedCategory = "All"

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    selectedCategory = spinnerData[position]
                    lifecycleScope.launch {
                        promosViewModel.fetchAllPromos(selectedCategory)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing here
                }
            }
        } catch (error: Exception) {
            commons.log(_tag, error.toString())
        }
    }

    private fun refreshPage() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                clearRewardList()
            } else {
                promosViewModel.fetchAllPromos(selectedCategory)
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