package com.example.hygieiamerchant.pages.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.databinding.FragmentTransactionsBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView

class TransactionsFragment : Fragment() {
    private val logTag = "TRANSACTIONS"
    private val transactionsViewModel: TransactionsViewModel by activityViewModels()
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val commons: Commons = Commons()
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionList: ArrayList<Transaction>
    private lateinit var recyclerViewAdapter: TransactionsAdapter
    private lateinit var imgMessage: ShapeableImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        initializeVariables()
        observeNetwork()
        setUpRecyclerView()
        setUpRefreshListener()

        return binding.root
    }

    private fun setUpRefreshListener(){
        commons.setOnRefreshListener(binding.swipeRefreshLayout){
            observeNetwork()
        }
    }

    private fun observeNetwork() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                clearTransactionList()
            } else {
                observeDataSetChange()
                setUpRecyclerView()
            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun observeDataSetChange() {
        transactionsViewModel.transactionList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                clearTransactionList()
                transactionList.addAll(list)
                recyclerViewAdapter.notifyDataSetChanged()
                progressBar.visibility = View.INVISIBLE
                if (transactionList.isEmpty()) {
                    showNoDataMessage(true)
                } else {
                    showNoDataMessage(false)
                }
            }
        }
    }

    private fun clearTransactionList() {
        transactionList.clear()
    }

    private fun showNoDataMessage(show: Boolean) {
        if (show) {
            imgMessage.visibility = View.VISIBLE
        } else {
            imgMessage.visibility = View.INVISIBLE
        }
    }

    private fun initializeVariables() {
        imgMessage = binding.imageMessage
        progressBar = binding.progressBar
        recyclerView = binding.recyclerView
        transactionList = ArrayList()
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .setCancelable(true)
            .create()
    }

    private fun setUpRecyclerView() {
        try {
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            transactionList = arrayListOf()
            recyclerViewAdapter = TransactionsAdapter(
                transactionList,
                object : TransactionsAdapter.OnItemClickListener {
                    override fun onItemClick(item: Transaction) {
                        val dialog = TransactionDetailsDialog(requireContext(), item)
                        dialog.show()
                    }
                }
            )

            recyclerView.adapter = recyclerViewAdapter
            binding.progressBar.visibility = View.VISIBLE
            transactionsViewModel.fetchAllTransactions()
        } catch (error: Exception) {
            commons.log(logTag, error.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}