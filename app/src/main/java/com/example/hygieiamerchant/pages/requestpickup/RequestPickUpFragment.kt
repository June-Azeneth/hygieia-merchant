package com.example.hygieiamerchant.pages.requestpickup

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.databinding.FragmentRequestPickUpBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.RequestRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date
import java.util.Locale

class RequestPickUpFragment : Fragment() {

    private var _binding: FragmentRequestPickUpBinding? = null
    private val binding get() = _binding!!
    private val requestRepo: RequestRepo = RequestRepo()
    private val userRepo: UserRepo = UserRepo()
    private val dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private val requestViewModel: RequestPickUpViewModel by activityViewModels()
    private lateinit var editTextDate: EditText
    private lateinit var notes: EditText
    private lateinit var phone: EditText
    private lateinit var send: AppCompatButton
    private lateinit var cancel: AppCompatButton
    private lateinit var address: Map<String, String>
    private lateinit var date: Date
    private lateinit var dialog: AlertDialog
    private var storeId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestPickUpBinding.inflate(inflater, container, false)

        initializeVariables()
        setUpOnClickListeners()
        populateFields()

        return binding.root
    }

    private fun setUpOnClickListeners() {
        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }
        send.setOnClickListener {
            if (requestViewModel.action.value == "create") {
                createRequest()
            } else {
                editRequest()
            }
        }
        cancel.setOnClickListener {
            findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
        }
    }

    private fun editRequest() {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                val data = Request(
                    date = date,
                    notes = notes.text.toString(),
                    phone = phone.text.toString()
                )

                requestViewModel.requestDetails.observe(viewLifecycleOwner) { request ->
                    if (request != null) {
                        requestRepo.editRequest(request.id, data) { success ->
                            if (success) {
                                Commons().showAlertDialogWithCallback(this,
                                    "Success",
                                    "Request updated successfully",
                                    "Okay",
                                    positiveButtonCallback = {
                                        findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
                                    })
                            } else {
                                Commons().showAlertDialog(
                                    requireContext(),
                                    "Failed!",
                                    "An error occurred. Please try again later.",
                                    "Okay"
                                )
                            }
                        }
                    }
                }
            } else {
                showConnectivityDialog()
            }
        }
    }

    private fun validateFields(callback: (Pair<Boolean, String>) -> Unit) {
        val date = editTextDate.text.toString()
        val phone = phone.text.toString()
        if (date.isEmpty() || phone.isEmpty()) {
            callback.invoke(Pair(false, "Fill in all required fields"))
        } else if (!isValidPhoneNumber(phone)) {
            callback.invoke(Pair(false, "Invalid phone number"))
        } else {
            callback.invoke(Pair(true, ""))
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Regular expression to match a phone number pattern
        val phoneRegex = "^[+]?[0-9]{10,13}\$"

        // Compile the regex pattern
        val pattern = Regex(phoneRegex)

        // Return true if the phone number matches the pattern, otherwise false
        return pattern.matches(phoneNumber)
    }

    private fun populateFields() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            binding.storeName.text = getString(R.string.store, details.name)
            binding.address.text =
                getString(R.string.address_, Commons().formatAddress(details.address, "short"))

            storeId = userRepo.getCurrentUserId().toString()
            address = details.address!!
        }

        if (requestViewModel.action.value == "edit") {
            requestViewModel.requestDetails.observe(viewLifecycleOwner) { request ->
                if (request != null) {
                    date = request.date!!
                    notes.setText(request.notes)
                    editTextDate.setText(Commons().dateFormatMMMDDYYYYDate(request.date))
                    phone.setText(request.phone)
                }
            }
        }
    }

    private fun createRequest() {
        Commons().observeNetwork(requireContext(), viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                validateFields { result ->
                    if (result.first) {
                        val data = Request(
                            storeId = storeId,
                            date = date,
                            address = address,
                            notes = notes.text.toString(),
                            phone = phone.text.toString()
                        )

                        binding.progressBar.visibility = View.VISIBLE
                        binding.send.visibility = View.INVISIBLE

                        requestRepo.addRequest(data) { success ->
                            binding.progressBar.visibility = View.GONE
                            binding.send.visibility = View.VISIBLE
                            if (success) {
                                Commons().showAlertDialogWithCallback(this,
                                    "Success",
                                    "Request successfully submitted",
                                    "Okay",
                                    positiveButtonCallback = {
                                        findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
                                    })
                            } else {
                                Commons().showAlertDialogWithCallback(this,
                                    "Failed",
                                    "Request sent failed. Please try again later.",
                                    "Okay",
                                    positiveButtonCallback = {
                                        findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
                                    })
                            }
                        }
                    } else {
                        Commons().showToast(result.second, requireContext())
                    }
                }
            } else {
                showConnectivityDialog()
            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing) dialog.show()
    }

    private fun initializeVariables() {
        send = binding.send
        editTextDate = binding.date
        notes = binding.notes
        cancel = binding.cancel
        phone = binding.phone
        dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MaterialAlertDialog_Rounded
        ).setView(R.layout.connectivity_dialog_box).setCancelable(true).create()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("MMMM-dd-yyyy", Locale.US)
                val formattedDate = dateFormat.format(selectedDate.time)

                editTextDate.setText(formattedDate)
                date = selectedDate.time
            }, year, month, day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}