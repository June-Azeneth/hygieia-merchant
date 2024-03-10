package com.example.hygieiamerchant.pages.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.databinding.FragmentTransactionsBinding
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.popups.GrantPointsPopUp
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        initializeVariables()
        setUpRecyclerView()
        observeDataSetChange()

        return binding.root
    }

    private fun observeNetwork() {

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
                        commons.log(logTag,item.customerId)
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