package com.example.hygieiamerchant.pages.transactions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.RedeemedRewards
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.repository.TransactionRepo
import com.example.hygieiamerchant.utils.Commons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionDetailsDialog(
    context: Context,
    private val transaction: Transaction,
) : Dialog(context) {
    private lateinit var transactionRepo: TransactionRepo
    private lateinit var transactionId: TextView
    private lateinit var date: TextView
    private lateinit var customerName: TextView
    private lateinit var product: TextView
    private lateinit var promoName: TextView
    private lateinit var totalPrice: TextView
    private lateinit var totalPoints: TextView
    private lateinit var pointsGranted: TextView
    private lateinit var header: TextView
    private lateinit var productList: ArrayList<RedeemedRewards>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionRepo = TransactionRepo()

        if (transaction.type == "redeem") {
            if (transaction.promoName != "") {
                setContentView(R.layout.transaction_details_redeem_promo)
                promoName = findViewById(R.id.promoName)
                product = findViewById(R.id.product)
            } else {
                setContentView(R.layout.transaction_details_redeem_reward)
                setUpRecyclerView()
            }
            totalPoints = findViewById(R.id.totalPoints)
            totalPrice = findViewById(R.id.totalPrice)
        } else {
            setContentView(R.layout.transaction_details_grant)
            pointsGranted = findViewById(R.id.pointsGranted)
        }
        header = findViewById(R.id.header)
        transactionId = findViewById(R.id.transactionId)
        date = findViewById(R.id.transactionDate)
        customerName = findViewById(R.id.customerName)
        setUpUi()
    }

    private fun setUpUi() {
        transactionId.text = transaction.id
        customerName.text = transaction.customerName
        date.text = transaction.addedOn?.let { Commons().dateFormatMMMDDYYYY(it) }

        if (transaction.type == "redeem") {
            totalPoints.text = Commons().formatDecimalNumber(transaction.totalPointsSpent)
            totalPrice.text = context.getString(
                R.string.peso_sign,
                (Commons().formatDecimalNumber(transaction.total))
            )
            if (transaction.promoName != "") {
                promoName.text = transaction.promoName
                product.text = transaction.product
            } else {
            }
        } else {
            pointsGranted.text = Commons().formatDecimalNumber(transaction.pointsGranted)
        }
    }

    private fun setUpRecyclerView() {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val products = transactionRepo.getTransactionProducts(transaction.id)
                // Handle the products data here
                if (products != null) {
                    val recyclerView: RecyclerView = findViewById(R.id.productsList)
                    productList = arrayListOf()
                    productList.addAll(products)

                    recyclerView.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    recyclerView.setHasFixedSize(true)
                    val adapter = RedeemedProductsAdapter(productList)
                    recyclerView.adapter = adapter
                } else {
                    // Handle the case when products data is null
                }
            }
        } catch (error: Exception) {
            Commons().showToast("An error occurred: $error", context)
        }
    }
}