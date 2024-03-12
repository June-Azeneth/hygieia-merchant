package com.example.hygieiamerchant.pages.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.utils.Commons

class TransactionsAdapter(
    private val transactionList: ArrayList<Transaction>,
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<TransactionsAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.transactions_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = transactionList[position]
        val context = holder.itemView.context

        if(currentItem.type == "redeem"){
            holder.type.text = context.getString(R.string.sold_an_item)
        }
        else{
            holder.type.text = context.getString(R.string.grant_points)
        }

        holder.date.text = currentItem.addedOn?.let { Commons().dateFormatMMMDDYYYY(it) }

        holder.view.setOnClickListener {
            onItemClickListener.onItemClick(currentItem)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.findViewById(R.id.transactionType)
        val date: TextView = itemView.findViewById(R.id.dateOfTransaction)
        val view: AppCompatButton = itemView.findViewById(R.id.view)
    }
}