package com.example.hygieiamerchant.pages.promos

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.databinding.FragmentCreatePromoBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.PromoRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date

class PromoCreateOrUpdateFragment : Fragment() {
    private var _binding: FragmentCreatePromoBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private val promoViewModel: PromosViewModel by activityViewModels()
    private val promosRepo: PromoRepo = PromoRepo()
    private val commons: Commons = Commons()
    private val userRepo: UserRepo = UserRepo()

    private lateinit var storage: FirebaseStorage
    private lateinit var promoName: TextInputEditText
    private lateinit var productName: TextInputEditText
    private lateinit var storePrice: TextInputEditText
    private lateinit var discountRate: TextInputEditText
    private lateinit var pointsRequired: TextInputEditText
    private lateinit var startDate: TextInputEditText
    private lateinit var endDate: TextInputEditText
    private lateinit var cancel: AppCompatButton
    private lateinit var submit: AppCompatButton
    private lateinit var photoPicker: ConstraintLayout
    private lateinit var promoImage: ShapeableImageView
    private lateinit var contentResolver: ContentResolver
    private lateinit var discPrice: TextView
    private lateinit var datePicker: AppCompatButton

    private var image: Uri? = null
    private var discountedPrice: Double = 0.0
    private var id: String = ""
    private var imageUrl: String? = null
    private var downloadUrl: String = ""
    private var timestampStartDate: Date? = null
    private var timestampEndDate: Date? = null

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imageTitle = getImageTitle(imageUri)
                this.image = imageUri
                binding.selectedImage.text = imageTitle
                promoImage.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePromoBinding.inflate(inflater, container, false)

        setPageTitle()
        initializeVariables()
        setUpUi()
        setUpActionListeners()
        setOnClickListeners()
        calculateDiscountedPrice()

        return binding.root
    }

    private fun setPageTitle() {
        if (promoViewModel.action.value == "update") {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Update Promo"
        } else {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Create Promo"
        }
    }

    private fun setUpUi() {
        if (promoViewModel.action.value == "update") {
            submit.text = "Update"
            populateFields()
        } else {
            submit.text = "Submit"
        }

        submit.setOnClickListener {
            if (promoViewModel.action.value == "create") {
                createPromo()
            } else {
                updatePromo()
            }
        }
    }

    private fun populateFields() {
        promoViewModel.singlePromo.observe(viewLifecycleOwner) { promo ->
            if (promo != null) {
                id = promo.id
                imageUrl = promo.photo
                promoName.setText(promo.promoName)
                productName.setText(promo.product)
                storePrice.setText(promo.price.toString())
                storePrice.setText(commons.formatDecimalNumber(promo.price))
                discountRate.setText(commons.formatDecimalNumber(promo.discountRate))
                pointsRequired.setText(commons.formatDecimalNumber(promo.pointsRequired))
                discPrice.text = commons.formatDecimalNumber(promo.discountedPrice)
                startDate.setText(promo.dateStart?.let { commons.dateFormatMMMDDYYYY(it) })
                endDate.setText(promo.dateEnd?.let { commons.dateFormatMMMDDYYYY(it) })
                timestampStartDate = promo.dateStart
                timestampEndDate = promo.dateEnd
                Glide.with(this).load(promo.photo).into(promoImage)
            }
        }
    }

    private fun validateFields(): Boolean {
        val promo = promoName.text.toString()
        val product = productName.text.toString()
        val price = storePrice.text.toString()
        val discount = discountRate.text.toString()
        val pointsRequired = pointsRequired.text.toString()
        val start = startDate.text.toString()
        val end = endDate.text.toString()
        return if (promo.isEmpty() || product.isEmpty() || price.isEmpty() || discount.isEmpty() || pointsRequired.isEmpty() || start.isEmpty() || end.isEmpty()) {
            commons.showToast("Fill in all the required fields", requireContext())
            false
        } else if (image == null && (imageUrl == null || imageUrl!!.isEmpty())) {
            commons.showToast("Please select a photo for this promo", requireContext())
            false
        } else {
            commons.showLoader(
                requireContext(), LayoutInflater.from(requireContext()), true
            )

            //return true
            true
        }
    }

    private fun setUpActionListeners() {
        photoPicker.setOnClickListener {
            openGallery.launch("image/*")
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.from_add_rewards_to_rewards)
        }
    }

    private fun createPromo() {
        if (validateFields()) {
            var path = "stores/${userRepo.getCurrentUserId()}/promos/${promoName.text.toString()}"
            image?.let {
                uploadImage(it) { img ->
                    val data = Promo(
                        promoName = promoName.text.toString(),
                        product = productName.text.toString(),
                        price = storePrice.text.toString().toDouble(),
                        discountRate = discountRate.text.toString().toDouble(),
                        pointsRequired = pointsRequired.text.toString().toDouble(),
                        photo = img,
                        storeId = userRepo.getCurrentUserId().toString(),
                        addedOn = commons.getDateAndTime(),
                        discountedPrice = discountedPrice,
                        dateStart = timestampStartDate,
                        dateEnd = timestampEndDate
                    )

                    promosRepo.addPromo(data) { success ->
                        commons.showLoader(
                            requireContext(), LayoutInflater.from(requireContext()), false
                        )
                        if (success) {
                            // Handle success
                            commons.showToast("Promo Item Created Successfully", requireContext())
                            findNavController().navigate(R.id.action_createPromoFragment_to_nav_promos)
                        } else {
                            // Handle failure
                            commons.showToast("An error occured. Please try again later", requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun uploadImage(
        imageUri: Uri,
        callback: (String) -> Unit
    ) {
        val storageRef =
            storage.reference.child("stores/${userRepo.getCurrentUserId()}/promos/${promoName.text.toString()}")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                downloadUrl = imageUrl.toString()
                callback(downloadUrl)
            }.addOnFailureListener {
                callback("")
            }

        }.addOnFailureListener {
            callback("")
        }
    }

    private fun updatePromo() {
        if (validateFields()) {
            promoViewModel.singlePromo.value?.let { currentPromo ->
                val updatedPromoName = promoName.text.toString()
                val updatedName = productName.text.toString()
                val updatedPrice = storePrice.text.toString().toDouble()
                val updatedDiscount = discountRate.text.toString().toDouble()
                val updatedPointsRequired = pointsRequired.text.toString().toDouble()
                val updatedPhoto = imageUrl ?: ""
                val updatedDiscountedPrice = calculateDiscountedPrice(updatedPrice, updatedDiscount)

                if (currentPromo.promoName != updatedPromoName ||
                    currentPromo.product != updatedName ||
                    currentPromo.price != updatedPrice ||
                    currentPromo.discountRate != updatedDiscount ||
                    currentPromo.pointsRequired != updatedPointsRequired ||
                    currentPromo.photo != updatedPhoto ||
                    currentPromo.discountedPrice != updatedDiscountedPrice ||
                    currentPromo.dateStart != timestampStartDate ||
                    currentPromo.dateEnd != timestampEndDate
                ) {
                    // Data has changed, proceed to update
                    if (image != null) {
                        uploadImage(image!!) { img ->
                            updateRewardWithData(img)
                        }
                    } else {
                        updateRewardWithData(updatedPhoto)
                    }
                } else {
                    // No changes in data
                    commons.showLoader(
                        requireContext(),
                        LayoutInflater.from(requireContext()),
                        false
                    )
                    commons.showToast("No changes detected.", requireContext())
                }
            }
        }
    }

    private fun updateRewardWithData(imageUrl: String) {
        val formattedDiscountedPrice = String.format("%.1f", discountedPrice)
        val data = Promo(
            promoName = promoName.text.toString(),
            product = productName.text.toString(),
            price = storePrice.text.toString().toDouble(),
            discountRate = discountRate.text.toString().toDouble(),
            pointsRequired = pointsRequired.text.toString().toDouble(),
            photo = imageUrl,
            storeId = userRepo.getCurrentUserId().toString(),
            updatedOn = commons.getDateAndTime(),
            discountedPrice = formattedDiscountedPrice.toDouble(),
            dateStart = timestampStartDate,
            dateEnd = timestampEndDate
        )

        promosRepo.updatePromo(id, data) { success ->
            if (success) {
                // Handle success
                commons.showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    false
                )
                commons.showToast("Promo item updated successfully", requireContext())
                findNavController().navigate(R.id.action_createPromoFragment_to_nav_promos)
            } else {
                // Handle failure
                commons.showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    false
                )
            }
        }
    }

    private fun initializeVariables() {
        storage = Firebase.storage
//        dateRange = binding.dateRange
        datePicker = binding.datePicker
        discPrice = binding.discountedPrice
        contentResolver = requireContext().contentResolver
        promoName = binding.promoName
        productName = binding.prodName
        storePrice = binding.storePrice
        discountRate = binding.discountRate
        pointsRequired = binding.pointsReq
        cancel = binding.cancel
        submit = binding.submitForm
        photoPicker = binding.photoPicker
        startDate = binding.startDate
        endDate = binding.endDate
        promoImage = binding.promoImage
    }

    private fun calculateDiscountedPrice(price: Double, discount: Double): Double {
        return price - (price * (discount / 100))
    }

    private fun calculateDiscountedPrice() {
        storePrice.doOnTextChanged { price, _, _, _ ->
            price?.let {
                val prc = storePrice.text.toString().toDoubleOrNull() ?: 0.0
                val discnt = discountRate.text.toString().toDoubleOrNull() ?: 0.0
                discountedPrice = calculateDiscountedPrice(prc, discnt)
                val formattedDiscountedPrice = String.format("%.1f", discountedPrice)
                binding.discountedPrice.text = formattedDiscountedPrice
            }
        }

        discountRate.doOnTextChanged { disc, _, _, _ ->
            disc?.let {
                val prc = storePrice.text.toString().toDoubleOrNull() ?: 0.0
                val dsnt = discountRate.text.toString().toDoubleOrNull() ?: 0.0
                discountedPrice = calculateDiscountedPrice(prc, dsnt)
                val formattedDiscountedPrice = String.format("%.1f", discountedPrice)
                binding.discountedPrice.text = formattedDiscountedPrice
            }
        }
    }

    private fun setOnClickListeners() {
        cancel.setOnClickListener {
            findNavController().navigate(R.id.action_createPromoFragment_to_nav_promos)
        }

        datePicker.setOnClickListener {
            showDateRangePickerDialog()
        }
    }

    private fun showDateRangePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setSelection(Pair(null, null))
            .build()

        datePicker.show(childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener {
            binding.startDate.setText(commons.dateFormatMMMDDYYYY(it.first))
            binding.endDate.setText(commons.dateFormatMMMDDYYYY(it.second))

            timestampStartDate = Date(it.first)
            timestampEndDate = Date(it.second)

        }
    }

    private fun getImageTitle(imageUri: Uri?): String {
        val projection = arrayOf(
            MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME
        )
        val cursor = imageUri?.let { contentResolver.query(it, projection, null, null, null) }
        cursor?.use {
            if (it.moveToFirst()) {
                val titleIndex = it.getColumnIndex(MediaStore.Images.Media.TITLE)
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val title = it.getString(titleIndex)
                val displayName = it.getString(displayNameIndex)
                return title ?: displayName ?: "Unknown Title"
            }
        }
        return "Unknown Title"
    }

    private fun handleSelectedDates(start: String, end: String) {
        // Do something with the selected start date and end date
        // For example, you can display them in TextViews or use them in calculations
        startDate.setText(start)
        endDate.setText(end)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        promoViewModel.clearReward()
        _binding = null
    }
}