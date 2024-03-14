package com.example.hygieiamerchant.pages.requestpickup

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.databinding.FragmentRequestPickUpBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.RequestRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import java.util.Date
import java.util.Locale

class RequestPickUpFragment : Fragment() {

    private var _binding: FragmentRequestPickUpBinding? = null
    private val binding get() = _binding!!
    private val requestRepo: RequestRepo = RequestRepo()
    private val userRepo: UserRepo = UserRepo()
    private val dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private lateinit var editTextDate: EditText
    private lateinit var notes: EditText
    private lateinit var send: AppCompatButton
    private lateinit var cancel: AppCompatButton
    private var storeId: String = ""
    private lateinit var address: Map<String, String>
    private lateinit var date: Date
    private var lguId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
            createRequest()
        }
        cancel.setOnClickListener {
            findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
        }
    }

    private fun validateFields(): Boolean {
        var date = editTextDate.text.toString()
        return date.isNotEmpty()
    }

    private fun populateFields() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            binding.storeName.text = getString(R.string.store, details.name)
            binding.address.text =
                getString(R.string.address_, Commons().formatAddress(details.address, "short"))
            binding.lgu.text = getString(R.string.lgu, details.lgu)

            storeId = userRepo.getCurrentUserId().toString()
            lguId = details.lguId
            address = details.address!!
        }
    }

    private fun createRequest() {
        if (validateFields()) {
            val data = Request(
                storeId = storeId,
                lguId = lguId,
                date = date,
                address = address,
                notes = notes.text.toString(),
            )

            requestRepo.addRequest(data) { success ->
                if (success) {
                    Commons().showAlertDialogWithCallback(
                        this,
                        "Success",
                        "Request successfully submitted",
                        "Okay",
                        positiveButtonCallback = {
                            findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
                        }
                    )
                } else {
                    Commons().showAlertDialogWithCallback(
                        this,
                        "Failed",
                        "Request sent failed. Please try again later.",
                        "Okay",
                        positiveButtonCallback = {
                            findNavController().navigate(R.id.action_requestPickUpFragment_to_nav_requests)
                        }
                    )
                }
            }
        } else {
            Commons().showToast("Please set a date for this request", requireContext())
        }
    }

    private fun initializeVariables() {
        send = binding.send
        editTextDate = binding.date
        notes = binding.notes
        cancel = binding.cancel
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("MMMM-dd-yyyy", Locale.US)
                val formattedDate = dateFormat.format(selectedDate.time)

                editTextDate.setText(formattedDate)
                date = selectedDate.time
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}