package com.example.hygieiamerchant.pages.ads_manager

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.databinding.FragmentAdvertisementDetailsFormBinding
import com.example.hygieiamerchant.repository.AdsRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date

class AdvertisementDetailsFormFragment : Fragment() {
    private var _binding: FragmentAdvertisementDetailsFormBinding? = null
    private val binding get() = _binding!!

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var imgUri: Uri? = null
    private var imgUrl: String = ""
    private var id: String = ""
    private var isNetworkAvailable: Boolean = true
    private var adsRepo: AdsRepo = AdsRepo()
    private var storage: FirebaseStorage = Firebase.storage
    private val userRepo: UserRepo = UserRepo()
    private val adsViewModel: AdsViewModel by activityViewModels()

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                imgUri = imageUri
                binding.poster.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdvertisementDetailsFormBinding.inflate(inflater, container, false)
        observeNetwork()
        setOnClickListeners()
        setUpUi()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.selectImage.setOnClickListener {
            openGallery.launch("image/*")
        }

        binding.setDuration.setOnClickListener {
            showDateRangePickerDialog()
        }

        binding.createAd.setOnClickListener {
            if (adsViewModel.action.value == "create") {
                createAd()
            } else {
                updateAd()
            }
        }
    }

    private fun validateFields(): Boolean {
        val title = binding.title.text.toString()
        val date = binding.setDuration.text.toString()

        if (adsViewModel.action.value == "create") {
            return if (title.isEmpty() || date == "Select ads duration") {
                Commons().showToast("Fill in all the required fields", requireContext())
                false
            } else if (imgUri == null) {
                Commons().showToast("Please select a photo for this poster", requireContext())
                false
            } else {
                showLoader(true)
                true
            }
        } else {
            return if (title.isEmpty() || date == "Select ads duration") {
                Commons().showToast("Fill in all the required fields", requireContext())
                false
            } else {
                showLoader(true)
                true
            }
        }
    }

    private fun setUpUi() {
        if (adsViewModel.action.value == "update") {
            binding.createAd.text = "Update"
            adsViewModel.selectedAd.observe(viewLifecycleOwner) { ad ->
                if (ad != null) {
                    binding.details.setText(ad.details)
                    startDate = ad.startDate
                    endDate = ad.endDate
                    binding.title.setText(ad.title)
                    binding.setDuration.text = getString(
                        R.string.date_range_transactions,
                        ad.startDate?.let { Commons().dateFormatMMMDDYYYY(it) },
                        ad.endDate?.let { Commons().dateFormatMMMDDYYYY(it) }
                    )

                    imgUrl = ad.poster
                    id = ad.id
                    binding.details.setText(ad.details)
                    Glide.with(this).load(ad.poster).into(binding.poster)
                }
            }
        }
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

            binding.setDuration.text = getString(
                R.string.date_range_transactions,
                Commons().dateFormatMMMDDYYYY(dateSelection.first),
                Commons().dateFormatMMMDDYYYY(dateSelection.second)
            )
        }
    }

    private fun createAd() {
        if (isNetworkAvailable) {
            if (validateFields()) {
                imgUri?.let {
                    uploadImage(it) { img ->
                        val data = Ads(
                            details = binding.details.text.toString(),
                            title = binding.title.text.toString(),
                            startDate = startDate,
                            endDate = endDate,
                            poster = img
                        )

                        adsRepo.createAd(data) { success ->
                            Commons().showLoader(
                                requireContext(), LayoutInflater.from(requireContext()), false
                            )
                            showLoader(false)
                            if (success) {
                                // Handle success
                                Commons().showToast(
                                    "Advertisement Created Successfully",
                                    requireContext()
                                )
                                findNavController().navigate(R.id.action_advertisementDetailsFormFragment_to_nav_ads)
                            } else {
                                // Handle failure
                                Commons().showToast(
                                    "An error occurred. Please try again later",
                                    requireContext()
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Commons().showToast("No internet connection. Please try again later", requireContext())
        }
    }

    private fun updateAd() {
        if (isNetworkAvailable) {
            if (validateFields()) {
                if (imgUri == null) {
                    val data = Ads(
                        details = binding.details.text.toString(),
                        title = binding.title.text.toString(),
                        startDate = startDate,
                        endDate = endDate,
                        poster = imgUrl
                    )

                    adsRepo.updateAd(id, data) { success ->
                        showLoader(false)
                        if (success) {
                            // Handle success
                            Commons().showToast(
                                "Advertisement Updated Successfully",
                                requireContext()
                            )
                            findNavController().navigate(R.id.action_advertisementDetailsFormFragment_to_nav_ads)
                        } else {
                            // Handle failure
                            Commons().showToast(
                                "An error occurred. Please try again later",
                                requireContext()
                            )
                        }
                    }
                } else {
                    imgUri?.let {
                        uploadImage(it) { img ->
                            val data = Ads(
                                title = binding.title.text.toString(),
                                startDate = startDate,
                                endDate = endDate,
                                poster = img
                            )

                            adsRepo.updateAd(id, data) { success ->
                                showLoader(false)
                                if (success) {
                                    // Handle success
                                    Commons().showToast(
                                        "Advertisement Updated Successfully",
                                        requireContext()
                                    )
                                    findNavController().navigate(R.id.action_advertisementDetailsFormFragment_to_nav_ads)
                                } else {
                                    // Handle failure
                                    Commons().showToast(
                                        "An error occurred. Please try again later",
                                        requireContext()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Commons().showToast("No internet connection. Please try again later", requireContext())
        }
    }

    private fun observeNetwork() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { available ->
            isNetworkAvailable = if (!available) {
                Commons().showToast("No internet connection", requireContext())
                false
            } else {
                true
            }
        }
    }

    private fun uploadImage(
        imageUri: Uri,
        callback: (String) -> Unit
    ) {
        val storageRef =
            storage.reference.child("stores/${userRepo.getCurrentUserId()}/promos/${binding.title.text.toString()}")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                imgUrl = imageUrl.toString()
                callback(imgUrl)
            }.addOnFailureListener {
                callback("")
            }

        }.addOnFailureListener {
            callback("")
        }
    }

    private fun showLoader(show: Boolean) {
        if (show) {
            binding.createAd.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.createAd.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adsViewModel.clearSelectedAd()
        _binding = null
    }
}