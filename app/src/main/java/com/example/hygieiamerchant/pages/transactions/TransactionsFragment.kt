package com.example.hygieiamerchant.pages.transactions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.adapters.ItemAdapter
import com.example.hygieiamerchant.data_classes.Item
import com.example.hygieiamerchant.databinding.FragmentDashboardBinding

class TransactionsFragment : Fragment() {

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<Item>
    lateinit var type : Array<String>
    lateinit var date : Array<String>
    lateinit var amount : Array<String>

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val customToolbar = root.findViewById<View>(R.id.header)
        val titleTextView: TextView = customToolbar.findViewById(R.id.titleTextView)
        titleTextView.text = "Transaction History"

        type = arrayOf(
            "Recieve Points",
            "Redeem Reward",
        )

        date = arrayOf(
            "09-10-2023",
            "09-10-2023"
        )

        amount = arrayOf(
            "20",
            "20"
        )

        newRecyclerView = binding.recyclerView
        newRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        newRecyclerView.setHasFixedSize(true)

        newArrayList = arrayListOf<Item>()
        getItemData()

        return binding.root
    }

    private fun getItemData() {
        for (i in type.indices){
            val transaction = Item(type[i], date[i], amount[i])
            newArrayList.add(transaction)
        }

        newRecyclerView.adapter = ItemAdapter(newArrayList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}