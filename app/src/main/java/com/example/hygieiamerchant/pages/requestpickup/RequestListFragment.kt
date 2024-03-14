package com.example.hygieiamerchant.pages.requestpickup

import android.app.DatePickerDialog
import android.icu.util.Calendar
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
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.databinding.FragmentRequestListBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.RequestRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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
            if (requestExist) {
                Commons().showAlertDialog(
                    requireContext(),
                    "Oops!",
                    "You have a pending/active request. Please wait for the LGUs response.",
                    "Okay"
                )
            } else {
                findNavController().navigate(R.id.action_nav_requests_to_requestPickUpFragment)
            }
        }

        binding.cancel.setOnClickListener {
            if (status == "active") {
                Commons().showAlertDialog(
                    requireContext(),
                    "Oops!",
                    "You can't cancel an active request.",
                    "Okay"
                )
            } else {
                cancelRequest()
            }
        }

        binding.edit.setOnClickListener {
            showDatePickerDialog() { date ->
                editRequest(date)
            }
        }
    }

    private fun editRequest(date: Date) {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                val data = Request(
                    date = date
                )
                commons.showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    true
                )
                requestRepo.editRequest(id, data) { success ->
                    commons.showLoader(
                        requireContext(),
                        LayoutInflater.from(requireContext()),
                        false
                    )
                    if (success) {
                        Commons().showAlertDialog(
                            requireContext(),
                            "Success!",
                            "Request updated successfully.",
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

    private fun showDatePickerDialog(callback: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                callback(selectedDate.time)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun observeDataSetChange() {
        requestViewModel.fetchAllRequests(userRepo.getCurrentUserId().toString())
        requestViewModel.requestDetails.observe(viewLifecycleOwner) { requests ->
            if (requests != null) {
                binding.loader.visibility = INVISIBLE
                showMessage(false)
                binding.details.visibility = VISIBLE
                binding.actions.visibility = VISIBLE
                binding.id.text = "ID: ${requests.id}"
                binding.date.text = "Date: ${requests.date.toString()}"
                binding.status.text = "Status: ${requests.status.uppercase()}"
                requestExist = true
                id = requests.id
                status = requests.status
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
            binding.storeName.text = "Store: ${user.name}"
            binding.lgu.text = "LGU: ${user.lgu}"
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