package com.example.hygieiamerchant.pages.scanQR

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hygieiamerchant.utils.Communicator
import com.example.hygieiamerchant.utils.QRCodeScan
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.utils.SharedViewModel
import com.example.hygieiamerchant.databinding.FragmentScanQRBinding
import com.example.hygieiamerchant.utils.cameraPermissionRequest
import com.example.hygieiamerchant.utils.isCameraPermissionGranted
import com.example.hygieiamerchant.utils.openPermissionSetting
import com.example.hygieiamerchant.utils.popups.GrantPointsPopUp
import com.example.hygieiamerchant.utils.popups.OptionsPopUp
import com.example.hygieiamerchant.utils.popups.RedeemProductPopUp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.Date
import kotlin.random.Random

class ScanQRFragment : Fragment() {
    private var logTag = "SCANQR"

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var _binding: FragmentScanQRBinding? = null
    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var layout: LinearLayout

    private lateinit var communicator: Communicator

    private var dataHashMap = HashMap<String, Any>()

    private val optionsPopUp: OptionsPopUp by lazy {
        OptionsPopUp()
    }

    private val grantPointsPopUp: GrantPointsPopUp by lazy {
        GrantPointsPopUp()
    }

    private val redeemProductPopUp: RedeemProductPopUp by lazy {
        RedeemProductPopUp()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startScanner()
            }
        }
    private lateinit var startScannerBTN: AppCompatButton
    private val binding get() = _binding!!

    private var passedQuantity: Int? = 0
    private var passedType: String? = ""
    private var currentBalance: Double = 0.0
    private var newBalance: Double = 0.0
    private var selectedProductID: String = ""
    private val hashMap = hashMapOf<String, Any>()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanQRBinding.inflate(inflater, container, false)
        requestCameraAndStartScanner()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dialog =
                MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
                    .setView(R.layout.custom_loader)
                    .setCancelable(true)
                    .create()

            val customToolbar = requireView().findViewById<View>(R.id.header)
            val titleTextView: TextView = customToolbar.findViewById(R.id.titleTextView)
            titleTextView.text = "Scan QR Code"

            firestore = FirebaseFirestore.getInstance()
            storageReference = FirebaseStorage.getInstance().reference

            startScannerBTN = binding.root.findViewById(R.id.scanNow)

            layout = requireView().findViewById(R.id.contentLayout)

            communicator = activity as Communicator

            startScannerBTN.setOnClickListener {
                startScanner()
            }

            passedQuantity = arguments?.getInt("quantity")
            passedType = arguments?.getString("type")
            selectedProductID = arguments?.getString("productID").toString()

            passedType?.let { revealElements(it) }

        } catch (e: Exception) {
            Log.e(logTag, e.toString())
        }
    }

    private fun addTextView(text: String, value: Double) {
        if (value != null) {
            addTextViewInternal(text, value.toString())
        } else {
            // Handle the case where value is null
        }
    }

    private fun addTextView(text: String, value: String?) {
        if (value != null) {
            addTextViewInternal(text, value)
        } else {
            // Handle the case where value is null
        }
    }

    private fun addTextViewInternal(text: String, value: String) {
        val textView = TextView(requireContext())
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text))

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_regular)
        textView.typeface = customTypeface

        textView.text = "$text: $value"

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.tiniest)

        layout.addView(textView, layoutParams)
    }

//    private fun addTextView(text: String, value: String) {
//        val textView = TextView(requireContext())
//        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text))
//
//        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_regular)
//        textView.typeface = customTypeface
//
//        textView.text = "$text: $value"
//
//        val layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        layoutParams.gravity = Gravity.CENTER
//        layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.tiniest)
//
//        layout.addView(textView, layoutParams)
//    }

    private fun revealElements(type: String) {
        binding.layoutRelative.visibility = VISIBLE
        binding.scanQRPromt.visibility = INVISIBLE
        binding.scanNow.visibility = INVISIBLE

        sharedViewModel.queryResult.observe(viewLifecycleOwner) { data ->
            binding.transactionCode.text = "Code: ${data["transactionCode"].toString()}"
            addTextView("Date", getCurrentDate())
            addTextView("Customer ID", data["userID"].toString())
            addTextView("Customer Name", data["customerName"].toString())

            val currentBalanceString = data["currentBalance"].toString()
            currentBalance = try {
                currentBalanceString.toDouble()
            } catch (e: NumberFormatException) {
                Log.e(
                    logTag,
                    "Error converting currentBalance to Int: $currentBalanceString"
                )
                0.0 // Default value or handle as needed
            }

            addTextView("Current Balance", data["currentBalance"].toString())

            if (type.contentEquals("grant")) {
                addTextView("Points to be Added", passedQuantity.toString())
                newBalance = currentBalance + (passedQuantity ?: 0)
                addTextView("New Balance", newBalance.toString())

                binding.editBTN.text = "Edit Quantity"
                binding.editBTN.setOnClickListener {
                    grantPointsPopUp.show(
                        (activity as AppCompatActivity).supportFragmentManager,
                        "show Grant Points Pop Up"
                    )
                }

                binding.submitBTN.setOnClickListener {
                    dialog.show()
                    updateCustomerBalance()
                    generateTransaction(type)
                    //                    dataHashMap.clear()
                    //                    sharedViewModel.setQueryResult(dataHashMap)
                }

            } else if (type.contentEquals("redeem")) {
                binding.editBTN.text = "Edit Product"
                val collectionRef = firestore.collection("rewards")
                val query = collectionRef.whereEqualTo("id", selectedProductID)

                try {
                    query.get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (document in documents) {
                                    //store specific document fields to hashmap
                                    hashMap["product_name"] =
                                        document.getString("product_name") ?: ""
                                    hashMap["pts_req"] =
                                        document.getDouble("pts_req") ?: 0.0
                                    hashMap["price"] = document.getDouble("store_price") ?: 0.0
                                    hashMap["discount_rate"] =
                                        document.getDouble("discount_rate") ?: 0.0
                                    hashMap["disc_price"] = document.getDouble("disc_price") ?: 0.0

                                    //access values stored inside the hashmap to update the UI
                                    addTextView(
                                        "Points to be deducted",
                                        hashMap["pts_req"].toString()
                                    )
                                    addTextView("Product", hashMap["product_name"].toString())
                                    addTextView("Price", "₱${hashMap["price"] as Double}")
                                    addTextView(
                                        "Discount",
                                        "${hashMap["discount_rate"] as Double}%"
                                    )
                                    addTextView("Due", "₱${hashMap["disc_price"].toString()}")

                                    //set the new balance of the customer
                                    val ptsReq = document.getDouble("pts_req")
                                    newBalance = currentBalance - (ptsReq ?: 0.0)
//                                        newBalance =
//                                        currentBalance - (document.getDouble("pts_req") ?: 0.0)
                                }
                            } else {
                                Log.e("REDEEM PRODUCT NOT FOUND", "Product not found.")
                            }
                        }
                        .addOnFailureListener { e ->
                            if (dialog.isShowing) {
                                dialog.hide()
                            }

                            Toast.makeText(requireContext(), "Error: $e", Toast.LENGTH_SHORT)
                                .show()
                        }
                } catch (err: RuntimeException) {
                    Log.e("ScanQRFragment", "Exception: ${err.message}")
                } catch (err: Exception) {
                    Log.e("ScanQRFragment", "RuntimeException: ${err.message}")
                } catch (io: IOException) {
                    Log.e("ScanQRFragment", "IOException: ${io.message}")
                    Toast.makeText(requireContext(), io.toString(), Toast.LENGTH_SHORT).show()
                }

                binding.editBTN.setOnClickListener {
                    redeemProductPopUp.show(
                        (activity as AppCompatActivity).supportFragmentManager,
                        "show Redeem Product Pop Up"
                    )
                }

                binding.submitBTN.setOnClickListener {
                    dialog.show()
                    updateCustomerBalance()
                    generateTransaction(type)
                }
            } else if (type.contentEquals("success")) {
                binding.message.visibility = VISIBLE
                binding.layoutRelative.removeAllViews()
                binding.scanNow.visibility = VISIBLE
                binding.scanQRPromt.visibility = VISIBLE
                binding.scanNow.text = "Scan Again"
                binding.scanQRPromt.setImageResource(R.drawable.checked)
            } else {
                binding.message.visibility = VISIBLE
                binding.message.text = "Failed"
                binding.message.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.layoutRelative.removeAllViews()
                binding.scanNow.visibility = VISIBLE
                binding.scanQRPromt.visibility = VISIBLE
                binding.scanNow.text = "Try Again"
                binding.scanQRPromt.setImageResource(R.drawable.failed)
            }
        }
    }

    //    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(currentDate)
    }

    private fun updateCustomerBalance() {
        val collectionRef = firestore.collection("consumer")

        sharedViewModel.queryResult.observe(viewLifecycleOwner) { data ->
            if (data != null && data.containsKey("userID")) {
                val userID = data["userID"].toString()
                val documentRef = collectionRef.document(userID)

                val updates = hashMapOf<String, Any>(
                    "currentBalance" to newBalance
                )

                documentRef.update(updates)
                    .addOnSuccessListener {
                        // Document updated successfully
                        if (dialog.isShowing) {
                            dialog.hide()
                        }
                        communicator.grantPointsQuantity(0, "success")
                        communicator.redeemProduct("", "success")
                        Log.d("FirestoreUpdate", "Document updated successfully")
                    }
                    .addOnFailureListener { e ->
                        // Handle errors
                        communicator.grantPointsQuantity(0, "failed")
                        communicator.redeemProduct("", "failed")
                        Log.e("FirestoreUpdate", "Error updating document", e)
                        if (dialog.isShowing) {
                            dialog.hide()
                        }
                    }
            } else {
                Log.e("FirestoreUpdate", "Invalid or missing data in queryResult")
            }
        }
    }

    private fun generateTransaction(type: String) {
        val collectionRef = firestore.collection("transaction")

        sharedViewModel.queryResult.observe(viewLifecycleOwner) { data ->
            if (data != null && data.containsKey("userID")) {
                val id = data["transactionCode"].toString()
                val documentRef = collectionRef.document(id)

                val data = hashMapOf(
                    "id" to id,
                    "customerID" to data["userID"].toString(),
                    "customerName" to data["customerName"].toString(),
                    "storeID" to "dummy store",
                    "storeName" to "dummy",
                    "type" to type,
                    "date" to getCurrentDate(),
                    "added_on" to getCurrentDate(),
                    "points_earned" to when (type) {
                        "grant" -> passedQuantity.toString().toDouble()
                        "redeem" -> 0.0
                        else -> 0.0
                    },
                    "points_spent" to when (type) {
                        "grant" -> 0.0
                        "redeem" -> hashMap["pts_req"].toString().toDouble()
                        else -> 0.0
                    },
                    "product" to when (type) {
                        "grant" -> ""
                        "redeem" -> hashMap["product_name"].toString()
                        else -> ""
                    },
                    "discount" to when (type) {
                        "grant" -> 0.0
                        "redeem" -> hashMap["discount_rate"].toString().toInt().toDouble()
                        else -> 0.0
                    },
                    "total" to when (type) {
                        "grant" -> 0.0
                        "redeem" -> hashMap["disc_price"].toString().toDouble()
                        else -> 0.0
                    }
                )

                documentRef.set(data)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Transaction added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error creating transaction",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Log.e("FirestoreSet", "Invalid or missing data in queryResult")
            }
        }
    }

    private fun startScanner() {
        if (isAdded && !isDetached) {
            QRCodeScan.startScanner(requireContext()) { barcodes ->
                barcodes.forEach { barcode ->
                    var userIDExtracted = barcode.rawValue.toString()

                    if (isAdded && !isDetached) {
                        dialog.show()
                        searchFirestore(userIDExtracted)
                    }
                }
            }
        }
    }

    private fun searchFirestore(userID: String) {

        val collectionRef = firestore.collection("consumer")
        val query = collectionRef.whereEqualTo("userID", userID)

        try {
            query.get()
                .addOnSuccessListener { documents ->
                    val documentData: HashMap<String, Any>?
                    if (!documents.isEmpty) {
                        documentData = documents.documents[0].data as HashMap<String, Any>
                        dataHashMap = documentData
                        dataHashMap["transactionCode"] = generateRandomNumber()
                        sharedViewModel.setQueryResult(dataHashMap)
                        showMessage("QR Code Scanning complete")
                        if (dialog.isShowing) {
                            dialog.hide()
                        }

                        optionsPopUp.show(
                            (activity as AppCompatActivity).supportFragmentManager,
                            "show Options Pop Up"
                        )
                    } else {
                        dataHashMap.clear()
                        if (dialog.isShowing) {
                            dialog.hide()
                        }
                        AlertDialog.Builder(requireContext())
                            .setTitle("QR Code Invalid")
                            .setMessage("Make sure to provide QR Codes that are registered in Hygieia and then try again.")
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    dataHashMap.clear()

                    if (dialog.isShowing) {
                        dialog.hide()
                    }

                    Toast.makeText(requireContext(), "Error: $e", Toast.LENGTH_SHORT)
                        .show()
                }
        } catch (err: Exception) {
            err.printStackTrace()
        }
    }

    private fun generateRandomNumber(): Int {
        val min = 1000000000 // Minimum value with 10 digits
        val max = 9999999999 // Maximum value with 10 digits

        return Random.nextInt(min.toInt(), (max + 1).toInt())
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun requestCameraAndStartScanner() {
        if (requireContext().isCameraPermissionGranted(cameraPermission)) {

        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                requireContext().cameraPermissionRequest {
                    requireContext().openPermissionSetting()
                }
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}