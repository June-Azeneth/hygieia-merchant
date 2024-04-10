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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentRequestListBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
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
    private var requestExist: Boolean = true
    private var id: String = ""
    private var status: String = ""
    private val dashboardViewModel: DashboardViewModel = DashboardViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestListBinding.inflate(inflater, container, false)

        initializeVariables()
        observeNetwork()
        setUpRefreshListener()
        setUpOnClickListeners()
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
            if (requestExist) {
                Commons().showAlertDialog(
                    requireContext(),
                    "Oops!",
                    "You have a pending/active request. Please wait for our response.",
                    "Okay"
                )
            } else {
                findNavController().navigate(R.id.action_nav_requests_to_requestPickUpFragment)
            }
        }

        binding.cancel.setOnClickListener {
            cancelRequest()
        }

        binding.edit.setOnClickListener {
            findNavController().navigate(R.id.action_nav_requests_to_requestPickUpFragment)
            requestViewModel.setSelectedAction("edit")
        }
    }

    private fun observeDataSetChange() {
        requestViewModel.fetchAllRequests(userRepo.getCurrentUserId().toString())
        requestViewModel.requestDetails.observe(viewLifecycleOwner) { requests ->
            if (requests != null) {
                val date = requests.date?.let { Commons().dateFormatMMMDDYYYY(it) }
                val time = requests.date?.let { Commons().dateFormatHHMM(it) }

                binding.loader.visibility = INVISIBLE
                showMessage(false)
                binding.details.visibility = VISIBLE
                binding.actions.visibility = VISIBLE
                binding.id.text = getString(R.string.request_id, requests.id)
                binding.date.text = getString(R.string.date, date)
                binding.status.text =
                    getString(R.string.request_status, requests.status.uppercase())
                requestExist = true
                id = requests.id
                status = requests.status

                if (status == "active") {
                    binding.time.text = getString(R.string.request_time, time)
                } else {
                    binding.time.text = ""
                }
            } else {
                requestExist = false
                binding.loader.visibility = INVISIBLE
                binding.actions.visibility = INVISIBLE
                binding.details.visibility = INVISIBLE
                showMessage(true)
            }
        }

        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            binding.storeName.text = getString(R.string.request_store_name, user.name)
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
    }

    private fun setUpRefreshListener() {
        commons.setOnRefreshListener(swipeRefreshLayout) {
            observeNetwork()
        }
    }
}