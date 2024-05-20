package com.example.hygieiamerchant.pages.ads_manager

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentAdvertisementDetailsFormBinding
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class AdvertisementDetailsFormFragment : Fragment() {
    private var _binding: FragmentAdvertisementDetailsFormBinding? = null
    private val binding get() = _binding!!

    private var startDate: Date? = null
    private var endDate: Date? = null

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                binding.poster.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdvertisementDetailsFormBinding.inflate(inflater, container, false)
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.selectImage.setOnClickListener {
            openGallery.launch("image/*")
        }

        binding.setDuration.setOnClickListener {
            showDateRangePickerDialog()
        }
    }

    private fun validateFields() {

    }

    private fun showDateRangePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setSelection(Pair(null, null))
            .build()

        datePicker.show(childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener { dateSelection ->
            startDate = Date(dateSelection.first ?: 0)
            endDate = Date(dateSelection.second ?: 0)

            // Update the text of the filterByDate button to show the selected date range
            binding.setDuration.text = getString(
                R.string.date_range_transactions,
                Commons().dateFormatMMMDDYYYY(dateSelection.first),
                Commons().dateFormatMMMDDYYYY(dateSelection.second)
            )
        }
    }
}