package com.example.hygieiamerchant.pages.signup

import android.content.ContentResolver
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.databinding.FragmentSignUpBinding
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var storeName: TextInputEditText
    private lateinit var storeEmail: TextInputEditText
    private lateinit var storeOwner: TextInputEditText
    private lateinit var phone: TextInputEditText

    //    private lateinit var sitio: TextView
    private lateinit var barangay: TextInputEditText
    private lateinit var city: TextInputEditText
    private lateinit var province: TextInputEditText
    private lateinit var imageUriFront: Uri
    private lateinit var imageUriBack: Uri
    private lateinit var lisOfIds: Spinner

    //    private lateinit var lguList: Spinner
    private lateinit var termsAndConditions: TextView

    //    private var list: ArrayList<Lgu> = arrayListOf()
    private var downloadUrlFront: String = ""
    private var downloadUrlBack: String = ""
    private val signUpViewModel: SignUpViewModel = SignUpViewModel()
    private val common: Commons = Commons()
    private var cityString: String = ""
    private var selectedIdType: String = ""
//    private var selectedLguId: String = ""

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        initializeVariables()
        setUpOnClickListeners()
        setUpSpinners()
        showTermsAndConditions()
        observeDataSetChange()
        setUpRefreshListener()
//        onInputChange()
        getSelectedIdType()

        signUpViewModel.fetchLguBasedOnUserCity(cityString)

        return binding.root
    }

    private fun getSelectedIdType() {
        lisOfIds.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedIdType = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setUpOnClickListeners() {
        uploadFront.setOnClickListener {
            getFrontPhotoName.launch("image/*")
        }

        uploadBack.setOnClickListener {
            getBackPhotoName.launch("image/*")
        }

        submit.setOnClickListener {
            showLoader(true)
            saveAccountRegistrationRequest()
        }

        binding.toLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun initializeVariables() {
        lisOfIds = binding.listOfValidIDs
//        lguList = binding.lguList
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
        barangay = binding.storeBrgyEditText
        city = binding.storeCityEditText
        province = binding.storeProvinceEditText
        cityString = city.text.toString()
        phone = binding.phoneEditText
    }

    private fun setUpRefreshListener() {
        common.setOnRefreshListener(binding.swipeRefreshLayout) {
            signUpViewModel.fetchLguBasedOnUserCity(cityString)
        }
    }

//    private fun onInputChange() {
//        city.doOnTextChanged { text, _, _, _ ->
//            selectedLguId = ""
//            text?.let {
//                val cityName = it.toString()
//                signUpViewModel.fetchLguBasedOnUserCity(cityName)
//            }
//        }
//    }

//    private fun getSelectedCity() {
//        lguList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?, view: View?, position: Int, id: Long
//            ) {
//                val selectedLgu = parent?.getItemAtPosition(position) as Lgu
//                selectedLguId = selectedLgu.id
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Do nothing if nothing is selected
//            }
//        }
//    }

    private fun observeDataSetChange() {
//        signUpViewModel.lguDetails.observe(viewLifecycleOwner) { listLgu ->
//            if (listLgu != null) {
//                // Update the list and notify the adapter of the Spinner
//                list.clear()
//                list.addAll(listLgu)
//                // Get a reference to the Spinner
//                val lguListAdapter = ArrayAdapter(requireContext(),
//                    android.R.layout.simple_spinner_item,
//                    list.map { it.name })
//                lguListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                lguList.adapter = lguListAdapter

        // Set a listener for item selection
//                lguList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>?,
//                        view: View?,
//                        position: Int,
//                        id: Long
//                    ) {
//                        selectedLguId = listLgu[position].id
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        // Do nothing
//                    }
//                }
//            }
//        }
    }


    private fun setUpSpinners() {
        val validIdsAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.valid_ids_array, android.R.layout.simple_spinner_item
        )
        validIdsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lisOfIds.adapter = validIdsAdapter
    }


    private fun saveAccountRegistrationRequest() {
        val isValid: Boolean = validateFields()
        if (isValid) {
            createRequest()
        } else {
            showLoader(false)
        }
    }

    private fun showLoader(loading: Boolean) {
        if (loading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.text.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.text.visibility = View.VISIBLE
        }
    }

    private fun getDateAndTime(): Timestamp {
        val currentDateTime = Calendar.getInstance().time
        return Timestamp(Date(currentDateTime.time))
    }

    private fun createRequest() {
        val storeName = storeName.text.toString()
        val storeEmail = storeEmail.text.toString()
        val storeOwner = storeOwner.text.toString()
        val barangay = barangay.text.toString()
        val city = city.text.toString()
        val province = province.text.toString()

        val address = mapOf(
            "barangay" to barangay,
            "city" to city,
            "province" to province
        )

        uploadImageToFirebaseStorage(imageUriFront, imageUriBack) { frontUrl, backUrl ->
            // Once you have the downloadUrl (validId), make the Firestore request
            val data = hashMapOf(
                "name" to storeName,
                "email" to storeEmail,
                "owner" to storeOwner,
                "address" to address,
                "validIdFront" to frontUrl,
                "validIdBack" to backUrl,
                "dateSubmitted" to getDateAndTime(),
                "idType" to selectedIdType,
                "status" to "pending",
                "phone" to phone.text.toString()
            )

            fireStore.collection("store").add(data).addOnSuccessListener {
                // Document added successfully
                showLoader(false)
                val title = "Account Request Sent"
                val message =
                    "Thank you for your interest in opening an account with us. " + "Your request has been successfully submitted to the Hygieia Admin team for review. " + "\n\nPlease allow us some time to process your application. " + "We will notify you via email once your application has been reviewed and processed." + "\n\nThank You!"
                common.showAlertDialogWithCallback(
                    this, title, message, "Got it!", positiveButtonCallback = {
                        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                    }
                )

            }.addOnFailureListener { e ->
                // Error occurred while adding document
                showLoader(false)
                Log.e("UPLOADING", "Error adding document", e)
                Toast.makeText(
                    requireContext(), "Error saving data to Firestore", Toast.LENGTH_SHORT
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

    private fun validateFields(): Boolean {
        val storeName = storeName.text.toString()
        val storeEmail = storeEmail.text.toString()
        val storeOwner = storeOwner.text.toString()
        val barangay = barangay.text.toString()
        val city = city.text.toString()
        val province = province.text.toString()
        val validIdFront = front.text.toString()
        val validIdBack = back.text.toString()
        val phone = phone.text.toString()

        //validate that all the necessary details are provided
        if (storeName.isBlank() || storeEmail.isBlank() || storeOwner.isBlank() || phone.isEmpty() || barangay.isBlank() || city.isBlank() || province.isBlank() || validIdFront.contentEquals(
                "Upload Front of ID"
            ) || validIdBack.contentEquals("Upload Back of ID")
        ) {
            common.showToast("Please fill in all required fields", requireContext())
            return false
        } else if (!common.validateEmail(storeEmail)) {
            common.showToast("Email is not valid", requireContext())
            return false
//        }else if(selectedLguId.isEmpty() || selectedLguId.contentEquals("")){
//            common.showToast("Please Select a Local Government Unit", requireContext())
//            return false
        } else if (selectedIdType == "Select ID" || selectedIdType.isEmpty()) {
            common.showToast("Please select a valid ID type", requireContext())
            return false
        } else if (phone.length < 11) {
            common.showToast("Please provide a valid phone number", requireContext())
            return false
        } else if (imageUriFront == null || imageUriBack == null) {
            common.showToast(
                "Please upload both front and back pictures of your valid ID", requireContext()
            )
            return false
        } else {
            return true
        }
    }

    private fun uploadImageToFirebaseStorage(
        imageUriFront: Uri, imageUriBack: Uri, callback: (String, String) -> Unit
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