package com.example.hygieiamerchant.pages.requestpickup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.data_classes.Requests
import com.example.hygieiamerchant.databinding.FragmentRequestListBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore

class RequestListFragment : Fragment() {

    private var _binding: FragmentRequestListBinding? = null
    val binding get() = _binding!!

    private lateinit var img: ShapeableImageView
    private lateinit var back: ImageView
    private lateinit var image: ShapeableImageView
    private lateinit var loadimage: ShapeableImageView
    private lateinit var loader: ProgressBar
    private lateinit var message: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<Requests>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var firestore: FirebaseFirestore

    private lateinit var adapter: RequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        try {
            _binding = FragmentRequestListBinding.inflate(inflater, container, false)

            firestore = FirebaseFirestore.getInstance()

            recyclerView = binding.root.findViewById(R.id.list_container)
            loader = binding.root.findViewById(R.id.loader)
            message = binding.root.findViewById(R.id.message)
            loadimage = binding.root.findViewById(R.id.image)
            image = binding.root.findViewById(R.id.image)

            swipeRefreshLayout = binding.root.findViewById(R.id.swipeRefreshLayout)
            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }

            img = binding.root.findViewById(R.id.icon)
            img.setOnClickListener {
                findNavController().navigate(R.id.action_requestListFragment_to_navigation_requestPickUp)
            }

            back = binding.root.findViewById(R.id.back)
            back.setOnClickListener {
                findNavController().navigate(R.id.action_to_dashboard)
            }
        } catch (error: NullPointerException) {
            Log.e("Error", error.toString())
        } catch (error: Exception) {
            Log.e("Error", error.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            Commons().setPageTitle("Pickup Requests", binding.root)
            displayData(binding.root)
        } catch (error: Exception) {
            Log.e("Error", error.toString())
        } catch (error: NullPointerException) {
            Log.e("Error", error.toString())
        }
    }

    private fun refreshData() {
        newArrayList.clear()
        retrieveData()
        swipeRefreshLayout.isRefreshing = false
    }

    private fun displayData(root: View) {
        loadimage.visibility = INVISIBLE
        message.visibility = INVISIBLE
        recyclerView = binding.root.findViewById(R.id.list_container)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        newArrayList = arrayListOf()

        adapter = RequestsAdapter(
            newArrayList,
            object : RequestsAdapter.OnItemClickListener {
                override fun onItemClick(request: Requests) {
                    // Handle item click
                }
            },
            object : RequestsAdapter.OnDeleteClickListener {
                override fun onDeleteClick(request: Requests) {
                    deleteDocument(request.id)
                    refreshData()
                    Toast.makeText(requireContext(), request.id, Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView.adapter = adapter

        loader.visibility = VISIBLE

        retrieveData()
    }

    private fun deleteDocument(id: String){
        try {
            val documentReference = firestore.collection("request").document(id)

            documentReference.delete()
                .addOnSuccessListener {
                    // Document successfully deleted
                    Toast.makeText(
                        requireContext(),
                        "Request Deleted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { e ->
                    // Handle errors
                    Log.w("Firestore", "Error deleting document", e)
                }
        }
        catch (error: Exception){
            Log.e("Request Delete Error", error.toString())
        }
    }

    private fun retrieveData() {
        try {
//            firestore.collection("request")
//                .get()
//                .addOnSuccessListener { result ->
//                    if (!result.isEmpty) {
//                        newArrayList.clear()
//                        for (document in result) {
//                            val requests = Requests(
//                                document.getString("id") ?: "",
//                                document.getString("date") ?: "",
//                                document.getString("storeName") ?: "",
//                                document.getString("address") ?: "",
//                                document.getString("notes") ?: "",
//                                document.getString("status") ?: "",
//                            )
//                            newArrayList.add(requests)
//                        }
//                        requireActivity().runOnUiThread {
//                            recyclerView.adapter = adapter
//                            itemsVisibility(INVISIBLE)
//                        }
//                    } else {
//                        Log.e("QUERYRESULT", "No documents found.")
//                        requireActivity().runOnUiThread {
//                            // You can update your UI accordingly or show a message
//                            image.setImageResource(R.drawable.empty_list)
//                            message.text = "You have no pending requests"
//                            itemsVisibility(VISIBLE)
//                        }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.w("TAG", "Error getting documents: ", exception)
//                    loader.visibility = View.GONE
//                }
        }
        catch (error: Exception){
            Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun itemsVisibility(visibility: Int){
        loader.visibility = View.GONE
        loadimage.visibility = visibility
        message.visibility = visibility
    }

}