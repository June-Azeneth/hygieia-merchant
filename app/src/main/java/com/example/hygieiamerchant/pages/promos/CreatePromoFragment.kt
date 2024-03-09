package com.example.hygieiamerchant.pages.promos

import android.app.DatePickerDialog
import android.content.ContentResolver
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date
import java.util.Locale

class CreatePromoFragment : Fragment() {
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

    private var image: Uri? = null
    private var storeName: String = ""
    private var downloadUrl: String = ""
    private var discountedPrice: Double = 0.0
    private var id: String = ""
    private var imageUrl: String? = null
    private var startDateString: String? = ""
    private var endDateString: String? = ""
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

        initializeVariables()
        setUpUi()
        setUpActionListeners()
        setOnClickListeners()
        retrieveStoreName()
        calculateDiscountedPrice()

        return binding.root
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
//                updatePromo()
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
                startDate.setText(promo.dateStart.toString())
                endDate.setText(promo.dateEnd.toString())
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

    private fun retrieveStoreName() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            storeName = details.name
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
                commons.uploadImage(it, path) { img ->
                    val data = Promo(
                        promoName = promoName.text.toString(),
                        product = productName.text.toString(),
                        price = storePrice.text.toString().toDouble(),
                        discountRate = discountRate.text.toString().toDouble(),
                        pointsRequired = pointsRequired.text.toString().toDouble(),
                        photo = img,
                        storeId = userRepo.getCurrentUserId().toString(),
                        storeName = storeName,
                        addedOn = commons.getDateAndTime(),
                        discountedPrice = discountedPrice,
                        dateStart = timestampStartDate,
                        dateEnd = timestampEndDate
                    )

                    promosRepo.addPromo(data) { success ->
                        if (success) {
                            // Handle success
                            commons.showLoader(
                                requireContext(), LayoutInflater.from(requireContext()), false
                            )
                            commons.showToast("Promo Item Created Successfully", requireContext())
                            findNavController().navigate(R.id.action_createPromoFragment_to_nav_promos)
                        } else {
                            // Handle failure
                            commons.showLoader(
                                requireContext(), LayoutInflater.from(requireContext()), false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updatePromo() {
        TODO("Not yet implemented")
    }

    private fun initializeVariables() {
        storage = Firebase.storage
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
        startDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        endDate.setOnClickListener {
            showDatePickerDialog(false)
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.action_createPromoFragment_to_nav_promos)
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

    private fun showDatePickerDialog(isStartDate: Boolean) {
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

//                val date = Timestamp(selectedDate.timeInMillis)


                if (isStartDate) {
                    startDateString = formattedDate
                    timestampStartDate = Date(selectedDate.timeInMillis)
                } else {
                    endDateString = formattedDate
                    timestampEndDate = Date(selectedDate.timeInMillis)
                }

                // If both start date and end date are selected, you can perform further actions here
                handleSelectedDates(startDateString!!, endDateString!!)
            }, year, month, day
        )
        datePickerDialog.show()
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