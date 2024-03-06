package com.example.hygieiamerchant.pages.signup

import android.content.ContentResolver
import android.content.Context
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Lgu
import com.example.hygieiamerchant.databinding.FragmentSignUpBinding
import com.example.hygieiamerchant.utils.Commons
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Date

class SignUpFragment : Fragment() {
    private lateinit var storage: FirebaseStorage
    private val fireStore = FirebaseFirestore.getInstance()
    private lateinit var contentResolver: ContentResolver
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var uploadFront: AppCompatButton
    private lateinit var uploadBack: AppCompatButton
    private lateinit var submit: ConstraintLayout
    private lateinit var front: TextView
    private lateinit var back: TextView
    private lateinit var storeName: TextView
    private lateinit var storeEmail: TextView
    private lateinit var storeOwner: TextView
    private lateinit var sitio: TextView
    private lateinit var barangay: TextView
    private lateinit var city: TextView
    private lateinit var province: TextView
    private lateinit var imageUriFront: Uri
    private lateinit var imageUriBack: Uri
    private lateinit var lisOfIds: Spinner
    private lateinit var lguList: Spinner
    private lateinit var termsAndConditions: TextView
    private var list: ArrayList<Lgu> = arrayListOf()
    private var downloadUrlFront: String = ""
    private var downloadUrlBack: String = ""
    private val signUpViewModel: SignUpViewModel = SignUpViewModel()
    private val common: Commons = Commons()
    private var cityString: String = ""

    private val getBackPhotoName =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imageTitle = getImageTitle(imageUri)
                this.imageUriFront = imageUri
                binding.backImage.text = imageTitle
            }
        }

    private val getFrontPhotoName =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imageTitle = getImageTitle(imageUri)
                this.imageUriBack = imageUri
                binding.frontImage.text = imageTitle
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        initializeVariables()
        setUpOnClickListeners()
        setUpSpinners()
        showTermsAndConditions()
        observeDataSetChange()
        setUpRefreshListener()
        onInputChange()

        signUpViewModel.fetchLguBasedOnUserCity(cityString)

        return binding.root
    }

    private fun setUpOnClickListeners() {
        uploadFront.setOnClickListener {
            getFrontPhotoName.launch("image/*")
        }

        uploadBack.setOnClickListener {
            getBackPhotoName.launch("image/*")
        }

        submit.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.text.visibility = View.INVISIBLE
            saveAccountRegistrationRequest()
        }

        binding.toLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun initializeVariables() {
        lisOfIds = binding.listOfValidIDs
        lguList = binding.lguList
        storage = Firebase.storage
        contentResolver = requireContext().contentResolver
        uploadFront = binding.uploadFront
        uploadBack = binding.uploadBack
        front = binding.frontImage
        back = binding.backImage
        submit = binding.submit
        storeName = binding.storeNameEditText
        storeEmail = binding.storeEmailEditText
        storeOwner = binding.storeOwnerEditText
        sitio = binding.storeSitioEditText
        barangay = binding.storeBrgyEditText
        city = binding.storeCityEditText
        province = binding.storeProvinceEditText

        cityString = city.text.toString()
    }

    private fun setUpRefreshListener() {
        common.setOnRefreshListener(binding.swipeRefreshLayout) {
            signUpViewModel.fetchLguBasedOnUserCity(cityString)
        }
    }

    private fun onInputChange() {
        city.doOnTextChanged { text, _, _, _ ->
            text?.let {
                val cityName = it.toString()
                signUpViewModel.fetchLguBasedOnUserCity(cityName)
            }
        }
    }

    private fun observeDataSetChange() {
        signUpViewModel.lguDetails.observe(viewLifecycleOwner) { lgu ->
            if (lgu != null) {
                // Update the list and notify the adapter of the Spinner
                list.clear()
                list.addAll(lgu)

                common.log("LGU", lgu.toString())

                // Get a reference to the Spinner
                val lguListAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    list.map { it.name }
                )
                lguListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lguList.adapter = lguListAdapter
            }
        }
    }

    private fun setUpSpinners() {
        val validIdsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.valid_ids_array, android.R.layout.simple_spinner_item
        )
        validIdsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lisOfIds.adapter = validIdsAdapter
    }


    private fun saveAccountRegistrationRequest() {
        val isValid: Boolean = validateFields()

        if (isValid) {
            createRequest()
        } else {
            Toast.makeText(
                requireContext(),
                "Something went wrong. Please try again!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getDateAndTime(): Timestamp {
        val currentDateTime = Calendar.getInstance().time
        return Timestamp(Date(currentDateTime.time))
    }

    private fun getCurrentDate(): Timestamp {
        val currentDate = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(Date(calendar.timeInMillis))
    }

    private fun createRequest() {
        val storeName = storeName.text.toString()
        val storeEmail = storeEmail.text.toString()
        val storeOwner = storeOwner.text.toString()
        val sitio = sitio.text.toString()
        val barangay = barangay.text.toString()
        val city = city.text.toString()
        val province = province.text.toString()

        val address = listOf(sitio, barangay, city, province)

        uploadImageToFirebaseStorage(imageUriFront, imageUriBack) { frontUrl, backUrl ->
            // Once you have the downloadUrl (validId), make the Firestore request
            val data = hashMapOf(
                "storeName" to storeName,
                "storeEmail" to storeEmail,
                "storeOwner" to storeOwner,
                "address" to address,
                "validIdFront" to frontUrl,
                "validIdBack" to backUrl,
                "dateSubmitted" to getDateAndTime(),
                "status" to "pending"
            )

            fireStore.collection("store")
                .add(data)
                .addOnSuccessListener {
                    // Document added successfully
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.text.visibility = View.VISIBLE
                    val title = "Account Request Sent"
                    val message = "Thank you for your interest in opening an account with us. " +
                            "Your request has been successfully submitted to the Hygieia Admin team for review. " +
                            "\n\nPlease allow us some time to process your application. " +
                            "We will notify you via email once your application has been reviewed and processed." +
                            "\n\nThank You!"
                    showAlertDialog(requireContext(), title, message)
                }
                .addOnFailureListener { e ->
                    // Error occurred while adding document
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.text.visibility = View.VISIBLE
                    Log.e("UPLOADING", "Error adding document", e)
                    Toast.makeText(
                        requireContext(),
                        "Error saving data to Firestore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showTermsAndConditions() {
        termsAndConditions = binding.termsAndConditions
        termsAndConditions.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.terms_and_conditions, null)
            builder.setView(dialogView)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun showAlertDialog(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Got it!") { dialog, _ ->
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun validateFields(): Boolean {
        val storeName = storeName.text.toString()
        val storeEmail = storeEmail.text.toString()
        val storeOwner = storeOwner.text.toString()
        val sitio = sitio.text.toString()
        val barangay = barangay.text.toString()
        val city = city.text.toString()
        val province = province.text.toString()
        val validId = front.text.toString()

        if (storeName.isBlank() ||
            storeEmail.isBlank() ||
            storeOwner.isBlank() ||
            sitio.isBlank() ||
            barangay.isBlank() ||
            city.isBlank() ||
            province.isBlank() ||
            validId.contentEquals("No Image Selected")
        ) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        } else if (!isEmailValid(storeEmail)) {
            Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")
        return emailRegex.matches(email)
    }

    private fun uploadImageToFirebaseStorage(
        imageUriFront: Uri,
        imageUriBack: Uri,
        callback: (String, String) -> Unit
    ) {

        val date = common.dateFormatMMMDDYYYY()

        val storageRefFront =
            storage.reference.child("store_valid_ids/${storeName.text}_${storeOwner.text}_FRONT_${date}")

        val storageRefBack =
            storage.reference.child("store_valid_ids/${storeName.text}_${storeOwner.text}_BACK_${date}")

        // Upload the image file to Firebase Storage
        val uploadTaskFront = storageRefFront.putFile(imageUriFront)
        val uploadTaskBack = storageRefBack.putFile(imageUriBack)

        // Register observers to listen for when the upload is successful or fails
        uploadTaskFront.addOnSuccessListener {
            storageRefFront.downloadUrl.addOnSuccessListener { frontUri ->
                downloadUrlFront = frontUri.toString()
                uploadTaskBack.addOnSuccessListener { backSnapshot ->
                    storageRefBack.downloadUrl.addOnSuccessListener { backUri ->
                        downloadUrlBack = backUri.toString()
                        // Call the callback function once both images have been uploaded successfully
                        callback(downloadUrlFront, downloadUrlBack)
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to get download URL for back image: $exception",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback("", "")
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Upload Failed for front image: $exception",
                    Toast.LENGTH_SHORT
                ).show()
                callback("", "")
            }
        }
    }

    private fun getImageTitle(imageUri: Uri?): String {
        val projection = arrayOf(
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME
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