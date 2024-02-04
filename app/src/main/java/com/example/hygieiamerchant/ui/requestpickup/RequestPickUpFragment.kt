package com.example.hygieiamerchant.ui.requestpickup

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
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.databinding.FragmentRequestPickUpBinding
import java.util.Locale

class RequestPickUpFragment : Fragment() {

    private var _binding: FragmentRequestPickUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var editTextDate: EditText
    private lateinit var send: AppCompatButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestPickUpBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Commons().setPageTitle("Send Request", binding.root)

        editTextDate = root.findViewById(R.id.editTextDate)

        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        send = root.findViewById(R.id.send)
        send.setOnClickListener {
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}