package com.example.hygieiamerchant.pages.requestpickup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.imageview.ShapeableImageView

class RequestsAdapter(
    private val requestList: ArrayList<Request>,
    private val onItemClickListener: OnItemClickListener,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<RequestsAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(request: Request)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(request: Request)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = requestList[position]

        holder.setDate.text = currentItem.date?.let { Commons().dateFormatMMMDDYYYY(it) }
        currentItem.status = currentItem.status.uppercase()
        holder.status.text = currentItem.status

        when (currentItem.status) {
            "pending" -> holder.status.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.accent_orange
                )
            )

            "UPCOMING" -> holder.status.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.green
                )
            )

            else -> holder.status.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.red
                )
            )
        }

        holder.deleteImage.setOnClickListener {
            onDeleteClickListener.onDeleteClick(currentItem)
        }
    }


    override fun getItemCount(): Int {
        return requestList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setDate: TextView = itemView.findViewById(R.id.date)
//        val address: TextView = itemView.findViewById(R.id.address)
        val status: TextView = itemView.findViewById(R.id.status)
        val deleteImage: ShapeableImageView = itemView.findViewById(R.id.delete)
    }
}