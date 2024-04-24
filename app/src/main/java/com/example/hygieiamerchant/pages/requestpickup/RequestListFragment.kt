package com.example.hygieiamerchant.pages.requestpickup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.databinding.FragmentRequestListBinding
import com.example.hygieiamerchant.repository.RequestRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore

class RequestListFragment : Fragment() {

    private var _binding: FragmentRequestListBinding? = null
    val binding get() = _binding!!
    private val commons: Commons = Commons()
    private val requestViewModel: RequestPickUpViewModel by activityViewModels()
    private val userRepo: UserRepo = UserRepo()
    private val requestRepo: RequestRepo = RequestRepo()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var image: ShapeableImageView
    private lateinit var loadImage: ShapeableImageView
    private lateinit var loader: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var dialog: AlertDialog
    private lateinit var createRequest: AppCompatButton
    private var id: String = ""
    private lateinit var requestList: ArrayList<Request>
    private lateinit var adapter: RequestsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestListBinding.inflate(inflater, container, false)

        initializeVariables()
        observeNetwork()
        setUpRefreshListener()
        setUpOnClickListeners()
        setUpRecyclerView()

        return binding.root
    }

    private fun cancelRequest() {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                commons.showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    true
                )
                requestRepo.cancelRequest(id) { success ->
                    commons.showLoader(
                        requireContext(),
                        LayoutInflater.from(requireContext()),
                        false
                    )
                    if (success) {
                        Commons().showAlertDialog(
                            requireContext(),
                            "Success!",
                            "Request cancelled successfully.",
                            "Okay"
                        )
                    } else {
                        Commons().showAlertDialog(
                            requireContext(),
                            "Failed!",
                            "An error occurred. Please try again later.",
                            "Okay"
                        )
                    }
                    observeDataSetChange()
                }
            } else {
                showConnectivityDialog()
                binding.loader.visibility = VISIBLE
            }
        }
    }

    private fun observeNetwork() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                showConnectivityDialog()
                binding.loader.visibility = VISIBLE
            } else {
                observeDataSetChange()
            }
        }
    }

    private fun setUpOnClickListeners() {
        createRequest.setOnClickListener {
            requestViewModel.setSelectedAction("create")
            findNavController().navigate(R.id.action_nav_requests_to_requestPickUpFragment)
        }
    }

    private fun observeDataSetChange() {
        requestViewModel.fetchAllRequests(userRepo.getCurrentUserId().toString())
        requestViewModel.requestDetails.observe(viewLifecycleOwner) { requests ->
            if (requests != null) {
                binding.loader.visibility = INVISIBLE
                showMessage(false)
                requestList.clear()
                requestList.addAll(requests)
                adapter.notifyDataSetChanged()
                if (requestList.isEmpty()) {
                    showMessage(true)
                } else {
                    showMessage(false)
                }
            }
        }


    }

    private fun showMessage(show: Boolean) {
        if (show) {
            binding.image.visibility = VISIBLE
        } else {
            binding.image.visibility = INVISIBLE
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun initializeVariables() {
        firestore = FirebaseFirestore.getInstance()
        loader = binding.loader
        loadImage = binding.image
        image = binding.image
        swipeRefreshLayout = binding.swipeRefreshLayout
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .setCancelable(true)
            .create()
        createRequest = binding.createRequest
        recyclerView = binding.recyclerView
    }

    private fun setUpRefreshListener() {
        commons.setOnRefreshListener(swipeRefreshLayout) {
            observeNetwork()
        }
    }

    private fun setUpRecyclerView() {
        try {
            val recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)

            requestList = arrayListOf()
            adapter = RequestsAdapter(
                requestList,
                object : RequestsAdapter.OnItemClickListener {
                    override fun onItemClick(request: Request) {
                        requestViewModel.setSelectedRequest(request)
                        requestViewModel.setSelectedAction("edit")
                        findNavController().navigate(R.id.action_nav_requests_to_requestPickUpFragment)
                    }
                },
                object : RequestsAdapter.OnCancelClickListener {
                    override fun onCancelClick(request: Request) {
                        id = request.id
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Cancel Request")
                            .setMessage("Are you sure you want to cancel this request?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                cancelRequest()
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
            recyclerView.adapter = adapter
        } catch (error: Exception) {
            Commons().showToast("Error: $error", requireContext())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}