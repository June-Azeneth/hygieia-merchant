package com.example.hygieiamerchant.pages.transactions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.repository.TransactionRepo
import com.example.hygieiamerchant.utils.Commons

class TransactionDetailsDialog(
    context: Context,
    private val transaction: Transaction,
) : Dialog(context) {
    private lateinit var transactionViewModel: TransactionsViewModel
    private lateinit var transactionRepo: TransactionRepo
    private lateinit var transactionId: TextView
    private lateinit var date: TextView
    private lateinit var customerName: TextView
    private lateinit var product: TextView
    private lateinit var promoName: TextView
    private lateinit var originalPrice: TextView
    private lateinit var soldAt: TextView
    private lateinit var pointsRequired: TextView
    private lateinit var pointsGranted: TextView
    private lateinit var redeem: LinearLayout
    private lateinit var grant: LinearLayout
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_details)
        initializeVariables()
        setUpUi()
    }

    private fun initializeVariables() {
        transactionViewModel = TransactionsViewModel()
        transactionRepo = TransactionRepo()
        transactionId = findViewById(R.id.transactionId)
        date = findViewById(R.id.transactionDate)
        customerName = findViewById(R.id.customerName)
        product = findViewById(R.id.product)
        promoName = findViewById(R.id.promoName)
        soldAt = findViewById(R.id.soldAt)
        pointsRequired = findViewById(R.id.pointsReq)
        pointsGranted = findViewById(R.id.pointsGranted)
        redeem = findViewById(R.id.sold)
        grant = findViewById(R.id.grant)
        header = findViewById(R.id.header)
    }

    private fun setUpUi() {
        transactionId.text = context.getString(R.string.id, transaction.id)
        customerName.text = context.getString(R.string.customer, transaction.customerName)
        date.text = transaction.addedOn?.let { "Date: ${Commons().dateFormatMMMDDYYYY(it)}" }
        if (transaction.type == "redeem") {
            redeem.visibility = View.VISIBLE
            grant.visibility = View.GONE

            header.setBackgroundColor(ContextCompat.getColor(context, R.color.accent_orange))
            product.text = context.getString(R.string.product, transaction.product)
            soldAt.text = context.getString(R.string.sold_at, Commons().formatDecimalNumber(transaction.total))
            pointsRequired.text =
                context.getString(R.string.points_required_, Commons().formatDecimalNumber(transaction.pointsRequired))

            if(transaction.promoName != ""){
                promoName.visibility = View.VISIBLE
                promoName.text = context.getString(R.string.promo, transaction.promoName)
            }else{
                promoName.visibility = View.GONE
            }

        } else {
            header.setBackgroundColor(ContextCompat.getColor(context, R.color.main_green))
            grant.visibility = View.VISIBLE
            redeem.visibility = View.GONE
            pointsGranted.text =
                context.getString(R.string.points_granted, Commons().formatDecimalNumber(transaction.pointsGranted))
        }
    }
}