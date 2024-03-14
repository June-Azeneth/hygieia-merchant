package com.example.hygieiamerchant.pages.profile

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.UserInfo
import com.example.hygieiamerchant.databinding.FragmentEditProfileBinding
import com.example.hygieiamerchant.pages.dashboard.DashboardViewModel
import com.example.hygieiamerchant.repository.UserRepo
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private var userRepo: UserRepo = UserRepo()
    private var storage: FirebaseStorage = Firebase.storage
    private var dashboardViewModel: DashboardViewModel = DashboardViewModel()
    private lateinit var storeName: TextInputEditText
    private lateinit var sitio: TextInputEditText
    private lateinit var barangay: TextInputEditText
    private lateinit var city: TextInputEditText
    private lateinit var province: TextInputEditText
    private lateinit var recyclables: TextInputEditText
    private lateinit var cancel: AppCompatButton
    private lateinit var submit: AppCompatButton
    private lateinit var profilePic: ShapeableImageView
    private lateinit var contentResolver: ContentResolver
    private lateinit var photoPicker: ConstraintLayout
    private lateinit var dialog: AlertDialog
    private var image: Uri? = null
    private var downloadUrl: String = ""
    private var imageUrl: String? = null

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imageTitle = getImageTitle(imageUri)
                this.image = imageUri
                binding.selectedImage.text = imageTitle
                profilePic.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        initializeVariables()
        populateFields()
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        cancel.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_nav_profile)
        }

        submit.setOnClickListener {
            validateFields { valid ->
                if (valid) {
                    detectChanges()
                }
            }
        }

        photoPicker.setOnClickListener {
            openGallery.launch("image/*")
        }
    }

    private fun initializeVariables() {
        contentResolver = requireContext().contentResolver
        submit = binding.submitForm
        cancel = binding.cancel
        storeName = binding.storeName
        sitio = binding.sitio
        barangay = binding.barangay
        province = binding.province
        city = binding.city
        recyclables = binding.recyclables
        photoPicker = binding.photoPicker
        profilePic = binding.profilePic
        dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.connectivity_dialog_box)
            .setCancelable(true)
            .create()
    }

    private fun populateFields() {
        dashboardViewModel.fetchUserInfo()
        dashboardViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val recyclablesString = user.recyclable.joinToString(", ")
                storeName.setText(user.name)
                imageUrl = user.photo
                recyclables.setText(recyclablesString)
                user.address?.let { addressMap ->
                    // Extract address components from the address map
                    val sitio = addressMap["sitio"] ?: ""
                    val barangay = addressMap["barangay"] ?: ""
                    val city = addressMap["city"] ?: ""
                    val province = addressMap["province"] ?: ""

                    // Set text fields with address components
                    this.sitio.setText(sitio)
                    this.barangay.setText(barangay)
                    this.city.setText(city)
                    this.province.setText(province)
                }

                Glide.with(this)
                    .load(user.photo)
                    .error(R.drawable.placeholder_image)
                    .into(profilePic)

            }
        }
    }

    private fun showConnectivityDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun detectChanges() {
        dashboardViewModel.userInfo.value?.let { user ->
            val updatedName = storeName.text.toString()
            val updatedPhoto = imageUrl ?: ""
            val address = mapOf(
                "sitio" to sitio.text.toString(),
                "barangay" to barangay.text.toString(),
                "city" to city.text.toString(),
                "province" to province.text.toString()
            )
            val updatedAddress: Map<String, String>? = address

            val recyclables = recyclables.text?.toString()?.trim()
            val recyclablesArray = recyclables?.split(",")?.map { it.trim() }?.toTypedArray()

            if (user.name != updatedName ||
                user.address != updatedAddress ||
                user.recyclable != recyclablesArray
            ) {
                Commons().showLoader(requireContext(),LayoutInflater.from(requireContext()), true)
                // Data has changed, proceed to update
                if (image != null) {
                    uploadImage(image!!) { img ->
                        updateProfile(img)
                    }
                } else {
                    updateProfile(updatedPhoto)
                }
            } else {
                // No changes in data
                Commons().showLoader(
                    requireContext(),
                    LayoutInflater.from(requireContext()),
                    false
                )
                Commons().showToast("No changes detected.", requireContext())
            }
        }
    }

    private fun updateProfile(imageUrl: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe network state within the view's lifecycle
            Commons().observeNetwork(requireContext(), viewLifecycleOwner) { network ->
                if (network) {
                    val address = mapOf(
                        "sitio" to sitio.text.toString(),
                        "barangay" to barangay.text.toString(),
                        "city" to city.text.toString(),
                        "province" to province.text.toString()
                    )

                    val recyclables = recyclables.text?.toString()?.trim()
                    val recyclablesList = recyclables?.split(",")?.map { it.trim() }
                        ?: listOf() // Use List<String> instead of Array<String>

                    val data = recyclablesList?.let {
                        UserInfo(
                            name = storeName.text.toString(),
                            address = address,
                            recyclable = it,
                            photo = imageUrl
                        )
                    }


                    userRepo.getCurrentUserId()?.let {
                        data?.let { userInfo ->
                            userRepo.updateProfile(it, userInfo) { success ->
                                if (success) {
                                    Commons().showToast(
                                        "Profile updated successfully",
                                        requireContext()
                                    )
                                    findNavController().navigate(R.id.action_editProfileFragment_to_nav_profile)
                                } else {
                                    Commons().showToast(
                                        "An error occurred. Please try again later.",
                                        requireContext()
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
    }

    private fun uploadImage(
        imageUri: Uri,
        callback: (String) -> Unit
    ) {
        val storageRef =
            storage.reference.child("stores/${userRepo.getCurrentUserId()}_photo")

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

    private fun validateFields(callback: (Boolean) -> Unit) {
        var storeName = storeName.text.toString()
        var sitio = sitio.text.toString()
        var barangay = barangay.text.toString()
        var city = city.text.toString()
        var province = province.text.toString()

        if (storeName.isEmpty() || sitio.isEmpty() || barangay.isEmpty() || city.isEmpty() || province.isEmpty()) {
            Commons().showToast("Please fill in all the required fields.", requireContext())
            callback(false)
        } else {
            callback(true)
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
}