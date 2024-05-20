package com.example.hygieiamerchant.pages.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentEditScehduleBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons

class EditScheduleFragment : Fragment() {
    private var _binding: FragmentEditScehduleBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val userRepo: UserRepo = UserRepo()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditScehduleBinding.inflate(inflater, container, false)
        setUpUi()
        observeChanges()
        return binding.root
    }

    private fun setUpUi() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user.days.isNotEmpty()) {
                user.days.forEach { day ->
                    val checkBox = when (day) {
                        "Monday" -> binding.monday
                        "Tuesday" -> binding.tuesday
                        "Wednesday" -> binding.wednesday
                        "Thursday" -> binding.thursday
                        "Friday" -> binding.friday
                        "Saturday" -> binding.saturday
                        "Sunday" -> binding.sunday
                        else -> null
                    }
                    checkBox?.isChecked = true
                }
            } else {
                binding.none.isChecked = true
            }
        }

        binding.save.setOnClickListener {
            updateSchedule()
        }
    }

    private fun observeChanges() {
        binding.none.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkForNoneCheck()
            }
        }

        val otherCheckboxes = listOf(
            binding.monday, binding.tuesday, binding.wednesday,
            binding.thursday, binding.friday, binding.saturday, binding.sunday
        )

        otherCheckboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.none.isChecked = false
                }
            }
        }
    }

    private fun checkForNoneCheck() {
        if (binding.none.isChecked) {
            binding.monday.isChecked = false
            binding.tuesday.isChecked = false
            binding.wednesday.isChecked = false
            binding.thursday.isChecked = false
            binding.friday.isChecked = false
            binding.saturday.isChecked = false
            binding.sunday.isChecked = false
        }
    }

    private fun updateSchedule() {
        showLoader(true)
        val checkedDays = mutableListOf<String>()
        val checkBoxes = listOf(
            "Monday" to binding.monday,
            "Tuesday" to binding.tuesday,
            "Wednesday" to binding.wednesday,
            "Thursday" to binding.thursday,
            "Friday" to binding.friday,
            "Saturday" to binding.saturday,
            "Sunday" to binding.sunday
        )

        for ((day, checkBox) in checkBoxes) {
            if (checkBox.isChecked) {
                checkedDays.add(day)
            }
        }
        userRepo.updateSched(checkedDays) { success ->
            showLoader(false)
            if (success) {
                Commons().showAlertDialogWithCallback(this,
                    "Success!",
                    "Your schedule is now updated.",
                    "Okay",
                    positiveButtonCallback = {
                        if (dashboardViewModel.fromPage.value == "profile") {
                            findNavController().navigate(R.id.action_editScehduleFragment_to_nav_profile)
                        }else{
                            findNavController().navigate(R.id.action_editScehduleFragment_to_nav_scan_qr_code)
                        }
                    })
            } else {
                Commons().showToast("Operation failed. Try again later.", requireContext())
            }
        }
    }

    private fun showLoader(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = VISIBLE
            binding.save.visibility = GONE
        } else {
            binding.progressBar.visibility = GONE
            binding.save.visibility = VISIBLE
        }
    }
}