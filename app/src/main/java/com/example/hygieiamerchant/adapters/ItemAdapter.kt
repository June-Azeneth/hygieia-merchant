package com.example.hygieiamerchant.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Items

class ItemAdapter(private val itemsList : ArrayList<Items>) : RecyclerView.Adapter<ItemAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transactions_list,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.type.text = currentItem.type
        holder.date.text = currentItem.date
        holder.amount.text = currentItem.amount
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val type : TextView = itemView.findViewById(R.id.type)
        val date : TextView = itemView.findViewById(R.id.date)
        val amount : TextView = itemView.findViewById(R.id.amount)
    }
}