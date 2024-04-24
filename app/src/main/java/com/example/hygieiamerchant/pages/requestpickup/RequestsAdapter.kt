package com.example.hygieiamerchant.pages.requestpickup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.utils.Commons

class RequestsAdapter(
    private val requestList: ArrayList<Request>,
    private val onItemClickListener: OnItemClickListener,
    private val onCancelClickListener: OnCancelClickListener
) : RecyclerView.Adapter<RequestsAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(request: Request)
    }

    interface OnCancelClickListener {
        fun onCancelClick(request: Request)
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
        val context = holder.itemView.context

        val date = currentItem.date?.let { Commons().dateFormatMMMDDYYYY(it) }
        val time = currentItem.date?.let { Commons().dateFormatHHMM(it) }

        holder.id.text = context.getString(R.string.request_id, currentItem.id)
        holder.status.text = currentItem.status.uppercase()
        holder.date.text = context.getString(R.string.request_date, date.toString())

        if (currentItem.status == "active") {
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
            holder.time.visibility = View.VISIBLE
            holder.time.text = context.getString(R.string.request_time, time.toString())
        } else {
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
            holder.time.visibility = View.GONE
        }

        holder.item.setOnClickListener {
            onItemClickListener.onItemClick(currentItem)
        }

        holder.cancel.setOnClickListener {
            onCancelClickListener.onCancelClick(currentItem)
        }
    }


    override fun getItemCount(): Int {
        return requestList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.id)
        val status: TextView = itemView.findViewById(R.id.status)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val item: CardView = itemView.findViewById(R.id.item)
        val cancel: AppCompatButton = itemView.findViewById(R.id.cancel)
    }
}