package com.example.hygieiamerchant.pages.rewards

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.databinding.FragmentAddRewardBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.RewardRepo
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.example.hygieiamerchant.utils.NetworkManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class RewardCreateOrUpdateFragment : Fragment() {
    private var _binding: FragmentAddRewardBinding? = null
    private val binding get() = _binding!!
    private lateinit var storage: FirebaseStorage
    private val commons: Commons = Commons()
    private lateinit var productName: TextInputEditText
    private lateinit var storePrice: TextInputEditText
    private lateinit var discount: TextInputEditText
    private lateinit var pointsRequired: TextInputEditText
    private lateinit var description: EditText
    private lateinit var cancel: AppCompatButton
    private lateinit var submit: AppCompatButton
    private lateinit var photoPicker: ConstraintLayout
    private lateinit var rewardImage: ShapeableImageView
    private lateinit var spinner: Spinner
    private lateinit var spinnerData: Array<String>
    private lateinit var contentResolver: ContentResolver
    private val rewardsRepo: RewardRepo = RewardRepo()
    private val userRepo: UserRepo = UserRepo()
    private val dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private val rewardsViewModel: RewardsViewModel by activityViewModels()
    private var image: Uri? = null
    private var storeName: String = ""
    private var selectedCategory: String = ""
    private var downloadUrl: String = ""
    private var discountedPrice: Double = 0.0
    private var id: String = ""
    private var imageUrl: String? = null

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imageTitle = getImageTitle(imageUri)
                this.image = imageUri
                binding.selectedImage.text = imageTitle
                rewardImage.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRewardBinding.inflate(inflater, container, false)

        setPageTitle()
        initializeVariables()
        setUpNetworkObservation()
        setUpSpinner()
        setUpActionListeners()
        calculateDiscountedPrice()
        retrieveStoreName()
        setUpUi()

        return binding.root
    }

    private fun setPageTitle() {
        if (rewardsViewModel.action.value == "update") {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Update Reward"
        } else {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Create Reward"
        }
    }

    private fun initializeVariables() {
        storage = Firebase.storage
        productName = binding.prodName
        storePrice = binding.storePrice
        discount = binding.discountRate
        pointsRequired = binding.pointsReq
        cancel = binding.cancel
        submit = binding.submitForm
        photoPicker = binding.photoPicker
        spinner = binding.spinner
        rewardImage = binding.rewardImage
        contentResolver = requireContext().contentResolver
        description = binding.description
    }

    private fun setUpUi() {
        if (rewardsViewModel.action.value == "update") {
            submit.text = getString(R.string.update)
            populateFields()
        } else {
            // Load empty/default fields
            submit.text = getString(R.string.add)
        }

        submit.setOnClickListener {
            Commons().observeNetwork(requireContext(), viewLifecycleOwner) { network ->
                if (network) {
                    if (rewardsViewModel.action.value == "create") {
                        createReward()
                    } else {
                        updateReward()
                    }
                } else {
                    Commons().showToast("No internet connection!", requireContext())
                }
            }
        }
    }

    private fun populateFields() {
        rewardsViewModel.singleReward.observe(viewLifecycleOwner) { reward ->
            if (reward != null) {
                id = reward.id
                imageUrl = reward.photo
                productName.setText(reward.name)
                selectedCategory = reward.category
                storePrice.setText(commons.formatDecimalNumber(reward.price))
                discount.setText(commons.formatDecimalNumber(reward.discount))
                pointsRequired.setText(commons.formatDecimalNumber(reward.pointsRequired))
                description.setText(reward.description)

                val position = spinnerData.indexOf(reward.category)

                // Set the selected item in the spinner
                if (position != -1) {
                    spinner.setSelection(position)
                }

                Glide.with(this)
                    .load(reward.photo)
                    .into(rewardImage)
            }
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

    private fun uploadImage(
        imageUri: Uri,
        callback: (String) -> Unit
    ) {
        val storageRef =
            storage.reference.child("stores/${userRepo.getCurrentUserId()}/rewards/${productName.text.toString()}")

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

    private fun validateFields(): Boolean {
        val name = productName.text.toString()
        val price = storePrice.text.toString()
        val discount = discount.text.toString()
        val pointsRequired = pointsRequired.text.toString()
        return if (name.isEmpty() || price.isEmpty() || discount.isEmpty() || pointsRequired.isEmpty()) {
            commons.showToast("Fill in all the required fields", requireContext())
            false
        } else if (image == null && (imageUrl == null || imageUrl!!.isEmpty())) {
            commons.showToast("Please select a photo for this reward", requireContext())
            false
        } else {
            // Validation successful, show loader and proceed to create reward
            commons.showLoader(
                requireContext(),
                LayoutInflater.from(requireContext()),
                true
            )
            true
        }
    }

    private fun retrieveStoreName() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { details ->
            storeName = details.name
        }
    }

    private fun createReward() {
        if (validateFields()) {
            image?.let {
                uploadImage(it) { img ->
                    val data = Reward(
                        name = productName.text.toString(),
                        price = storePrice.text.toString().toDouble(),
                        discount = discount.text.toString().toDouble(),
                        pointsRequired = pointsRequired.text.toString().toDouble(),
                        photo = img,
                        storeId = userRepo.getCurrentUserId().toString(),
                        addedOn = commons.getDateAndTime(),
                        category = selectedCategory,
                        discountedPrice = discountedPrice,
                        description = binding.description.text.toString()
                    )

                    rewardsRepo.addReward(data) { success ->
                        if (success) {
                            // Handle success
                            commons.showLoader(
                                requireContext(),
                                LayoutInflater.from(requireContext()),
                                false
                            )
                            commons.showToast("Reward Item Created Successfully", requireContext())
                            findNavController().navigate(R.id.from_add_rewards_to_rewards)
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
            }
        }
    }

    private fun updateReward() {
        if (validateFields()) {
            rewardsViewModel.singleReward.value?.let { currentReward ->
                val updatedName = productName.text.toString()
                val updatedPrice = storePrice.text.toString().toDouble()
                val updatedDiscount = discount.text.toString().toDouble()
                val updatedPointsRequired = pointsRequired.text.toString().toDouble()
                val updatedPhoto = imageUrl ?: ""
                val updatedCategory = selectedCategory
                val updatedDescription = binding.description.text.toString()
                val updatedDiscountedPrice = calculateDiscountedPrice(updatedPrice, updatedDiscount)

                if (currentReward.name != updatedName ||
                    currentReward.price != updatedPrice ||
                    currentReward.discount != updatedDiscount ||
                    currentReward.pointsRequired != updatedPointsRequired ||
                    currentReward.photo != updatedPhoto ||
                    currentReward.category != updatedCategory ||
                    currentReward.discountedPrice != updatedDiscountedPrice ||
                    currentReward.description != updatedDescription
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
        if (selectedCategory == "Select A Category") {
            selectedCategory = ""
        }
        val data = Reward(
            name = productName.text.toString(),
            price = storePrice.text.toString().toDouble(),
            discount = discount.text.toString().toDouble(),
            pointsRequired = pointsRequired.text.toString().toDouble(),
            photo = imageUrl,
            storeId = userRepo.getCurrentUserId().toString(),
            updatedOn = commons.getDateAndTime(),
            category = selectedCategory,
            discountedPrice = formattedDiscountedPrice.toDouble(),
            description = binding.description.text.toString()
        )

        rewardsRepo.updateReward(id, data) { success ->
            if (success) {
                // Handle success
                commons.showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    false
                )
                commons.showToast("Reward Item Updated Successfully", requireContext())
                findNavController().navigate(R.id.from_add_rewards_to_rewards)
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

    private fun calculateDiscountedPrice(price: Double, discount: Double): Double {
        return price - (price * (discount / 100))
    }

    private fun calculateDiscountedPrice() {
        storePrice.doOnTextChanged { price, _, _, _ ->
            price?.let {
                val prc = storePrice.text.toString().toDoubleOrNull() ?: 0.0
                val discnt = discount.text.toString().toDoubleOrNull() ?: 0.0
                discountedPrice = calculateDiscountedPrice(prc, discnt)
                val formattedDiscountedPrice = String.format("%.1f", discountedPrice)
                binding.discountedPrice.text = formattedDiscountedPrice
            }
        }

        discount.doOnTextChanged { disc, _, _, _ ->
            disc?.let {
                val prc = storePrice.text.toString().toDoubleOrNull() ?: 0.0
                val dsnt = discount.text.toString().toDoubleOrNull() ?: 0.0
                discountedPrice = calculateDiscountedPrice(prc, dsnt)
                val formattedDiscountedPrice = String.format("%.1f", discountedPrice)
                binding.discountedPrice.text = formattedDiscountedPrice
            }
        }
    }

    private fun setUpNetworkObservation() {
        val networkManager = NetworkManager(requireContext())
        networkManager.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (!isNetworkAvailable) {
                binding.submitForm.isEnabled = false
                Commons().showToast("No internet connection!", requireContext())
            } else {
                binding.submitForm.isEnabled = true
            }
        }
    }

    private fun setUpSpinner() {
        spinnerData = arrayOf(
            "Food and Beverages",
            "Electronics",
            "Clothing",
            "Appliances",
            "Household Necessities",
            "Entertainment",
            "Accessories",
            "Others"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, spinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedCategory = spinnerData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        rewardsViewModel.clearReward()
        _binding = null
    }

}