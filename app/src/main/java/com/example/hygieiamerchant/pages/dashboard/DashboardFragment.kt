package com.example.hygieiamerchant.pages.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.adapters.ItemAdapter
import com.example.hygieiamerchant.data_classes.Items
import com.example.hygieiamerchant.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment() {

    val db = Firebase.firestore

    val TAG = "Hygieia:"

    private var _binding: FragmentHomeBinding? = null
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<Items>
    lateinit var type : Array<String>
    lateinit var date : Array<String>
    lateinit var amount : Array<String>


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val dashboardViewModel =
//            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        addDataRow(tableLayout, "Coke", "25")
        addDataRow(tableLayout, "Cheese Cake", "15")
        addDataRow(tableLayout, "Siomai 3pcs", "9")
        addDataRow(tableLayout, "Meat Burger", "5")

        // active promos
        val active_promos: TableLayout = root.findViewById(R.id.active_promos)
        addDataRow(active_promos, "Earth Day Celebration", "25")
        addDataRow(active_promos, "Christmas Special", "15")

        // top recyclers
        val top_recylers: TableLayout = root.findViewById(R.id.top_recyclers)
        addDataRow(top_recylers, "Elias Ainsworth", "250")
        addDataRow(top_recylers, "Chise Hatori", "150")

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

        newArrayList = arrayListOf<Items>()
        getItemData()

        setNavigationOnClickListener(R.id.requestPickUp, R.id.action_to_pickup_request_list)
        setNavigationOnClickListener(R.id.profile, R.id.action_dashboard_to_profile)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setNavigationOnClickListener(viewId: Int, actionId: Int) {
        val view: ImageView = binding.root.findViewById(viewId)
        view.setOnClickListener {
            findNavController().navigate(actionId)
        }
    }

    private fun getItemData() {
        for (i in type.indices){
            val transaction = Items(type[i], date[i], amount[i])
            newArrayList.add(transaction)
        }

        newRecyclerView.adapter = ItemAdapter(newArrayList)
    }

    private fun addDataRow(tableLayout: TableLayout, prod_name: String, sold: String) {
        val tableRow = TableRow(requireContext())

        val product_name = createTextView(prod_name, R.color.white)
        tableRow.addView(product_name)

        val quantity_sold = createTextView(sold, R.color.white)
        tableRow.addView(quantity_sold)

        tableLayout.addView(tableRow)
    }

    private fun createTextView(text: String, backgroundColor: Int): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        textView.setPadding(8, 8, 8, 8)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
        textView.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
        textView.textSize = resources.getDimension(R.dimen.text_tiniest)
        textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_semibold)
        return textView
    }
}